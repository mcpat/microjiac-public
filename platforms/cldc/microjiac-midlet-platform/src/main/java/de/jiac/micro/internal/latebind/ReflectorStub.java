/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC MIDlet-Platform.
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
package de.jiac.micro.internal.latebind;

import com.github.libxjava.lang.IClassLoader;

import de.jiac.micro.core.scope.Scope;
import de.jiac.micro.internal.util.AbstractReflector;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
abstract class ReflectorStub extends AbstractReflector {
    private static int countArguments(String descr) {
        int count= 0;
        loop: for(int i= 0; count < 2 && i < descr.length(); ++i) {
            char ch= descr.charAt(i);
            
            switch(ch) {
                case 'Z': case 'C': case 'B': case 'S': case 'I': case 'F': case 'J': case 'D': {
                    count++;
                    break;
                }
                
                case 'L': {
                    count++;
                    i= descr.indexOf(';', i);
                    break;
                }
                
                case ')': {
                    break loop;
                }
            }
        }
        
        return count;
    }
    
    public final Object invokeMethodWithDescriptor(Object obj, String className, String mName, String mDescr, Object arguments) throws Exception {
        final IClassLoader classLoader= Scope.getContainer().getClassLoader();
        final Class cls= classLoader.loadClass(className);
        
        if(countArguments(mDescr) == 1) {
            synchronized (oneArgument) {
                oneArgument[0]= arguments;
                
                try {
                    return invokeMethodWithDescriptor0(obj, cls, mName, mDescr, oneArgument);
                } finally {
                    oneArgument[0]= null;
                }
            }
        }
        
        return invokeMethodWithDescriptor0(obj, cls, mName, mDescr, (Object[]) arguments);
    }

    protected abstract Object invokeMethod0(Object obj, String name, Object[] arguments) throws Exception;
    protected abstract void writeProperty0(Object obj, String name, Object[] arguments) throws Exception;
    protected abstract Object invokeMethodWithDescriptor0(Object obj, Class cls, String name, String descr, Object[] arguments) throws Exception;
    
}
