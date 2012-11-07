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

import java.util.Vector;

import de.jiac.micro.core.io.IAddress;

/**
 * Context interface to configure invocations of remote service
 * methods.
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public interface IServiceContext {
    /**
     * Sets the mode for the invocation of a service method.
     * <p>
     * Note: The {@link #lookupProviders(int) lookup} method
     * is also affected.
     * </p>
     * 
     * @param blocking
     * 
     * @see #isBlocking()
     */
    void setBlocking(boolean blocking);
    
    /**
     * Returns {@code true} if invocation mode is blocking
     * or {@code false} otherwise.
     * 
     * @return {@code true} if blocking, {@code false} otherwise
     */
    boolean isBlocking();
    
    /**
     * Returns {@code true} if a method invocation was issued and
     * the result is not available and the timeout has not been reached
     * or {@code false} otherwise.
     * <p>
     * Note:<br/>
     * If calling one of the service methods or {@link #lookupProviders(int)}
     * while this context is locked, the invocation results in an
     * {@link RuntimeException}!
     * </p>
     * 
     * @return  {@code true} if locked, {@code false} otherwise
     */
    boolean isLocked();
    
    /**
     * Initialises the timeout for the next call of a service method. During
     * an invocation this context is locked at most {@code timeout} ms. If
     * this context is also in blocking mode, then the calling thread is blocked
     * until the result is available or the timeout occurs.
     * <p>
     * Note: The timeout also affects calls to the {@link #lookupProviders(int) lookup}
     * method.
     * </p>
     * 
     * @param timeout  the call duration in milliseconds (ms)
     * 
     * @see #isLocked()
     */
    void setTimeout(int timeout);
    
    /**
     * Returns the current timeout value of this context.
     * 
     * @return  the call duration in milliseconds (ms)
     */
    int getTimeout();
    
    /**
     * 
     * @param max
     * 
     * @see #isLocked()
     */
    void lookupProviders(int max);
    
    /**
     * 
     * @param providerAddress
     * 
     * @see #isLocked()
     */
    void setPreferredProvider(IAddress providerAddress);
    
    /**
     * Returns the live collection of all know providers. You may
     * manipulate the order of providers or remove them from
     * the collection.
     * 
     * @return live collection of providers
     * 
     * @see #isLocked()
     */
    Vector getProviders();
}
