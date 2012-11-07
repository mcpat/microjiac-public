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
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public abstract class AbstractViolationHandler extends AbstractSchedulable {
    protected AbstractViolationHandler() {
        super(new SporadicParameters());
    }
    
    public final long getMinimumInterarrival() {
        return getSporadicParameters().getMinimumInterarrival();
    }
    
    public final void setMinimumInterarrival(long minimumInterarrival) {
        getSporadicParameters().setMinimumInterarrival(minimumInterarrival);
    }
    
    protected abstract void runShort();
    
    private final SporadicParameters getSporadicParameters() {
        return (SporadicParameters) getParameters();
    }
}
