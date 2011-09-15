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

import java.io.IOException;
import java.util.Enumeration;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Session;

import com.github.libxjava.io.BinaryDeserialiserStream;
import com.github.libxjava.io.BinarySerialiserStream;
import com.github.libxjava.io.ByteArrayInputBuffer;
import com.github.libxjava.io.ByteArrayOutputBuffer;
import com.github.libxjava.lang.IClassLoader;

import de.jiac.micro.internal.io.Message;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public final class ContentTransformer {
    private final ByteArrayInputBuffer _inputBuffer;
    private final ByteArrayOutputBuffer _outputBuffer;
    
    private final BinaryDeserialiserStream _deserialiser;
    private final BinarySerialiserStream _serialiser;
    
    ContentTransformer(IClassLoader classLoader) {
        _inputBuffer= new ByteArrayInputBuffer();
        _outputBuffer= new ByteArrayOutputBuffer();
        
        _deserialiser= new BinaryDeserialiserStream(classLoader, _inputBuffer);
        _serialiser= new BinarySerialiserStream(_outputBuffer);
    }
    
    byte[] toByteArray(Object obj) throws IOException {
        synchronized(_serialiser) {
            _outputBuffer.reset();
            try {
                _serialiser.writeObject(obj);
                _serialiser.flush();
                return _outputBuffer.toByteArray();
            } finally {
                _serialiser.flush();
            }
        }
    }
    
    Object toObject(byte[] b) throws IOException, ClassNotFoundException {
        if(b == null || b.length == 0) {
            return null;
        }
        
        synchronized(_deserialiser) {
            _deserialiser.flush();
            _inputBuffer.setInput(b);
            try {
                return _deserialiser.readObject();
            } finally {
                _deserialiser.flush();
            }
        }
    }
    
    BytesMessage pack(Message message, Session session) throws IOException, JMSException {
        final Object payload= message.getContent();
        final javax.jms.BytesMessage result= session.createBytesMessage();
        result.writeBytes(toByteArray(payload));
        
        for(Enumeration keys= message.getHeaderKeys(); keys.hasMoreElements(); ) {
            String key= (String) keys.nextElement();
            result.setStringProperty(key, message.getHeader(key));
        }
            
        return result;
    }
    
    Message unpack(BytesMessage message) throws ClassNotFoundException, IOException, JMSException {
        final int length= (int) message.getBodyLength();
        final byte[] data= new byte[length];
        message.readBytes(data);
        
        final Object payload= toObject(data);
        final Message result= new Message(null, payload);
        
        for(final Enumeration keys= message.getPropertyNames(); keys.hasMoreElements(); ) {
            final Object keyObj= keys.nextElement();
            
            if(keyObj instanceof String) {
                final String key= (String) keyObj;
                final Object valueObj= message.getObjectProperty(key);
                
                if(valueObj instanceof String) {
                    result.setHeader(key, (String)valueObj);
                }
            }
        }
        return result;
    }
}
