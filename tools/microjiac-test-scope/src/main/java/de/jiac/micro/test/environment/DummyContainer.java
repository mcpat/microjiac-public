/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Test-Scope.
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
package de.jiac.micro.test.environment;

import de.jiac.micro.core.IContainer;
import de.jiac.micro.internal.core.AbstractContainer;
import de.jiac.micro.internal.core.AbstractContainerConfiguration;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class DummyContainer extends AbstractContainer {
    public DummyContainer(String name) {
        addHandle(new AbstractContainerConfiguration(name, name, getClass()) {
                public void configure(IContainer instance) {
                    // TODO Auto-generated method stub
                    
                }
            }
        );
    }
    
    public void cleanup() {}
    public void initialise() {}
    public void start() {}
    public void stop() {}
}