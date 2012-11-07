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
package de.jiac.micro.internal.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.libxjava.lang.IClassLoader;

import de.jiac.micro.core.IContainer;
import de.jiac.micro.core.IHandle;
import de.jiac.micro.core.ILifecycleAware;
import de.jiac.micro.util.List;
import de.jiac.micro.util.List.Node;

/**
 * @author Erdene-Ochir Tuguldur
 * @author Marcel Patzlaff
 */
public abstract class AbstractContainer implements IContainer, ILifecycleAware {
    protected final List handles;
    private String _displayName;
    private IClassLoader _classLoader;
    
    protected AbstractContainer() {
        handles= new List();
    }

    public final IHandle getHandle(Class handleClass) {
        for (Node n= handles.head(), end= handles.tail(); (n= n.next()) != end;) {
            IHandle handle= (IHandle) n.value();
            if(handleClass.isInstance(handle)) {
                return handle;
            }
        }
        return null;
    }

    public final void addHandle(IHandle handle) {
        handles.addFirst(handle);
    }
    
    public final void removeHandle(IHandle handle) {
        handles.remove(handle);
    }
    
    public final Logger getLogger() {
        return getLogger(null);
    }
    
    public final Logger getLogger(String name) {
        String displayName= getDisplayName();
        return LoggerFactory.getLogger(name == null ? displayName : displayName + "." + name);
    }
    
    
    public final IClassLoader getClassLoader() {
        return _classLoader;
    }

    public abstract void cleanup();
    public abstract void initialise();
    public abstract void start();
    public abstract void stop();
    
    protected final String getDisplayName() {
        synchronized(this) {
            if(_displayName == null) {
                AbstractContainerConfiguration conf= (AbstractContainerConfiguration) getHandle(AbstractContainerConfiguration.class);
                _displayName= conf.displayName;
            }
        }
        
        return _displayName;
    }
    
    /*package*/ void setClassLoader(IClassLoader classLoader) {
        if(_classLoader != null) {
            throw new IllegalArgumentException("classloader already initialised");
        }
        
        _classLoader= classLoader;
    }
}
