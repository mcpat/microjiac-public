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
/*
 * $Id$
 */
package de.jiac.micro.ext.service;

import de.jiac.micro.core.IHandle;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public interface IServiceHandle extends IHandle {
    /**
     * Deploys a new service instance and makes it available
     * for remote usage.
     * 
     * @param service   the service to be deployed
     */
    void deployService(IService service);
    
    /**
     * Undeploys the specified service if currently registered.
     * The service will not be available for remote users.
     * 
     * @param service   the service to be undeployed
     */
    void undeployService(IService service);
    
    /**
     * Creates new service context bound to the specified
     * service interface.
     * <p>
     * The result can be safely casted to the specified
     * service interface.
     * </p>
     * 
     * @param serviceInterface  {@code Class} object of the
     *                          service interface
     *                           
     * @return                  the context instance for the
     *                          specified service interface
     */
    IServiceContext createContext(Class serviceInterface);
}
