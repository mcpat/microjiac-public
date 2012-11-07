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

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * 
 * @author Vladimir Sch&ouml;ner
 * @version $Revision$
 */
public class SporadicParameters extends Parameters {
    /*package*/ final static class DataEnumerator implements Enumeration {
        private final Vector _data= new Vector();
        private Class[] _filterTypes;
        private int _lastRead= -1;
        private boolean _willReadMore= true;
        
        /*package*/ DataEnumerator() {}
        
        public boolean hasMoreElements() {
            synchronized (_data) {
                if(!_willReadMore || _lastRead + 1 >= _data.size()) {
                    _willReadMore= false;
                    return false;
                }
                
                return true;
            }
        }

        public Object nextElement() {
            synchronized (_data) {
                if(!_willReadMore) {
                    throw new NoSuchElementException("DataEnumerator");
                }
                
                return _data.elementAt(++_lastRead);
            }
        }
        
        /*package*/ void setFilterTypes(Class[] filterTypes) {
            _filterTypes= filterTypes;
        }
        
        /*package*/ void addData(Object data) {
            synchronized (_data) {
                if(data == null) {
                    return;
                }
                
                if(_filterTypes != null) {
                    checkFilter: {
                        for(int i= 0; i < _filterTypes.length; ++i) {
                            if(_filterTypes[i].isInstance(data)) {
                                break checkFilter;
                            }
                        }
                        
                        // data is not filtered
                        return;
                    }
                }
                
                _data.addElement(data);
            }
        }
        
        /*package*/ int size() {
            synchronized (_data) {
                return _data.size();
            }
        }
        
        /*package*/ void reset(int minDelete) {
            synchronized (_data) {
                if(_lastRead < minDelete) {
                    _lastRead= minDelete - 1;
                }
                
                for(;_lastRead >= 0; --_lastRead) {
                    _data.removeElementAt(0);
                }
                
                _willReadMore= true;
                _lastRead= -1;
            }
        }
    }
    
    /*package*/ final DataEnumerator data;
	/*package*/ long minimumInterarrival = 0;
	
	public SporadicParameters() {
	    data= new DataEnumerator();
	}
	
	public final long getMinimumInterarrival() {
		return minimumInterarrival;
	}
	
	public final void setMinimumInterarrival(long minimumInterarrival) {
	    if(minimumInterarrival < 0) {
	        throw new IllegalArgumentException("minimum interarrival must be >= 0L");
	    }
	    
	    this.minimumInterarrival= minimumInterarrival;
	    
//	    final PrimitiveScheduler s= getScheduler();
//        
//        if(s != null) {
//            s.minimumInterarrivalChanged(this, minimumInterarrival);
//        }
		
		// FIXME: Tuguldur, is this necessary???
		if (getDeadline() <= 0) {
			setDeadline(minimumInterarrival);
		}
	}
}
