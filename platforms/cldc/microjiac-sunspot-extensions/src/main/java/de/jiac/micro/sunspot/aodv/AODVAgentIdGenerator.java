/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC SunSPOT-Extensions.
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

package de.jiac.micro.sunspot.aodv;

import de.jiac.micro.core.IAgent;
import de.jiac.micro.core.scope.AgentScope;

/**
 *
 * @author Marcel Patzlaff
 */
public class AODVAgentIdGenerator {
    private static final String PROPERTY_KEY= "AODV-AGENT-ID";
    private static int ID_COUNTER= 0;
    
    public static Integer getAgentId() {
        IAgent agent= AgentScope.getAgentReference();
        Integer id= (Integer) agent.getProperty(PROPERTY_KEY);
        
        if(id == null) {
            synchronized (AODVNodeComponent.class) {
                if(ID_COUNTER > 255) {
                    throw new IllegalStateException("too many agents");
                }
                
                id= new Integer(ID_COUNTER++);
            }
            
            agent.setProperty(PROPERTY_KEY, id);
        }
        
        return id;
    }
}
