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

import de.jiac.micro.core.io.IMessage;
import de.jiac.micro.ips.AbstractProtocolPart;
import de.jiac.micro.ips.AbstractProtocolParticipant;
import de.jiac.micro.ips.IMessageConstants;
import de.jiac.micro.ips.ProtocolException;

/**
 *
 * @author Marcel Patzlaff
 */
public final class RequestProtocolParticipant extends AbstractProtocolParticipant {
    protected static final ProtocolState INITIALISED;
    protected static final ProtocolState PROCESS_REQUEST;
    
    static {
        INITIALISED= new ProtocolState() {
            public ProtocolState handleStateChange(Interaction ia, String performative, IMessage message, AbstractProtocolPart part) {
                if(performative == IMessageConstants.PERFORMATIVE_REQUEST) {
                    ((RequestProtocolParticipant) part).handleRequest(ia, message);
                    return getNextState(ia);
                }
                
                return super.handleStateChange(ia, performative, message, part);
            }

            protected ProtocolState getNextState(String performative) {
                if(performative == IMessageConstants.PERFORMATIVE_AGREE) {
                    return PROCESS_REQUEST;
                } else if(performative == IMessageConstants.PERFORMATIVE_FAILURE) {
                    return null;
                } else if(performative == IMessageConstants.PERFORMATIVE_INFORM_RESULT) {
                    return null;
                } else if(performative == IMessageConstants.PERFORMATIVE_REFUSE) {
                    return null;
                }
                
                throw new ProtocolException(performative);
            }
        };
        
        PROCESS_REQUEST= new ProtocolState() {
            public boolean isBlockingState() {
                return true;
            }

            public ProtocolState process(Interaction ia, AbstractProtocolPart part) {
                ((RequestProtocolParticipant) part).processRequest(ia);
                return getNextState(ia);
            }

            protected ProtocolState getNextState(String performative) {
                if(performative == IMessageConstants.PERFORMATIVE_FAILURE) {
                    return null;
                } else if(performative == IMessageConstants.PERFORMATIVE_INFORM_RESULT) {
                    return null;
                }
                
                throw new ProtocolException(performative);
            }
        };
    }
    
    private final IRequestParticipantHandler _handler;
    
    public RequestProtocolParticipant(String name, IRequestParticipantHandler handler) {
        super(name, INITIALISED);
        
        if(handler == null) {
            throw new IllegalArgumentException("handler must not be null");
        }
        
        _handler= handler;
    }

    public static void issueAgree(Interaction ia) {
        issuePerformative(ia, IMessageConstants.PERFORMATIVE_AGREE, null);
    }
    
    public static void issueRefuse(Interaction ia) {
        issuePerformative(ia, IMessageConstants.PERFORMATIVE_REFUSE, null);
    }
    
    public static void issueFailure(Interaction ia) {
        issuePerformative(ia, IMessageConstants.PERFORMATIVE_FAILURE, null);
    }
    
    public static void issueResponse(Interaction ia, Object content) {
        issuePerformative(ia, IMessageConstants.PERFORMATIVE_INFORM_RESULT, content);
    }

    protected void handleRequest(Interaction ia, IMessage message) {
        _handler.onRequest(ia, message.getContent());
    }
    
    protected void processRequest(Interaction ia) {
        _handler.processRequest(ia);
    }
}
