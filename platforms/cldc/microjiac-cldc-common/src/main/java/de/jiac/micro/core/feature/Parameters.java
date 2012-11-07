/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC CLDC-Common.
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
package de.jiac.micro.core.feature;

import de.jiac.micro.agent.IScheduler;
import de.jiac.micro.core.scope.AgentScope;

/**
 * 
 * @author Vladimir Sch&ouml;ner
 * @version $Revision$
 */
public abstract class Parameters {
    /*package*/ AbstractSchedulable schedulable;
    /*package*/ volatile long lastExecution;
    /*package*/ int priority = 0;
    /*package*/ long cost = 0;
    /*package*/ long deadline = 0;
    
    /*package*/ AbstractViolationHandler deadlineMissHandler= null;
    /*package*/ AbstractViolationHandler costOverrunHandler= null;

    public final int getPriority() {
        return priority;
    }

    public final void setPriority(int priority) {
        this.priority= priority;
    }

    public final long getCost() {
        return cost;
    }

    public final void setCost(long cost) {
        if(cost < 0) {
            throw new IllegalArgumentException("cost must be >= 0L");
        }
        
        this.cost= cost;
    }

    public final long getDeadline() {
        return deadline;
    }

    public final void setDeadline(long deadline) {
        if(deadline < 0) {
            throw new IllegalArgumentException("deadline must be >= 0L");
        }
        
        this.deadline= deadline;
    }

    public final AbstractViolationHandler getDeadlineMissHandler() {
        return deadlineMissHandler;
    }
    
    public final void setDeadlineMissHandler(AbstractViolationHandler deadlineMissHandler) {
        this.deadlineMissHandler= deadlineMissHandler;
    }

    public final AbstractViolationHandler getCostOverrunHandler() {
        return costOverrunHandler;
    }

    public final void setCostOverrunHandler(AbstractViolationHandler overrunHandler) {
        this.costOverrunHandler= overrunHandler;
    }
    
    public final boolean isEnabled() {
        final IScheduler s = getScheduler();
        
        if(s != null) {
            return s.containsSchedulable(schedulable);
        }
        
        return false;
    }
    
    public final void setEnabled(boolean enabled) {
        final IScheduler s = getScheduler();

        if (s != null) {
            if(enabled) {
                s.addSchedulable(schedulable);
            } else {
                s.removeSchedulable(schedulable);
            }
        }
    }
    
    protected final static IScheduler getScheduler() {
        return (IScheduler) AgentScope.getAgentHandle(IScheduler.class);
    }
}
