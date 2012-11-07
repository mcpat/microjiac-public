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
package de.jiac.micro.internal.core;

import com.github.libxjava.lang.IClassLoader;

import de.jiac.micro.core.IContainer;
import de.jiac.micro.core.IContainerConfiguration;


/**
 * @author Erdene-Ochir Tuguldur
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public abstract class AbstractContainerConfiguration implements IContainerConfiguration {
    public final String id;
    public final String displayName;
    public final Class specifiedClass;
    
    protected AbstractContainerConfiguration(String id, String displayName, Class specifiedClass) {
        this.id= id;
        this.displayName= displayName;
        this.specifiedClass= specifiedClass;
    }
    
    public final IContainer newInstance(IClassLoader classLoader) throws Exception {
        AbstractContainer container= (AbstractContainer) specifiedClass.newInstance();
        container.setClassLoader(classLoader);
        return container;
    }
    
    public abstract void configure(IContainer instance);
}
