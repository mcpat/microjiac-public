/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Emulated-Service-Engine.
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

/*
 * $Id$ 
 */
package de.jiac.micro.ext.service.test;

import org.slf4j.Logger;

import de.jiac.micro.agent.AbstractActiveBehaviour;
import de.jiac.micro.core.IAgent;
import de.jiac.micro.core.scope.AgentScope;
import de.jiac.micro.ext.service.IServiceContext;
import de.jiac.micro.ext.service.IServiceHandle;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class LogProducerElement extends AbstractActiveBehaviour {
    private Logger _logger;
    private IServiceContext _remoteConsoleServiceContext;
    private IConsoleService _remoteConsoleService;

    public void stop() {
        _logger= null;
        _remoteConsoleService= null;
        _remoteConsoleServiceContext= null;
    }

    public void start() {
        IAgent agent= AgentScope.getAgentReference();
        _logger= agent.getLogger("LogProducer");
        IServiceHandle serviceHandle= (IServiceHandle) agent.getHandle(IServiceHandle.class);
        _remoteConsoleServiceContext= serviceHandle.createContext(IConsoleService.class);
        _remoteConsoleService= (IConsoleService) _remoteConsoleServiceContext;
    }

    protected void runShort() {
        _remoteConsoleServiceContext.setBlocking(true);
        _logger.info("try to invoke doPrintln");
        try {
            _remoteConsoleService.doPrintln("Hallo Welt");
            _logger.info("doPrintln finished successfully");
        } catch (Exception e) {
            _logger.warn("doPrintln threw an exception", e);
        }
    }
}
