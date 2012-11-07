/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Java6-Platform.
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
 * $Id$ 
 */
package de.jiac.micro.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import de.jiac.micro.internal.compile.ByteArrayJavaFileObject;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ByteArrayJavaFileCache {
    private final URLStreamHandler _byteArrayHandler;
    private class ByteArrayJavaFileURLConnection extends URLConnection {
        private String _key;
        private byte[] _content;
        
        public ByteArrayJavaFileURLConnection(URL url) {
            super(url);
        }
        
        @Override
        public void connect() throws IOException {
            if(!connected) {
                _key= url.getPath();
                ByteArrayJavaFileObject bajfo= fileCache.get(_key);
                
                if(bajfo == null) {
                    throw new IOException("could not connect to " + _key);
                }
                
                _content= bajfo.getByteArray();
                connected= true;
            }
        }

        @Override
        public InputStream getInputStream() throws IOException {
            connect();
            return new ByteArrayInputStream(_content);
        }

        @Override
        public int getContentLength() {
            try {
                connect();
                return _content.length;
            } catch (IOException e) {
                return -1;
            }
        }

        @Override
        public String getContentType() {
            return "application/java-byte-code";
        }
    }
    
    protected final HashMap<String, ByteArrayJavaFileObject> fileCache;
    
    public ByteArrayJavaFileCache(Collection<ByteArrayJavaFileObject> files) {
        _byteArrayHandler= new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) {
                return new ByteArrayJavaFileURLConnection(u);
            }
        };
        
        fileCache= new HashMap<String, ByteArrayJavaFileObject>();
        for(ByteArrayJavaFileObject bajfo : files) {
            fileCache.put(bajfo.toUri().getPath(), bajfo);
        }
    }
    
    public URL[] getURLs() {
        URL[] urls= new URL[fileCache.size()];
        Iterator<ByteArrayJavaFileObject> iter= fileCache.values().iterator();
        for(int i= 0; iter.hasNext(); ++i) {
            try {
                urls[i]= new URL(null, iter.next().toUri().toString(), _byteArrayHandler);
            } catch (MalformedURLException e) {
                // should not happen
                e.printStackTrace();
            }
        }
        return urls;
    }
}
