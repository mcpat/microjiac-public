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
package de.jiac.micro.internal.util;

import com.github.libxjava.lang.IClassLoader;

import de.jiac.micro.core.handle.IReflector;

/**
 * @author Erdene-Ochir Tuguldur
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public abstract class AbstractReflector implements IReflector {
    protected final static String GET_PREFIX= "get";
    protected final static String IS_PREFIX= "is";
    protected final static String SET_PREFIX= "set";
    
    public static AbstractReflector newInstance(IClassLoader classLoader) throws Exception {
        return (AbstractReflector) classLoader.loadClass("de.jiac.micro.internal.latebind.Reflector").newInstance();
    }

    protected final Object[] noArgs= new Object[0];
    protected final Object[] oneArgument= new Object[1];
    
    public final void writeProperty(Object obj, String property, Object argument) throws Exception {
        if(argument == null || !argument.getClass().isArray()) {
            synchronized (oneArgument) {
                oneArgument[0]= argument;
                
                try {
                    writeProperty0(obj, property, oneArgument);
                    return;
                } finally {
                    oneArgument[0]= null;
                }
            }
        }
        
        writeProperty0(obj, property, (Object[]) argument);
    }
    
    public final Object invokeMethod(Object obj, String name, Object arguments) throws Exception {
        if(arguments == null || !arguments.getClass().isArray()) {
            synchronized (oneArgument) {
                oneArgument[0]= arguments;
                
                try {
                    return invokeMethod0(obj, name, oneArgument);
                } finally {
                    oneArgument[0]= null;
                }
            }
        }
        
        return invokeMethod0(obj, name, (Object[]) arguments);
    }
    
    public abstract Object invokeMethodWithDescriptor(Object obj, String className, String mName, String mDescr, Object arguments) throws Exception;

    protected abstract void writeProperty0(Object obj, String name, Object[] arguments) throws Exception;
    protected abstract Object invokeMethod0(Object obj, String name, Object[] arguments) throws Exception;
}
