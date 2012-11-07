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

import org.slf4j.Logger;

import de.jiac.micro.util.List.Node;

/**
 * @author Erdene-Ochir Tuguldur
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public final class LifecycleHandler {
    public static interface LifecycleState {
        byte CONSTRUCTED = 1; // initial state
        byte INITIALISED = 2;
        byte ACTIVE = 3;
        byte DESTROYED = 0; // final state
    }
    
    public static interface ILifecycleContext {
        ILifecycleAware lifecycleAware();
        byte lifecycleState();
    }
    
    public static class SimpleLifecycleContext implements ILifecycleContext {
        /*package*/ byte lifecycleState= LifecycleState.CONSTRUCTED;
        private final Object _la;
        
        public SimpleLifecycleContext(Object la) {
            _la= la;
        }
        
        public ILifecycleAware lifecycleAware() {
            return _la instanceof ILifecycleAware ? (ILifecycleAware) _la : null;
        }
        public byte lifecycleState() {
            return lifecycleState;
        }
    }
    
    public static class ListableLifecycleContext extends Node implements ILifecycleContext {
        /*package*/ byte lifecycleState= LifecycleState.CONSTRUCTED;
        
        public final ILifecycleAware lifecycleAware() {
            final Object value= value();
            return value instanceof ILifecycleAware ? (ILifecycleAware) value : null;
        }
        
        public final byte lifecycleState() {
            return lifecycleState;
        }
    }
    
    private LifecycleHandler() {}
    
    public static boolean makeTransitionsTo(ILifecycleAware la, byte currentState, byte targetState, Logger logger) {
        if(currentState == targetState || currentState == LifecycleState.DESTROYED) {
            return false;
        }
        
        final boolean down= targetState < currentState;
        final boolean debug= logger != null && logger.isDebugEnabled();
        while (currentState != targetState) {
            currentState+= down ? -1 : 1;

            if(la != null) {
                try {
                    switch (currentState) {
                        case LifecycleState.CONSTRUCTED: {
                            if (down) {
                                if(debug) {
                                    logger.debug("cleaning up " + la);
                                }
                                la.cleanup();
                            }
                            
                            break;
                        }
        
                        case LifecycleState.INITIALISED: {
                            if (down) {
                                if(debug) {
                                    logger.debug("stopping " + la);
                                }
                                la.stop();
                            } else {
                                if(debug) {
                                    logger.debug("initialising " + la);
                                }
                                la.initialise();
                            }
                            
                            break;
                        }
        
                        case LifecycleState.ACTIVE: {
                            if (!down) {
                                if(debug) {
                                    logger.debug("starting " + la);
                                }
                                la.start();
                            }
                            
                            break;
                        }
                    }
                } catch (RuntimeException re) {
                    if(logger != null) {
                        logger.warn("corrupted state change", re);
                    }
                }
            }
        }
        
        return true;
    }
    
    public static boolean makeTransitionsTo(ILifecycleContext context, byte targetState, Logger logger) {
        boolean result= makeTransitionsTo(context.lifecycleAware(), context.lifecycleState(), targetState, logger);
        
        if(result) {
            if(context instanceof SimpleLifecycleContext) {
                ((SimpleLifecycleContext) context).lifecycleState= targetState;
            } else if(context instanceof ListableLifecycleContext) {
                ((ListableLifecycleContext) context).lifecycleState= targetState;
            }
        }
        return result;
    }
}
