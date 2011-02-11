/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Reflective-Service-Engine.
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

package de.jiac.micro.ext.service.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import de.jiac.micro.ext.service.IServiceContext;
import de.jiac.micro.internal.util.SignatureUtil;

/**
 * @author Marcel Patzlaff
 */
/*package*/ final class ProxyServiceContext extends AbstractServiceContext implements InvocationHandler {
    /*package*/ ProxyServiceContext(ServiceEngine engine, Class serviceClass) {
        super(serviceClass);
        setServiceEngine(engine);
    }
    
    public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
        if(method.getDeclaringClass() == IServiceContext.class) {
            // context manipulation
            return method.invoke(this, arguments);
        } else if(method.getName().startsWith("do")) {
            synchronized (this) {
                ensureReleased();
                
                String mDescriptor= SignatureUtil.getMethodDescriptor(
                        method.getParameterTypes(),
                        method.getReturnType()
                );
                
                if(hasAsyncResult()) {
                    return fetchAsyncResult(method.getName(), mDescriptor);
                }
                
                return engine.searchAndInvoke(this, method.getName(), mDescriptor, arguments);
            }
        }

        throw new RuntimeException("unsupported operation: " + method.getName());
    }
}
