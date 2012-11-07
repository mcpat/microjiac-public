/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC MIDlet-Maven-Plugin.
 *
 * Copyright (c) 2007-2012 DAI-Labor, Technische Universität Berlin
 *
 * This library includes software developed at DAI-Labor, Technische
 * Universität Berlin (http://www.dai-labor.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
/*
 * $Id$ 
 */
package de.jiac.micro.reflect;

import java.beans.IntrospectionException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import de.dailab.jiac.common.aamm.beans.ClassDescriptor;
import de.dailab.jiac.common.aamm.beans.ClassInfo;
import de.dailab.jiac.common.aamm.beans.Introspector;
import de.dailab.jiac.common.aamm.beans.MethodDescriptor;
import de.dailab.jiac.common.aamm.beans.PropertyDescriptor;
import de.jiac.micro.reflect.filter.IFilter;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ClassInfoReducer {
    public final static class ReducedClassInfo extends ClassInfo {
        protected final ClassInfo originalInfo;
        protected int mask;
        
        private final HashSet<MethodDescriptor> _reducedMethods;
        private MethodDescriptor[] _rmds;
        private final HashMap<String, PropertyDescriptor> _reducedProperties;
        private PropertyDescriptor[] _rpds;
        
        protected ReducedClassInfo(ClassInfo originalInfo, int mask) {
            this.originalInfo= originalInfo;
            this.mask= mask;
            _rpds= null;
            _rmds= null;
            
            _reducedProperties= new HashMap<String, PropertyDescriptor>();
            PropertyDescriptor[] pds= originalInfo.getPropertyDescriptors();
            for(int i= 0; i < pds.length; ++i) {
                _reducedProperties.put(pds[i].getName(), pds[i]);
            }
            
            _reducedMethods= new HashSet<MethodDescriptor>();
            _reducedMethods.addAll(Arrays.asList(originalInfo.getMethodDescriptors()));
        }
        
        public ClassDescriptor getClassDescriptor() {
            return originalInfo.getClassDescriptor();
        }

        public MethodDescriptor[] getMethodDescriptors() {
            if(_rmds == null) {
                _rmds= _reducedMethods.toArray(new MethodDescriptor[_reducedMethods.size()]);
            }
            
            return _rmds;
        }

        public PropertyDescriptor[] getPropertyDescriptors() {
            if(_rpds == null) {
                _rpds= _reducedProperties.values().toArray(new PropertyDescriptor[_reducedProperties.size()]);
            }
            
            return _rpds;
        }
        
        public boolean hasWritablePropertiesForMask() {
            if(mask == IFilter.IGNORE || (mask & IFilter.PROPERTIES) <= 0) {
                return false;
            }
            
            PropertyDescriptor[] pds= getPropertyDescriptors();
            for(int i= 0; i < pds.length; ++i) {
                if(pds[i].isWritable()) {
                    return true;
                }
            }
            
            return false;
        }
        
        public boolean needsMethodsWithDescriptors() {
            return mask != IFilter.IGNORE && (mask & IFilter.METHODS_WITH_DESCRIPTORS) != 0;
        }
        
        /*package*/ void reduceWith(ReducedClassInfo sprci) {
            // reduce properties
            PropertyDescriptor[] pds= sprci.originalInfo.getPropertyDescriptors();
            for(int i= 0; i < pds.length; ++i) {
                PropertyDescriptor pd= pds[i];
                
                PropertyDescriptor rpd= _reducedProperties.remove(pd.getName());
                if(rpd != null) {
                    _rpds= null;
                    rpd= PropertyDescriptor.reduce(rpd, pd);
                    
                    if(rpd != null) {
                        _reducedProperties.put(pd.getName(), rpd);
                    }
                }
            }
            
            // reduce methods
            MethodDescriptor[] mds= sprci.originalInfo.getMethodDescriptors();
            for(int i= 0; i < mds.length; ++i) {
                if(_reducedMethods.remove(mds[i])) {
                    _rmds= null;
                }
            }
        }
    }
    
    private final HashMap<Class<?>, ReducedClassInfo> _classes= new HashMap<Class<?>, ReducedClassInfo>();
    
    private final HashMap<Class<?>, Integer> _masks= new HashMap<Class<?>, Integer>();
    private final HashSet<Class<?>> _ignored= new HashSet<Class<?>>();
    
    public void ignoreClass(Class<?> cls) {
        _ignored.add(cls);
    }
    
    public void insert(Class<?> cls, int mask) {
        _masks.put(cls, Integer.valueOf(mask));
    }
    
    public void reduceAll() throws IntrospectionException {
        for(Map.Entry<Class<?>, Integer> entry : _masks.entrySet()) {
            internalReduce(Introspector.getBeanInfo(entry.getKey()), entry.getValue().intValue());
        }
    }
    
    public ReducedClassInfo[] getSorted() {
        ReducedClassInfo[] infos= _classes.values().toArray(new ReducedClassInfo[_classes.size()]);
        Arrays.sort(infos);
        return infos;
    }
    
    private ReducedClassInfo internalReduce(ClassInfo classInfo, int mask) {
        Class<?> cls= classInfo.getClassDescriptor().getClazz();
        ReducedClassInfo rci= _classes.get(cls);
        
        if(rci == null) {
            rci= new ReducedClassInfo(classInfo, mask);
            _classes.put(cls, rci);
        } else {
            rci.mask |= mask;
        }
        
        LinkedList<Class<?>> jumpOver= new LinkedList<Class<?>>();
        int jumpIndex= 0;
        // walk up hierarchy
        try {
            do {
                Class<?> spcls= cls.getSuperclass();
                if(spcls != null) {
                    if(_ignored.contains(spcls)) {
                        if(!jumpOver.contains(spcls)) {
                            jumpOver.add(spcls);
                        }
                    } else {
                        ReducedClassInfo sprci= internalReduce(Introspector.getBeanInfo(spcls), rci.mask);
                        rci.reduceWith(sprci);
                    }
                }
                
                Class<?>[] intfs= cls.getInterfaces();
                if(intfs != null) {
                    for(int i= 0; i < intfs.length; ++i) {
                        if(_ignored.contains(intfs[i])) {
                            if(!jumpOver.contains(intfs[i])) {
                                jumpOver.add(intfs[i]);
                            }
                        } else {
                            ReducedClassInfo intfrci= internalReduce(Introspector.getBeanInfo(intfs[i]), rci.mask);
                            rci.reduceWith(intfrci);
                        }
                    }
                }
                
                cls= jumpIndex < jumpOver.size() ? jumpOver.get(jumpIndex++) : null;
            } while(cls != null);
        } catch (IntrospectionException ie) {
            // should not happen here because we only do simple cache lookups
            throw new AssertionError(ie);
        }
        
        return rci;
    }
}
