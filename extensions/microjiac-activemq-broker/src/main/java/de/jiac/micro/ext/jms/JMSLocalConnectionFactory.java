/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC ActiveMQ-Broker.
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
package de.jiac.micro.ext.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import de.jiac.micro.core.IHandle;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class JMSLocalConnectionFactory implements ConnectionFactory, IHandle {
    private final ConnectionFactory _connectionFactory;
    
    public JMSLocalConnectionFactory(ConnectionFactory localConnectionFactory) {
        _connectionFactory= localConnectionFactory;
    }
    
    public Connection createConnection() throws JMSException {
        return _connectionFactory.createConnection();
    }

    public Connection createConnection(String userName, String password) throws JMSException {
        return _connectionFactory.createConnection(userName, password);
    }
}
