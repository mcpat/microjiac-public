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
/*
 * $Id$ 
 */
package de.jiac.micro.interaction.rr;

import java.io.IOException;

import de.jiac.micro.agent.handle.ICommunicationHandle;
import de.jiac.micro.core.io.IMessage;
import de.jiac.micro.core.scope.AgentScope;
import de.jiac.micro.interaction.AbstractInteractionProtocol;

/**
 * Simple request-response protocol
 * 
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class RequestResponseProtocol extends AbstractInteractionProtocol {
    private static String REQUEST_SPECIFIER_KEY= "mjiac-request-specifier";
    private static String RESPONSE_ID_KEY= "mjiac-response-id";
    private static String REQUEST_ID_KEY= "mjiac-request-id";
    
    private IRequestHandler _requestHandler;
    
    public RequestResponseProtocol(String name) {
        super(name);
    }
    
    public void setRequestHandler(IRequestHandler handler) {
        _requestHandler= handler;
    }
    
    public String broadcastRequest(String key, Object content, String group, long timeout, Object ctx) throws IOException {
        ICommunicationHandle ch= (ICommunicationHandle) AgentScope.getAgentHandle(ICommunicationHandle.class);
        IMessage message= ch.createMessage();
        message.setContent(content);
        message.setHeader(REQUEST_SPECIFIER_KEY, key);
        setProtocolHeader(message);
        
        synchronized (interactions) {
            Interaction i= newInteraction(timeout, ctx);
            message.setHeader(REQUEST_ID_KEY, i.id);
            ch.sendMessage(ch.getMulticastAddressForName(group), message);
            return i.id;
        }
    }
    
    public String request(String key, Object content, String target, long timeout, Object ctx) throws IOException {
        ICommunicationHandle ch= (ICommunicationHandle) AgentScope.getAgentHandle(ICommunicationHandle.class);
        IMessage message= ch.createMessage();
        message.setContent(content);
        message.setHeader(REQUEST_SPECIFIER_KEY, key);
        setProtocolHeader(message);
        
        synchronized (interactions) {
            Interaction i= newInteraction(timeout, ctx);
            message.setHeader(REQUEST_ID_KEY, i.id);
            ch.sendMessage(ch.getAddressForString(target), message);
            return i.id;
        }
    }
    
    public void respond(String requestId, String key, Object content, String target) throws IOException {
        ICommunicationHandle ch= (ICommunicationHandle) AgentScope.getAgentHandle(ICommunicationHandle.class);
        IMessage message= ch.createMessage();
        message.setContent(content);
        setProtocolHeader(message);
        message.setHeader(REQUEST_SPECIFIER_KEY, key);
        message.setHeader(RESPONSE_ID_KEY, requestId);
        ch.sendMessage(ch.getAddressForString(target), message);
    }
    
    protected void handleMessage(IMessage message) {
        String requestId= message.getHeader(REQUEST_ID_KEY);
        String responseId= message.getHeader(RESPONSE_ID_KEY);
        
        if(requestId != null) {
            String key= message.getHeader(REQUEST_SPECIFIER_KEY);
            String source= message.getHeader(IMessage.DefaultHeader.SOURCE_ADDRESS);
            _requestHandler.onRequest(requestId, key, message.getContent(), source);
        } else if(responseId != null) {
            synchronized (interactions) {
                Interaction i= findInteraction(responseId);
                if(i != null) {
                    String key= message.getHeader(REQUEST_SPECIFIER_KEY);
                    String source= message.getHeader(IMessage.DefaultHeader.SOURCE_ADDRESS);
                    _requestHandler.onResponse(responseId, key, message.getContent(), source, i.context);
                }
                
                // TODO: in broadcast requests it could make sense to wait until timeout
                interactions.remove(i);
            }
        }
    }

    protected void timeoutInteraction(Interaction ia) {
        _requestHandler.onTimeout(ia.id, ia.context);
    }
}
