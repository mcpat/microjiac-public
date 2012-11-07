/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Base-Implementation.
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



/**
 * API-Compilation Stub. The specific implementation is part of the platforms!
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public abstract class AbstractSchedulable {
    protected AbstractSchedulable() {
        throw new RuntimeException("Stub!");
    }
    
    public final void enable() {
        throw new RuntimeException("Stub!");
    }
    
    public final void disable() {
        throw new RuntimeException("Stub!");
    }
    
    public final boolean isEnabled() {
        throw new RuntimeException("Stub!");
    }
    
    public final long getCost() {
        throw new RuntimeException("Stub!");
    }
    
    public final void setCost(long cost) {
        throw new RuntimeException("Stub!");
    }

    public final AbstractViolationHandler getCostOverrunHandler() {
        throw new RuntimeException("Stub!");
    }
    
    public final void setCostOverrunHandler(AbstractViolationHandler overrunHandler) {
        throw new RuntimeException("Stub!");
    }

    public final long getDeadline() {
        throw new RuntimeException("Stub!");
    }
    
    public final void setDeadline(long deadline) {
        throw new RuntimeException("Stub!");
    }

    public final AbstractViolationHandler getDeadlineMissHandler() {
        throw new RuntimeException("Stub!");
    }
    
    public final void setDeadlineMissHandler(AbstractViolationHandler deadlineMissHandler) {
        throw new RuntimeException("Stub!");
    }

    public final int getPriority() {
        throw new RuntimeException("Stub!");
    }
    
    public final void setPriority(int priority) {
        throw new RuntimeException("Stub!");
    }
}
