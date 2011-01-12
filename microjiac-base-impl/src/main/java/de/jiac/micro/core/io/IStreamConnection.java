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
package de.jiac.micro.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connection;

import de.jiac.micro.core.handle.IResourceHandle;

/**
 * This interface maps specific connections (like StreamConnection,
 * DatagramConnection etc...) to a generic IStreamConnection. It also allows
 * only one {@link InputStream} / {@link OutputStream} per connection.
 * 
 * IStreamConnections can be obtained by using
 * {@link IResourceHandle#openStreamConnection(String)}.
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public interface IStreamConnection extends Connection {
    /**
     * Method to obtain the InputStream for this connection.
     * 
     * @return the single InputStream for this connection
     * @throws IOException
     *             if no InputStream is available or the connection is closed
     */
    InputStream getInputStream() throws IOException;

    /**
     * Method to obtain the OutputStream for this connection.
     * 
     * @return the single OutputStream for this connection
     * @throws IOException
     *             if no OutputStream is available or the connection is closed
     */
    OutputStream getOutputStream() throws IOException;

    /**
     * Closes the complete connection <strong>and</strong> both streams if
     * acquired!
     */
    void close();

    /**
     * Checks whether the underlying connection is still open.
     * 
     * @return <code>true</code> if this connection is still open and
     *         <code>false</code> otherwise
     */
    boolean isOpen();
}
