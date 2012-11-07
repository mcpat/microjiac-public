/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Reflective-Service-Engine.
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
package de.jiac.micro.ext.service.impl;

import java.lang.reflect.Proxy;

import org.slf4j.Logger;

import com.github.libxjava.lang.IClassLoader;

import de.jiac.micro.core.IContainer;
import de.jiac.micro.core.scope.Scope;
import de.jiac.micro.ext.service.IService;
import de.jiac.micro.ext.service.IServiceContext;
import de.jiac.micro.ext.service.IServiceHandle;

/**
 * @author Marcel Patzlaff
 */
/*package*/ final class ServiceHandleImpl implements IServiceHandle {
    private final Class[] _twoClasses= new Class[2];
    
    private final ServiceEngine _engine;
    
    /*package*/ ServiceHandleImpl(ServiceEngine engine) {
        _engine= engine;
        _twoClasses[0]= IServiceContext.class;
    }
    
    public void deployService(IService service) {
        _engine.deployService(service);
    }

    public void undeployService(IService service) {
        _engine.undeployService(service);
    }

    public IServiceContext createContext(Class serviceInterface) {
        if(serviceInterface == null) {
            throw new IllegalArgumentException("serviceInterface must not be null");
        }
        
        if(!serviceInterface.isInterface() || !IService.class.isAssignableFrom(serviceInterface)) {
            throw new IllegalArgumentException("serviceInterface is not a valid interface");
        }
        
        synchronized (_twoClasses) {
            try {
                _twoClasses[1]= serviceInterface;
                ProxyServiceContext proxy= new ProxyServiceContext(_engine, serviceInterface);
                final IContainer container= Scope.getScope().getContainerReference();
                IClassLoader classLoader= container.getClassLoader();
                if(classLoader instanceof ClassLoader) {
                    return (IServiceContext) Proxy.newProxyInstance((ClassLoader) classLoader, _twoClasses, proxy);
                } else {
                    Logger logger= container.getLogger("ServiceHandle");
                    logger.warn("No explicit class loader found. Guessing one...");
                    return (IServiceContext) Proxy.newProxyInstance(serviceInterface.getClassLoader(), _twoClasses, proxy);
                }
            } finally {
                _twoClasses[1]= null;
            }
        }
    }
}
