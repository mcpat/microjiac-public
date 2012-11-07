/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Emulated-Service-Engine.
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

import com.github.libxjava.lang.IClassLoader;

import de.jiac.micro.core.scope.Scope;
import de.jiac.micro.ext.service.IService;
import de.jiac.micro.ext.service.IServiceContext;
import de.jiac.micro.ext.service.IServiceHandle;

/**
 * @author Marcel Patzlaff
 */
/*package*/ final class ServiceHandleImpl implements IServiceHandle {
    private static final String EMULATION_CLASS_PREFIX= "ContextFor_";
    
    private final ServiceEngine _engine;
    
    /*package*/ ServiceHandleImpl(ServiceEngine engine) {
        _engine= engine;
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
        
        String intfName= serviceInterface.getName().replace('$', '_');
        StringBuffer className= new StringBuffer(intfName);

        // keep package name and insert prefix
        int lastPoint= intfName.lastIndexOf('.');
        className.insert(lastPoint < 0 ? 0 : lastPoint + 1, EMULATION_CLASS_PREFIX);
        
        try {
            IClassLoader classLoader= Scope.getContainer().getClassLoader();
            Class contextClass= classLoader.loadClass(className.toString());
            EmulatedProxyServiceContext context= (EmulatedProxyServiceContext) contextClass.newInstance();
            context.setServiceEngine(_engine);
            return context;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("context for the given interface could not be found: " + e.toString());
        }
    }
}
