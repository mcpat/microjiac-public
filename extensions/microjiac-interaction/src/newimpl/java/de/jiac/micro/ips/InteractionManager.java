/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Interaction.
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
package de.jiac.micro.ips;

import com.github.libxjava.util.BasicArrayList;
import com.github.libxjava.util.BasicEnumeration;
import com.github.libxjava.util.BasicHashMap;

import de.jiac.micro.core.io.IMessage;
import de.jiac.micro.ips.AbstractProtocolPart.Interaction;
import de.jiac.micro.ips.AbstractProtocolPart.ProtocolState;

/**
 *
 * @author Marcel Patzlaff
 */
/*package*/ final class InteractionManager implements IProtocolRegistry {
    private final BasicHashMap _protocolParticipants;
    private final BasicArrayList _currentInteractions;
    
    /*package*/ InteractionManager() {
        _protocolParticipants= new BasicHashMap();
        _currentInteractions= new BasicArrayList();
    }
    
    /*package*/ void addInteraction(Interaction ctx) {
        synchronized (_currentInteractions) {
            _currentInteractions.add(ctx);
        }
    }
    
    /*package*/ void handleMessage(IMessage message) {
        String id= message.getHeader(IMessageConstants.HEADER_CONVERSATION_ID);
        
        if(id != null) {
            Interaction ctx;
            synchronized (_currentInteractions) {
                ctx= findContext(id);
                
                if(ctx == null) {
                    String protocol= message.getHeader(IMessageConstants.HEADER_PROTOCOL);
                    
                    if(protocol != null) {
                        ctx= newParticipantInteraction(protocol, id);
                    }
                }
            }

            // TODO: should we do something if id == null || ctx == null ?
           
            if(ctx != null) {
                processInteraction(ctx, message);
            }
        }
    }
    
    public void registerProtocolPart(AbstractProtocolPart protocolPart) {
        if(protocolPart == null) {
            throw new IllegalArgumentException("protocol part must not be null");
        }
        
        if(protocolPart instanceof AbstractProtocolParticipant) {
            synchronized (_protocolParticipants) {
                String name= protocolPart.protocolName;
                AbstractProtocolParticipant old= (AbstractProtocolParticipant) _protocolParticipants.put(name, protocolPart);
                
                if(old != null && old != protocolPart) {
                    _protocolParticipants.put(name, old);
                    throw new IllegalArgumentException("protocol participant for '" + name + "' already specified");
                }
            }
        }
        
        protocolPart.manager= this;
    }

    public void unregisterProtocolPart(AbstractProtocolPart protocolPart) {
        if(protocolPart == null) {
            throw new IllegalArgumentException("protocol part must not be null");
        }
        
        synchronized (_protocolParticipants) {
            _protocolParticipants.remove(protocolPart.protocolName);
        }
        
        synchronized (_currentInteractions) {
            for(BasicEnumeration e= _currentInteractions.enumeration(); e.hasMoreElements();) {
                Interaction ctx= (Interaction) e.nextElement();
                
                if(ctx.protocolPart == protocolPart) {
                    // TODO: abort interaction
                    e.remove();
                }
            }
        }
        
        protocolPart.manager= null;
    }
    
    /**
     * Caller must hold lock on interactions
     */
    private Interaction findContext(String id) {
        for(int i= _currentInteractions.size() - 1; i >= 0; i--) {
            Interaction ia= (Interaction) _currentInteractions.get(i);
            
            if(ia.id.equals(id)) {
                return ia;
            }
        }
        return null;
    }
    
    private Interaction newParticipantInteraction(String protocol, String id) {
        synchronized (_protocolParticipants) {
            AbstractProtocolParticipant participant= (AbstractProtocolParticipant) _protocolParticipants.get(protocol);
            
            if(participant != null) {
                // TODO: timeouts
                return new Interaction(id, participant, -1, null);
            }
        }
        
        return null;
    }
    
    private void processInteraction(Interaction ia, IMessage message) {
        String performative= message.getHeader(IMessageConstants.HEADER_PERFORMATIVE);

        if(performative != null) {
            // enable reference-based equality checks
            performative= performative.intern();
            message.setHeader(IMessageConstants.HEADER_PERFORMATIVE, performative);
            
            ProtocolState nextState= ia.state.handleStateChange(ia, performative, message, ia.protocolPart);
            if(nextState != null) {
                ia.state= nextState;
            } else {
                // interaction is finished
                synchronized (_currentInteractions) {
                    _currentInteractions.remove(ia);
                }
            }
        } else {
            // TODO: what to do here?
        }
    }
}
