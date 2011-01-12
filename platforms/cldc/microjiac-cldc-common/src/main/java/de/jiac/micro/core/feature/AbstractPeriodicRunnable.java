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
public abstract class AbstractPeriodicRunnable extends AbstractSchedulable {
    protected AbstractPeriodicRunnable() {
        super(new PeriodicParameters());
    }

	public final long getStart() {
		return getPeriodicParameters().getStart(); 
	}
	
	public final void setStart(long start) {
	    getPeriodicParameters().setStart(start);
	}
	
	public final long getPeriod() {
		return getPeriodicParameters().getPeriod(); 
	}
	
	public final void setPeriod(long period) {
	    getPeriodicParameters().setPeriod(period);
	}
	
	protected abstract void runShort();
	
	/*package*/ final PeriodicParameters getPeriodicParameters() {
	    return (PeriodicParameters) getParameters();
	}
}
