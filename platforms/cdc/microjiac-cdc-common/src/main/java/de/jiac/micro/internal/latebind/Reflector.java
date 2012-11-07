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
package de.jiac.micro.internal.latebind;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.github.libxjava.lang.IClassLoader;

import de.jiac.micro.core.scope.Scope;
import de.jiac.micro.internal.util.AbstractReflector;
import de.jiac.micro.internal.util.SignatureUtil;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public final class Reflector extends AbstractReflector {
    public Object invokeMethodWithDescriptor(Object obj, String className, String mName, String mDescr, Object arguments) throws Exception {
        IClassLoader classLoader= Scope.getContainer().getClassLoader();
        Class declClass= classLoader.loadClass(className);
        
        if(!declClass.isInstance(obj)) {
            throw new IllegalArgumentException("method cannot be applied on the specified object: type mismatch");
        }
        
        Class[] signature= SignatureUtil.getSignatureClasses(classLoader, mDescr);
        
        if(signature.length == 2) {
            synchronized (oneArgument) {
                oneArgument[0]= arguments;
                
                try {
                    return invokeMethod0(obj, declClass, mName, signature, oneArgument);
                } finally {
                    oneArgument[0]= null;
                }
            }
        }
        
        return invokeMethod0(obj, declClass, mName, signature, (Object[]) arguments);
    }
    
    protected Object invokeMethod0(Object obj, String name, Object[] arguments) throws Exception {
        Class clazz= obj.getClass();
        Method method= guessMethod(clazz, name, arguments);
        return method.invoke(obj, arguments);
    }
    
    private Object invokeMethod0(Object obj, Class clazz, String name, Class[] signature, Object[] arguments) throws Exception {
        Method method= null;
        
        if(signature == null) {
            method= guessMethod(clazz, name, arguments);
        } else {
            Class[] paramTypes= new Class[signature.length - 1];
            System.arraycopy(signature, 1, paramTypes, 0, paramTypes.length);
            method= clazz.getDeclaredMethod(name, paramTypes);
            
            if(method.getReturnType() != signature[0]) {
                throw new NoSuchMethodException("return type mismatch: expected" + String.valueOf(signature[0]) + " but found " + String.valueOf(method.getReturnType()));
            }
        }
        
        return method.invoke(obj, arguments);
    }

    protected void writeProperty0(Object obj, String property, Object[] arguments) throws Exception {
        accessProperty(obj, property, true, arguments);
    }

    private Object accessProperty(Object obj, String property, boolean write, Object[] arguments) throws Exception {
        // check for correct index types
        if((write && arguments.length == 2) || (!write && arguments.length == 1)) {
            if(arguments[0] == null || (arguments[0].getClass() != String.class && arguments[0].getClass() != Integer.class)) {
                throw new IllegalArgumentException("bad property index '" + arguments[0] + "'");
            }
        }
        
        Class clazz= obj.getClass();
        Method[] methods= clazz.getMethods();
        
        // capitalise property name
        char[] propChars= property.toCharArray();
        propChars[0]= Character.toUpperCase(propChars[0]);
        String capProp= new String(propChars);
        
        for(int i= 0; i < methods.length; ++i) {
            // 1. check modifiers
            int mods= methods[i].getModifiers();
            if(Modifier.isStatic(mods) || !Modifier.isPublic(mods)) {
                continue;
            }
            
            // 2. check for return type and parameter count
            if(!write && methods[i].getReturnType() == void.class) {
                continue;
            }
            
            if(methods[i].getParameterTypes().length != arguments.length) {
                continue;
            }
            
            // 3. check for name matching
            String methodName= methods[i].getName();
            int start;
            if(write) {
                if(methodName.startsWith(SET_PREFIX)) {
                    start= SET_PREFIX.length();
                } else {
                    continue;
                }
            } else {
                if(methodName.startsWith(IS_PREFIX)) {
                    start= IS_PREFIX.length();
                } else if (methodName.startsWith(GET_PREFIX)) {
                    start= GET_PREFIX.length();
                } else {
                    continue;
                }
            }
            
            if(methodName.substring(start).equals(capProp)) {
                if(write) {
                    // convert value if necessary
                    int valueIndex= arguments.length - 1;
                    if(arguments[valueIndex] != null && arguments[valueIndex].getClass() == String.class) {
                        arguments[valueIndex]= convert((String) arguments[valueIndex], methods[i].getParameterTypes()[valueIndex]);
                    }
                }
                return methods[i].invoke(obj, arguments);
            }
        }
        
        throw new NoSuchMethodException("no property '" + property + "' found for type " + clazz.getName());
    }
    
    private Method guessMethod(Class cls, String methodName, Object[] arguments) throws NoSuchMethodException {
        Method[] methods= cls.getMethods();
        
        int bestRank= Integer.MAX_VALUE;
        Method bestMethod= null;
        
        for(int i= 0; i < methods.length; ++i) {
            // 1. check modifiers -> method must be a public member
            int mods= methods[i].getModifiers();
            if(Modifier.isStatic(mods) || !Modifier.isPublic(mods)) {
                continue;
            }
            // 2. check name and length of argument list
            if(methods[i].getName().equals(methodName) && methods[i].getParameterTypes().length == arguments.length) {
                // 3. calculate distance between parameter types and the types of the provided arguments
                int currentRank= calculateRank(methods[i].getParameterTypes(), arguments);
                
                if(currentRank < bestRank) {
                    bestRank= currentRank;
                    bestMethod= methods[i];
                }
            }
        }
        
        if(bestMethod == null) {
            StringBuffer error= new StringBuffer();
            error.append(cls.getName()).append('.').append(methodName).append('(');
            for(int k= 0; k < arguments.length; ++k) {
                error.append(arguments[k] == null ? "java.lang.Object" : arguments[k].getClass().getName());
                
                if(k < arguments.length - 1) {
                    error.append(',');
                }
            }
            error.append(')');
            throw new NoSuchMethodException(error.toString());
        }
        
        return bestMethod;
    }
    
    private int calculateRank(Class[] types, Object[] arguments) {
        int sum= 0;
        for(int i= 0; i < types.length; ++i) {
            // if arguments[i] == null and types[i] is not primitive then the best rank (= 0) is assigned
            if(arguments[i] == null) {
                // if types[i] is primitive then there is no match
                if(types[i].isPrimitive()) {
                    return Integer.MAX_VALUE;
                }
                continue;
            }
            
            Class argClass= arguments[i].getClass();
            int dist= getDistance(types[i], argClass);
            // if current types did not match then return
            if(dist >= Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            sum+= dist;
        }
        
        return sum;
    }
    
    private Object convert(String str, Class expected) throws Exception {
        if(expected == String.class || expected == Object.class) {
            return str;
        }
        
        expected= wrap(expected);
        Method valueOf= expected.getDeclaredMethod("valueOf", new Class[]{String.class});
        
        if(valueOf != null && Modifier.isStatic(valueOf.getModifiers())) {
            return valueOf.invoke(expected, new Object[]{str});
        }
        
        return null;
    }
    
    private int getDistance(Class superType, Class descendent) {
        // remove dimensions without increasing distance
        while(superType.isArray() && descendent.isArray()) {
            superType= superType.getComponentType();
            descendent= descendent.getComponentType();
        }
        
        // check for array type assignability
        if(superType.isArray()) {
            // descendent is no array so both types are incompatible
            return Integer.MAX_VALUE;
        } else if(descendent.isArray()) {
            // you can only assign an array to an object
            if(superType != Object.class) {
                return Integer.MAX_VALUE;
            }
            
            int count= 1;
            while(descendent.isArray()) {
                descendent= descendent.getComponentType();
                count++;
            }
            return descendent.isPrimitive() ? count - 1 : count;
        } else {
            // both types are no array
            
            // wrap superType if it is primitive
            superType= wrap(superType);
            descendent= wrap(descendent);

            if(!superType.isAssignableFrom(descendent)) {
                return Integer.MAX_VALUE;
            }
            
            Set currentLevel= new HashSet();
            currentLevel.add(descendent);
            int distance= 0;
            
            // ascend hierarchy
            for(; currentLevel.size() > 0; ++distance) {
                Set nextLevel= new HashSet();
                for(Iterator i= currentLevel.iterator(); i.hasNext(); ) {
                    Class current= (Class) i.next();

                    if(current == superType) {
                        return distance;
                    }
                    
                    Class currentSuper= current.getSuperclass();
                    if(currentSuper != null && superType.isAssignableFrom(currentSuper)) {
                        nextLevel.add(currentSuper);
                    }
                    
                    Class[] superInfc= current.getInterfaces();
                    for(int k= 0; k < superInfc.length; ++k) {
                        if(superInfc[k] != null && superType.isAssignableFrom(superInfc[k])) {
                            nextLevel.add(superInfc[k]);
                        }
                    }
                }
                
                currentLevel= nextLevel;
            }
            
            return distance;
        }
    }
    
    private Class wrap(Class clazz) {
        if(!clazz.isPrimitive()) {
            return clazz;
        }
        
        if(clazz == boolean.class) {
            return Boolean.class;
        } else if(clazz == byte.class) {
            return Byte.class;
        } else if(clazz == char.class) {
            return Character.class;
        } else if(clazz == double.class) {
            return Double.class;
        } else if(clazz == float.class) {
            return Float.class;
        } else if(clazz == int.class) {
            return Integer.class;
        } else if(clazz == long.class) {
            return Long.class;
        } else if(clazz == short.class) {
            return Short.class;
        }
        
        throw new IllegalArgumentException("unknown primitive type '" + clazz.toString() + "'");
    }
}
