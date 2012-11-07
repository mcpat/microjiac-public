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
package de.jiac.micro.agent;

import de.jiac.micro.core.IHandle;
import de.jiac.micro.core.feature.AbstractSchedulable;

/**
 * Each agent has a scheduler for periodic and sporadic tasks.
 * 
 * @author Erdene-Ochir Tuguldur
 * @author Marcel Patzlaff
 */
public interface IScheduler extends IHandle {
    /**
     * Adds a sporadic or periodic task to this scheduler.
     * 
     * @param schedulable   the task to add
     */
    void addSchedulable(AbstractSchedulable schedulable);
    
    /**
     * Removes a sporadic or periodic task from this scheduler.
     * 
     * @param schedulable   the task to remove
     */
    void removeSchedulable(AbstractSchedulable schedulable);
    
    /**
     * Checks whether the sporadic or periodic task is already
     * known to this scheduler.
     * 
     * @param schedulable   the task to find
     * @return  {@code true} if already known, {@code false} otherwise
     */
    boolean containsSchedulable(AbstractSchedulable schedulable);
}
