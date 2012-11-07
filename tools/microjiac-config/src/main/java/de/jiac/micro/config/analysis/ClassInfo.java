/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Config.
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
package de.jiac.micro.config.analysis;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public final class ClassInfo {
    final static int LIFECYCLE_AWARE= 0;
    final static int ACTUATOR= 1;
    final static int SENSOR= 2;
    final static int HANDLE= 3;
    final static int CONNECTION_FACTORY= 4;
    final static int BEHAVIOUR= 5;
    final static int NODE_COMPONENT= 6;
    
    public static ClassInfo createInterfaceInfo(ClassInfoPool pool, int version, String className, Set<String> superInterfaceNames) {
        return new ClassInfo(pool, version, className, null, superInterfaceNames, true);
    }
    
    public static ClassInfo createClassInfo(ClassInfoPool pool, int version, String className, String superClassName, Set<String> superInterfaceNames) {
        return new ClassInfo(pool, version, className, superClassName, superInterfaceNames, false);
    }
    
    public final int version;
    public final boolean isInterface;
    public final String className;
    public final String superClassName;
    public final Map<MethodKey, Set<String>> referencedHandlesInMethods= new HashMap<MethodKey, Set<String>>();
    public final Set<String> referencedFieldClasses= new HashSet<String>();
    
    public final Map<MethodKey,Set<String>> referencedClassesInMethods= new HashMap<MethodKey, Set<String>>();
    
    final Set<String> superInterfaceNames;
    String directHandle;
    final Set<String> indirectHandles= new HashSet<String>();
    
    protected final HashSet<String> ancestors= new HashSet<String>();
//    protected final HashSet<String> derivatives= new HashSet<String>();
    
    private final BitSet _classStates;
    private boolean _mergedWithAncestors= false;
    private final ClassInfoPool _pool;
    
    private ClassInfo(ClassInfoPool pool, int version, String className, String superClassName, Set<String> superInterfaceNames, boolean isInterface) {
        this.version= version;
        this.className= className;
        this.superInterfaceNames= superInterfaceNames;
        this.superClassName= superClassName;
        this.isInterface= isInterface;
        _classStates= new BitSet(7);
        _pool= pool;
    }
    
    public final boolean isActuator() {
        return hasState(ACTUATOR);
    }
    
    public final boolean isConnectionFactory() {
        return hasState(CONNECTION_FACTORY);
    }
    
    public final boolean isHandle() {
        return hasState(HANDLE);
    }
    
    public final boolean isLifecycleAware() {
        return hasState(LIFECYCLE_AWARE);
    }
    
    public final boolean isSensor() {
        return hasState(SENSOR);
    }
    
    public boolean isNodeComponent() {
        return !isInterface && hasState(NODE_COMPONENT);
    }
    
    public boolean isBehaviour() {
        return !isInterface && hasState(BEHAVIOUR);
    }
    
    public String getProvidedHandle() {
        if(isActuator() || isNodeComponent()) {
            return directHandle;
        }
        
        return null;
    }
    
    @Override
    public String toString() {
        StringBuilder builder= new StringBuilder();
        builder.append(isInterface ? "interface " : "class ");
        builder.append(className).append(": ");
        
        if(isActuator()) {
            builder.append("A ");
        }
        
        if(isConnectionFactory()) {
            builder.append("C ");
        }
        
        if(isHandle()) {
            builder.append("H ");
        }
        
        if(isLifecycleAware()) {
            builder.append("L ");
        }
        
        if(isSensor()) {
            builder.append("S ");
        }
        
        if(isBehaviour()) {
            builder.append("B ");
        }
        
        if(isNodeComponent()) {
            builder.append("N ");
        }
        
        builder.append(referencedClassesInMethods);
        builder.append(referencedHandlesInMethods);
        
        return builder.toString();
    }
    
    public final boolean isAssignableFrom(ClassInfo other) {
        if(other == this) {
            return true;
        }
        
        other.ensureMerged();
        return other.ancestors.contains(className);
    }
    
    protected final boolean hasState(int index) {
        ensureMerged();
        
        if(index >= _classStates.length()) {
            return false;
        }
        
        return _classStates.get(index);
    }
    
    protected final void setState(int index) {
        _classStates.set(index);
    }
    
    protected void ensureMerged() {
        if(!_mergedWithAncestors) {
            mergeWithAncestors();
            _mergedWithAncestors= true;
        }
    }
    
    private final void mergeWithAncestors() {
        if(!isInterface) {
            ClassInfo superInfo= _pool.getClassInfo(superClassName);
            
            if(superInfo != null) {
                superInfo.ensureMerged();
                _classStates.or(superInfo._classStates);
                ancestors.add(superInfo.className);
                ancestors.addAll(superInfo.ancestors);
            }
        }
            
        for(Iterator<String> iter= superInterfaceNames.iterator(); iter.hasNext();) {
            ClassInfo superInterfaceInfo= _pool.getClassInfo(iter.next());
            if(superInterfaceInfo != null) {
                superInterfaceInfo.ensureMerged();
                _classStates.or(superInterfaceInfo._classStates);
                ancestors.add(superInterfaceInfo.className);
                ancestors.addAll(superInterfaceInfo.ancestors);
            }
        }
    }
}
