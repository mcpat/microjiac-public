/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Base-Implementation.
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

package de.jiac.micro.agent.handle;

import java.io.IOException;

import de.jiac.micro.core.IAgent;
import de.jiac.micro.core.IHandle;
import de.jiac.micro.core.io.IAddress;
import de.jiac.micro.core.io.IMessage;
import de.jiac.micro.core.io.IMulticastAddress;
import de.jiac.micro.core.io.IUnicastAddress;

/**
 * This interface exposes all features of the agents or nodes communication system.
 * There are several different implementations available that map communication
 * on agent level as well as on node level (like an agent communication channel).
 * 
 * @see IAgent#getHandle(Class)
 * 
 * @author Marcel Patzlaff
 */
public interface ICommunicationHandle extends IHandle {
	
    /**
     * Registers the current agent to the given group.
     * 
     * @param address
     *            the {@link IMulticastAddress} identifier for the group to join
     * @throws IllegalArgumentException
     *             if the group name is not valid
     * @throws SecurityException
     *             if the current agent has no permissions
     */
    void joinGroup(IMulticastAddress address) throws IOException;

    /**
     * Unregisters the current agent from the given group.
     * 
     * @param group
     *            the {@link IMulticastAddress} identifier for the group to leave
     * @exception IllegalArgumentException
     *                if the group name is not valid
     */
    void leaveGroup(IMulticastAddress address) throws IOException;

    /**
     * Sends a message to the specified target (1:1) and (1:n)
     * 
     * @param address
     *            the address of the target {@link IAddress} 
     * @param message
     *            the message to send
     * @throws SecurityException
     *             if the agent has no permissions
     */
    void sendMessage(IAddress address, IMessage message) throws IOException;

    /**
     * Returns the {@link IMulticastAddress} addresses that are associated with application
     * 
     * @return addresses that are associated with application
     */
    IUnicastAddress[] getLocalAddresses();

    /**
     * Creates a new and empty message.
     * 
     * @return a new empty message
     */
    IMessage createMessage();
    
    /**
     * Returns a multicast address for a group name
     * 
     * @param groupName
     * 			the string identifier for a group name
     * @return the multicast address for a group name
     */
    IMulticastAddress getMulticastAddressForName(String groupName);
    
    IAddress getAddressForString(String addressStr);
}
