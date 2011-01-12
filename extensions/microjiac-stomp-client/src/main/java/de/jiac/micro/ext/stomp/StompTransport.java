/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC STOMP-Client.
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
 * $Id: StompTransport.java 23214 2009-05-06 12:18:42Z marcel $ 
 */
package de.jiac.micro.ext.stomp;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.github.libxjava.concurrent.AbstractSingleThreadRunner;
import com.github.libxjava.io.ByteArrayOutputBuffer;
import com.github.libxjava.util.SerialisableHashtable;

import de.jiac.micro.core.handle.IResourceHandle;
import de.jiac.micro.core.io.IMessage;
import de.jiac.micro.core.io.IStreamConnection;
import de.jiac.micro.core.scope.AbstractScopeAwareRunner;
import de.jiac.micro.core.scope.Scope;
import de.jiac.micro.internal.io.Message;

/**
 * Implementation of the STOMP protocol (client side) as a MicroJIAC transport.
 * 
 * @author Marcel Patzlaff
 * @version $Revision: 23214 $
 */
public final class StompTransport extends Transport {
    public static final String GROUP_PREFIX= "G#";
    public static final String MBOX_PREFIX= "M#";
    
    public static final String PREFIX_HEADER= "stomp_prefix";
    public static final String DESTINATION_HEADER= "stomp_destination";
    
    /*package*/ static final byte[] TOPIC_BYTES;
    /*package*/ static final byte[] QUEUE_BYTES;
    private static final byte[] CMD_SUBSCRIBE;
    private static final byte[] CMD_UNSUBSCRIBE;
    private static final byte[] CMD_SEND;
    private static final byte[] CMD_CONNECT;
    private static final byte[] CMD_DISCONNECT;
    /*package*/ static final byte[] NO_CONTENT;
    /*package*/ static final String STOMP_DESTINATION= "destination";

    private final static Vector IGNORE_HEADERS;

    static {
        IGNORE_HEADERS = new Vector();
        IGNORE_HEADERS.addElement(STOMP_DESTINATION);
        IGNORE_HEADERS.addElement("receipt");
        IGNORE_HEADERS.addElement("content-length");
        IGNORE_HEADERS.addElement(PREFIX_HEADER);
        IGNORE_HEADERS.addElement(DESTINATION_HEADER);

        TOPIC_BYTES = new byte[] {(byte)'/', (byte)'t', (byte)'o', (byte)'p', (byte)'i', (byte)'c', (byte)'/'};
        QUEUE_BYTES = new byte[] {(byte)'/', (byte)'q', (byte)'u', (byte)'e', (byte)'u', (byte)'e', (byte)'/'};

        CMD_SUBSCRIBE = new byte[] {(byte)'S', (byte)'U', (byte)'B', (byte)'S', (byte)'C', (byte)'R', (byte)'I', (byte)'B', (byte)'E'};
        CMD_UNSUBSCRIBE = new byte[] {(byte)'U', (byte)'N', (byte)'S', (byte)'U', (byte)'B', (byte)'S', (byte)'C', (byte)'R', (byte)'I', (byte)'B', (byte)'E'};
        CMD_SEND = new byte[] {(byte)'S', (byte)'E', (byte)'N', (byte)'D'};
        CMD_CONNECT= new byte[] {(byte)'C', (byte)'O', (byte)'N', (byte)'N', (byte)'E', (byte)'C', (byte)'T'};
        CMD_DISCONNECT= new byte[] {(byte)'D', (byte)'I', (byte)'S', (byte)'C', (byte)'O', (byte)'N', (byte)'N', (byte)'E', (byte)'C', (byte)'T'};
        NO_CONTENT= new byte[0];
    }
    
    /*package*/ final class Receiver extends AbstractScopeAwareRunner {
        private StringBuffer _firstBuffer= new StringBuffer();
        private StringBuffer _secondBuffer= new StringBuffer();
        private ByteArrayOutputBuffer _binaryBuffer= new ByteArrayOutputBuffer();
        
        public Receiver() {
            super("stomp-receiver");
        }
        
        protected void doRun() {
            final IStreamConnection connection= _connection;
            try {
                while(!isCancelled() && connection != null && connection == _connection) {
                    internalReceive();
                }
            } catch (Exception e) {
                if(connection == _connection)
                delegate.onError(StompTransport.this, e);
            }
        }
        
        protected void internalReceive() throws IOException {
            ensureOpen();
            
            InputStream in= _connection.getInputStream();
            synchronized(in) {
                SerialisableHashtable headers= new SerialisableHashtable();
                String command= null;
                String destPrefix= null;
                String destName= null;
                byte[] content= NO_CONTENT; 
                int ch;
                
                _firstBuffer.setLength(0);
                _secondBuffer.setLength(0);
                StringBuffer strBuffer= _firstBuffer;
                boolean headersClosed= false;
                boolean headersOpen= false;
                
                // read header section -> null bytes are normal values here
                for(ch= in.read();; ch= in.read()) {
                    if(ch < 0) {
                        throw new EOFException("unexpected end of stream");
                    }
                    
                    switch (ch) {
                        case '\n': {
                            if(_firstBuffer.length() <= 0) {
                                if(headersOpen) {
                                    headersClosed= true;
                                }
                            } else {
                                headersOpen= true;
                                String key= dropWhiteSpaces(_firstBuffer).toString();
                                
                                if(_secondBuffer.length() <= 0 && command == null) {
                                    command= key;
                                } else {
                                    dropWhiteSpaces(_secondBuffer);
                                    
                                    if(key.equals(STOMP_DESTINATION)) {
                                        // convert STOMP destination to MicroJIAC destination
                                        if(_secondBuffer.charAt(1) == 't') {
                                            _secondBuffer.delete(0, TOPIC_BYTES.length);
                                            destPrefix= GROUP_PREFIX; 
                                        } else {
                                            _secondBuffer.delete(0, QUEUE_BYTES.length);
                                            destPrefix= MBOX_PREFIX;
                                        }
                                        
                                        destName= _secondBuffer.toString().toLowerCase();
                                    } else {
                                        headers.put(key, _secondBuffer.toString());
                                    }
                                }
                                
                                _firstBuffer.setLength(0);
                                _secondBuffer.setLength(0);
                            }
                            
                            strBuffer= _firstBuffer;
                            break;
                        }
                        
                        case ':': {
                            if(strBuffer == _firstBuffer) {
                                strBuffer= _secondBuffer;
                                break;
                            }
                            
                            // fall through if we are in second buffer
                        }

                        default: {
                            strBuffer.append((char)ch);
                        }
                    }
                    
                    if(headersOpen && headersClosed) {
                        break;
                    }
                }
                
                String contentLength= (String) headers.get("content-length");
                if(contentLength != null) {
                    try {
                        int toRead= Integer.parseInt(contentLength);
                        
                        if(toRead < 0) {
                            throw new IOException("specified content-length '" + contentLength + "' is invalid");
                        } else if(toRead > 0) {
                            content= new byte[toRead];
                            
                            for(int offset= 0; toRead > 0;) {
                                int numBytes= in.read(content, offset, toRead);
                                
                                if(numBytes < 0) {
                                    throw new EOFException("unexpected end-of-stream: needed to read '" + toRead + "' more bytes");
                                }
                                
                                offset+= numBytes;
                                toRead-= numBytes;
                            }
                        }
                        
                        // check and consume trailing null byte
                        if(in.read() != 0) {
                            throw new IOException("content-length bytes read but there was no trailing null byte");
                        }
                    } catch (NumberFormatException nfe) {
                        throw new IOException("specified content-length '" + contentLength + "' is no valid integer");
                    }
                } else {
                    _binaryBuffer.reset();
                    
                    // read content until null byte is consumed
                    while((ch= in.read()) != 0) {
                        _binaryBuffer.write(ch);
                    }
                    
                    content= _binaryBuffer.toByteArray();
                }
                
                receive(command, destPrefix, destName, headers, content);
            }
        }
        
        protected StringBuffer dropWhiteSpaces(StringBuffer buffer) {
            char ch;
            int offset;
            
            // remove whitespaces at head
            offset= -1;
            for(int i= 0; i < buffer.length(); ++i) {
                ch= buffer.charAt(i);
                if(ch == ' ' || ch == '\r' || ch == '\n') {
                    offset= i;
                } else {
                    break;
                }
            }
            
            if(offset >= 0) {
                buffer.delete(0, offset + 1);
            }
            
            // remove whitespace at tail
            offset= -1;
            for(int i= buffer.length() - 1; i >= 0; --i) {
                ch= buffer.charAt(i);
                if(ch == ' ' || ch == '\r' || ch == '\n') {
                    offset= i;
                } else {
                    break;
                }
            }
            
            if(offset >= 0) {
                buffer.delete(offset, buffer.length());
            }
            
            return buffer;
        }
    }
    
    private final Hashtable _loginData= new Hashtable();
    private final Hashtable _subscribeData= new Hashtable();
    private final Receiver _receiver= new Receiver();

    private String _serverURL= null;
    private ContentTransformer _transformer;
    /*package*/ volatile IStreamConnection _connection = null;

    public StompTransport() {
        _subscribeData.put("activemq.noLocal", "true");
    }
    
    // === Methods from Transport === //
    public void doRegister(String prefix, String name) throws IOException {
        transmit(CMD_SUBSCRIBE, prefix, name, _subscribeData, null);
    }

    public void doSend(IMessage m) throws IOException {
        String prefix= m.getHeader(PREFIX_HEADER);
        String destination= m.getHeader(DESTINATION_HEADER);
        Message message= (Message) m;
        transmit(CMD_SEND, prefix, destination, message.getHeaders(), _transformer.toByteArray(message.getContent()));
    }

    public void doUnregister(String prefix, String name) throws IOException {
        transmit(CMD_UNSUBSCRIBE, prefix, name, null, null);
    }
    
    public synchronized void doStart() throws IOException {
        if(_serverURL == null) {
            throw new IOException("server URL is not set");
        }
        
        if(_connection != null) {
            doStop();
        }
        _transformer= new ContentTransformer(delegate.getClassLoader());
        
        IResourceHandle rh= (IResourceHandle) Scope.getContainer().getHandle(IResourceHandle.class);
        _connection= rh.openStreamConnection(_serverURL);
        delegate.getLogger().debug("stomp: open connection to '" + _serverURL + "'");
        // initialise the connection
        transmit(CMD_CONNECT, null, null, _loginData, null);
        
        try {
            _receiver.start();
            _receiver.waitForState(2000, AbstractSingleThreadRunner.STARTED);
        } catch (InterruptedException ie) {
            delegate.getLogger().error("stomp: could not start receiver", ie);
        }
    }

    // === Initialisation Methods === //

    public synchronized void doStop() {
        if(_connection != null) {
            delegate.getLogger().debug("stomp: closing connection to remote broker");
            try {transmit(CMD_DISCONNECT, null, null, null, null);} catch (IOException ioe) {/* ignore it */}
            final IStreamConnection c= _connection;
            _connection= null;
            c.close();
            
            try {
                _receiver.stop();
                _receiver.waitForState(2000, AbstractSingleThreadRunner.STOPPED);
            } catch (InterruptedException ie) {
                delegate.getLogger().warn("stomp: could not stop receiver", ie);
            }
            
            _transformer= null;
        }
    }
    
    /**
     * The server URL must be set before {@link #doStart()} is called.
     * It specifies the URL where the remote broker runs.
     * 
     * @param url       the URL of the remote broker
     */
    public void setServerURL(String url) {
        _serverURL= url;
    }
    
    public void setLogin(String login) {
        if(login == null) {
            _loginData.remove("login");
        } else {
            _loginData.put("login", login);
        }
    }
    
    public void setPasscode(String passcode) {
        if(passcode == null) {
            _loginData.remove("passcode");
        } else {
            _loginData.put("passcode", passcode);
        }
    }

    protected void receive(String command, String destPrefix, String destName, SerialisableHashtable headers, byte[] content) {
        if("MESSAGE".equalsIgnoreCase(command)) {
            try {
                IMessage message= new Message(headers, _transformer.toObject(content));
                delegate.onMessage(this, message, destName);
            } catch (Exception e) {
                delegate.onError(this, e);
            }
        }
    }

    /*package*/ void ensureOpen() throws IOException {
        if (_connection == null) {
            throw new IOException("connection is closed");
        }
    }

    private void transmit(byte[] command, String destPrefix, String destName, Hashtable headers, byte[] content) throws IOException {
        ensureOpen();

        OutputStream out = _connection.getOutputStream();
        synchronized (out) {
            out.write(command);
            out.write('\n');

            if (destPrefix != null) {
                out.write(STOMP_DESTINATION.getBytes());
                out.write(':');
                out.write(destPrefix == GROUP_PREFIX ? TOPIC_BYTES : QUEUE_BYTES);
                out.write(destName.getBytes());
                out.write('\n');
            }

            if (headers != null) {
                String key;
                String value;
                for (Enumeration keys = headers.keys(); keys.hasMoreElements();) {
                    key = keys.nextElement().toString();
                    if (!IGNORE_HEADERS.contains(key)) {
                        value = headers.get(key).toString();
                        out.write(key.getBytes());
                        out.write(':');
                        out.write(value.getBytes());
                        out.write('\n');
                    }
                }
            }

            if (content != null) {
                out.write("content-length:".getBytes());
                out.write(Integer.toString(content.length).getBytes());
                out.write('\n');
                out.write('\n');
                out.write(content);
            } else {
                out.write('\n');
            }

            out.write('\u0000');
            out.flush();
        }
    }
}
