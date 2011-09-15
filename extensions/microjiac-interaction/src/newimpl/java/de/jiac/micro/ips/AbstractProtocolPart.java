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

package de.jiac.micro.ips;

import de.jiac.micro.agent.handle.ICommunicationHandle;
import de.jiac.micro.core.io.IMessage;
import de.jiac.micro.core.io.IUnicastAddress;
import de.jiac.micro.core.scope.Scope;


/**
 * 
 * @author Marcel Patzlaff
 */
public abstract class AbstractProtocolPart {
    public static final class Interaction {
        protected long startTime;
        protected long deadline;
        public final String id; 
        public final Object context;
        
        /*package*/ ProtocolState state;
        /*package*/ ProtocolState nextState;
        /*package*/ final AbstractProtocolPart protocolPart;
        
        /*package*/ Interaction(AbstractProtocolPart protocolPart, long timeout, Object ctx) {
            this.protocolPart= protocolPart;
            this.startTime= System.currentTimeMillis();
            this.deadline= startTime + timeout;
            this.context= ctx;
            this.state= protocolPart.initialState;
            id= String.valueOf(hashCode());
        }
        
        /*package*/ Interaction(String id, AbstractProtocolPart protocolPart, long timeout, Object ctx) {
            this.protocolPart= protocolPart;
            this.startTime= System.currentTimeMillis();
            this.deadline= startTime + timeout;
            this.context= ctx;
            this.state= protocolPart.initialState;
            this.id= id;
        }
    }
    
    protected static abstract class ProtocolState {
        protected static ProtocolState getNextState(Interaction ia) {
            ProtocolState next= ia.nextState;
            ia.nextState= null;
            return next;
        }
        
        public ProtocolState handleStateChange(Interaction ia, String performative, IMessage message, AbstractProtocolPart part) {
            return part.handleOutOfSequence(ia, performative, message);
        }
        
        public ProtocolState process(Interaction ia, AbstractProtocolPart part) {
            return null;
        }
        
        public boolean isBlockingState() {
            return false;
        }
        
        public void issueStateChange(Interaction ia, String performative, AbstractProtocolPart part) {
            throw new ProtocolException(performative);
        }
        
        protected ProtocolState getNextState(String performative) {
            throw new ProtocolException(performative);
        }
    }
    
    protected static IUnicastAddress getSourceAddress(IMessage message) {
        String addStr= message.getHeader(IMessage.DefaultHeader.SOURCE_ADDRESS);
        return (IUnicastAddress) getCommunicationHandle().getAddressForString(addStr);
    }
    
    protected static ICommunicationHandle getCommunicationHandle() {
        return (ICommunicationHandle) Scope.getContainer().getHandle(ICommunicationHandle.class);
    }
    
    protected static IMessage newInteractionMessage(ICommunicationHandle ch, Interaction ctx, String performative) {
        IMessage message= ch.createMessage();
        message.setHeader(IMessageConstants.HEADER_LANGUAGE, IMessageConstants.LANGUAGE_SERIALISED);
        message.setHeader(IMessageConstants.HEADER_CONVERSATION_ID, ctx.id);
        message.setHeader(IMessageConstants.HEADER_PROTOCOL, ctx.protocolPart.protocolName);
        message.setHeader(IMessageConstants.HEADER_PERFORMATIVE, performative);
        return message;
    }
    
    protected static void sendMessage(ICommunicationHandle ch, Interaction ia, IMessage message) {
        
    }
    
    protected static void issuePerformative(Interaction ia, String performative, Object content) {
        ensureValidPerformative(ia, performative);
        ICommunicationHandle ch= getCommunicationHandle();
        IMessage message= newInteractionMessage(ch, ia, performative);
        if(content != null) {
            message.setContent(content);
        }
        sendMessage(ch, ia, message);
    }
    
    protected static void ensureValidPerformative(Interaction ia, String performative) {
        if(ia.nextState != null) {
            throw new ProtocolException("multiple steps");
        }
        
        ia.nextState= ia.state.getNextState(performative);
    }
    
    /*package*/ final String protocolName;
    /*package*/ final ProtocolState initialState;
    /*package*/ volatile InteractionManager manager;
    
    protected AbstractProtocolPart(String name, ProtocolState initialState) {
        this.protocolName= name;
        this.initialState= initialState;
    }
     
    protected final ProtocolState handleOutOfSequence(Interaction ia, String performative, IMessage message) {
        return null;
    }
}
