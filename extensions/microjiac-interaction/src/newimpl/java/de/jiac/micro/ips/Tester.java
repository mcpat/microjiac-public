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

import java.io.IOException;

import de.jiac.micro.agent.handle.ICommunicationHandle;
import de.jiac.micro.core.io.IAddress;
import de.jiac.micro.core.io.IMessage;
import de.jiac.micro.core.io.IMulticastAddress;
import de.jiac.micro.core.io.IUnicastAddress;
import de.jiac.micro.internal.io.Message;
import de.jiac.micro.ips.AbstractProtocolPart.Interaction;
import de.jiac.micro.ips.request.IRequestInitiatorHandler;
import de.jiac.micro.ips.request.IRequestParticipantHandler;
import de.jiac.micro.ips.request.RequestProtocolInitiator;
import de.jiac.micro.ips.request.RequestProtocolParticipant;

/**
 *
 * @author Marcel Patzlaff
 */
public class Tester {
    private static final class InitiatorHandler implements IRequestInitiatorHandler {
        public void onAgree(Interaction ia, Object content) {
            System.out.println("I: onAgree");
        }

        public void onFailure(Interaction ia, Object content) {
            System.out.println("I: onFailure");
        }

        public void onRefuse(Interaction ia, Object content) {
            System.out.println("I: onRefuse");
        }

        public void onResult(Interaction ia, Object content) {
            System.out.println("I: onResult");
        }
    }
    
    private static final class ParticipantHandler implements IRequestParticipantHandler {
        public void onRequest(Interaction ia, Object content) {
            System.out.println("P: onRequest");
            RequestProtocolParticipant.issueResponse(ia, content);
        }

        public void processRequest(Interaction ia) {
            System.out.println("P: processRequest");
        }
    }
    
    private static final class Communicator implements ICommunicationHandle {
        public IMessage createMessage() {
            return new Message();
        }

        public IAddress getAddressForString(String addressStr) {
            // TODO Auto-generated method stub
            return null;
        }

        public IUnicastAddress[] getLocalAddresses() {
            // TODO Auto-generated method stub
            return null;
        }

        public IMulticastAddress getMulticastAddressForName(String groupName) {
            // TODO Auto-generated method stub
            return null;
        }

        public void joinGroup(IMulticastAddress address) throws IOException {
            // TODO Auto-generated method stub
            
        }

        public void leaveGroup(IMulticastAddress address) throws IOException {
            // TODO Auto-generated method stub
            
        }

        public void sendMessage(IAddress address, IMessage message) throws IOException {
            // TODO Auto-generated method stub
            
        }
    }
    
    
    protected static ICommunicationHandle COMM;
    
    public static void main(String[] args) throws Exception {
        InteractionManager iim= new InteractionManager();
        RequestProtocolInitiator initiator= new RequestProtocolInitiator("test", new InitiatorHandler());
        iim.registerProtocolPart(initiator);
        
        initiator.issueRequest(null, "Hallo Welt", 1000L, null);
        
        InteractionManager pim= new InteractionManager();
        RequestProtocolParticipant participant= new RequestProtocolParticipant("test", new ParticipantHandler());
        pim.registerProtocolPart(participant);
    }
}
