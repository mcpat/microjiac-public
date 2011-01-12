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
package de.jiac.micro.core.feature;

import java.util.Enumeration;

import junit.framework.TestCase;

import org.slf4j.Logger;

import com.github.libxjava.io.IDeserialiser;
import com.github.libxjava.io.ISerialiser;

import de.jiac.micro.agent.memory.IFact;
import de.jiac.micro.core.scope.Scope;
import de.jiac.micro.test.environment.TestScope;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class PrimitiveSchedulerTest extends TestCase {
    static class MyFact implements IFact {
        public void deserialise(IDeserialiser arg0) {                        
        }
        public void serialise(ISerialiser arg0) {
        }
    }
    
    public void testScheduler() throws Exception {
        final Object lock= new Object();
        
        synchronized(lock) {
            TestScope.runInScope(new Runnable() {
                public void run() {
                    final Logger logger= Scope.getContainer().getLogger("schedulerTest");
                    
                    final MyFact myFact = new MyFact();
                    
                    final PrimitiveScheduler scheduler = new PrimitiveScheduler(logger);
                    
                    AbstractPeriodicRunnable periodic = new AbstractPeriodicRunnable() {
                        protected void runShort() {
                            scheduler.getShortTermMemory().notice(myFact);
                        }           
                    };
                    scheduler.addSchedulable(periodic);
                    
                    
                    AbstractSporadicRunnable sporadic = new AbstractSporadicRunnable() {
                        protected Class[] filterDataTypes() {
                            return new Class[]{MyFact.class};
                        }
                        protected void runShort(Enumeration sensorReadings) {
                        }                   
                    };              
                    scheduler.addSchedulable(sporadic);
                    
                    scheduler.start();
                    scheduler.getShortTermMemory().notice(myFact);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) { }
                    scheduler.getShortTermMemory().notice(myFact);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) { }
                    scheduler.stop();
                    
                    synchronized (lock) {
                        lock.notify();
                    }
                }
            });
            
            lock.wait();
        }
    }
}
