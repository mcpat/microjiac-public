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

package de.jiac.micro.agent.memory;

import java.util.Enumeration;

import de.jiac.micro.core.IHandle;

/**
 * This interface describes the capabilities of a
 * long term memory.
 * <p>
 * There is no garanty on how long the data will be stored.
 * This depends on the kind of implementation the specific
 * platform provides.
 * 
 * @author Marcel Patzlaff
 */
public interface ILongTermMemory extends IHandle {
    /**
     * Puts new knowledge into the memory.
     * 
     * @param knowledge     the new knowledge to memorise
     */
    void memorise(IFact knowledge);
    
    /**
     * Obtains the last memorised knowledge of the specified type.
     * 
     * @param factType      the type of knowledge to check
     * 
     * @return      the latest memorised instance or {@code null} if none was found
     */
    IFact rememberNewest(Class factType);
    
    /**
     * Obtains the first memorised knowledge of the specified type.
     * 
     * @param factType      the type of knowledge to check
     * 
     * @return      the first memorised instance or {@code null} if none was found
     */
    IFact rememberOldest(Class factType);
    
    /**
     * Obtains all memorised knowledge instances of the specified type.
     * 
     * @param factType      the type of knowledge to retrieve
     * 
     * @return      an enumeration about all memorised instances.
     */
    Enumeration rememberAll(Class factType);
    
    /**
     * Removes the specific knowledge instance from this memory.
     * 
     * @param knowlegde     the knowledge to remove
     */
    void forget(IFact knowlegde);
    
    /**
     * Removes all knowledge instances of the specified type.
     * 
     * @param factType      the type of knowledge to remove
     */
    void forgetAll(Class factType);
}
