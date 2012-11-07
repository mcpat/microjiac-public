/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC ActiveMQ-Broker.
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
 * $Id: ActiveMQBroker.java 23214 2009-05-06 12:18:42Z marcel $
 */
package de.jiac.micro.ext.jms;

import java.net.URI;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.broker.jmx.ManagementContext;
import org.apache.activemq.network.NetworkConnector;
import org.slf4j.Logger;

import de.jiac.micro.core.IHandle;
import de.jiac.micro.core.INode;
import de.jiac.micro.core.scope.AgentScope;
import de.jiac.micro.core.scope.NodeScope;
import de.jiac.micro.internal.core.AbstractNodeComponent;
import de.jiac.micro.internal.core.AbstractNodeConfiguration;

/**
 * @author Marcel Patzlaff
 * @version $Revision: 23214 $
 */
public class ActiveMQBroker extends AbstractNodeComponent {
    private JMSTransportConnector[] _transportConnectors;
    private String[] _networkConnectors;
    private BrokerService _broker;
    private boolean _useJMX= false;
    private Logger _logger;
    
    private JMSLocalConnectionFactory _localConnectionFactory;

    public void cleanup() {
        _logger.debug("stop broker");
        try {
            _broker.stop();
        } catch (Exception e) {
            _logger.error("could not stop broker", e);
        }
        
        _logger.debug("cleanup broker");
        _logger= null;
        _broker= null;
        _localConnectionFactory= null;
    }

    public void initialise() {
        INode node= NodeScope.getNodeReference();
        
        _logger= node.getLogger("ActiveMQ");
        AbstractNodeConfiguration nodeConf= (AbstractNodeConfiguration) node.getHandle(AbstractNodeConfiguration.class);
        String brokerName= nodeConf.id.substring(nodeConf.id.lastIndexOf('#') + 1) + node.hashCode();
        brokerName= brokerName.toLowerCase();
        _logger.debug("initialise broker");
        
        _broker= new BrokerService();
        _broker.setBrokerName(brokerName);
        
        _broker.setUseJmx(_useJMX);
        if(_useJMX) {
            ManagementContext context = new ManagementContext();
            context.setJmxDomainName("de.jiac.micro");
            context.setCreateConnector(false);
            _broker.setManagementContext(context);
        }
        _broker.setUseShutdownHook(false);
        _broker.setPersistent(false);
        
        for(int i= 0; _networkConnectors != null && i < _networkConnectors.length; ++i) {
            try {
                URI networkUri = new URI(_networkConnectors[i]);
                NetworkConnector networkConnector = _broker.addNetworkConnector(networkUri);
                networkConnector.setDuplex(true);
                networkConnector.setNetworkTTL(20);
            } catch (Exception e) {
                _logger.error("could not initialise broker", e);
            }
        }
        
        for(int i= 0; _transportConnectors != null && i < _transportConnectors.length; ++i) {
            try {
                TransportConnector tc= _broker.addConnector(_transportConnectors[i].transportURI);
                
                if(_transportConnectors[i].discoveryURI != null) {
                    URI uri= new URI(_transportConnectors[i].discoveryURI);
                    tc.setDiscoveryUri(uri);
//                    NetworkConnector nc= new DiscoveryNetworkConnector(uri);
//                    _broker.addNetworkConnector(nc);
                }
            } catch (Exception e) {
                _logger.error("could not initialise broker", e);
            }
        }
        
        _localConnectionFactory= new JMSLocalConnectionFactory(new ActiveMQConnectionFactory("vm://" + _broker.getBrokerName()));
        
        _logger.debug("start broker");
        try {
            _broker.start();
        } catch (Exception e) {
            _logger.error("could not start broker", e);
        }
    }
    
    public void setUseJMX(boolean val) {
        _useJMX= val;
    }
    
    public void setTransportConnectors(JMSTransportConnector[] connectors) {
        _transportConnectors= connectors;
    }
    
    public void setNetworkConnectors(String[] networkConnectors) {
        _networkConnectors= networkConnectors;
    }
    
    protected void addHandlesOn(AgentScope agent) {
        super.addHandlesOn(agent);
        agent.getContainerReference().addHandle(_localConnectionFactory);
    }
    
    protected void removeHandlesFrom(AgentScope agent) {
        IHandle factory= agent.getContainerReference().getHandle(JMSLocalConnectionFactory.class);
        
        if(factory != null) {
            agent.getContainerReference().removeHandle(factory);
        }
        
        super.removeHandlesFrom(agent);
    }
}
