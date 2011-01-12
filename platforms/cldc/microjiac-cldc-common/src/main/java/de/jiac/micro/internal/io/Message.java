/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC CLDC-Common.
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
package de.jiac.micro.internal.io;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import com.github.libxjava.io.IDeserialiser;
import com.github.libxjava.io.ISerialiser;
import com.github.libxjava.util.SerialisableHashtable;

import de.jiac.micro.agent.memory.IFact;
import de.jiac.micro.core.io.IMessage;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class Message implements IMessage, IFact {
    private SerialisableHashtable _headers;
    private Object _content;
    
    public Message() {}
    
    public Message(SerialisableHashtable headers, Object content) {
        _headers= headers;
        _content= content;
    }
    
    public Object getContent() {
        return _content;
    }

    public String getHeader(String key) {
        return _headers == null ? null : (String) _headers.get(key);
    }

    public Enumeration getHeaderKeys() {
        return _headers == null ? null : _headers.keys();
    }
    
    public Hashtable getHeaders() {
        return _headers;
    }

    public void setContent(Object content) {
        _content= content;
    }

    public void setHeader(String key, String value) {
        if(_headers == null) {
            if(value == null) {
                return;
            }
            
            _headers= new SerialisableHashtable();
        }
        
        if(value == null) {
            _headers.remove(key);
        } else {
            _headers.put(key, value);
        }
    }

    public void deserialise(IDeserialiser in) throws IOException, ClassNotFoundException {
        _headers= (SerialisableHashtable) in.readObject();
        _content= in.readObject();
    }

    public void serialise(ISerialiser out) throws IOException {
        out.writeObject(_headers);
        out.writeObject(_content);
    }

    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("Message\n");

        for (Enumeration e = getHeaderKeys(); e!= null && e.hasMoreElements();) {
            String key = (String) e.nextElement();
            result.append(key).append(": ").append(getHeader(key)).append('\n');
        }

        // result.append("destination: ").append(getDestination()).append('\n');
        result.append("content: ").append(String.valueOf(getContent()));
        return result.toString();
    }

//    public IUnicastAddress getSourceAddress() {
//	    return null;
//    }
}
