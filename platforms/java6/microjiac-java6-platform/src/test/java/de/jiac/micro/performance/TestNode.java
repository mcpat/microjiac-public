/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Java6-Platform.
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
package de.jiac.micro.performance;

import de.jiac.micro.core.IScope;
import de.jiac.micro.internal.core.AbstractNodeConfiguration;
import de.jiac.micro.internal.core.CDCNode;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class TestNode extends CDCNode {
    private final Object _sync= new Object();
    
    @Override
    public void onStart(IScope scope) {
        synchronized (_sync) {
            super.onStart(scope);
            _sync.notify();
        }
    }

    @Override
    public void start() {
        synchronized (_sync) {
            super.start();
            AbstractNodeConfiguration config= (AbstractNodeConfiguration) getHandle(AbstractNodeConfiguration.class);
            int count= config.agentClassNames != null ? config.agentClassNames.length : 0;
            while(count > 0) {
                try {
                    // we have to wait explicitly for all agents to start
                    _sync.wait();
                    count--;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
