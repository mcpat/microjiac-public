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
import java.util.List;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
/*package*/ final class LocalFolderEntry extends ClassPathEntry {
    private final File _rootFolder;
    
    /*package*/ LocalFolderEntry(File rootFolder) {
        _rootFolder= rootFolder;
    }

    protected URL findResource(String name) throws IOException {
        File folder= getFolder(name);
        
        if(folder == null) {
            return null;
        }
        
        int lastSlash= name.lastIndexOf('/');
        if(lastSlash > 0) {
            String fileName= name.substring(lastSlash + 1);
            
            File file= new File(folder, fileName);
            if(file.exists() && file.isFile()) {
                // we have to use this method because of Java 1.2 compatibility
                return file.toURL();
            }
        }
        
        return null;
    }

    protected void findResources(String name, List urls) throws IOException {
        // there is at most one file with the given name
        URL file= findResource(name);
        
        if(file != null) {
            urls.add(file);
        }
    }
    
    private File getFolder(String name) {
        int slash= name.lastIndexOf('/');
        
        if(slash > 0) {
            String path= name.substring(0, slash);
            path= path.replace('/', File.separatorChar);
            
            File folder= new File(_rootFolder, path);
            if(folder.exists() && folder.isDirectory()) {
                return folder;
            }
            return null;
        } else {
            return _rootFolder;
        }
    }
}
