/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Interaction.
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
package de.jiac.micro.interaction;

import java.util.Enumeration;
import java.util.Hashtable;

import de.jiac.micro.agent.AbstractReactiveBehaviour;
import de.jiac.micro.agent.IActuator;
import de.jiac.micro.core.IContainer;
import de.jiac.micro.core.IHandle;
import de.jiac.micro.core.io.IMessage;
import de.jiac.micro.core.scope.Scope;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class InteractionManagerElement extends AbstractReactiveBehaviour implements IActuator {
    protected final static String INTERACTION_HEADER_KEY= "mjiac-interaction";
    
    private static final class Registry implements IInteractionRegistry {
        protected final Hashtable registeredProtocols;
        
        protected Registry() {
            registeredProtocols= new Hashtable();
        }

        public boolean registerProtocol(AbstractInteractionProtocol proto) {
            if(proto == null) {
                throw new IllegalArgumentException("protocol must be non-null");
            }
            
            synchronized (registeredProtocols) {
                AbstractInteractionProtocol registered= (AbstractInteractionProtocol) registeredProtocols.get(proto.name);
                
                if(registered != null) {
                    return registered == proto;
                }
                
                registeredProtocols.put(proto.name, proto);
                return true;
            }
        }

        public boolean unregisterProtocol(AbstractInteractionProtocol proto) {
            if(proto == null) {
                throw new IllegalArgumentException("protocol must be non-null");
            }
            
            synchronized (registeredProtocols) {
                AbstractInteractionProtocol registered= (AbstractInteractionProtocol) registeredProtocols.get(proto.name);
                
                if(registered != proto) {
                    return false;
                }
                
                registeredProtocols.remove(proto.name);
                return true;
            }
        }
    }
    
    private final Class[] _filterDataTypes;
    private final Registry _registry;
    
    public InteractionManagerElement() {
        _filterDataTypes= new Class[] {
            IMessage.class
        };
        
        _registry= new Registry();
    }
    
    public IHandle getHandle() {
        return _registry;
    }
    
    protected Class[] filterDataTypes() {
        return _filterDataTypes;
    }

    protected void runShort(Enumeration sensorReadings) {
        while(sensorReadings.hasMoreElements()) {
            IMessage message= (IMessage) sensorReadings.nextElement();
            String interaction= message.getHeader(INTERACTION_HEADER_KEY);
            
            if(interaction != null) {
                AbstractInteractionProtocol proto= (AbstractInteractionProtocol) _registry.registeredProtocols.get(interaction);
                
                if(proto != null) {
                    proto.handleMessage(message);
                } else {
                    IContainer cont= Scope.getContainer();
                    cont.getLogger().warn("Interaction: received message for unknown protocol '" + interaction + "'");
                }
            }
        }
    }
}
