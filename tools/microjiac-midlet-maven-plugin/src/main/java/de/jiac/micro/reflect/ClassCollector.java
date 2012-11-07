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
import java.io.File;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.logging.Log;

import de.jiac.micro.reflect.ClassInfoReducer.ReducedClassInfo;
import de.jiac.micro.reflect.filter.IFilter;
import de.jiac.micro.reflect.filter.IgnoreFilter;
import de.jiac.micro.reflect.filter.ServiceFilter;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ClassCollector {
    public final static String CLASS_SUFFIX= ".class";
    
    private ClassLoader _loader;
    private List<IFilter> _filters;
    private List<Enumeration<Class<?>>> _classEnumerations;
    private ReducedClassInfo[] _toGenerate;
    
    public ReducedClassInfo[] getClassInfosForSourceGeneration() {
        return _toGenerate;
    }
    
    /**
     * Initialises the class collector. It is expected that only
     * directories or archive files (*.zip, *.jar) are provided!
     * 
     * @param files
     */
    public void initialise(File[] files, ClassLoader loader) throws MalformedURLException {
        _loader= loader;
        
        // TODO: remove this debugging stuff
        _filters= new LinkedList<IFilter>();
        _filters.add(new IgnoreFilter());
        _filters.add(new ServiceFilter());
//        _filters.add(new AgentElementFilter());
//        _filters.add(new NodeFilter());
//        _filters.add(new AgentFilter());
        
        // initialise enumerators
        _classEnumerations= new LinkedList<Enumeration<Class<?>>>();
        for(File current : files) {
            if(current.isDirectory()) {
                _classEnumerations.add(new DirectoryClassEnumerator(current, _loader));
            } else {
                _classEnumerations.add(new ArchiveClassEnumerator(current, _loader));
            }
        }
    }
    
    public void process(Log log) throws IntrospectionException {
        Set<Class<?>> delayed= new HashSet<Class<?>>();
        
        final ClassInfoReducer reducer= new ClassInfoReducer();
        
        for (Enumeration<Class<?>> classes : _classEnumerations) {
            filter(reducer, classes, delayed);
        }
        
        // now filter the delayed classes again
        filter(reducer, Collections.enumeration(delayed), null);
        
        reducer.reduceAll();
        _toGenerate= reducer.getSorted();
    }
    
    private void filter(ClassInfoReducer reducer, Enumeration<Class<?>> classes, Set<Class<?>> delayed) throws IntrospectionException {
        while (classes.hasMoreElements()) {
            Class<?> current = classes.nextElement();
            
            int mask = 0;
            for (IFilter filter : _filters) {
                mask |= filter.filter(current);
            }
            
            if(mask == IFilter.IGNORE) {
                reducer.ignoreClass(current);
                continue;
            }
            
            if((mask & IFilter.DELAY) != 0) {
                if(delayed != null) {
                    delayed.add(current);
                    continue;
                } else {
                    mask^= IFilter.DELAY;
                }
            }
            
            if(mask != IFilter.NONE) {
                reducer.insert(current, mask);
            }
        }
    }
}
