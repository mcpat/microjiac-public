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

import java.net.URL;
import java.util.List;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
/*package*/ final class URLFileEntry extends ClassPathEntry {
    private final URL _url;
    
    /*package*/ URLFileEntry(URL url) {
        _url= url;
    }
    
    protected URL findResource(String name) {
        if(itsMe(name)) {
            return _url;
        }
        return null;
    }

    protected void findResources(String name, List urls) {
        if(itsMe(name)) {
            urls.add(_url);
        }
    }
    
    private boolean itsMe(String name) {
        String pathStr= _url.getPath();
        
        if(pathStr.endsWith(name)) {
            return pathStr.length() - name.length() == (pathStr.startsWith("/") ? 1 : 0);
        }
        
        return false;
    }
}
