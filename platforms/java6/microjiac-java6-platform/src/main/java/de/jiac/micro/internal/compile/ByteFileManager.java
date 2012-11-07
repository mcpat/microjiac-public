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
package de.jiac.micro.internal.compile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ByteFileManager<M extends JavaFileManager> extends ForwardingJavaFileManager<M> {
    private final HashMap<String, ByteArrayJavaFileObject> _store;
    
    public ByteFileManager(M fileManager) {
        super(fileManager);
        _store= new HashMap<String, ByteArrayJavaFileObject>();
    }

    @Override
    @SuppressWarnings("unused")
    public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) {
        ByteArrayJavaFileObject bajfo= new ByteArrayJavaFileObject(className, kind);
        _store.put(className, bajfo);
        return bajfo;
    }
    
    public Map<String, ByteArrayJavaFileObject> getStore() {
        return Collections.unmodifiableMap(_store);
    }
}
