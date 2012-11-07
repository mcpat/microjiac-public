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
package de.jiac.micro.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaCompiler.CompilationTask;

import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.slf4j.LoggerFactory;

import de.jiac.micro.cl.ClassPath;
import de.jiac.micro.cl.ContainerClassLoader;
import de.jiac.micro.config.generator.AbstractConfiguration;
import de.jiac.micro.config.generator.ConfigurationGenerator;
import de.jiac.micro.config.generator.NodeConfiguration;
import de.jiac.micro.internal.compile.ByteArrayJavaFileObject;
import de.jiac.micro.internal.compile.ByteFileManager;
import de.jiac.micro.internal.compile.CharSequenceJavaFileObject;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public final class ApplicationLauncher {
    private static final class PrefixedConsumer implements StreamConsumer {
        private final PrintStream _out;
        private final String _prefix;
        
        PrefixedConsumer(PrintStream out, String prefix) {
            _out= out;
            _prefix= prefix;
        }
        
        public void consumeLine(String line) {
            _out.println("[" + _prefix + "]: " + line);
        }
    }
    
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
    
    /**
     * The main method of the launcher accepts one
     * namespace that contains an application definition.
     * 
     * @param args
     */
    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("usage: " + ApplicationLauncher.class.getName() + " namespace");
            System.exit(0);
            return;
        }

        // generate configuration sources
        String namespace= args[0];
        AbstractConfiguration[] configs;
        try {
            configs= ConfigurationGenerator.execute(
                namespace,
                ApplicationLauncher.class.getClassLoader(),
                LoggerFactory.getLogger(ApplicationLauncher.class));
        } catch (Exception e) {
        	e.printStackTrace();
            System.err.println("namespace does not contain a valid application definition: " + e.getMessage());
            System.exit(1);
            return;
        }
        
        // prepare compilation
        JavaCompiler compiler= ToolProvider.getSystemJavaCompiler();
        if(compiler == null) {
            System.err.println("Java compiler could not be found. Possibly you are running a JRE instead of a JDK!");
            System.exit(1);
            return;
        }
        
        final List<NodeConfiguration> nodes= new ArrayList<NodeConfiguration>();
        ArrayList<JavaFileObject> sources= new ArrayList<JavaFileObject>();
        for(AbstractConfiguration config : configs) {
            sources.add(new CharSequenceJavaFileObject(config.className, config.source));
            
            // collect nodes for later execution
            if(config instanceof NodeConfiguration) {
                nodes.add((NodeConfiguration) config);
            }
        }
        
        StandardJavaFileManager stdFileManager= compiler.getStandardFileManager(null, null, null);
        ByteFileManager<StandardJavaFileManager> byteFileManager= new ByteFileManager<StandardJavaFileManager>(stdFileManager);
        ConfigurationDiagnosticListener listener= new ConfigurationDiagnosticListener();
        CompilationTask task= compiler.getTask(null, byteFileManager, listener, null, null, sources);
        
        // compile
        boolean success= task.call().booleanValue();
        try {
            byteFileManager.close();
        } catch (IOException e1) {
            e1.printStackTrace();
            System.err.println("could not close file manager");
        }
        
        if(!success) {
            System.err.println("could not compile node configurations");
            System.err.println("  trying to dump generated source files...");
            
            File targetFolder= dumpSources(configs);
            
            if(targetFolder != null) {
                System.err.println(" dumped into " + targetFolder.toString());
            } else {
                System.err.println(" failed");
            }
            System.exit(1);
            return;
        }
        
        // launch the nodes
        Map<String, ByteArrayJavaFileObject> classes= byteFileManager.getStore();
        if(nodes.size() <= 0) {
            System.err.println("no nodes configured");
            System.exit(1);
        } else if(nodes.size() == 1) {
            // start the node in the current vm
            launchInCurrentVM(nodes.get(0).className, classes);
        } else {
            // write the configuration
            launchInNewVMs(nodes, classes);
        }
    }
    
    private static void launchInCurrentVM(final String nodeConfigClassName, final Map<String, ByteArrayJavaFileObject> classes) {
        ByteArrayJavaFileCache cache= new ByteArrayJavaFileCache(classes.values());
        ClassPath cp= new ClassPath();
        
        for(URL url : cache.getURLs()) {
            cp.addURL(url);
        }
        
        // FIXME: this class loader sucks... create it more specific!!!
        final ContainerClassLoader nodeLoader= new ContainerClassLoader(cp, ApplicationLauncher.class.getClassLoader());
        
        try {
            NodeLauncher.instantiateAndLaunch(nodeLoader, nodeConfigClassName);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.err.println("could not launch node in current vm");
            System.exit(1);
            return;
        }

    }
    
    private static void launchInNewVMs(List<NodeConfiguration> nodes, Map<String, ByteArrayJavaFileObject> classes){
        File temporyClassesFolder= new File(System.getProperty("java.io.tmpdir"), "microjiac_classes_" + System.currentTimeMillis());
        
        if(!temporyClassesFolder.exists()) {
            temporyClassesFolder.mkdirs();
        }
        
        for(int i= 0; i < nodes.size(); ++i) {
            final NodeConfiguration nodeConfiguration= nodes.get(i);
            
            File nodeFolder= new File(temporyClassesFolder, String.valueOf(i));
            
            if(!nodeFolder.exists()) {
                nodeFolder.mkdir();
            }
            
            System.out.println("write configurations for '" + nodeConfiguration.className + "' to '" + nodeFolder);
            
            final ArrayList<String> relevantClassNames= new ArrayList<String>(Arrays.asList(nodeConfiguration.fullQualifiedAgentConfigurationNames));
            relevantClassNames.add(nodeConfiguration.className);
            
            for(String relevantClassName : relevantClassNames) {
                String packageName= relevantClassName.substring(0, relevantClassName.lastIndexOf('.'));
                File targetFolder= new File(nodeFolder, packageName.replace('.', File.separatorChar));
                
                if(!targetFolder.exists()) {
                    targetFolder.mkdirs();
                }
                
                File classFile= new File(targetFolder, relevantClassName.substring(relevantClassName.lastIndexOf('.') + 1) + ".class");
                try {
                    FileOutputStream output= new FileOutputStream(classFile);
                    output.write(classes.get(relevantClassName).getByteArray());
                    output.flush();
                    output.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace(System.err);
                    System.err.println("could not create class file '" + classFile + "'");
                    System.exit(1);
                    return;
                }
            }
        }

        final File workingDir= new File(System.getProperty("user.dir"));
        final File javaCommand= getJavaCommand();

        final CountDownLatch allDone= new CountDownLatch(nodes.size());
        
        for(int i= 0; i < nodes.size(); ++i) {
            final String classpath= (System.getProperty("java.class.path") + File.pathSeparator + new File(temporyClassesFolder, String.valueOf(i)).getAbsolutePath());
            
            String[] entries= classpath.split(File.pathSeparator);
            
            String launcherPath= null;
            StringBuilder nodePath= new StringBuilder();
            for(int e= 0; e < entries.length; ++e) {
//                // FIXME: dirty hack
//                if(entries[e].contains("-launcher")) {
//                    launcherPath= entries[e];
//                } else {
                    if(nodePath.length() > 0) {
                        nodePath.append(File.pathSeparatorChar);
                    }
                    nodePath.append(entries[e]);
//                }
            }

            final String className= nodes.get(i).className;
            final Commandline cl= new Commandline(
                javaCommand.getPath() +
                " -classpath \"" + nodePath.toString() + "\"" + 
//                " -classpath \"" + launcherPath + "\"" +
                " " + NodeLauncher.class.getName() +
                " -c " + className
            );
            cl.setWorkingDirectory(workingDir.getPath());
            
            final String nodeName= className.substring(className.lastIndexOf('.') + 1);
            new Thread(nodeName) {
                public void run() {
                    try {
                        System.out.println(cl.toString());
                        CommandLineUtils.executeCommandLine(cl, new PrefixedConsumer(System.out, nodeName), new PrefixedConsumer(System.err, nodeName));
                    } catch (CommandLineException e) {
                        e.printStackTrace();
                    } finally {
                        allDone.countDown();
                    }
                    
                    System.out.println(nodeName + " finished execution");
                }
            }.start();
        }
        
        while(true) {
            try {
                allDone.await();
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static File getJavaCommand() {
        String osName = System.getProperty("os.name");
        System.out.println("osName = " + osName);

        String javaCommand = "java";

        boolean isWindowsBased = osName.startsWith("Windows");
        System.out.println("os is windows based: " + isWindowsBased);

        if (isWindowsBased) {
            javaCommand = javaCommand + ".exe";
            System.out.println("adjusted java command to meet windows naming conventions");
        }

        File cmd = new File(System.getProperty("java.home") + File.separator + "bin" + File.separator + javaCommand);
        System.out.println("cmd = " + cmd);

        if (!cmd.exists() || !cmd.isFile()) {
            throw new IllegalStateException("the java command cannot be found");
        }
        if (!cmd.canRead() || !cmd.canExecute()) {
            throw new IllegalStateException("you have no access rights to the java command");
        }

        return cmd;
    }
    
    private static File dumpSources(AbstractConfiguration[] configs ) {
        File temporySourcesFolder= new File(System.getProperty("java.io.tmpdir"), "microjiac_sources_" + System.currentTimeMillis());
        
        if(!temporySourcesFolder.exists()) {
            temporySourcesFolder.mkdirs();
        }
        
        for(AbstractConfiguration config : configs) {
            int lastPoint= config.className.lastIndexOf('.');
            String packName= lastPoint > 0 ? config.className.substring(0, lastPoint) : null;
            String fileName= lastPoint > 0 ? config.className.substring(lastPoint + 1) : config.className;
            
            File packFolder= packName != null ? new File(temporySourcesFolder, packName.replace('.', File.separatorChar)) : temporySourcesFolder;
            
            if(!packFolder.exists()) {
                packFolder.mkdirs();
            }
            
            File sourceFile= new File(packFolder, fileName + ".java");
            
            try {
                PrintStream printer= new PrintStream(new FileOutputStream(sourceFile));
                printer.print(config.source);
                printer.flush();
                printer.close();
            } catch (IOException e) {
                System.err.println("  cannot dump " + sourceFile.toString());
                e.printStackTrace(System.err);
            }
        }
        
        return temporySourcesFolder;
    }
    
    private ApplicationLauncher() {}
}
