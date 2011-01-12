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

package de.jiac.micro.ips.request;

import de.jiac.micro.agent.handle.ICommunicationHandle;
import de.jiac.micro.core.io.IAddress;
import de.jiac.micro.core.io.IMessage;
import de.jiac.micro.ips.AbstractProtocolInitiator;
import de.jiac.micro.ips.AbstractProtocolPart;
import de.jiac.micro.ips.IMessageConstants;

/**
 *
 * @author Marcel Patzlaff
 */
public final class RequestProtocolInitiator extends AbstractProtocolInitiator {
    protected static final ProtocolState STARTED;
    protected static final ProtocolState AGREED;
    
    static {
        STARTED= new ProtocolState() {
            public ProtocolState handleStateChange(Interaction ia, String performative, IMessage message, AbstractProtocolPart part) {
                if(performative == IMessageConstants.PERFORMATIVE_AGREE) {
                    ((RequestProtocolInitiator) part).handleAgree(ia, message);
                    return AGREED;
                } else if(performative == IMessageConstants.PERFORMATIVE_REFUSE) {
                    ((RequestProtocolInitiator) part).handleRefuse(ia, message);
                    return null;
                } else if(performative == IMessageConstants.PERFORMATIVE_INFORM_RESULT) {
                    ((RequestProtocolInitiator) part).handleResult(ia, message);
                    return null;
                } else if(performative == IMessageConstants.PERFORMATIVE_FAILURE) {
                    ((RequestProtocolInitiator) part).handleFailure(ia, message);
                    return null;
                }
                
                return super.handleStateChange(ia, performative, message, part);
            }
        };
        
        AGREED= new ProtocolState() {
            public ProtocolState handleStateChange(Interaction ia, String performative, IMessage message, AbstractProtocolPart part) {
                if(performative == IMessageConstants.PERFORMATIVE_FAILURE) {
                    ((RequestProtocolInitiator) part).handleFailure(ia, message);
                    return null;
                } else if(performative == IMessageConstants.PERFORMATIVE_INFORM_RESULT) {
                    ((RequestProtocolInitiator) part).handleResult(ia, message);
                    return null;
                }
                
                return super.handleStateChange(ia, performative, message, part);
            }
        };
    }
    
    private final IRequestInitiatorHandler _handler;
    
    public RequestProtocolInitiator(String name, IRequestInitiatorHandler handler) {
        super(name, STARTED);
        
        if(handler == null) {
            throw new IllegalArgumentException("handler must not be null");
        }
        
        _handler= handler;
    }

    public String issueRequest(IAddress target, Object content, long timeout, Object userContext) {
        Interaction ctx= newContext(target, timeout, userContext);
        ICommunicationHandle ch= getCommunicationHandle();
        IMessage message= newInteractionMessage(ch, ctx, IMessageConstants.PERFORMATIVE_REQUEST);
        message.setContent(content);
        sendMessage(ch, ctx, message);
        return ctx.id;
    }

    protected void timeoutInteraction(Interaction ia) {

    }
    
    protected void handleAgree(Interaction ia, IMessage message) {
        _handler.onAgree(ia, message.getContent());
    }
    
    protected void handleFailure(Interaction ia, IMessage message) {
        _handler.onFailure(ia, message.getContent());
    }
    
    protected void handleRefuse(Interaction ia, IMessage message) {
        _handler.onRefuse(ia, message.getContent());
    }
    
    protected void handleResult(Interaction ia, IMessage message) {
        _handler.onResult(ia, message.getContent());
    }
}
