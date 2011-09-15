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

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.slf4j.Logger;

import com.github.libxjava.lang.IClassLoader;

import de.jiac.micro.core.io.IAddress;
import de.jiac.micro.core.io.IMessage;
import de.jiac.micro.internal.io.Message;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
final class JMSSender {
    private final Logger _logger;
    private final Session _session;
    private final MessageProducer _producer;

    private ContentTransformer _transformer;

    public JMSSender(Connection connection, IClassLoader classLoader, Logger log) throws JMSException {
        _session= connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        _producer= _session.createProducer(null);
        _transformer= new ContentTransformer(classLoader);
        _logger= log;
    }

    public void send(IMessage message, IAddress address) throws Exception {
        Destination destination= null;

        String addressName= ((JMSAddress) address).getAddressName();

        if (address.getType() == IAddress.MULTICAST) {
            destination= _session.createTopic(addressName);
        } else {
            destination= _session.createQueue(addressName);
        }

        final BytesMessage jmsMessage= _transformer.pack((Message) message, _session);
        jmsMessage.setJMSDestination(destination);
        _producer.send(destination, jmsMessage);
        _logger.debug("JMS: message sent to " + destination.toString());
    }

}
