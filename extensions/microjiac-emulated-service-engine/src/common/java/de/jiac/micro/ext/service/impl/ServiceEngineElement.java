/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Emulated-Service-Engine.
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
package de.jiac.micro.ext.service.impl;

import java.io.IOException;

import org.slf4j.Logger;

import de.jiac.micro.agent.IActuator;
import de.jiac.micro.agent.handle.ICommunicationHandle;
import de.jiac.micro.core.IHandle;
import de.jiac.micro.core.ILifecycleAware;
import de.jiac.micro.core.io.IMulticastAddress;
import de.jiac.micro.core.scope.AgentScope;
import de.jiac.micro.ext.service.IServiceHandle;
import de.jiac.micro.interaction.IInteractionRegistry;
import de.jiac.micro.interaction.rr.RequestResponseProtocol;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ServiceEngineElement implements IActuator, ILifecycleAware {
    private static final String PROTOCOL_NAME= "ServiceProtocol";
    
    private RequestResponseProtocol _requestProtocol;
    private ServiceEngine _serviceEngine;
    private IServiceHandle _serviceHandle;
    
    private IMulticastAddress _serviceGroup;
    private Logger logger;
    
    public void cleanup() {
        logger= null;
    }

    public void initialise() {
        _requestProtocol= new RequestResponseProtocol(PROTOCOL_NAME);
        _serviceEngine= new ServiceEngine(_requestProtocol);
        _serviceHandle= new ServiceHandleImpl(_serviceEngine);
        logger= AgentScope.getAgentReference().getLogger("ServiceEngine");
    }

    public void start() {
        IInteractionRegistry registry= (IInteractionRegistry) AgentScope.getAgentHandle(IInteractionRegistry.class);
        
        if(registry == null) {
            logger.error("ServiceEngine: could not find interaction manager");
            return;
        }
        
        if(!registry.registerProtocol(_requestProtocol)) {
            logger.error("ServiceEngine: could not register request protocol");
        }
        
        ICommunicationHandle ch= (ICommunicationHandle) AgentScope.getAgentHandle(ICommunicationHandle.class);
        if(ch == null) {
            logger.error("ServiceEngine: could not find communication layer");
            return;
        }
        
        _serviceGroup= ch.getMulticastAddressForName(ServiceEngine.SEARCH_GROUP);
        try {
            ch.joinGroup(_serviceGroup);
        } catch (IOException e) {
            logger.error("could not join group '" + _serviceGroup + "'", e);
        }
    }

    public void stop() {
        ICommunicationHandle ch= (ICommunicationHandle) AgentScope.getAgentHandle(ICommunicationHandle.class);
        if(ch == null) {
            logger.error("ServiceEngine: could not find communication layer");
            return;
        }
        
        try {
            ch.leaveGroup(_serviceGroup);
        } catch (IOException e) {
            logger.error("could not leave group '" + _serviceGroup + "'", e);
        }
        
        IInteractionRegistry registry= (IInteractionRegistry) AgentScope.getAgentHandle(IInteractionRegistry.class);
        
        if(registry == null) {
            logger.error("ServiceEngine: could not find interaction manager");
            return;
        }
        
        if(!registry.unregisterProtocol(_requestProtocol)) {
            logger.error("ServiceEngine: could not unregister request protocol");
        }
    }
    
    public IHandle getHandle() {
        return _serviceHandle;
    }
}
