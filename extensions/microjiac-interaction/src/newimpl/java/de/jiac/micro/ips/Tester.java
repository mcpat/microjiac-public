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
import java.util.Vector;

import de.dailab.keks.io.Address;
import de.jiac.micro.agent.handle.ICommunicationHandle;
import de.jiac.micro.core.IHandle;
import de.jiac.micro.core.io.IAddress;
import de.jiac.micro.core.io.IMessage;
import de.jiac.micro.core.io.IMulticastAddress;
import de.jiac.micro.core.io.IUnicastAddress;
import de.jiac.micro.core.scope.Scope;
import de.jiac.micro.internal.io.Message;
import de.jiac.micro.ips.AbstractProtocolPart.Interaction;
import de.jiac.micro.ips.request.IRequestInitiatorHandler;
import de.jiac.micro.ips.request.IRequestParticipantHandler;
import de.jiac.micro.ips.request.RequestProtocolInitiator;
import de.jiac.micro.ips.request.RequestProtocolParticipant;
import de.jiac.micro.test.environment.TestScope;

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
        private final IUnicastAddress _localAddress;
        
        private final Vector _messages= new Vector();
        
        Communicator() {
            _localAddress= Address.createUnicastAddress("foo", String.valueOf(hashCode()));
        }
        
        private Communicator _dst;
        
        void link(Communicator dst) {
            _dst= dst;
        }
        
        void handleMessage(IAddress target, IMessage message) {
            synchronized (_messages) {
                _messages.addElement(message);
                _messages.notify();
            }
        }
        
        void workOnMessages() {
            while(true) {
                Message m= null;
                System.out.println("work on messages");
                synchronized (_messages) {
                    while(_messages.size() <= 0) {
                        try {
                            _messages.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                    
                    m= (Message) _messages.elementAt(0);
                    _messages.removeElementAt(0);
                }
                
                if(m != null) {
                    InteractionManager im= (InteractionManager) Scope.getContainer().getHandle(InteractionManager.class);
                    im.handleMessage(m);
                }
            }
        }
        
        public IMessage createMessage() {
            return new Message();
        }

        public IAddress getAddressForString(String addressStr) {
            return Address.createAddressForURI(addressStr);
        }

        public IUnicastAddress[] getLocalAddresses() {
            return new IUnicastAddress[] {_localAddress};
        }

        public IMulticastAddress getMulticastAddressForName(String groupName) {
            return Address.createMulticastAddress(groupName);
        }

        public void joinGroup(IMulticastAddress address) throws IOException {
            // noop
        }

        public void leaveGroup(IMulticastAddress address) throws IOException {
            // noop
        }

        public void sendMessage(IAddress address, IMessage message) throws IOException {
            _dst.handleMessage(address, message);
        }
    }
    
    public static void main(String[] args) throws Exception {
        final InteractionManager iim= new InteractionManager();
        final Communicator ic= new Communicator();
        final RequestProtocolInitiator initiator= new RequestProtocolInitiator("test", new InitiatorHandler());
        iim.registerProtocolPart(initiator);
        
        Thread isatt= TestScope.createScopeAwareTestThread(
            new Runnable() {
                public void run() {
                    System.out.println("try to issue request");
                    initiator.issueRequest(null, "Hallo Welt", 1000L, null);
                    System.out.println("request seems to be successful");
                    ic.workOnMessages();
                }
            },
            new IHandle[]{ic, iim}
        );
        
        InteractionManager pim= new InteractionManager();
        final Communicator pc= new Communicator();
        RequestProtocolParticipant participant= new RequestProtocolParticipant("test", new ParticipantHandler());
        pim.registerProtocolPart(participant);
        
        Thread psatt= TestScope.createScopeAwareTestThread(
            new Runnable() {
                public void run() {
                    pc.workOnMessages();
                }
            },
            new IHandle[]{pc, pim}
        );
        
        pc.link(ic);
        ic.link(pc);
        psatt.start();
        Thread.sleep(1000);
        isatt.start();
    }
}
