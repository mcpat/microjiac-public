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
package de.jiac.micro.internal.util;

import java.util.ArrayList;

import com.github.libxjava.lang.IClassLoader;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public final class SignatureUtil {
    private final static StringBuffer SIGNATURE_BUFFER= new StringBuffer();
    private final static ArrayList SIGNATURE_CLASS_NAMES= new ArrayList();
    
    public final static Class[] getSignatureClasses(IClassLoader classLoader, String methodDescriptor) throws ClassNotFoundException {
        synchronized (SIGNATURE_BUFFER) {
            try {
                SIGNATURE_BUFFER.append(methodDescriptor);
                SIGNATURE_BUFFER.delete(0, methodDescriptor.indexOf('(') + 1);
                while(SIGNATURE_BUFFER.charAt(0) != ')') {
                    SIGNATURE_CLASS_NAMES.add(getClassForInternalName(classLoader, SIGNATURE_BUFFER));
                }
                
                SIGNATURE_BUFFER.deleteCharAt(0);
                SIGNATURE_CLASS_NAMES.add(0, getClassForInternalName(classLoader, SIGNATURE_BUFFER));
                
                Class[] result= new Class[SIGNATURE_CLASS_NAMES.size()];
                SIGNATURE_CLASS_NAMES.toArray(result);
                return result;
            } finally {
                SIGNATURE_BUFFER.setLength(0);
                SIGNATURE_CLASS_NAMES.clear();
            }
        }
    }
    
    public final static String getMethodDescriptor(Class[] parameterTypes, Class returnType) {
        synchronized (SIGNATURE_BUFFER) {
            try {
                SIGNATURE_BUFFER.append('(');
                for(int i= 0; i < parameterTypes.length; ++i) {
                    getDescriptor(SIGNATURE_BUFFER, parameterTypes[i]);
                }
                
                SIGNATURE_BUFFER.append(')');
                getDescriptor(SIGNATURE_BUFFER, returnType);
                return SIGNATURE_BUFFER.toString();
            } finally {
                SIGNATURE_BUFFER.setLength(0);
            }
        }
    }
    
//    public final static String getDeclaringClassName(String descriptor) {
//        return descriptor.substring(0, descriptor.indexOf('#'));
//    }
//    
//    public final static String getMethodName(String descriptor) {
//        return descriptor.substring(descriptor.indexOf('#') + 1, descriptor.indexOf('('));
//    }
//
    private static void getDescriptor(final StringBuffer buf, final Class c) {
        Class d = c;
        while (true) {
            if (!Object.class.isAssignableFrom(c)) {
                char car;
                if (d == int.class) {
                    car = 'I';
                } else if (d == void.class) {
                    car = 'V';
                } else if (d == boolean.class) {
                    car = 'Z';
                } else if (d == byte.class) {
                    car = 'B';
                } else if (d == char.class) {
                    car = 'C';
                } else if (d == short.class) {
                    car = 'S';
                } else if (d == double.class) {
                    car = 'D';
                } else if (d == float.class) {
                    car = 'F';
                } else /* if (d == long.class) */{
                    car = 'J';
                }
                buf.append(car);
                return;
            } else {
                if (!d.isArray()) {
                    buf.append('L');
                }
                
                String name = d.getName();
                int len = name.length();
                for (int i = 0; i < len; ++i) {
                    char car = name.charAt(i);
                    buf.append(car == '.' ? '/' : car);
                }
                
                if(!d.isArray()) {
                    buf.append(';');
                }
                return;
            }
        }
    }
    
    private static final Class getClassForInternalName(IClassLoader classLoader, StringBuffer buf) throws ClassNotFoundException {
        StringBuffer className= null;
        char ch= buf.charAt(0);
        buf.deleteCharAt(0);
        int index= 0;
        boolean keepSeparator= false;
        switch(ch) {
            case 'V': return void.class;
            case 'Z': return boolean.class;
            case 'C': return char.class;
            case 'B': return byte.class;
            case 'S': return short.class;
            case 'I': return int.class;
            case 'F': return float.class;
            case 'J': return long.class;
            case 'D': return double.class;
            
            case '[': {
                className= new StringBuffer();
                className.append(ch);
                for(; (ch= buf.charAt(index)) == '['; ++index) {
                    className.append(ch);
                }
                
                className.append(ch);
                
                if(ch != 'L') {
                    buf.delete(0, index + 1);
                    return classLoader.loadClass(className.toString());
                }
                
                // fall through to L case
                keepSeparator= true;
                index++;
            }
            
            default: {
                if(className == null) {
                    className= new StringBuffer();
                }
                
                for(; (ch= buf.charAt(index)) != ';' ; ++index) {
                    className.append(ch == '/' ? '.' : ch);
                }
                
                if(keepSeparator) {
                    className.append(';');
                }
                
                buf.delete(0, index + 1);
                return classLoader.loadClass(className.toString());
            }
        }
    }
    
    private SignatureUtil() {}
}
