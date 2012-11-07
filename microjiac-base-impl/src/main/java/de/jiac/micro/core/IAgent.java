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
package de.jiac.micro.core;


/**
 * Tiny interface to describe an agent.
 * 
 * @author Marcel Patzlaff
 * @author Erdene-Ochir Tuguldur
 */
public interface IAgent extends IContainer {
    /**
     * Obtains an agent property associated with the specified key.
     * 
     * @param key       the key for the property (= property name)
     * @return      the value of the property or {@code null} if not set
     */
    Object getProperty(String key);
    
    /**
     * Associates a property key with the specified value.
     * <p>
     * If {@code value} is {@code null} then this method just removes the property.
     * 
     * @param key       the key for the property (= property name)
     * @param value     the value for the property or {@code null} if the property
     *                  should be removed
     */
    void setProperty(String key, Object value);
}
