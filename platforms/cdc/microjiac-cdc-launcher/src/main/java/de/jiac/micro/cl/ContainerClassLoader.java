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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ContainerClassLoader extends ClassLoader implements ITransformingClassLoader {
    private final ClassPath _classPath;
    private final IClassTransformer _transformer;
    
    private final ArrayList _loaderHierarchy;
    
    public ContainerClassLoader(ClassPath classPath, ClassLoader parent, IClassTransformer transformer) {
        super(parent);
        _classPath = classPath;
        _transformer= transformer;
        
        _loaderHierarchy= new ArrayList();
        for(ClassLoader current= this; current != null; current= current.getParent()) {
            _loaderHierarchy.add(current);
        }
    }
    
    public ContainerClassLoader(ClassPath classPath, ClassLoader parent) {
        this(classPath, parent, null);
    }
    
    public ContainerClassLoader(ClassPath classPath) {
        this(classPath, null, null);
    }
    
    public ClassPath getClassPath() {
        return _classPath;
    }
    
    public final synchronized boolean isTransformerForClass(IClassTransformer transformer, String className) {
        final String resourceName= className.replace('.', '/').concat(".class");
        ClassLoader responsibleLoader= null;
        for(int i= _loaderHierarchy.size() - 1; i >= 0; --i) {
            ClassLoader loader= (ClassLoader) _loaderHierarchy.get(i);
            if(loader.getResource(resourceName) != null) {
                responsibleLoader= loader;
                break;
            }
        }
        
        if(responsibleLoader instanceof ITransformingClassLoader) {
            return ((ITransformingClassLoader) responsibleLoader).hasTransformer(transformer);
        }
        
        return false;
    }
    
    public boolean hasTransformer(IClassTransformer transformer) {
        return transformer != null && _transformer == transformer;
    }

    protected Class findClass(String name) throws ClassNotFoundException {
        String path = name.replace('.', '/').concat(".class");
        URL url;
        try {
            url= _classPath.findResource(path);
        } catch (IOException ioe) {
            throw new ClassNotFoundException(name, ioe);
        }

        if (url != null) {
            try {
                return defineClass(name, url);
            } catch (IOException e) {
                throw new ClassNotFoundException(name, e);
            }
        }

        throw new ClassNotFoundException(name);
    }

    protected URL findResource(String name) {
        try {
            return _classPath.findResource(name);
        } catch (IOException ioe) {
            return null;
        }
    }

    protected Enumeration findResources(String name) throws IOException {
        return _classPath.findResources(name);
    }
    
    private Class defineClass(String name, URL url) throws IOException {
        InputStream input= url.openStream();
        
        if(input != null) {
            ByteArrayOutputStream buffer= new ByteArrayOutputStream();
            byte[] readBuffer= new byte[128];
            
            for(int numBytes= 0; (numBytes= input.read(readBuffer)) > 0;) {
                buffer.write(readBuffer, 0, numBytes);
            }
            
            int len= buffer.size();
            byte[] classContent= buffer.toByteArray();
            if(_transformer != null) {
                classContent= _transformer.transformClass(this, classContent , 0, len);
                len= classContent.length;
            }
            
            return defineClass(name, classContent, 0, len);
        }
        
        throw new IOException("could not open inputstream for " + url.toString());
    }
}
