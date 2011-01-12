/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC ActiveMQ-Broker.
 *
 * Copyright (c) 2007-2011 DAI-Labor, Technische Universität Berlin
 *
 * This library includes software developed at DAI-Labor, Technische
 * Universität Berlin (http://www.dai-labor.de)
 *
 * This library is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * $Id$ 
 */
package de.jiac.micro.ext.jms;

import java.io.IOException;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.slf4j.Logger;

import com.github.libxjava.lang.IClassLoader;

import de.jiac.micro.agent.IActuator;
import de.jiac.micro.agent.ISensor;
import de.jiac.micro.agent.handle.ICommunicationHandle;
import de.jiac.micro.agent.memory.IShortTermMemory;
import de.jiac.micro.core.IAgent;
import de.jiac.micro.core.IHandle;
import de.jiac.micro.core.ILifecycleAware;
import de.jiac.micro.core.io.IAddress;
import de.jiac.micro.core.io.IMessage;
import de.jiac.micro.core.io.IMulticastAddress;
import de.jiac.micro.core.io.IUnicastAddress;
import de.jiac.micro.core.scope.AgentScope;
import de.jiac.micro.internal.io.Message;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public final class JMSCommunicationElement implements ISensor, IActuator, ILifecycleAware, ICommunicationHandle {
    private IShortTermMemory _stm;
    private ConnectionFactory _connectionFactory;
    private Connection _connection;
    private Logger _logger;

    private JMSSender _sender;
    private JMSReceiver _receiver;
    private IUnicastAddress _localAddress;
    
    public void setShortTermMemory(IShortTermMemory stm) {
        _stm= stm;
    }

    public IHandle getHandle() {
        return this;
    }

    public void cleanup() {
        _sender= null;
        _receiver= null;
        
        if(_connection != null) {
            try {
                _connection.close();
            } catch (JMSException e) {
                _logger.warn("JMS: could not close connection", e);
            }
            _connection= null;
        }
        
        _connectionFactory= null;
        _localAddress= null;
        _logger= null;
    }

    public void initialise() {
        IAgent agent= AgentScope.getAgentReference();
        IClassLoader classLoader= (IClassLoader) agent.getHandle(IClassLoader.class);
        _logger= agent.getLogger("JMS");
        
        _connectionFactory= (JMSLocalConnectionFactory) agent.getHandle(JMSLocalConnectionFactory.class);
        
        if(_connectionFactory == null) {
            _logger.error("JMS: node has no local JMS broker");
            // TODO: build local factory?
        }
        
        if(_connectionFactory != null) {
            try {
                _connection= _connectionFactory.createConnection();
            } catch (JMSException e) {
                _logger.error("JMS: could not create connection", e);
            }
        }
        
        if(_connection != null) {
            try {
                _receiver= new JMSReceiver(_connection, classLoader, _logger, _stm);
            } catch (JMSException e) {
                _logger.error("JMS: could not create receiver", e);
                _receiver= null;
            }
            
            try {
                _sender= new JMSSender(_connection, classLoader, _logger);
            } catch (JMSException e) {
                _logger.error("JMS: could not create sender", e);
                _sender= null;
            }
        }
        
        _localAddress= new JMSAddress(IAddress.UNICAST, "agent" + System.currentTimeMillis() + "h" + System.identityHashCode(AgentScope.getAgentReference()));
    }

    public void start() {
        if(_connection != null) {
            try {
                _connection.start();
            } catch (JMSException e) {
                _logger.error("JMS: could not start connection", e);
            }
        }
        
        try {
            changeRegistration(_localAddress, false);
        } catch (IOException e) {
            _logger.error("JMS: could not register unicast address '" + _localAddress + "'", e);
        }
    }
    
    public void stop() {
        try {
            changeRegistration(_localAddress, true);
        } catch (IOException e) {
            _logger.error("JMS: could not unregister unicast address '" + _localAddress + "'", e);
        }
        
        if(_connection != null) {
            try {
                _connection.stop();
            } catch (JMSException e) {
                _logger.error("JMS: could not stop connection", e);
            }
        }
    }

    public IMessage createMessage() {
        return new Message();
    }

    public IAddress getAddressForString(String addressStr) {
        return JMSAddress.getAddressForString(addressStr);
    }

    public IUnicastAddress[] getLocalAddresses() {
        return new IUnicastAddress[] {_localAddress};
    }

    public IMulticastAddress getMulticastAddressForName(String groupName) {
        return new JMSAddress(IAddress.MULTICAST, groupName);
    }

    public void joinGroup(IMulticastAddress address) throws IOException {
        changeRegistration(address, false);
    }

    public void leaveGroup(IMulticastAddress address) throws IOException {
        changeRegistration(address, true);
    }

    public void sendMessage(IAddress address, IMessage message) throws IOException {
        if(_sender == null) {
            throw new IOException("sender is not available");
        }
        
        try {
            message.setHeader(IMessage.DefaultHeader.SOURCE_ADDRESS, _localAddress.toString());
            message.setHeader(IMessage.DefaultHeader.TARGET_ADDRESS, address.toString());
            _sender.send(message, address);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void changeRegistration(IAddress address, boolean unregister) throws IOException {
        if(_receiver == null) {
            throw new IOException("receiver is not available");
        }
        
        if(unregister) {
            _receiver.unregister(address);
        } else {
            _receiver.register(address);
        }
    }
}
