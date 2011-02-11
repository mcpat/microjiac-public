/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Emulated-Service-Engine.
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

import org.slf4j.Logger;

import de.jiac.micro.core.scope.Scope;
import de.jiac.micro.ext.service.IService;


/**
 * Abstract base class for all generated (emulated) executors of
 * service interfaces.
 * 
 * @author Marcel Patzlaff
 */
public abstract class EmulatedProxyServiceContext extends AbstractServiceContext implements IService {
    protected EmulatedProxyServiceContext(Class serviceClass) {
        super(serviceClass);
    }

    protected final Object searchAndInvoke(String mName, String mDescr, Object[] arguments) {
        try {
            synchronized (this) {
                ensureReleased();
                
                if(hasAsyncResult()) {
                    return fetchAsyncResult(mName, mDescr);
                }
                
                return engine.searchAndInvoke(this, mName, mDescr, arguments);
            }
        } catch (Exception e) {
            Logger logger= Scope.getScope().getContainerReference().getLogger("ServiceContext");
            logger.error("EPSC: error while invoking service '" + mName + "'", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public final String doGetDescription() {
        return (String) searchAndInvoke("doGetDescription", "()Ljava/lang/String;", null);
    }
}
