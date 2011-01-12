/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Config.
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
package de.jiac.micro.config.analysis;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class ClassInfoPool {
    public static boolean ignoreClass(String className) {
        return className.startsWith("java.") || className.startsWith("javax.");
    }
    
    final class ClassInfoCreator extends EmptyVisitor implements Opcodes {
        protected String owner;
        protected ClassInfo classInfo;
        
        private final HashMap<String, Integer> _classStates;
        private final HashMap<String, Integer> _interfaceStates;
        
        protected ClassInfoCreator() {
            _classStates= new HashMap<String, Integer>();
            _classStates.put("de.jiac.micro.core.AbstractNodeComponent", Integer.valueOf(ClassInfo.NODE_COMPONENT));
            _classStates.put("de.jiac.micro.agent.AbstractActiveBehaviour", Integer.valueOf(ClassInfo.BEHAVIOUR));
            _classStates.put("de.jiac.micro.agent.AbstractReactiveBehaviour", Integer.valueOf(ClassInfo.BEHAVIOUR));
            
            _interfaceStates= new HashMap<String, Integer>();
            _interfaceStates.put("de.jiac.micro.core.IHandle", Integer.valueOf(ClassInfo.HANDLE));
            _interfaceStates.put("de.jiac.micro.agent.IActuator", Integer.valueOf(ClassInfo.ACTUATOR));
            _interfaceStates.put("de.jiac.micro.agent.ISensor", Integer.valueOf(ClassInfo.SENSOR));
            _interfaceStates.put("de.jiac.micro.core.feature.ILifecycleAware", Integer.valueOf(ClassInfo.LIFECYCLE_AWARE));
            _interfaceStates.put("de.jiac.micro.core.feature.IConnectionFactory", Integer.valueOf(ClassInfo.CONNECTION_FACTORY));
        }
        
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            owner= name;
            
            String className= name.replace('/', '.');
            HashSet<String> superInterfaceNames= new HashSet<String>();
            for(int i= 0; interfaces != null && i < interfaces.length; ++i) {
                superInterfaceNames.add(interfaces[i].replace('/', '.'));
            }
            
            requestClassInfos(superInterfaceNames);
            
            Integer state= null;
            if((access & ACC_INTERFACE) > 0) {
                classInfo= ClassInfo.createInterfaceInfo(ClassInfoPool.this, version, className, superInterfaceNames);
                state= _interfaceStates.get(className);
            } else {
                String superClassName= superName == null ? null : superName.replace('/', '.');
                classInfo= ClassInfo.createClassInfo(ClassInfoPool.this, version, className, superClassName, superInterfaceNames);
                state= _classStates.get(className);
                
                if(superClassName != null) {
                    requestClassInfo(superClassName);
                }
            }
            
            if(state != null) {
                classInfo.setState(state.intValue());
            }
            
        }

        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            Type type= Type.getType(desc);
            
            if(type.getSort() == Type.ARRAY) {
                type.getElementType();
            }
            
            if(type.getSort() == Type.OBJECT) {
                String className= type.getClassName();
                if(!ignoreClass(className)) {
                    requestClassInfo(className);
                    classInfo.referencedFieldClasses.add(className);
                }
            }
            
            // TODO Auto-generated method stub
            return null;
        }

        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if(classInfo.isInterface || (access & ACC_ABSTRACT) != 0 || (access & ACC_SYNTHETIC) != 0) {
                return null;
            }
            
            return new MethodAnalyser(this, access, name, desc, signature, exceptions);
        }
        
        protected ClassInfo getInfo() {
            ClassInfo result= classInfo;
            classInfo= null;
            return result;
        }
        
        protected void registerDependencyForMethod(MethodKey methodKey, String className) {
            if(className == null || ignoreClass(className)) {
                return;
            }
            
            requestClassInfo(className);
            
            Set<String> references= classInfo.referencedClassesInMethods.get(methodKey);
            references.add(className);
        }
    }
    
    private final ClassLoader _classLoader;
    private final ClassInfoCreator _classInfoCreator;
    private final HashSet<String> _requestedInfo;
    private final HashMap<String, ClassInfo> _pool;
    
    public ClassInfoPool(ClassLoader classLoader) {
        _classLoader= classLoader;
        _requestedInfo= new HashSet<String>();
        _pool= new HashMap<String, ClassInfo>();
        _classInfoCreator= new ClassInfoCreator();
    }
    
    public void requestClassInfo(String className) {
        if(ignoreClass(className)) {
            return;
        }
        
        _requestedInfo.add(className);
    }
    
    public void requestClassInfos(Collection<String> classNames) {
        for(String className : classNames) {
            requestClassInfo(className);
        }
    }
    
    public void buildPool() {
        while(!_requestedInfo.isEmpty()) {
            String next= _requestedInfo.iterator().next();
            _requestedInfo.remove(next);
            
            if(!_pool.containsKey(next)) {
                try {
                    InputStream in= _classLoader.getResourceAsStream(next.replace('.', '/') + ".class");
                    ClassReader reader= new ClassReader(in);
                    reader.accept(_classInfoCreator, 0);
                    _pool.put(next, _classInfoCreator.getInfo());
                } catch (Exception e) {
                    e.printStackTrace();
                    _pool.put(next, null);
                }
            }
            
            _requestedInfo.removeAll(_pool.keySet());
        }
    }
    
    public ClassInfo getClassInfo(String className) {
        return _pool.get(className);
    }
    
    public HashSet<ClassInfo> getDerivativeClassInfos(ClassInfo root) {
        HashSet<ClassInfo> result= new HashSet<ClassInfo>();
        for(ClassInfo ci : _pool.values()) {
            if(ci == root) {
                continue;
            }
            
            if(root.isAssignableFrom(ci)) {
                result.add(ci);
            }
        }
        
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder= new StringBuilder();
        
        for(Map.Entry<String, ClassInfo> entry : _pool.entrySet()) {
            if(entry.getValue() == null) {
                System.out.println("MISSING: " + entry.getKey());
            } else {
                builder.append(entry.getValue().toString()).append('\n');
            }
        }
        
        return builder.toString();
    }
}
