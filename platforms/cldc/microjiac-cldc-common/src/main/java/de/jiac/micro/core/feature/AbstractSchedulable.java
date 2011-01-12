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

package de.jiac.micro.core.feature;




/**
 * @author Erdene-Ochir Tuguldur
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public abstract class AbstractSchedulable {
	private final Parameters _parameters;
	
	protected AbstractSchedulable(Parameters shortRunnableParameters) {
	    _parameters= shortRunnableParameters;
	    _parameters.schedulable= this;
	}
	
    public final void enable() {
        _parameters.setEnabled(true);
    }
    
    public final void disable() {
        _parameters.setEnabled(false);
    }
    
    public final boolean isEnabled() {
        return _parameters.isEnabled();
    }
	
	public final int getPriority() {
		return _parameters.getPriority();
	}

	public final void setPriority(int priority) {
		_parameters.setPriority(priority);
	}

	public final long getCost() {
		return _parameters.getCost();
	}

	public final void setCost(long cost) {
		_parameters.setCost(cost);
	}

	public final long getDeadline() {
		return _parameters.getDeadline();
	}

	public final void setDeadline(long deadline) {
		_parameters.setDeadline(deadline);
	}

	public final AbstractViolationHandler getDeadlineMissHandler() {
		return _parameters.getDeadlineMissHandler();
	}
	
	public final void setDeadlineMissHandler(AbstractViolationHandler deadlineMissHandler) {
	    _parameters.setDeadlineMissHandler(deadlineMissHandler);
	}
	
	public final AbstractViolationHandler getCostOverrunHandler() {
	    return _parameters.getCostOverrunHandler();
	}
	
	public final void setCostOverrunHandler(AbstractViolationHandler overrunHandler) {
	    _parameters.setCostOverrunHandler(overrunHandler);
	}
	
	/*package*/ final Parameters getParameters() {
	    return _parameters;
	}
}
