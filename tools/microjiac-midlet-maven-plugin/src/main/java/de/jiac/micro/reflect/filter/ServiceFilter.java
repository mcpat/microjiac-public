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
package de.jiac.micro.reflect.filter;

import java.lang.reflect.Modifier;
import java.util.HashSet;


/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class ServiceFilter implements IFilter {
    public static final String ISERVICE_CLASS_NAME= "de.jiac.micro.ext.service.IService";
    
    private Class<?> _iService= null;
    private boolean _loaded= false;
    private final HashSet<Class<?>> _implemented= new HashSet<Class<?>>();
    
    public int filter(Class<?> clazz) {
        init(ISERVICE_CLASS_NAME, clazz.getClassLoader());
        
        if(_iService != null && _iService.isAssignableFrom(clazz)) {
            if(!clazz.isInterface()) {
                // found service implementation
                if(!Modifier.isAbstract(clazz.getModifiers())) {
                    for(Class<?> c= clazz; c != null; c= c.getSuperclass()) {
                        for(Class<?> intf : c.getInterfaces()) {
                            if(_iService.isAssignableFrom(intf)) {
                                _implemented.add(intf);
                            }
                        }
                    }
                }
                
                return IGNORE;
            } else {
                if(_iService.equals(clazz)) {
                    return NONE;
                }
                
                return (_implemented.contains(clazz) ? METHODS_WITH_DESCRIPTORS : DELAY) | CONTEXT;
            }
        }
        
        return NONE;
    }
    
    private void init(String name, ClassLoader loader) {
        if(_loaded) {
            return;
        }
        
        try {
            _iService= Class.forName(name, false, loader);
        } catch (Exception e) {
            System.out.println("no service interface found");
        }
        
        _loaded= true;
    }

}
