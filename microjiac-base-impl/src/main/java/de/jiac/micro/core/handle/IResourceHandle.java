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

/*
 * $Id$
 */
package de.jiac.micro.core.handle;

import java.io.IOException;

import javax.microedition.io.Connection;

import de.jiac.micro.agent.IConnectionFactory;
import de.jiac.micro.core.IHandle;
import de.jiac.micro.core.io.IStreamConnection;

/**
 * This interface provides functionalities to acquire resources
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public interface IResourceHandle extends IHandle {
    /**
     * This method opens a connection to the specified URL. Connections created
     * by this method <strong>MUST</strong> be released via
     * {@link #close(Connection)}!
     * 
     * <p>
     * Note: {@link IConnectionFactory IConnectionFactories} that are registered
     * with the current agent are also available through this method!
     * 
     * @param url
     *            The location to open the connection to
     * @return The appropriate connection.
     * @exception IOException
     *                if the URL is malformed or the connection could not be
     *                opened
     */
    Connection open(String url) throws IOException;

    /**
     * This method opens a connection to the specified URL and tries to map it
     * into an {@link IStreamConnection}.
     * 
     * @param url
     *            The location to open the connection to
     * @return The appropriate IStreamConnection.
     * @exception IOException
     *                if no mapping could be found or everything
     *                {@link #open(String)} throws
     */
    IStreamConnection openStreamConnection(String url) throws IOException;

    /**
     * Use this method to release the connection and all acquired resources.
     * Otherwise the agent won't be able to open subsequent connections if the
     * limit is reached!
     * 
     * @param con
     *            The connection to close.
     */
    void close(Connection con);
}
