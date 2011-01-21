/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC SunSPOT-Extensions.
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
package de.jiac.micro.sunspot.aodv;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.slf4j.Logger;

import com.github.libxjava.io.BinaryDeserialiserStream;
import com.github.libxjava.io.BinarySerialiserStream;
import com.sun.spot.peripheral.radio.LowPan;
import com.sun.spot.peripheral.radio.RadioFactory;
import com.sun.spot.peripheral.radio.mhrp.aodv.AODVManager;

import de.jiac.micro.agent.handle.ICommunicationHandle;
import de.jiac.micro.core.IAgent;
import de.jiac.micro.core.IHandle;
import de.jiac.micro.core.io.IAddress;
import de.jiac.micro.core.io.IMessage;
import de.jiac.micro.core.io.IMulticastAddress;
import de.jiac.micro.core.io.IUnicastAddress;
import de.jiac.micro.core.scope.AbstractScopeAwareRunner;
import de.jiac.micro.core.scope.AgentScope;
import de.jiac.micro.core.scope.NodeScope;
import de.jiac.micro.internal.core.AbstractNodeComponent;
import de.jiac.micro.internal.io.Message;
import de.jiac.micro.util.List;
import de.jiac.micro.util.List.Node;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class AODVNodeComponent extends AbstractNodeComponent implements IHandle {
    protected static String getAgentKey() {
        return String.valueOf(AgentScope.getAgentScope().hashCode());
    }
    
    private final class AODVCommunicator implements ICommunicationHandle {
        protected AODVCommunicator() {}
        
        public IMessage createMessage() {
            return new Message();
        }

        public IUnicastAddress[] getLocalAddresses() {
            return new IUnicastAddress[] {Address.createUnicastAddress(nodeAddress, getAgentKey())};
        }
        
        public IMulticastAddress getMulticastAddressForName(String groupName) {
            return Address.createMulticastAddress(groupName);
        }
        
        public IAddress getAddressForString(String addressStr) {
            return Address.parseAddress(addressStr);
        }

        public void joinGroup(IMulticastAddress address) {
            changeGroupAssociation((Address) address, true);
        }
        
        public void leaveGroup(IMulticastAddress address) {
            changeGroupAssociation((Address) address, false);
        }
        
        public void sendMessage(IAddress address, IMessage message) throws IOException {
            internalSendMessage((Address) address, (Message) message);
        }
    }
    
    private final class MessageProcessor extends AbstractScopeAwareRunner {
        protected MessageProcessor() {
            super("MessageProcessor");
        }

        protected void doRun() {
            while(!isCancelled()) {
                internalProcessMessage();
            }
        }
    }
    
    protected long nodeAddress;
    
    private final Hashtable _agents;
    private final Hashtable _groups;
    private final MessageProcessor _processor;
    
    private BinarySerialiserStream _serialiser;
    private MessageOutputStream _messageOutput;
    
    private BinaryDeserialiserStream _deserialiser;
    private MessageInputStream _messageInput;
    private ProtocolManager _protocol;
    
    
    public AODVNodeComponent() {
        _agents= new Hashtable();
        _groups= new Hashtable();
        _processor= new MessageProcessor();
    }

    public void cleanup() {
        _processor.stop();
        _protocol.stop();
        LowPan.getInstance().deregisterProtocol(ProtocolManager.PROTOCOL_NUM);
        super.cleanup();
    }

    public void initialise() {
    	super.initialise();
    	_protocol= new ProtocolManager(LowPan.getInstance());
        _messageOutput= new MessageOutputStream(_protocol);
        _serialiser= new BinarySerialiserStream(_messageOutput);
        
        nodeAddress= RadioFactory.getRadioPolicyManager().getIEEEAddress();
        
        _messageInput= new MessageInputStream(_protocol);
        _deserialiser= new BinaryDeserialiserStream(NodeScope.getNodeReference().getClassLoader(), _messageInput);
        
        LowPan.getInstance().setRoutingManager(AODVManager.getInstance());
        LowPan.getInstance().registerProtocol(ProtocolManager.PROTOCOL_NUM, _protocol);
        _protocol.start();
        _processor.start();
    }
    
    public void register(IMessageListener listener) {
        synchronized (_agents) {
            IMessageListener oldListener= (IMessageListener) _agents.put(getAgentKey(), listener);
            if(oldListener != null && oldListener != listener) {
                throw new Error("implementation error");
            } else if(oldListener == null) {
                AgentScope.getAgentReference().addHandle(new AODVCommunicator());
            }
        }
    }
    
    public void unregister(IMessageListener listener) {
        synchronized (_agents) {
            final String key= getAgentKey();
            IMessageListener oldListener= (IMessageListener) _agents.get(key);
            if(oldListener != listener) {
                throw new Error("implementation error");
            }
            
            for(Enumeration addresses= _groups.keys(); addresses.hasMoreElements();) {
                Address group= (Address) addresses.nextElement();
                changeGroupAssociation(group, false);
            }
            
            _agents.remove(key);
            IAgent agentRef= AgentScope.getAgentReference();
            IHandle commHandle= agentRef.getHandle(AODVCommunicator.class);
            if(commHandle != null) {
                agentRef.removeHandle(commHandle);
            }
        }
    }

    protected void internalProcessMessage() {
        synchronized (_deserialiser) {
            final Logger logger= NodeScope.getNodeReference().getLogger("AODV");
            
            logger.info("try to read message... ");
            MessageID mid= _protocol.nextAvailableMessage();
            logger.info(" new message available: " + mid.toString());
            _messageInput.setMessageID(mid);
            
            try {
                Message message= new Message();
                message.deserialise(_deserialiser);
                logger.info(" message deserialised");
                String targetStr= message.getHeader(IMessage.DefaultHeader.TARGET_ADDRESS);
                Address targetAddress= Address.parseAddress(targetStr);
                
                synchronized (_agents) {
                    switch(targetAddress.getType()) {
                        case IAddress.UNICAST: {
                            String agentName= targetAddress.getSelector();
                            IMessageListener agent= (IMessageListener)  _agents.get(agentName);
                            if(agent == null) {
                                logger.warn("MessageLayer: received message for unknown agent '" + targetStr + "'");
                            } else {
                                agent.processMessage(message);
                            }
                            
                            break;
                        }
                        
                        case IAddress.MULTICAST: {
                            List members= (List) _groups.get(targetAddress);
                            if(members == null) {
                                logger.warn("MessageLayer: received message for unknown group '" + targetStr + "'");
                            } else {
                                for(Node n= members.head(), end= members.tail(); (n= n.next()) != end;) {
                                    // performance leak for larger groups -> run in parallel!
                                    String agentKey= (String) n.value();
                                    IMessageListener agent= (IMessageListener) _agents.get(agentKey);
                                    
                                    // FIXME: message is shared
                                    agent.processMessage(message);
                                }
                            }
                            
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                _deserialiser.flush();
                _protocol.clearIncoming(mid);
            }
        }
    }
    
    protected void addHandlesOn(AgentScope scope) {
        scope.getContainerReference().addHandle(this);
    }

    protected void changeGroupAssociation(Address group, boolean join) {
        synchronized (_agents) {
            final String key= getAgentKey();
            IMessageListener listener= (IMessageListener) _agents.get(key);
            
            if(listener == null) {
                throw new Error("implementation error");
            }
            
            List groupMembers= (List) _groups.get(group);
            
            if(join) {
                if(groupMembers == null) {
                    groupMembers= new List();
                    _groups.put(group, groupMembers);
                }
                
                if(!groupMembers.contains(key)) {
                    groupMembers.addLast(key);
                }
            } else if(groupMembers != null) {
                groupMembers.remove(key);
                
                if(groupMembers.size() <= 0) {
                    _groups.remove(group);
                }
            }
        }
    }
    
    protected void internalSendMessage(Address target, Message message) throws IOException {
        final String key= getAgentKey();
        
        IUnicastAddress sourceAddress= Address.createUnicastAddress(nodeAddress, key);
        message.setHeader(IMessage.DefaultHeader.SOURCE_ADDRESS, sourceAddress.toString());
        message.setHeader(IMessage.DefaultHeader.TARGET_ADDRESS, target.toString());
        
        synchronized (_serialiser) {
            MessageID mid;
            
            switch(target.getType()) {
                case IAddress.UNICAST: {
                    mid= _protocol.newOutgoingMessageID(target.getNodeAddress(), false);
                    break;
                }
                
                case IAddress.MULTICAST: {
                    mid= _protocol.newOutgoingMessageID(0, true);
                    break;
                }
                
                default: {
                    throw new IOException("unknown address type: '" + target + "'");
                }
            }
            
            _messageOutput.setMessageID(mid);
            
            try {
                message.serialise(_serialiser);
            } finally {
                _serialiser.flush();
                _protocol.clearOutgoing(mid);
            }
        }
    }

    protected void removeHandlesFrom(AgentScope scope) {
        scope.getContainerReference().removeHandle(this);
    }
}
