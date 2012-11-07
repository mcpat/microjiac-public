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
package de.jiac.micro.mojo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import de.jiac.micro.reflect.ClassCollector;
import de.jiac.micro.reflect.ReflectorGenerator;
import de.jiac.micro.reflect.ServiceContextGenerator;
import de.jiac.micro.reflect.ClassInfoReducer.ReducedClassInfo;

/**
 * Reflects through all runtime classes.
 * It collects all properties and also generates the late-bounded reflector.
 * 
 * @goal reflect
 * @description Generates the static accessor class.
 * @requiresDependencyResolution compile
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ReflectorMojo extends AbstractMojo {
    private final static String REFLECTOR_RELATIVE_PATH= ReflectorGenerator.MY_PACKAGE.replace('.', File.separatorChar);
    private final static String REFLECTOR_FILE_NAME= ReflectorGenerator.MY_NAME + ".java";
    
    /**
     * The directory for the compiled classes.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     * @readonly
     */
    private File outputDirectory;
    
    /**
     * The directory for generated sources.
     * 
     * @parameter expression="${project.build.directory}/generated-src"
     * @required
     */
    private File generatedSourceDirectory;
    
    /**
     * The Maven project reference.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    
    public void execute() throws MojoExecutionException {
        try {
            ClassCollector classCollector= new ClassCollector();
            classCollector.initialise(getFileLocations(), createBinaryPath());
            classCollector.process(getLog());
            
            ReducedClassInfo[] toGenerate= classCollector.getClassInfosForSourceGeneration();
            if(!generatedSourceDirectory.exists()) {
                generatedSourceDirectory.mkdirs();
            }
            
            getLog().info("generate reflector");
            File reflectorPath= new File(generatedSourceDirectory, REFLECTOR_RELATIVE_PATH);
            if(!reflectorPath.exists()) {
                reflectorPath.mkdirs();
            }
            
            File reflectorFile= new File(reflectorPath, REFLECTOR_FILE_NAME);
            if(!reflectorFile.exists()) {
                reflectorFile.createNewFile();
            }
            
            PrintStream out= new PrintStream(new FileOutputStream(reflectorFile));
            ReflectorGenerator.generateReflector(out, toGenerate);
            out.flush();
            out.close();
            
            ServiceContextGenerator.generateContexts(getLog(), generatedSourceDirectory, toGenerate);
        } catch (Exception x) {
            throw new MojoExecutionException("error during execution", x);
        }
        
        project.addCompileSourceRoot(generatedSourceDirectory.getAbsolutePath());
    }
    
    private File[] getFileLocations() {
        return new File[]{outputDirectory};
    }
    
    private ClassLoader createBinaryPath() throws MojoExecutionException {
        Set<URI> binaryPath= new HashSet<URI>();
        
        try {
            binaryPath.add(outputDirectory.toURI());
            
            HashSet<Artifact> artifacts= new HashSet<Artifact>();
            HashSet<String> extractedDependencies= (HashSet<String>) getPluginContext().get(ExtractionMojo.EXTRACTED_DEPENDENCIES);
            
            artifacts.addAll(project.getCompileArtifacts());
            
            for(Artifact artifact : artifacts) {
                String artId= artifact.getDependencyConflictId();
                if(extractedDependencies.contains(artId)) {
                    getLog().debug("Skip " + artId);
                    continue;
                }

                File file=  artifact.getFile();
                if(file == null) {
                    throw new DependencyResolutionRequiredException(artifact);
                }

                binaryPath.add(file.toURI());
            }
            
            URL[] urls= new URL[binaryPath.size()];
            int i= 0;
            for(URI uri : binaryPath) {
                urls[i++]= uri.toURL();
            }
            
            return new URLClassLoader(urls);
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException("failed to resolve dependency", e);
        } catch (MalformedURLException me) {
            throw new MojoExecutionException("failed to convert file to URL", me);
        }
        
    }
}
