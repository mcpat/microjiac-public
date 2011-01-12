/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC MIDlet-Maven-Plugin.
 *
 * Copyright (c) 2007-2011 DAI-Labor, Technische Universität Berlin
 *
 * This library includes software developed at DAI-Labor, Technische
 * Universität Berlin (http://www.dai-labor.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

/*
 * $Id$ 
 */
package de.jiac.micro.reflect;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This enumerator browses through the given archive file
 * (*.zip or *.jar) and loads all classes it contains.
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ArchiveClassEnumerator implements Enumeration { 
// Enumeration<Class> {
    private ZipFile _zipFile;
    
    private File _file;
    private ClassLoader _loader;
    
    private Enumeration<ZipEntry> _entries;
    private Class _current;
    
    public ArchiveClassEnumerator(File archiveFile, ClassLoader loader) {
        if(!archiveFile.exists() || !archiveFile.isFile()) {
            throw new IllegalArgumentException("archiveFile have to be an existing file");
        }
        
        String name= archiveFile.getName();
        if(!(name.endsWith(".zip") || name.endsWith(".jar"))) {
            throw new IllegalArgumentException("archiveFile have to be a zip or jar archive");
        }
        
        _file= archiveFile;
        _loader= loader;
    }
    
    public boolean hasMoreElements() {
        if(_current != null) {
            return true;
        }
        
        return loadNext();
    }

    public Class nextElement() {
        if(!hasMoreElements()) {
            throw new NoSuchElementException("ArchiveClassEnumerator");
        }
        
        try {
            return _current;
        } finally {
            _current= null;
        }
    }
    
    private boolean loadNext() {
        if(_file == null) {
            return false;
        }
        
        if(_zipFile == null) {
            try {
                _zipFile= new ZipFile(_file);
            } catch (IOException ioe) {
                return false;
            }
            
            _entries= (Enumeration<ZipEntry>)_zipFile.entries();
        }
        
        while(_entries.hasMoreElements()) {
            ZipEntry current= _entries.nextElement();
            String name= current.getName();
            if(name.endsWith(ClassCollector.CLASS_SUFFIX)) {
                try {
                    name= name.substring(0, name.length() - ClassCollector.CLASS_SUFFIX.length());
                    name= name.replace('/', '.');
                    _current= Class.forName(name, false, _loader);
                    return true;
                } catch (Exception e) {
                    // ignore this
//                    e.printStackTrace();
                }
            }
        }
        
        // zipFile has no more entries -> close it
        try {_zipFile.close();} catch (IOException ioe) {}
        _entries= null;
        _file= null;
        
        return false;
    }
}
