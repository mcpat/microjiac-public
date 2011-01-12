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
package de.jiac.micro.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import de.jiac.micro.cl.ClassPath;
import de.jiac.micro.cl.ContainerClassLoader;
import de.jiac.micro.cl.IClassTransformer;
import de.jiac.micro.core.IScope;
import de.jiac.micro.internal.Options.Option;

/**
 * Launcher for CDC and higher JAVA editions.
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public final class NodeLauncher {
    public static void main(String[] args) throws Exception {
        Options opts= new Options();
        Option configOpt= opts.createOption("c", "config", "full qualified classname for the node configuration");
        Option classPathOpt= opts.createOption("p", "classpath", "search path for classes");
        Option classPathFileOpt= opts.createOption("f", "classpathfile", "text file with classpath entry per line");
        Option transformerOpt= opts.createOption("t", "transformer", "full qualified classname of the transformer");
        opts.load(args);
        
        String classPathStr= classPathOpt.getValue();
        String classPathFile= classPathFileOpt.getValue();
        String className= configOpt.getValue();
        String transformerName= transformerOpt.getValue();

        if(className == null) {
            opts.printUsage(NodeLauncher.class.getName());
            System.exit(1);
        }
        
        final ClassPath classPath;
        
        if(classPathStr != null || classPathFile == null) {
            classPath= buildClassPath(classPathStr);
        } else {
            classPath= buildClassPathFromFile(classPathFile);
        }
        
        final IClassTransformer transformer;
        if(transformerName != null) {
            IClassTransformer instance;
            try {
                Class transformerClass= NodeLauncher.class.getClassLoader().loadClass(transformerName);
                instance= (IClassTransformer) transformerClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Could not load transformer. Will proceed without it...");
                instance= null;
            }
            
            transformer= instance;
        } else {
            transformer= null;
        }
        
        final ContainerClassLoader classLoader= (ContainerClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return new ContainerClassLoader(classPath, ClassLoader.getSystemClassLoader(), transformer);
            }
        });
        
        try {
            instantiateAndLaunch(classLoader, className);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            System.exit(1);
            return;
        }
    }
    
    protected static void instantiateAndLaunch(final ContainerClassLoader loader, final String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class nodeScopeClass= loader.loadClass(IScope.NODE_SCOPE_CLASS);
        final IScope nodeScope= (IScope) nodeScopeClass.newInstance();
        System.out.println("initialising node scope");
        nodeScope.setup(loader, className, null);
        nodeScope.signal(IScope.SIG_START);
        
        Runtime.getRuntime().addShutdownHook(
            new Thread() {
                public void run() {
                    nodeScope.signal(IScope.SIG_TERMINATE);
                }
            }
        );
    }
    
    protected static ClassPath buildClassPath(String cpStr) {
        ClassPath cp= new ClassPath();
        
        if(cpStr != null) {
            final char separator= File.pathSeparatorChar;
            
            for(int end= 0, start= 0; (end= cpStr.indexOf(separator, start)) >= 0 || (end= cpStr.length()) > start; start= end + 1) {
                String fileName= cpStr.substring(start, end);
                
                File file= new File(fileName);
                if(file.exists()) {
                    cp.addFile(file);
                }
            }
        }
        
        return cp;
    }
    
    protected static ClassPath buildClassPathFromFile(String classPathFile) throws IOException {
        ClassPath cp= new ClassPath();
        File txtFile= new File(classPathFile);
        BufferedReader reader= new BufferedReader(new FileReader(txtFile));
        
        try {
            while(reader.ready()) {
                String entry= reader.readLine();
                File file= new File(entry);
                if(file.exists()) {
                    cp.addFile(file);
                }
            }
        } finally {
            try {
                reader.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        
        return cp;
    }
    
    private NodeLauncher() {}
}
