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
package de.jiac.micro.core.feature;

import java.util.Vector;

import org.slf4j.Logger;

import com.github.libxjava.concurrent.AbstractSingleThreadRunner;

import de.jiac.micro.agent.IScheduler;
import de.jiac.micro.agent.memory.IFact;
import de.jiac.micro.agent.memory.IShortTermMemory;
import de.jiac.micro.core.scope.AbstractScopeAwareRunner;

/**
 * @author Erdene-Ochir Tuguldur
 * @author Marcel Patzlaff
 * @author Vladimir Sch&ouml;ner
 */
public final class PrimitiveScheduler implements IScheduler {
    private final class PeriodicScheduler extends AbstractScopeAwareRunner {
        private Vector _queue = new Vector();
        
        protected PeriodicScheduler() {
            super("PeriodicScheduler");
        }
        
        public void doRun() {
            PeriodicParameters actual = null;
            long startTime;
            while (!isCancelled()) {
                synchronized (_queue) {
                    if (_queue.isEmpty()) {
                        try {
                            _queue.wait();
                        } catch (InterruptedException e) {
                        }
                        continue;
                    }
                    
                    actual = getMin();
                    startTime = System.currentTimeMillis();
                    
                    // actual != null because _queue is not empty
                    if (actual.nextExecutionTime > startTime) {
                        try {
                            _queue.wait(actual.nextExecutionTime - startTime);
                        } catch (InterruptedException e) {
                        }
                        // we do not know why we are here -> re-run this loop
                        continue;
                    }
                }
                
                try {
                    actual.lastExecution= startTime;
                    ((AbstractPeriodicRunnable) actual.schedulable).runShort();
                } catch (Exception e) {
                    if (logger.isErrorEnabled()) {
                        logger.error("uncaught exception in: '" + actual.schedulable.toString() + "'", e);
                    }
                }
                
                synchronized (_queue) {
                    // update task
                    long currentTime = System.currentTimeMillis();
                    actual.estimatedDuration= ((actual.estimatedDuration + (currentTime - startTime)) / 2);
                    // update further fields only if the current task is still registered
                    if (actual.getPeriod() <= 0) {
                        _queue.removeElement(actual);
                    } else if (_queue.contains(actual)) {
                        actual.nextExecutionTime= Math.max(actual.nextExecutionTime + actual.getPeriod(), currentTime);
                    }
                }
                Thread.yield();
            }
        }
        
        protected void add(PeriodicParameters pars, int index) {
            synchronized (_queue) {
                long time = pars.start;
                long currentTime = System.currentTimeMillis();
                long firstTime;
                if (!_queue.contains(pars)) {
                    if (index != -1) {
                    	_queue.insertElementAt(pars, index);
                    } else {
                    	//default insert operation - tail of the queue
                    	_queue.addElement(pars);
                    }
                    firstTime = time;
                } else {
                    firstTime = time > pars.nextExecutionTime ? time : pars.nextExecutionTime;
                }
                pars.nextExecutionTime= firstTime > currentTime ? firstTime : currentTime;
                _queue.notify();
            }
        }
        
        protected boolean contains(PeriodicParameters pars) {
            synchronized (_queue) {
                return _queue.contains(pars);
            }
        }
        
        protected void remove(PeriodicParameters pars) {
            pars.nextExecutionTime= -1; //
            synchronized (_queue) {
                if (_queue.removeElement(pars)) {
                    _queue.notify();
                }
            }
        }
        
        protected void unblock() {
            synchronized (_queue) {
                _queue.notify();
            }
        }
        
        private PeriodicParameters getMin() {
            PeriodicParameters result = null;
            for (int i = 0; i < _queue.size(); ++i) {
                PeriodicParameters actual = (PeriodicParameters) _queue.elementAt(i);
                
                if (result == null) {
                    result = (PeriodicParameters) _queue.elementAt(i);
                    continue;
                }
                
                if (actual.nextExecutionTime < result.nextExecutionTime) {
                    result = actual;
                }
            }
            return result;
        }
    }
    
    private final class SporadicScheduler extends AbstractScopeAwareRunner implements IShortTermMemory {
        private Vector _queue= new Vector();
        
        private boolean _changed= false;
        
        protected SporadicScheduler() {
            super("SporadicScheduler");
        }
        
        public void notice(IFact newStuff) {
            if(isCancelled()) {
                return;
            }
            
            synchronized (_queue) {
                for(int i= 0; i < _queue.size(); ++i) {
                    SporadicParameters sp= (SporadicParameters) _queue.elementAt(i);
                    sp.data.addData(newStuff);
                    _changed= true;
                }
                
                _queue.notify();
            }
        }

        protected void doRun() {
            SporadicParameters actual;
            int index= 0;
            int numData= 0;
            long nextArrival= Long.MAX_VALUE;
            long startTime;
            long allowedNextArrival;
            
            while(!isCancelled()) {
                synchronized (_queue) {
                    if(index >= _queue.size()) {
                        // we are through
                        if(_changed) {
                            _changed= false;
                            index= 0;
                        } else {
                            long currentTime= System.currentTimeMillis();
                            try {
                                if(nextArrival > currentTime) {
                                    _queue.wait(nextArrival - currentTime);
                                } else {
                                    _queue.wait();
                                } 
                            } catch (InterruptedException e) {
                                // ignore
                            }
                        }
                        nextArrival= Long.MAX_VALUE;
                        continue;
                    } else {
                        // we have something to do
                        actual= (SporadicParameters) _queue.elementAt(index++);
                        startTime= System.currentTimeMillis();
                        allowedNextArrival= actual.lastExecution + actual.getMinimumInterarrival();
                        
                        if(allowedNextArrival > startTime) {
                            nextArrival= nextArrival > allowedNextArrival ? allowedNextArrival : nextArrival;
                            continue;
                        }
                        
                        numData= actual.data.size();
                        if(numData <= 0) {
                            continue;
                        }
                    }
                }
                
                actual.lastExecution= startTime;
                try {
                    ((AbstractSporadicRunnable) actual.schedulable).runShort(actual.data);
                } catch (Exception e) {
                    if (logger.isErrorEnabled()) {
                        logger.error("uncaught exception in: '" + String.valueOf(actual.schedulable) + "'", e);
                    }
                }
                
                actual.data.reset(numData);
            }
        }
        
        protected void add(SporadicParameters sp) {
            synchronized (_queue) {
                if(!_queue.contains(sp)) {
                    _queue.addElement(sp);
                }
            }
        }
        
        protected boolean contains(SporadicParameters sp) {
            synchronized (_queue) {
                return _queue.contains(sp);
            }
        }
        
        protected void remove(SporadicParameters sp) {
            synchronized (_queue) {
                _queue.removeElement(sp);
            }
        }

        protected void unblock() {
            synchronized (_queue) {
                _queue.notify();
            }
        }
    }
    
    protected final Logger logger;
    
    private final PeriodicScheduler _periodicScheduler;
    private final SporadicScheduler _sporadicScheduler;
	
	public PrimitiveScheduler(Logger logger) {
	    this.logger= logger;
		_periodicScheduler= new PeriodicScheduler();
		_sporadicScheduler= new SporadicScheduler();
	}

	public void addSchedulable(AbstractSchedulable schedulable) {
	    if(schedulable instanceof AbstractPeriodicRunnable) {
	        _periodicScheduler.add((PeriodicParameters)schedulable.getParameters(), -1);
	    } else {
	        _sporadicScheduler.add((SporadicParameters)schedulable.getParameters());
	    }
	}
	
	public boolean containsSchedulable(AbstractSchedulable schedulable) {
	    if(schedulable instanceof AbstractPeriodicRunnable) {
	        return _periodicScheduler.contains(((AbstractPeriodicRunnable) schedulable).getPeriodicParameters());
	    } else {
	        return _sporadicScheduler.contains(((AbstractSporadicRunnable) schedulable).getSporadicParameters());
	    }
    }

    public void removeSchedulable(AbstractSchedulable schedulable) {
	    if(schedulable instanceof AbstractPeriodicRunnable) {
	        _periodicScheduler.remove((PeriodicParameters)schedulable.getParameters());
	    } else {
	        _sporadicScheduler.remove((SporadicParameters)schedulable.getParameters());
	    }
	}

    public void start() {
        try {
            _sporadicScheduler.start();
            _sporadicScheduler.waitForState(5000, AbstractSingleThreadRunner.STARTED);
        } catch (InterruptedException ie) {
            logger.error("could not start sporadic scheduler", ie);
        }
        
        try {
            _periodicScheduler.start();
            _periodicScheduler.waitForState(5000, AbstractSingleThreadRunner.STARTED);
        } catch (InterruptedException e) {
            logger.error("could not start periodic scheduler", e);
        }
    }
    
    public void stop() {
        try {
            _periodicScheduler.stop();
            _periodicScheduler.waitForState(5000, AbstractSingleThreadRunner.STOPPED);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        try {
            _sporadicScheduler.stop();
            _sporadicScheduler.waitForState(5000, AbstractSingleThreadRunner.STOPPED);
        } catch (InterruptedException ie) {
            logger.error("could not start sporadic scheduler", ie);
        }
    }
    
    public IShortTermMemory getShortTermMemory() {
        return _sporadicScheduler;
    }
    
//	/*package*/ void priorityChanged(Parameters pars, int newPriority) {
//	    // TODO
//	}
//	
//	/*package*/ void costChanged(Parameters pars, long newCost) {
//	    if(newCost < 0) {
//            throw new IllegalArgumentException("cost must be >= 0L");
//        }
//	    
//	    // TODO
//    }
//	
//	/*package*/ void deadlineChanged(Parameters pars, long newDeadline) {
//	    if(newDeadline < 0) {
//            throw new IllegalArgumentException("deadline must be >= 0L");
//        }
//	    
//	    // TODO
//    }
//	
//	/*package*/ void minimumInterarrivalChanged(SporadicParameters pars, long newMi) {
//	    if(newMi < 0) {
//            throw new IllegalArgumentException("minimum interarrival must be >= 0L");
//        }
//	    
//	    // TODO
//	}
//	
//	/*package*/ void startChanged(PeriodicParameters pars, long newStart) {
//	    if(newStart < 0) {
//            throw new IllegalArgumentException("start must be >= 0L");
//        }
//	    
//	    pars.start= newStart;
//    }
//	
//	/*package*/ void periodChanged(PeriodicParameters pars, long newPeriod) {
//	    if(newPeriod <= 0) {
//            throw new IllegalArgumentException("period must be > 0L");
//        }
//	    
//	    pars.period= newPeriod;
//	}
//	
//	/*package*/ void deadlineMissHandlerChanged(Parameters pars, AbstractViolationHandler newHandler) {
//	    // TODO
//	}
//	
//	/*package*/ void costOverrunHandlerChanged(Parameters pars, AbstractViolationHandler newHandler) {
//	    // TODO
//	}
}