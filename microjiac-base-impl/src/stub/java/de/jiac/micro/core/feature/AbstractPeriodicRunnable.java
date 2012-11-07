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
public abstract class AbstractPeriodicRunnable extends AbstractSchedulable {
    protected AbstractPeriodicRunnable() {
        throw new RuntimeException("Stub!");
    }
    
    public final long getPeriod() {
        throw new RuntimeException("Stub!");
    }
    
    public final void setPeriod(long period) {
        throw new RuntimeException("Stub!");
    }

    public final long getStart() {
        throw new RuntimeException("Stub!");
    }
    
    public final void setStart(long start) {
        throw new RuntimeException("Stub!");
    }
    
    protected abstract void runShort();
}
