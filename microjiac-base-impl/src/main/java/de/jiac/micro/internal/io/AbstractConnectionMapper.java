/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Base-Implementation.
 *
 * Copyright (c) 2007-2012 DAI-Labor, Technische Universität Berlin
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
 * $Id: AbstractConnectionMapper.java 21858 2009-02-02 16:42:09Z marcel $
 */
package de.jiac.micro.internal.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connection;

import de.jiac.micro.core.io.IStreamConnection;

/**
 *
 * @author Marcel Patzlaff
 * @version $Revision: 21858 $
 */
public abstract class AbstractConnectionMapper implements IStreamConnection {
    protected Connection connection;
    private InputStream _in;
    private OutputStream _out;
    
    public final synchronized void close() {
        if(!isOpen()) {
            return;
        }
        
        if(_out != null) {
            try {_out.close();} catch( IOException ioe) {}
        }
        
        if(_in != null) {
            try {_in.close();} catch( IOException ioe) {}
        }
        
        try {connection.close();} catch( IOException ioe) {}

        _out= null;
        _in= null;
        connection= null;
    }

    public final synchronized InputStream getInputStream() throws IOException {
        if(!isOpen()) {
            throw new IOException("connection is closed");
        }
        
        if(_in == null) {
            _in= openInputStream();
        }
        
        return _in;
    }

    public final synchronized OutputStream getOutputStream() throws IOException {
        if(!isOpen()) {
            throw new IOException("connection is closed");
        }
        
        if(_out == null) {
            _out= openOutputStream();
        }
        
        return _out;
    }

    public final synchronized boolean equals(Object obj) {
        return connection != null ? connection.equals(obj) : super.equals(obj);
    }

    public final synchronized int hashCode() {
        return connection != null ? connection.hashCode() : super.hashCode();
    }

    public final synchronized boolean isOpen() {
        return connection != null;
    }
    
    /**
     * Initialises this mapper.
     * 
     * @param mapped        the open connection of a specific type
     * @throws IOException  if <code>mapped</code> has a wrong type or another problem occured
     */
    public abstract void initialise(Connection mapped) throws IOException;
    protected abstract InputStream openInputStream() throws IOException;
    protected abstract OutputStream openOutputStream() throws IOException;
}
