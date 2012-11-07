/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC STOMP-Client.
 *
 * Copyright (c) 2007-2012 DAI-Labor, Technische Universität Berlin
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
 * $Id: StompCommunicationElement.java 23214 2009-05-06 12:18:42Z marcel $ 
 */
package de.jiac.micro.ext.stomp;

import java.io.IOException;
import java.util.Vector;

import org.slf4j.Logger;

import com.github.libxjava.lang.IClassLoader;

import de.jiac.micro.agent.IActuator;
import de.jiac.micro.agent.ISensor;
import de.jiac.micro.agent.handle.ICommunicationHandle;
import de.jiac.micro.agent.memory.IFact;
import de.jiac.micro.agent.memory.IShortTermMemory;
import de.jiac.micro.core.IHandle;
import de.jiac.micro.core.ILifecycleAware;
import de.jiac.micro.core.io.IAddress;
import de.jiac.micro.core.io.IMessage;
import de.jiac.micro.core.io.IMulticastAddress;
import de.jiac.micro.core.io.IUnicastAddress;
import de.jiac.micro.core.scope.AgentScope;
import de.jiac.micro.core.scope.Scope;
import de.jiac.micro.ext.stomp.Transport.ITransportDelegate;
import de.jiac.micro.internal.io.Message;

/**
 * @author Vladimir
 * @version $Revision: 23214 $
 *
 */
public class StompCommunicationElement implements ISensor, IActuator, ICommunicationHandle, ILifecycleAware {
    private final static class StompClient implements ITransportDelegate {
        private final IClassLoader _classLoader;
        private final IShortTermMemory _shortMemory;
        protected StompTransport transport;
        
        public StompClient(IClassLoader classLoader, IShortTermMemory stm) {
            transport = new StompTransport();
            transport.setTransportDelegate(this);
            _shortMemory= stm;
            _classLoader = classLoader;
        }
        
        public void onError(Transport source, Object error) {
            if(error instanceof Throwable) {
                AgentScope.getAgentReference().getLogger().error("onError", (Throwable) error);
            } else {
                AgentScope.getAgentReference().getLogger().error("onError:: " + String.valueOf(error));
            }
        }

        public void onMessage(Transport source, IMessage message, String destName) {
            _shortMemory.notice((IFact) message);
        }
        
        public IClassLoader getClassLoader() {
            return _classLoader;
        }

        public Logger getLogger() {
            return AgentScope.getAgentReference().getLogger("STOMP");
        }
    }
    

	private Vector _groups;
	private StompClient _client;
	private StompAddress _localAddress;
	private String _serverURI;
	private IShortTermMemory _stm;
	
	private boolean _started= false;

	public IHandle getHandle() {
		return this;
	}

	public IMessage createMessage() {
		return new Message();
	}

    public void cleanup() {
        _groups = null;
    }

    public void initialise() {
        _groups = new Vector();
        _client = new StompClient(Scope.getContainer().getClassLoader(), _stm);
        _localAddress= new StompAddress(IAddress.UNICAST, "agent" + System.currentTimeMillis() + "h" + System.identityHashCode(AgentScope.getAgentReference()));
    }
    
    public void setServerURI(String serverURI) {
        _serverURI= serverURI;
    }

    public void start() {
        Logger logger= AgentScope.getAgentReference().getLogger("STOMP");
        
    	try {
    		// create Stomp connections to broker
    	    if(_serverURI == null) {
    	        logger.error("unspecified server URI");
    	    }
    	    
    		_client.transport.setServerURL(_serverURI);
    		_client.transport.doStart();
    	} catch (IOException io) {
    	    logger.error("cannot start the stomp protocol", io); 
    	}
    	
    	_started= true;
    	
    	// register for our address
    	try {
            _client.transport.doRegister(StompTransport.MBOX_PREFIX, _localAddress.getAddressName());
            logger.debug("registered message box: '" + _localAddress.getAddressName() + "'");
        } catch (IOException ie) {
            logger.error("could not register for local address: '" + _localAddress + "'", ie);
        }
    	
    	// check groups and registrate now
    	for(int i= 0; i < _groups.size(); ++i) {
    	    try {
                _client.transport.doRegister(StompTransport.GROUP_PREFIX, (String) _groups.elementAt(i));
                logger.debug("registered group: '" + (String) _groups.elementAt(i) + "'");
            } catch (IOException e) {
                logger.error("could not register for group: '" + _groups.elementAt(i) + "'", e);
            }
    	}
    }

    public void stop() {
        _started= false;
    	_client.transport.doStop();
   	}

	public void setShortTermMemory(IShortTermMemory stm) {
		_stm = stm;
	}

	public IUnicastAddress[] getLocalAddresses() {
		return new IUnicastAddress[] {_localAddress};
	}

	public IMulticastAddress getMulticastAddressForName(String groupName) {
		return new StompAddress(IAddress.MULTICAST, groupName);
	}
	
	public IAddress getAddressForString(String addressStr) {
        return StompAddress.getAddressForString(addressStr);
    }

    public void joinGroup(IMulticastAddress address) throws IOException {
		String group = ((StompAddress) address).getAddressName();
		
		if(!_groups.contains(group)) {
			_groups.addElement(group);
			
			if(_started) {
			    _client.transport.doRegister(StompTransport.GROUP_PREFIX, group);
			}
		}
	}

	public void leaveGroup(IMulticastAddress address) throws IOException {
	    String group = ((StompAddress) address).getAddressName();
		if(_groups.removeElement(group)) {
	        if(!_groups.contains(group)) {
	            _groups.addElement(group);
	            
	            if(_started) {
	                _client.transport.doUnregister(StompTransport.GROUP_PREFIX, group);
	            }
	        }
		}
	}

	public void sendMessage(IAddress address, IMessage message) throws IOException {
	    String addressName= ((StompAddress) address).getAddressName();
        message.setHeader(IMessage.DefaultHeader.SOURCE_ADDRESS, _localAddress.toString());
	    message.setHeader(IMessage.DefaultHeader.TARGET_ADDRESS, address.toString());
	    
	    switch(address.getType()) {
	        case IAddress.UNICAST: {
    		    message.setHeader(StompTransport.PREFIX_HEADER, StompTransport.MBOX_PREFIX);
    	        message.setHeader(StompTransport.DESTINATION_HEADER, addressName);
    	        _client.transport.doSend(message);
    	        break;
	        } 
	        case IAddress.MULTICAST: {
    	        message.setHeader(StompTransport.PREFIX_HEADER, StompTransport.GROUP_PREFIX);
    	        message.setHeader(StompTransport.DESTINATION_HEADER, addressName);
    	        _client.transport.doSend(message);
    	        break;
	        }
	        
	        default: {
	            throw new IOException("invalid address type " + address.getType());
	        }
	    }
	}
}
