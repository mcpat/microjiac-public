/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC CDC-Common.
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

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;

import com.github.libxjava.lang.IClassLoader;

import de.jiac.micro.cl.ClassPath;
import de.jiac.micro.cl.ContainerClassLoader;

/**
 * @author Erdene-Ochir Tuguldur
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class CDCNode extends AbstractNode {
	protected IClassLoader getClassLoaderForAgent(String[] classPath) {
	    final ClassPath cp= new ClassPath();
	    if(classPath != null) {
	        for(int i= 0; i < classPath.length; ++i) {
	            File file= new File(classPath[i]);
	            
	            if(file.exists() && file.isFile()) {
	                cp.addFile(file);
	            } else {
	                getLogger().warn("invalid class path entry: " + file);
	            }
	        }
	    }
	    
	    final ContainerClassLoader myLoader= (ContainerClassLoader) getClassLoader();
	    
	    ContainerClassLoader acl= (ContainerClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return new ContainerClassLoader(cp, myLoader);
            }
	    });
	    return acl;
	}
}
