/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Java6-Platform.
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
package de.jiac.micro.performance;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaCompiler.CompilationTask;

import org.slf4j.LoggerFactory;

import de.jiac.micro.cl.ClassPath;
import de.jiac.micro.cl.ContainerClassLoader;
import de.jiac.micro.config.generator.AbstractConfiguration;
import de.jiac.micro.config.generator.ConfigurationGenerator;
import de.jiac.micro.config.generator.NodeConfiguration;
import de.jiac.micro.core.IScope;
import de.jiac.micro.internal.ByteArrayJavaFileCache;
import de.jiac.micro.internal.compile.ByteArrayJavaFileObject;
import de.jiac.micro.internal.compile.ByteFileManager;
import de.jiac.micro.internal.compile.CharSequenceJavaFileObject;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class TestLauncherWithCompilation {
    private static class ConfigurationDiagnosticListener implements DiagnosticListener<JavaFileObject> {
        protected int errors= 0;
        
        protected ConfigurationDiagnosticListener() {}
        
        public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
            if(diagnostic.getKind() == Kind.ERROR) {
                errors++;
                System.err.println(diagnostic.getMessage(null));
            }
        }
    }
    
    static void execute(String[] args) {
        if(args.length != 1) {
            System.out.println("usage: " + TestLauncherWithCompilation.class.getName() + " namespace");
            System.exit(0);
            return;
        }

        String namespace= args[0];
        AbstractConfiguration[] configs;
        try {
            configs= ConfigurationGenerator.execute(namespace, TestLauncherWithCompilation.class.getClassLoader(), LoggerFactory.getLogger(TestLauncherWithCompilation.class));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("namespace does not contain a valid application definition: " + e.getMessage());
            System.exit(1);
            return;
        }
        
        Map<String, ByteArrayJavaFileObject> classes;
        final List<NodeConfiguration> nodes= new ArrayList<NodeConfiguration>();
        
        try {
            JavaCompiler compiler= ToolProvider.getSystemJavaCompiler();
            ArrayList<JavaFileObject> sources= new ArrayList<JavaFileObject>();
            for(AbstractConfiguration config : configs) {
                sources.add(new CharSequenceJavaFileObject(config.className, config.source));
                
                if(config instanceof NodeConfiguration) {
                    nodes.add((NodeConfiguration) config);
                }
            }
            
            StandardJavaFileManager stdFileManager= compiler.getStandardFileManager(null, null, null);
            ByteFileManager<StandardJavaFileManager> byteFileManager= new ByteFileManager<StandardJavaFileManager>(stdFileManager);
            ConfigurationDiagnosticListener listener= new ConfigurationDiagnosticListener();
            CompilationTask task= compiler.getTask(null, byteFileManager, listener, null, null, sources);
            task.call();
            byteFileManager.close();
            classes= byteFileManager.getStore();
            
            if(listener.errors > 0) {
                System.err.println("could not compile node configurations");
                System.exit(1);
                return;
            }
            
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.err.println("could not compile node configurations");
            System.exit(1);
            return;
        }
        
        if(nodes.size() != 1) {
            System.err.println("exactly one node has to be configured");
            System.exit(1);
        } else {
            // start the node in the current vm
            launchInCurrentVM(nodes.get(0).className, classes);
        }
    }
    
    private static void launchInCurrentVM(final String nodeConfigClassName, final Map<String, ByteArrayJavaFileObject> classes) {
        ByteArrayJavaFileCache cache= new ByteArrayJavaFileCache(classes.values());
        ClassPath cp= new ClassPath();
        
        for(URL url : cache.getURLs()) {
            cp.addURL(url);
        }
        
        // FIXME: this class loader sucks... create it more specific!!!
        final ContainerClassLoader nodeLoader= new ContainerClassLoader(cp, TestLauncherWithCompilation.class.getClassLoader());
        
        try {
            instantiateAndLaunch(nodeLoader, nodeConfigClassName);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.err.println("could not launch node in current vm");
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
}
