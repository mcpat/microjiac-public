/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC CDC-Launcher.
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
package de.jiac.micro.cl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public final class ClassPath {
    private static String stripLeadingSlash(String name) {
        if(name.charAt(0) == '/') {
            return name.substring(1);
        }
        
        return name;
    }
    
    private final ArrayList _entries= new ArrayList();
    
    public ClassPath() {}
    
    public void addFile(File file) {
        if(file.exists()) {
            if(file.isDirectory()) {
                _entries.add(new LocalFolderEntry(file));
                return;
            } else {
                String name= file.getName();
                
                if(LocalArchiveEntry.isArchive(name)) {
                    _entries.add(new LocalArchiveEntry(file));
                    return;
                }
            }
        }
        
        throw new IllegalArgumentException("invalid class path entry: " + file.toString());
    }
    
    public void addURL(URL url) {
        // TODO: also support remote archives???
        _entries.add(new URLFileEntry(url));
    }
    
    public URL findResource(String name) throws IOException {
        name= stripLeadingSlash(name);
        
        for(int i= 0; i < _entries.size(); ++i) {
            ClassPathEntry current= (ClassPathEntry) _entries.get(i);
            URL url = current.findResource(name);
            if(url != null) {
                return url;
            }
        }
        
        return null;
    }
    
    public Enumeration findResources(String name) throws IOException {
        name= stripLeadingSlash(name);
        
        ArrayList urls= new ArrayList();
        for(int i= 0; i < _entries.size(); ++i) {
            ClassPathEntry current= (ClassPathEntry) _entries.get(i);
            current.findResources(name, urls);
        }

        return Collections.enumeration(urls);
    }
}
