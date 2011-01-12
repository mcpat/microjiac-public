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

package de.jiac.micro.agent;

import java.io.IOException;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;


/**
 * The IConnectionFactory should be extended by {@link ISensor sensor} or {@link IActuator actuator} elements to modify
 * or extend the standard <a href="http://developers.sun.com/mobility/midp/articles/genericframework/">GCF</a>
 * behaviour.
 * 
 * @author Marcel Patzlaff
 */
public interface IConnectionFactory {
    /**
     * This method must return the URI scheme that can be used with this connection factory. It is called only once
     * during the initialisation of the associated agent element.
     * 
     * <p>
     * If this factory specifies a URI that is also understood by the {@link Connector} then this factory overrides the
     * standard mechanism.
     * </p>
     * 
     * @return the scheme for the URIs this factory accepts
     */
    String getScheme();

    /**
     * This method must return a connection to the specified URI.
     * 
     * <p>
     * If <code>null</code> is returned the given <code>uri</code> is applied with the {@link Connector}. If this
     * method throws an exception, then the {@link Connector} will <b>not</b> be called! So you are able to just
     * support some URIs with the specified scheme and delegate the rest to the standard {@link Connector}.
     * </p>
     * 
     * @param uri   the identifier that describes the connection endpoint
     * @return      a new or cached instance of the connection or <code>null</code>.
     * @throws      IOException
     *              if error occur while establishing the connection
     */
    Connection openConnection(String uri) throws IOException;
}
