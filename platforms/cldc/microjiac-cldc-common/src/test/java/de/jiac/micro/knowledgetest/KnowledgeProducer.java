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

import de.jiac.micro.agent.AbstractActiveBehaviour;
import de.jiac.micro.agent.ISensor;
import de.jiac.micro.agent.memory.IShortTermMemory;

public class KnowledgeProducer extends AbstractActiveBehaviour implements ISensor {
    private IShortTermMemory _stm;
    
	protected void runShort() {
System.out.println("launch the runShort");
		_stm.notice(new TestClassB());
	}

    public void setShortTermMemory(IShortTermMemory stm) {
        _stm= stm;
    }
}
