/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC MIDlet-Maven-Plugin.
 *
 * Copyright (c) 2007-2012 DAI-Labor, Technische Universität Berlin
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
package de.jiac.micro.util;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.Enumeration;

/**
 * This is a modified version of the standard {@link URLClassLoader}. It does not delegate the
 * search for resources to the parent classloader. This behaviour is required by the configuration generator
 * to avoid finding the same xml files multiple times.
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ModifiedURLClassLoader extends URLClassLoader {
    public ModifiedURLClassLoader(URL[] urls) {
        super(urls, ModifiedURLClassLoader.class.getClassLoader());
    }

    public ModifiedURLClassLoader(URL[] urls, URLStreamHandlerFactory factory) {
        super(urls, ModifiedURLClassLoader.class.getClassLoader(), factory);
    }

    public URL getResource(String name) {
        return findResource(name);
    }

    public Enumeration<URL> getResources(String name) throws IOException {
        return findResources(name);
    }
}
