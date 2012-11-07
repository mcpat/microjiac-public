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
package de.jiac.micro.core.scope;

import org.slf4j.Logger;

import com.github.libxjava.concurrent.AbstractSingleThreadRunner;
import com.github.libxjava.lang.IClassLoader;

import de.jiac.micro.core.IContainer;
import de.jiac.micro.core.IContainerConfiguration;
import de.jiac.micro.core.IScope;
import de.jiac.micro.core.LifecycleHandler;
import de.jiac.micro.core.LifecycleHandler.LifecycleState;
import de.jiac.micro.core.LifecycleHandler.SimpleLifecycleContext;
import de.jiac.micro.core.handle.IReflector;

/**
 * @author Erdene-Ochir Tuguldur
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class Scope implements IScope {
	private final class ScopeRunner extends AbstractSingleThreadRunner {
	    private final Class _configurationSuperClass;
	    
		protected IClassLoader classLoader;
		protected String configurationClassName;
		protected IScopeStateChangeListener listener;
		
		private byte _signal= -1;
		private Object _signalMutex;
		
		protected ScopeRunner(String name, Class configurationSuperClass) {
		    super(name);
		    _configurationSuperClass= configurationSuperClass;
		    _signalMutex= new Object();
		}
		
		protected final void doRun() {
	        SimpleLifecycleContext ref;
	        try {
		        try {
	                IContainerConfiguration configuration= (IContainerConfiguration) classLoader.loadClass(configurationClassName).newInstance();
	                
	                if(!_configurationSuperClass.isInstance(configuration)) {
	                    throw new RuntimeException("excepted " + _configurationSuperClass.getName() + " and got " + configuration.getClass().getName());
	                }
	                
	                final IContainer c= configuration.newInstance(classLoader);
	                
	                ref= new SimpleLifecycleContext(c);
	                getScopeMemory().setReference(ref);
	                configuration.configure(c);
	                
	                c.addHandle(configuration);
	                
	                try {
	                    IReflector reflector= (IReflector) classLoader.loadClass(IReflector.REFLECTOR_NAME).newInstance();
	                    c.addHandle(reflector);
	                } catch (Exception e) {
	                    c.getLogger().warn("no reflector found", e);
	                }

	                if(listener != null) {
	                    listener.onSetup(Scope.this);
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	                throw new RuntimeException(e.toString());
	            }
	            
    			// wait for SIG_TERMINATE
			    processSignals(ref);
	        } finally {
    			// terminate scope
    			getScopeMemory().setReference(null);
	        }
		}
		
		protected void communicateSignal(byte signal) {
		    synchronized(_signalMutex) {
		        _signal= signal;
		        _signalMutex.notify();
		    }
		}
		
		protected void forkExecution(Runnable runnable, String name) {
            ScopeImplBinder.createScopeAwareThread(Scope.this, runnable, name).start();
        }

        private void processSignals(SimpleLifecycleContext ref) {
            int currentSignal;
            final Logger logger= ((IContainer) ref.lifecycleAware()).getLogger();
            for(;;) {
                synchronized(_signalMutex) {
                    if(_signal < 0) {
                        try {
                            _signalMutex.wait();
                        } catch (InterruptedException e) {
                            // ignore
                        }
                    }
                    
                    currentSignal= _signal;
                    _signal= -1;
                }
            
				switch(currentSignal) {
					case IScope.SIG_INITIALISE: case IScope.SIG_PAUSE: {
						boolean paused= currentSignal == IScope.SIG_PAUSE;
						LifecycleHandler.makeTransitionsTo(ref, LifecycleState.INITIALISED, logger);
						
						if(paused && listener != null) {
							listener.onPause(Scope.this);
						}
						break;
					}
					
					case IScope.SIG_START: case IScope.SIG_RESUME: {
						LifecycleHandler.makeTransitionsTo(ref, LifecycleState.ACTIVE, logger);
						
						if(listener != null) {
						    listener.onStart(Scope.this);
						}
						break;
					}
					
					case IScope.SIG_TERMINATE: {
						LifecycleHandler.makeTransitionsTo(ref, LifecycleState.DESTROYED, logger);
						
						if(listener != null) {
							listener.onTerminate(Scope.this);
						}
						return;
					}
				}
			}
		}
	}
	
	public static Scope getScope() throws IllegalThreadStateException {
	    Thread thread= Thread.currentThread();
	    
	    if(thread instanceof IScopeAwareThread) {
	        return ((IScopeAwareThread) thread).getScope();
	    } else {
            // maybe the platform dependent ScopeFactory knows a way to obtain scope
            Scope result= ScopeImplBinder.guessScope();
            
            if(result != null) {
                return result;
            }
	    }
	    
	    throw new IllegalThreadStateException("Current thread is not context aware: " + thread.toString());
	}
	
    protected static Scope getScope(Class scopeType) throws IllegalThreadStateException {
        Scope result= getScope();

        if(result == null || result.getClass() != scopeType) {
            throw new IllegalThreadStateException("Current thread is not allowed to access " + scopeType.getName());
        }
        
        return result;
    }
    
    public static Thread createScopeAwareThread(Scope scope, Runnable target, String name) {
        return ScopeImplBinder.createScopeAwareThread(scope, target, name);
    }
    
    public final static Thread createScopeAwareThread(Runnable target, String name) {
        return ScopeImplBinder.createScopeAwareThread(getScope(), target, name);
    }
    
    public final static IContainer getContainer() {
        return getScope().getContainerReference();
    }
    
    public final static void executeInScope(Runnable runnable) {
        ScopeImplBinder.executeInScope(runnable);
    }
    
    private final IScopeMemory _memory;
    private final ScopeRunner _runner;
    
    protected Scope(String runnerName, Class configurationSuperClass) {
    	//TODO: How should we estimate the scope size? default value is now 1MB.
        _memory= ScopeImplBinder.createScopeMemory(this, 1024*1024);
        _runner= new ScopeRunner(runnerName, configurationSuperClass);
    }
    
    public final IScopeMemory getScopeMemory() {
        return _memory;
    }
    
    public final IContainer getContainerReference() {
        SimpleLifecycleContext scopeContext= (SimpleLifecycleContext) _memory.getReference();
        
        if(scopeContext == null) {
            throw new RuntimeException("Scope is not active");
        }
        
        return (IContainer) scopeContext.lifecycleAware();
    }
    
    public final void signal(byte signal) {
        if(_runner.isInState(AbstractSingleThreadRunner.STARTED)) {
			_runner.communicateSignal(signal);
		}
    }

    public final void setup(IClassLoader classLoader, String configurationClassName, IScopeStateChangeListener listener) {
        // TODO: check whether this scope is inactive before launching another thread!!!
        if(!_runner.isInState(AbstractSingleThreadRunner.STOPPED)) {
            throw new IllegalThreadStateException("This scope is still active.");
        }
        
		_runner.classLoader= classLoader;
		_runner.configurationClassName= configurationClassName;
		_runner.listener= listener;
		_runner.start();
		
		try {
			if(!_runner.waitForState(5000, AbstractSingleThreadRunner.STARTED)) {
			    throw new RuntimeException("Scope runner could not be started");
			}
		} catch (InterruptedException e) {
			throw new RuntimeException("Scope runner could not be started: " + e.toString());
		}
    }
}
