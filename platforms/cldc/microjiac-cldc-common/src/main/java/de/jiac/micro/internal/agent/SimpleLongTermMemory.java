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
package de.jiac.micro.internal.agent;

import java.util.Enumeration;
import java.util.Vector;

import de.jiac.micro.agent.memory.IFact;
import de.jiac.micro.agent.memory.ILongTermMemory;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class SimpleLongTermMemory implements ILongTermMemory {
    private Vector _memory= new Vector();
    
    public void forget(IFact knowlegde) {
        if(knowlegde == null) {
            throw new IllegalArgumentException("knowledge must not be null");
        }
        
        synchronized (_memory) {
            _memory.removeElement(knowlegde);
        }
    }

    public void forgetAll(Class factType) {
        if(factType == null || !IFact.class.isAssignableFrom(factType)) {
            throw new IllegalArgumentException("invalid fact type");
        }
        
        synchronized(_memory) {
            for(int i= _memory.size() - 1; i >= 0; --i) {
                Object fact= _memory.elementAt(i);
                if(factType.isInstance(fact)) {
                    _memory.removeElementAt(i);
                }
            }
        }
    }

    public void memorise(IFact knowledge) {
        if(knowledge == null) {
            throw new IllegalArgumentException("knowledge must not be null");
        }
        
        synchronized (_memory) {
            if(!_memory.contains(knowledge)) {
                _memory.addElement(knowledge);
            }
        }
    }

    public Enumeration rememberAll(Class factType) {
        if(factType == null || !IFact.class.isAssignableFrom(factType)) {
            throw new IllegalArgumentException("invalid fact type");
        }
        
        synchronized (_memory) {
            Vector result= new Vector();
            for(int i= 0; i < _memory.size(); ++i) {
                Object fact= _memory.elementAt(i);
                if(factType.isInstance(fact)) {
                    result.addElement(fact);
                }
            }
            
            return result.elements();
        }
    }

    public IFact rememberNewest(Class factType) {
        if(factType == null || !IFact.class.isAssignableFrom(factType)) {
            throw new IllegalArgumentException("invalid fact type");
        }
        
        synchronized (_memory) {
            for(int i= _memory.size() - 1; i >= 0; --i) {
                IFact fact= (IFact) _memory.elementAt(i);
                if(factType.isInstance(fact)) {
                    return fact;
                }
            }
        }
        
        return null;
    }

    public IFact rememberOldest(Class factType) {
        if(factType == null || !IFact.class.isAssignableFrom(factType)) {
            throw new IllegalArgumentException("invalid fact type");
        }
        
        synchronized (_memory) {
            int last= _memory.size() - 1;
            for(int i= 0; i <= last; ++i) {
                IFact fact= (IFact) _memory.elementAt(i);
                if(factType.isInstance(fact)) {
                    return fact;
                }
            }
        }
        
        return null;
    }
}
