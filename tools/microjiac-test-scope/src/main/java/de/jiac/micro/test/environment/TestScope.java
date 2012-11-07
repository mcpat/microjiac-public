/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Test-Scope.
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
package de.jiac.micro.test.environment;

import de.jiac.micro.core.IContainer;
import de.jiac.micro.core.IHandle;
import de.jiac.micro.core.LifecycleHandler.SimpleLifecycleContext;
import de.jiac.micro.core.scope.Scope;
import de.jiac.micro.internal.core.DummyContainer;

/** 
 * @author Vladimir Sch&ouml;ner
 * @author Marcel Patzlaff
 */
public class TestScope extends Scope {
	public TestScope(String runnerName) {
		super(runnerName, Object.class);

		DummyContainer container= new DummyContainer(runnerName);
		SimpleLifecycleContext context= new SimpleLifecycleContext(container);
		getScopeMemory().setReference(context);
	}
	
    public static Thread createScopeAwareTestThread(Runnable target, IHandle[] scopeHandles) {
    	String runnerName= target + String.valueOf(System.currentTimeMillis());
    	TestScope scope= new TestScope(runnerName);
    	
    	if(scopeHandles != null && scopeHandles.length > 0) {
    	    IContainer container= scope.getContainerReference();
    	    for(int i= 0; i < scopeHandles.length; ++i) {
    	        container.addHandle(scopeHandles[i]);
    	    }
    	}
    	
        return createScopeAwareThread(scope, target, runnerName);
    }
    
    public static void runInScope(Runnable runnable) {
        createScopeAwareTestThread(runnable, null).start();
    }
    
    public static void runInScope(Runnable runnable, IHandle[] scopeHandles) {
        createScopeAwareTestThread(runnable, scopeHandles).start();
    }
}