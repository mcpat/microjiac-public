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
/*
 * $Id$
 */
package de.jiac.micro.core.feature;


/**
 * 
 * @author Vladimir Sch&ouml;ner
 * @version $Revision$
 */
public class PeriodicParameters extends Parameters {
    /*package*/ long estimatedDuration = 0;
    /*package*/ long nextExecutionTime = -1;
    
	/*package*/ volatile long start = 0;
	/*package*/ volatile long period= 1;

	public final long getStart() {
		return start; 
	}
	
	public final void setStart(long start) {
	    if(start < 0) {
	        throw new IllegalArgumentException("start must be >= 0L");
	    }
	    
	    this.start= start;
//	    final PrimitiveScheduler s= getScheduler();
//	    
//	    if(isEnabled() && s != null) {
//	        s.startChanged(this, start);
//	    }
	}
	
	public final long getPeriod() {
		return period;
	}
	
	public final void setPeriod(long period) {
	    if(period <= 0) {
	        throw new IllegalArgumentException("period must be > 0L");
	    }
	    
	    this.period= period;
//	    final PrimitiveScheduler s= getScheduler();
//	    
//        if(s != null) {
//            s.periodChanged(this, period);
//        }
	}
}
