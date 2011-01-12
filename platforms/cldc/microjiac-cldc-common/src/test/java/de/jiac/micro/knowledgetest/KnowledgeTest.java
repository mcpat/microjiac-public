/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC CLDC-Common.
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

/*
 * $Id$ 
 */
package de.jiac.micro.knowledgetest;

import junit.framework.TestCase;

import com.github.libxjava.lang.SimpleClassLoader;

import de.jiac.micro.core.IScope;
import de.jiac.micro.core.IScope.IScopeStateChangeListener;
import de.jiac.micro.core.scope.NodeScope;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class KnowledgeTest extends TestCase {
    public void testKnowledge() throws Exception {
        final NodeScope nodeScope= new NodeScope();
        
        final Object lock= new Object();
        
        final IScopeStateChangeListener listener= new IScopeStateChangeListener() {
            public void onPause(IScope scope) {
                
            }

            public void onSetup(IScope scope) {
                synchronized (lock) {
                    lock.notify();
                }
            }

            public void onStart(IScope scope) {
                
            }

            public void onTerminate(IScope scope) {
                synchronized (lock) {
                    lock.notify();
                }
            }
        };
        
        synchronized (lock) {
            nodeScope.setup(
                    new SimpleClassLoader(),
                    NodeConf.class.getName(),
                    listener
            );
            
            lock.wait(5000);
        }
        
        nodeScope.signal(IScope.SIG_START);
        
        Thread.sleep(5000);
        synchronized (lock) {
            nodeScope.signal(IScope.SIG_TERMINATE);
            lock.wait(2000);
        }
    }
}
