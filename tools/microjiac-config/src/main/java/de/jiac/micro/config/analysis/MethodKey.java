/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Config.
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
package de.jiac.micro.config.analysis;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class MethodKey {
    public final String methodName;
    public final String methodDesc;
    
    public MethodKey(String methodName, String methodDesc) {
        this.methodName= methodName;
        this.methodDesc= methodDesc;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof MethodKey)) {
            return false;
        }
        
        MethodKey other= (MethodKey) obj;
        return methodName.equals(other.methodName) && methodDesc.equals(other.methodDesc);
    }

    @Override
    public int hashCode() {
        return methodName.hashCode() ^ methodDesc.hashCode();
    }

    @Override
    public String toString() {
        return methodName + methodDesc;
    }
}
