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
package de.jiac.micro.performance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import de.jiac.micro.cl.ClassPath;
import de.jiac.micro.cl.ContainerClassLoader;
import de.jiac.micro.core.IScope;
import de.jiac.micro.internal.Options;
import de.jiac.micro.internal.Options.Option;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class TestLauncherPreCompiled {
    public static void execute(String[] args) throws Exception {
        Options opts= new Options();
        Option configOpt= opts.createOption("c", "config", "full qualified classname for the node configuration");
        Option classPathOpt= opts.createOption("p", "classpath", "search path for classes");
        Option classPathFileOpt= opts.createOption("f", "classpathfile", "text file with classpath entry per line");
        opts.load(args);
        
        String classPathStr= classPathOpt.getValue();
        String classPathFile= classPathFileOpt.getValue();
        String className= configOpt.getValue();

        if(className == null) {
            opts.printUsage(TestLauncherPreCompiled.class.getName());
            System.exit(1);
        }
        
        ClassPath classPath;
        
        if(classPathStr != null || classPathFile == null) {
            classPath= buildClassPath(classPathStr);
        } else {
            classPath= buildClassPathFromFile(classPathFile);
        }
        
        ContainerClassLoader classLoader= new ContainerClassLoader(classPath, ClassLoader.getSystemClassLoader());
        try {
            instantiateAndLaunch(classLoader, className);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return;
        }

    }
    
    protected static void instantiateAndLaunch(final ContainerClassLoader loader, final String className) throws Exception {
        Class nodeScopeClass= loader.loadClass(IScope.NODE_SCOPE_CLASS);
        final IScope nodeScope= (IScope) nodeScopeClass.newInstance();
        System.out.println("initialising node scope");
        final Object startLock= new Object();
        
        final IScope.IScopeStateChangeListener l= new IScope.IScopeStateChangeListener() {
            public void onPause(IScope scope) {
                
            }

            public void onSetup(IScope scope) {
                
            }

            public void onStart(IScope scope) {
                synchronized (startLock) {
                    startLock.notify();
                }
            }

            public void onTerminate(IScope scope) {
                
            }
        };
        
        nodeScope.setup(loader, className, l);
        
        synchronized (startLock) {
            nodeScope.signal(IScope.SIG_START);
            startLock.wait();
        }
        
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
        
        while(reader.ready()) {
            String entry= reader.readLine();
            File file= new File(entry);
            if(file.exists()) {
                cp.addFile(file);
            }
        }
        
        return cp;
    }

}
