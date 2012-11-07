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
import de.jiac.micro.core.ILifecycleAware;

/**
 * The {@code IActuator} interface describes an agent element that connects the
 * agent to some kind of environment. Other elements can use routines via the
 * handle of this actuator.
 * <p>
 * As different devices have different capabilities with respect to
 * communication, actuators should be used to hide these details. Define a
 * generic handler interface and other agent elements may remain unchanged if
 * the agent is deployed to another device. Only the configuration has to be
 * fitted to reflect the devices capabilities.
 * 
 * @see IHandle
 * @see ISensor
 * 
 * @author Marcel Patzlaff
 */
public interface IActuator extends IAgentElement {
    /**
     * Returns the specific handle that exploits the capabilities of
     * this actuator.
     * <p>
     * If this actuator also implements {@link ILifecycleAware} then this
     * method is called <strong>after</strong> calling {@link ILifecycleAware#initialise()}.
     * 
     * @return  specific handle for this actuator
     */
    IHandle getHandle();
}
