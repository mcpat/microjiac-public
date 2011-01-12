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

import de.jiac.micro.core.ILifecycleAware;
import de.jiac.micro.core.feature.AbstractSporadicRunnable;


/**
 * Describes an abstract reactive behaviour of an agent. It is triggered sporadically according to the specified
 * filter types.
 * 
 * @author Marcel Patzlaff
 */
public abstract class AbstractReactiveBehaviour extends AbstractSporadicRunnable implements IAgentElement, ILifecycleAware {
    protected AbstractReactiveBehaviour() {
        super();
    }
    
    public void cleanup() {}
    public void initialise() {}
    
    /**
     * Default implementation just {@link #enable() enables} the current behaviour. 
     */
    public void start() {
        enable();
    }
    
    /**
     * Default implementation just {@link #disable() disables} the current behaviour. 
     */
    public void stop() {
        disable();
    }
}
