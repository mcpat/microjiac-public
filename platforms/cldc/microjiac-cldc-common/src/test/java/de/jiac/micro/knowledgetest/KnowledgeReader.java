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
package de.jiac.micro.knowledgetest;

import java.util.Date;
import java.util.Enumeration;

import de.jiac.micro.agent.AbstractReactiveBehaviour;

public class KnowledgeReader extends AbstractReactiveBehaviour {
    protected Class[] filterDataTypes() {
        return new Class[] {ITestA.class, ITestB.class, TestClassB.class};
    }

    protected void runShort(Enumeration sensorReadings) {
        while(sensorReadings.hasMoreElements()) {
            System.out.println(new Date(System.currentTimeMillis()) + ":: reader called -> " + sensorReadings.nextElement());
        }
    }
}
