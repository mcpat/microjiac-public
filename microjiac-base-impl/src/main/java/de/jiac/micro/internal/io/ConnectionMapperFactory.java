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
 * $Id: ConnectionMapperFactory.java 22106 2009-02-17 13:41:23Z schoener $ 
 */
package de.jiac.micro.internal.io;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import com.github.libxjava.lang.IClassLoader;

import de.jiac.micro.core.io.IStreamConnection;

/**
 * @author Marcel Patzlaff
 * @version $Revision: 22106 $
 */
public final class ConnectionMapperFactory {
    public static final String PATH= "de/jiac/micro/conmap/";
    
    private ConnectionMapperFactory() {}
    
    public static IStreamConnection openStreamConnection(IClassLoader classLoader, String scheme) throws IOException {
        Connection c= Connector.open(scheme);
        try {
            return getStreamConnection(classLoader, scheme, c);
        } catch (IOException ioe) {
            try {c.close();}catch(IOException innerIOE) {}
            throw ioe;
        }
    }
    
    public static IStreamConnection getStreamConnection(IClassLoader classLoader, String scheme, Connection c) throws IOException {
        int colon= scheme.indexOf(':');
        String type= scheme.substring(0, colon).toLowerCase();

        /* 
         * Override type mapping if connection is a stream connection
         * and thus reduce unnecessary overhead.
         */
        if(c instanceof StreamConnection) {
            type= "stream";
        }
        
        // first look for type mapping
        String className= searchClassName(classLoader, type);
        
        if(className == null) {
            throw new IOException("no stream mapping found for '" + type + "'");
        }
        
        // try to instantiate
        try {
            AbstractConnectionMapper stream= (AbstractConnectionMapper) classLoader.loadClass(className).newInstance();
            stream.initialise(c);
            return stream;
        } catch (ClassNotFoundException cfne) {
            throw new IOException("no stream mapping found for '" + type + "': " + cfne.getMessage());
        } catch (InstantiationException ie) {
            throw new IOException("could not instantiate stream mapping for + '" + type + "': " + ie.getMessage());
        } catch (IllegalAccessException iae) {
            throw new IOException("could not access constructor of stream mapping for '" + type + "': " + iae.getMessage());
        } catch (ClassCastException cce) {
            throw new IOException("stream mapping for '" + type + "' does not implement '" + IStreamConnection.class.getName() + "': " + cce.getMessage());
        }
    }
    
    private static String searchClassName(IClassLoader classLoader, String type) {
        // search for mapping
        InputStream in= classLoader.getResourceAsStream(PATH + type);
        String result= null;
        if(in != null) {
            StringBuffer buffer= new StringBuffer();
            int ch;
            try {
                while((ch= in.read()) > 0) {
                    switch(ch) {
                        case '\r': case '\n': case ' ': {
                            continue;
                        }
                        default: {
                            buffer.append((char) ch);
                        }
                    }
                }
                
                result= buffer.toString();
            } catch (IOException ioe) {
                // fall through
            }
        }
        
        return result;
    }
}
