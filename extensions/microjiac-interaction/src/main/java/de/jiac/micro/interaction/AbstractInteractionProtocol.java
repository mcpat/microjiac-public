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
package de.jiac.micro.interaction;

import de.jiac.micro.core.feature.AbstractPeriodicRunnable;
import de.jiac.micro.core.io.IMessage;
import de.jiac.micro.util.List;
import de.jiac.micro.util.List.Node;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public abstract class AbstractInteractionProtocol {
    protected static class Interaction {
        public final long startTime;
        public final long deadline;
        public final String id; 
        public final Object context;
        
        protected Interaction(long timeout, Object ctx) {
            startTime= System.currentTimeMillis();
            deadline= startTime + timeout;
            id= String.valueOf(hashCode());
            context= ctx;
        }
    }
    
    private final class ProtocolTimer extends AbstractPeriodicRunnable {
        private long _lastExecution= 0L;
        
        protected ProtocolTimer() {}
        
        protected void runShort() {
            final long currentTime= System.currentTimeMillis();
            
            long minDeadline= Long.MAX_VALUE;
            
            synchronized (interactions) {
                for(Node n= interactions.head(), end= interactions.tail(); (n= n.next()) != end;) {
                    Interaction i= (Interaction) n.value();
                    
                    if(i.deadline <= currentTime) {
                        Node tmp= n.previous();
                        interactions.delete(n);
                        n= tmp;
                        timeoutInteraction(i);
                    } else {
                        if(i.deadline < minDeadline) {
                            minDeadline= i.deadline;
                        }
                    }
                }
                
                setPeriod(minDeadline - currentTime);
                _lastExecution= currentTime;
            }
        }
        
        protected void adjustPeriod(long deadline) {
            if(isEnabled()) {
                long oldStart= getStart();
                
                if(deadline < oldStart) {
                    setStart(deadline);
                } else {
                    long oldNextExec= _lastExecution + getPeriod();
                    if(oldNextExec > deadline) {
                        disable();
                        setStart(deadline);
                        enable();
                    }
                }
            } else {
                setStart(deadline);
                enable();
            }
        }
    }
    
    private final ProtocolTimer _timer;
    protected final List interactions;
    protected final String name;
    
    protected AbstractInteractionProtocol(String protocolName) {
        _timer= new ProtocolTimer();
        interactions= new List();
        name= protocolName;
    }
    
    /**
     * Caller must hold the lock on interactions!
     */
    protected final Interaction newInteraction(long timeout, Object ctx) {
        Interaction i= new Interaction(timeout, ctx);
        
        // TODO: insert the new interaction in deadlines ascending order
        interactions.addLast(i);
        _timer.adjustPeriod(i.deadline);
        return i;
    }
    
    /**
     * Caller must hold the lock on interactions!
     */
    protected final Interaction findInteraction(String id) {
        for(Node n= interactions.head(), end= interactions.tail(); (n= n.next()) != end;) {
            Interaction i= (Interaction)n.value();
            
            if(i.id.equals(id)) {
                return i;
            }
        }
        
        return null;
    }
    
    protected final void setProtocolHeader(IMessage message) {
        message.setHeader(InteractionManagerElement.INTERACTION_HEADER_KEY, name);
    }
    
    protected abstract void handleMessage(IMessage message);
    protected abstract void timeoutInteraction(Interaction ia);
}
