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
/*
 * $Id$ 
 */
package de.jiac.micro.core;

/**
 * This interface should be implemented by node components and agent
 * elements to notify their container of lifecycle awareness. If
 * implemented, the initialisation and activation phases are ensured
 * via the core lifecycle handler.
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public interface ILifecycleAware {
    /**
     * This method is invoked after the current instance is fully
     * configured. It is safe to acquire system resources or handles
     * here. Nevertheless container handles should not be acquired here
     * because the associated handle provider is most likely not
     * intialised yet.
     * <p>
     * If the current instance is also a handle provider, the handle
     * must be available after this method completes!
     * </p>
     */
    void initialise();
    
    /**
     * Start routines like thread or schedulable activation should be
     * done in this method. It is safe to acquire container handles
     * in this method because each component of the container is
     * fully initialised!
     */
    void start();
    
    /**
     * Implementations should ensure that all threads and schedulables
     * of the current instance are stopped when this method returns.
     */
    void stop();
    
    /**
     * This method is invoked once immediately before the current instance
     * is disposed. Implementers have to ensure that every resource
     * that was obtained during the call of {@link #initialise()} is released!
     */
    void cleanup();
}
