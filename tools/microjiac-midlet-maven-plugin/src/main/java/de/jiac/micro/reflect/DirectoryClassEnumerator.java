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
import java.util.*;

/**
 * This enumerator browses through the given class directory tree and loads all classes this structure contains.
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class DirectoryClassEnumerator implements Enumeration { 
// Enumeration<Class> {
    /**
     * Utility class that allows us to walk through the folder
     * structure without recursion.
     */
    protected class ClassFolder {
        private ClassFolder _parent;
        private File _file;
        private String _package;

        private Enumeration<File> _files;
        private Enumeration<ClassFolder> _folders;
        
        public ClassFolder(File file) {
            this(null, file);
        }

        public ClassFolder(ClassFolder parent, File file) {
            _parent = parent;
            _file = file;
            initialise();
        }
        
        public void initialise() {
            File[] children= _file.listFiles();
            List<File> classFiles= new ArrayList<File>();
            List<ClassFolder> folders= new ArrayList<ClassFolder>();
            for(File child : children) {
                if(child.isDirectory()) {
                    folders.add(new ClassFolder(this, child));
                } else if(child.getName().endsWith(ClassCollector.CLASS_SUFFIX)){
                    classFiles.add(child);
                }
            }
            _files= Collections.enumeration(classFiles);
            _folders= Collections.enumeration(folders);
        }
        
        public ClassFolder getParent() {
            return _parent;
        }

        public String getPackage() {
            if (_package == null) {
                StringBuilder builder = new StringBuilder();
                insertPackage(builder);
                _package = builder.toString();
            }

            return _package;
        }

        public Enumeration<ClassFolder> getFolders() {
            return _folders;
        }

        public Enumeration<File> getFiles() {
            return _files;
        }

        protected void insertPackage(StringBuilder builder) {
            if (_parent != null) {
                _parent.insertPackage(builder);
                
                if(builder.length() > 0) {
                    builder.append('.');
                }
                builder.append(_file.getName());
            }
        }
    }

    private File _root;
    private ClassLoader _loader;
    private ClassFolder _currentFolder;
    private Class _current;

    public DirectoryClassEnumerator(File rootFolder, ClassLoader loader) {
        if(!rootFolder.exists() || !rootFolder.isDirectory()) {
            throw new IllegalArgumentException("rootFolder have to be an existing directory");
        }
        _root = rootFolder;
        _loader = loader;
    }

    public boolean hasMoreElements() {
        if (_current != null) {
            return true;
        }

        return loadNext();
    }

    public Class nextElement() {
        if (!hasMoreElements()) {
            throw new NoSuchElementException("DirectoryClassEnumerator");
        }

        try {
            return _current;
        } finally {
            _current = null;
        }
    }

    private boolean loadNext() {
        if(_root == null) {
            return false;
        }
        
        if(_currentFolder == null) {
            _currentFolder= new ClassFolder(_root);
        }
        
        while(_currentFolder.getFiles().hasMoreElements() || _currentFolder.getFolders().hasMoreElements() || _currentFolder.getParent() != null) {
            while(_currentFolder.getFiles().hasMoreElements()) {
                File classFile= _currentFolder.getFiles().nextElement();
                String fileName= classFile.getName();
                fileName= fileName.substring(0, fileName.length() - ClassCollector.CLASS_SUFFIX.length());
                String packageName= _currentFolder.getPackage();
                
                String className= packageName.length() > 0 ? packageName + '.' + fileName : fileName;
                
                try {
                    _current= Class.forName(className, false, _loader);
                    return true;
                } catch (Exception e) {
                    // fall through
                }
            }
            
            if(_currentFolder.getFolders().hasMoreElements()) {
                _currentFolder= _currentFolder.getFolders().nextElement();
            } else if(_currentFolder.getParent() != null){
                _currentFolder= _currentFolder.getParent();
            } else {
                break;
            }
        }
        
        _root= null;
        return false;
    }
}
