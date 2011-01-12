/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC CDC-Launcher.
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
package de.jiac.micro.cl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
final class LocalArchiveEntry extends ClassPathEntry {
    private final static URLStreamHandler ARCHIVE_URL_STREAM_HANDLER= new URLStreamHandler() {
        protected URLConnection openConnection(URL u) {
            String name= u.getFile().substring(1);
            ArrayList sequences= new ArrayList();
            
            int sequence= 0;
            for(int index= 0; (index= name.indexOf('!', sequence)) > 0; sequence= index + 1) {
                sequences.add(name.substring(sequence, index));
            }
            
            sequences.add(name.substring(sequence));
            String[] seqs= new String[sequences.size()];
            sequences.toArray(seqs);
            return new ArchiveURLConnection(u, seqs);
        }
    };
    
    private static class ArchiveURLConnection extends URLConnection {
        private final String[] _sequences;
        private byte[] _content;
        private int _contentLength;
        private long _lastModified;
        
        
        protected ArchiveURLConnection(URL url, String[] sequences) {
            super(url);
            _sequences= sequences;
        }
        
        public void connect() throws IOException {
            if(!connected) {
                
                ZipInputStream zipInput= new ZipInputStream(new FileInputStream(_sequences[0]));
                ByteArrayOutputStream content= new ByteArrayOutputStream(0);
                long lm= 0;
                byte[] readBuffer= new byte[128];
                try {
                    walkDeeper: for(int s= 1; s < _sequences.length; ++s) {
                        if(zipInput == null) {
                            zipInput= new ZipInputStream(new ByteArrayInputStream(content.toByteArray(), 0, content.size()));
                        }
                        
                        for(ZipEntry ze; (ze= zipInput.getNextEntry()) != null;) {
                            if(ze.getName().equals(_sequences[s])) {
                                lm= ze.getTime();
                                
                                int estimated= (int) ze.getSize();
                                if(estimated <= 0) {
                                    estimated= 1024;
                                }
                                content.reset();
                                
                                for(int numBytes= 0; (numBytes= zipInput.read(readBuffer, 0, readBuffer.length)) >= 0;) {
                                    content.write(readBuffer, 0, numBytes);
                                }
                                
                                try {
                                    zipInput.close();
                                    zipInput= null;
                                } catch (IOException ioe) {
                                    // ignore
                                }
                                
                                continue walkDeeper;
                            }
                        }
                        
                        throw new IOException("could not connect to url " + url.toString());
                    }
                } finally {
                    if(zipInput != null) {
                        try {
                            zipInput.close();
                        } catch (IOException ioe) {
                            // ignore
                        }
                        zipInput= null;
                    }
                }
                
                _lastModified= lm;
                _content= content.toByteArray();
                _contentLength= content.size();
                connected= true;
            }
        }

        public int getContentLength() {
            try {
                connect();
            } catch (IOException e) {
                return 0;
            }
            
            return _contentLength;
        }

        public InputStream getInputStream() throws IOException {
            connect();
            return new ByteArrayInputStream(_content);
        }

        public long getLastModified() {
            try {
                connect();
            } catch (IOException e) {
                return 0;
            }
            
            return _lastModified;
        }
    }
    
    private final static class InnerArchive {
        final String name;
        final byte[] content;
        
        InnerArchive(String name, byte[] content) {
            this.name= name;
            this.content= content;
        }
    }
    
    protected static URL createURL(List urlStack, String name) throws IOException {
        StringBuffer buffer= new StringBuffer();
        
        for(int i= 0; i < urlStack.size(); ++i) {
            buffer.append('!').append(urlStack.get(i));
        }
        
        buffer.append('!').append(name);
        
        return new URL(
            "archive",
            null,
            -1,
            buffer.toString(),
            ARCHIVE_URL_STREAM_HANDLER
        );
    }
    
    private static boolean findRecursive(ZipInputStream zipInput, List urlStack, String name, List urls, boolean one) throws IOException {
        ArrayList innerQueue= null;
        
        for(ZipEntry entry= null; (entry= zipInput.getNextEntry()) != null; ) {
            if(entry.isDirectory()) {
                continue;
            }
            
            String en= entry.getName();
            if(en.equals(name)) {
                urls.add(createURL(urlStack, name));
                
                if(one) {
                    return true;
                }
            } else if(en.indexOf('/') < 0 && isArchive(en)) {
                // top level inner archive
                if(innerQueue == null) {
                    innerQueue= new ArrayList();
                }
                
                // TODO: may fail, getSize is not always correct
                byte[] buffer= new byte[(int) entry.getSize()];
                for(int i= 0, off= 0; i < buffer.length; i+= zipInput.read(buffer, off, buffer.length - i));
                innerQueue.add(new InnerArchive(en, buffer));
            }
        }
        
        while(innerQueue != null && innerQueue.size() > 0) {
            InnerArchive innerArchive= (InnerArchive) innerQueue.remove(0);
            ZipInputStream innerZipInput= new ZipInputStream(new ByteArrayInputStream(innerArchive.content));
            urlStack.add(innerArchive.name);
            try {
                if(findRecursive(innerZipInput, urlStack, name, urls, one) && one) {
                    return true;
                }
            } finally {
                urlStack.remove(urlStack.size() - 1);
                close(innerZipInput);
            }
        }
        
        return false;
    }
    
    private static void close(InputStream in) {
        try {
            in.close();
        } catch (Exception e) {
            // ignore;
        }
    }
    
    protected static boolean isArchive(String name) {
        // TODO: we should look into the file to determine if it is an archive or not!
        int p= name.lastIndexOf('.');
        
        if(p > 0) {
            String suffix= name.substring(p + 1);
            
            if(suffix.equalsIgnoreCase("jar") || suffix.equalsIgnoreCase("zip")) {
                return true;
            }
        }
        
        return false;
    }
    
    private final File _archiveFile;
    
    /*package*/ LocalArchiveEntry(File archiveFile) {
        _archiveFile= archiveFile;
    }
    
    protected URL findResource(String name) throws IOException {
        ArrayList urls= new ArrayList();
        findRecursive(name, urls, true);
        return urls.size() <= 0 ? null : (URL) urls.get(0);
    }

    protected void findResources(String name, List urls) throws IOException {
        findRecursive(name, urls, false);
    }
    
    private void findRecursive(String name, List urls, boolean one) throws IOException {
        ArrayList urlStack= new ArrayList();
        ZipInputStream zipInput= new ZipInputStream(new FileInputStream(_archiveFile));
        urlStack.add(_archiveFile.getAbsolutePath());
        findRecursive(zipInput, urlStack, name, urls, one);
        close(zipInput);
    }
}
