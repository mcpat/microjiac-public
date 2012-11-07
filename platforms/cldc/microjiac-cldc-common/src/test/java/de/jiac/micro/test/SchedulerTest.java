/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC CLDC-Common.
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
package de.jiac.micro.test;

import junit.framework.TestCase;

import com.github.libxjava.lang.SimpleClassLoader;

import de.jiac.micro.agent.AbstractActiveBehaviour;
import de.jiac.micro.core.IScope;
import de.jiac.micro.core.scope.AgentScope;
import de.jiac.micro.internal.core.AbstractAgent;
import de.jiac.micro.internal.core.AbstractAgentConfiguration;
import de.jiac.micro.internal.core.NonRTAgent;

public class SchedulerTest extends TestCase {
    public static class AgentConfig extends AbstractAgentConfiguration {
        public AgentConfig() {
            super("SchedulerAgent", "SchedulerAgent", NonRTAgent.class);
        }

        protected void configureAgent(AbstractAgent agent) {
            PeriodicRunnable pr= new PeriodicRunnable("a", 200);
            pr.setPeriod(2000);
            pr.setStart(0);
            agent.addAgentElement(pr);
            
            pr= new PeriodicRunnable("b", 400);
            pr.setPeriod(500);
            pr.setStart(0);
            agent.addAgentElement(pr);
            
            pr= new PeriodicRunnable("c", 1000);
            pr.setPeriod(2500);
            pr.setStart(100);
            agent.addAgentElement(pr);
        }
    }
    
	private static class PeriodicRunnable extends AbstractActiveBehaviour {
		public PeriodicRunnable(String name, long duration) {
			_name= name;
			_duration= duration;
		}
		private long _duration;
		
		private int _count= 0;
		private long _estimatedInterval= 0;
		private long _lastTime= -1;
		private String _name= null;

		public void runShort() {
			System.out.println("run "+ _name);
			long currentTime= System.currentTimeMillis();
			
			if(_lastTime > 0) {
				_estimatedInterval= ((_estimatedInterval * _count) + (currentTime - _lastTime)) / (++_count);
			}
			_lastTime= currentTime;
			
			try {Thread.sleep(_duration);} catch (InterruptedException e) {}
		}
		
		public void cleanup() {
            System.out.println(toString());
        }

        public String toString() {
	        return "statistic for '" + _name + "': called '" + _count + "'; estimated interval '" + _estimatedInterval + "'";
	    }
	}
	
	public void testScheduling() throws Exception {
	    AgentScope agentScope= new AgentScope();
	    agentScope.setup(new SimpleClassLoader(), AgentConfig.class.getName(), null);
	    agentScope.signal(IScope.SIG_START);
		System.out.println("wait 60 seconds now...");
		Thread.sleep(10000);
		agentScope.signal(IScope.SIG_TERMINATE);
		Thread.sleep(2000);
	}
}
