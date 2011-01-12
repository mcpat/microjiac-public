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

package de.jiac.micro.agent;

import de.jiac.micro.agent.memory.IShortTermMemory;
import de.jiac.micro.core.ILifecycleAware;

/**
 * The {@code ISensor} interface describes an agent element that connects the
 * agent to some kind of environment. Data from the environment is processed in
 * this sensor and delegated to the agents knowledge store.
 * <p>
 * As different devices have different capabilities with respect to
 * communication, sensors should be used to hide these details. Define a generic
 * data representation (= knowledge) and other agent elements may remain
 * unchanged if the agent is deployed to another device. Only the configuration
 * has to be fitted to reflect the devices capabilities.
 * 
 * @see IActuator
 * @see IShortTermMemory
 * 
 * @author Marcel Patzlaff
 * @author Erdene-Ochir Tuguldur
 */
public interface ISensor extends IAgentElement {
    /**
     * This method is called only once after instantiation of this sensor.
     * <p>
     * If this sensor also implements {@link ILifecycleAware} then this method
     * is called <strong>before</strong> calling {@link ILifecycleAware#initialise()}.
     * 
     * @param stm   the short term knowledge store
     */
    void setShortTermMemory(IShortTermMemory stm);
}
