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

import java.util.HashMap;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.slf4j.Logger;

import com.github.libxjava.lang.IClassLoader;

import de.jiac.micro.agent.memory.IFact;
import de.jiac.micro.agent.memory.IShortTermMemory;
import de.jiac.micro.core.io.IAddress;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
final class JMSReceiver implements MessageListener {
    private final Logger _logger;
    private final ContentTransformer _transformer;
    private final Session _session;
    private IShortTermMemory _stm;
    private HashMap _consumers;

    public JMSReceiver(Connection connection, IClassLoader classLoader, Logger log, IShortTermMemory stm)
            throws JMSException {
        _session= connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        _transformer= new ContentTransformer(classLoader);
        _stm= stm;
        _logger= log;
        _consumers= new HashMap();
    }

    public synchronized void register(IAddress address) {
        // first check if we already have a listener like that
        String key= address.toString();
        if (_consumers.containsKey(key)) {
            _logger.warn("JMS: there is already a listener for '" + key + "' registered");
            return;
        }
        // then create a listener if needed and map it with the others.
        final MessageConsumer consumer= createConsumer((JMSAddress) address);
        if (consumer != null) {
            _consumers.put(key, consumer);
        }
    }

    public synchronized void unregister(IAddress address) {
        final String key= address.toString();
        final MessageConsumer consumer= (MessageConsumer) _consumers.remove(key);

        if (consumer != null) {
            destroyConsumer(consumer);
        }

        _logger.debug("JMS: unregistered for address '" + address + "'");
    }

    public void onMessage(Message message) {
        try {
            /*
             * By default JMS only delivers a message once per session. So it
             * doesn't matter for which selector we received it, we won't get it
             * through another consumer!
             */

            if (message instanceof BytesMessage) {
                IFact fact= _transformer.unpack((BytesMessage) message);
                _stm.notice(fact);
            } else {
                _logger.warn("JMS: unsupported message type: " + message.getClass());
            }
        } catch (Exception e) {
            _logger.error("JMS: error while receiving message", e);
        }
    }

    private MessageConsumer createConsumer(JMSAddress address) {
        MessageConsumer consumer= null;

        try {
            final String add= address.getAddressName();
            final Destination dest;
            if (address.getType() == IAddress.MULTICAST) {
                dest= _session.createTopic(add);
            } else {
                dest= _session.createQueue(add);
            }

            consumer= _session.createConsumer(dest);
            consumer.setMessageListener(this);
        } catch (JMSException e) {
            _logger.error("JMS: could not initialise consumer", e);
            destroyConsumer(consumer);
            consumer= null;
        }

        return consumer;
    }

    private void destroyConsumer(MessageConsumer consumer) {
        if (consumer != null) {
            try {
                consumer.close();
            } catch (JMSException e) {
                _logger.warn("JMS: could not destroy consumer", e);
            }
        }
    }
}
