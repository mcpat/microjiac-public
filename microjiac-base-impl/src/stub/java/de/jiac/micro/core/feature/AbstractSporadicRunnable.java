/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Base-Implementation.
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

/*
 * $Id$ 
 */
package de.jiac.micro.core.feature;

import java.util.Enumeration;

/**
 * API-Compilation Stub. The specific implementation is part of the platforms!
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public abstract class AbstractSporadicRunnable extends AbstractSchedulable {
    protected AbstractSporadicRunnable() {
        throw new RuntimeException("Stub!");
    }
    
    public final long getMinimumInterarrival() {
        throw new RuntimeException("Stub!");
    }
    
    /**
     * To specify the minimum time distance between two subsequent executions of this behaviour.
     * Data that occurs in between the two executions will <strong>not</strong> be forwarded to this behaviour!
     * 
     * @param minimumInterrival     the minimum time distance between two subsequent calls
     */
    public final void setMinimumInterarrival(long minimumInterrival) {
        throw new RuntimeException("Stub!");
    }
    
    /**
     * This method is called each time new data is available in the
     * {@link de.jiac.micro.agent.memory.IShortTermMemory short-term memory}.
     * If the type of data matches the filters, then {@link #runShort(Enumeration)} is executed.
     */
    protected abstract Class[] filterDataTypes();
    
    /**
     * @param sensorReadings    the current readings that match with the specified filters. Due to efficiency reasons
     *                          this reference must only be used in this method and must not be stored!
     */
    protected abstract void runShort(Enumeration sensorReadings);
}
