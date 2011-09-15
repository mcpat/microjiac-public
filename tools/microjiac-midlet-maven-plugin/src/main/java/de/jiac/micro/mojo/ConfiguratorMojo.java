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
package de.jiac.micro.mojo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.DefaultProjectBuilderConfiguration;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.apache.maven.project.artifact.MavenMetadataSource;
import org.apache.maven.project.inheritance.ModelInheritanceAssembler;
import org.apache.maven.project.interpolation.ModelInterpolationException;
import org.apache.maven.project.interpolation.ModelInterpolator;
import org.slf4j.Logger;

import de.jiac.micro.config.generator.AbstractConfiguration;
import de.jiac.micro.config.generator.ConfigurationGenerator;
import de.jiac.micro.util.ModifiedURLClassLoader;

/**
 * Generates all configuration classes for nodes and agents.
 * 
 * @goal genconfig
 * @requiresDependencyResolution compile
 * @description Generates the configurator class to ease the obfuscation.
 *
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ConfiguratorMojo extends AbstractMojo {
    public static final String GENERATED_CONFIGURATIONS_KEY= "Generated-Configurations";

    /**
     * The Maven project reference.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    
    /**
     * The directory for the compiled classes.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     * @readonly
     */
    private File outputDirectory;
    
    /**
     * @parameter expression="${project.build.directory}/generated-src"
     * @required
     */
    private File generatedSourceDirectory;
    
    /**
     * Defines the namespace where the application definition is located
     * 
     * @parameter
     * @required
     */
    private String applicationDefinition;
    
    /**
     * The reference to an artifact resolver.
     * 
     * @component
     * @required
     */
    private ArtifactResolver artifactResolver;

    /**
     * The factory to create artifacts with.
     *
     * @component
     * @required
     */
    private ArtifactFactory artifactFactory;

    /**
     * The source to creata artifact meta data from.
     * 
     * @component
     * @required
     */
    private ArtifactMetadataSource metadataSource;

    /**
     * The Maven project builder.
     * 
     * @component
     * @required
     */
    private MavenProjectBuilder mavenProjectBuilder;

    /**
     * The reference to the default model inheritance assembler.
     * 
     * @component
     * @required
     */
    private ModelInheritanceAssembler modelInheritanceAssembler;
    
    /**
     * The reference to the default model interpolator.
     * 
     * @component
     * @required
     */
    private ModelInterpolator modelInterpolator;
    
    /**
     * The reference to the local artifact repository.
     *
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * The list of remote artifact repositories.
     *
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @required
     * @readonly
     */
    private List<ArtifactRepository> remoteRepositories;
    
    /**
     * Processes all dependencies first and then creates a new JVM to
     * generate the configurator implementation with.
     */
    public void execute() throws MojoExecutionException {
        ClassLoader classloader;
        
        try {
            classloader= createClassLoader();
        } catch (Exception e) {
            throw new MojoExecutionException("could not create classloader from dependencies", e);
        }
        
        AbstractConfiguration[] configurations;
        
        try {
            configurations= ConfigurationGenerator.execute(generatedSourceDirectory, applicationDefinition, classloader, getSLF4JLogger());
        } catch (Exception e) {
            throw new MojoExecutionException("could not generate the configurator", e);
        }
        
        getPluginContext().put(GENERATED_CONFIGURATIONS_KEY, configurations);
        project.addCompileSourceRoot(generatedSourceDirectory.getPath());
    }
    
    private ClassLoader createClassLoader() throws Exception {
        Set<URI> classpath= new HashSet<URI>();

        // the current projects runtime dependencies
        HashSet<Artifact> artifacts= new HashSet<Artifact>();
//        artifacts.addAll(project.getSystemArtifacts());
//        artifacts.addAll(project.getRuntimeArtifacts());
        artifacts.addAll(project.getCompileArtifacts());
        
        for(Artifact artifact : artifacts) {
            File file= artifact.getFile();
            
            if(file != null) {
                classpath.add(file.toURI());
            } else {
                getLog().warn("could not find artifact file for '" + artifact.getArtifactId());
            }
        }
        
        classpath.add(outputDirectory.toURI());
        
        // plugin dependencies, because we want to run a class from this plugin
        MavenXpp3Reader reader = new MavenXpp3Reader();
        InputStreamReader in = new InputStreamReader(getPluginPOM(classpath));
        Model pluginModel = checkModel(reader.read(in));
        
        Set pluginDeps = transitivelyResolvePomDependencies(pluginModel.getGroupId(), pluginModel.getArtifactId(), pluginModel.getVersion());

        for (Object artObj : pluginDeps) {
            File file = ((Artifact) artObj).getFile();

            if (file != null) {
                classpath.add(file.toURI());
            }
        }
        
        URL[] urls= new URL[classpath.size()];
        int i= 0;
        for(URI uri : classpath) {
            urls[i++]= uri.toURL();
        }

        return new ModifiedURLClassLoader(urls);
    }

    private InputStream getPluginPOM(Set<URI> classpath) throws IOException {
        File file = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
        classpath.add(file.toURI());

        JarFile jarFile = new JarFile(file);
        for (JarEntry entry : Collections.list(jarFile.entries())) {
            if (entry.getName().endsWith("pom.xml")) {
                return jarFile.getInputStream(entry);
            }
        }

        return null;
    }

    private Set transitivelyResolvePomDependencies(String groupId, String artifactId, String version)
            throws ProjectBuildingException, InvalidDependencyVersionException, ArtifactResolutionException, ArtifactNotFoundException {
        //get the pom as an Artifact 
        Artifact pomArtifact = artifactFactory.createPluginArtifact(groupId, artifactId, VersionRange.createFromVersion(version));

        //load the pom as a MavenProject
        MavenProject tempProject = mavenProjectBuilder.buildFromRepository(pomArtifact, remoteRepositories, localRepository);

        //get all of the dependencies for the project
        List dependencies = tempProject.getDependencies();

        //make Artifacts of all the dependencies
        Set dependencyArtifacts = MavenMetadataSource.createArtifacts(artifactFactory, dependencies, null, null, null);

        //not forgetting the Artifact of the project itself
        dependencyArtifacts.add(tempProject.getArtifact());

        //resolve all dependencies transitively to obtain a comprehensive list of jars        
        ArtifactResolutionResult result = artifactResolver.resolveTransitively(dependencyArtifacts, pomArtifact, Collections.EMPTY_MAP, localRepository,
                remoteRepositories, metadataSource, null, Collections.EMPTY_LIST);

        return result.getArtifacts();
    }
    
    private Model checkModel(Model model) throws ModelInterpolationException, ProjectBuildingException {
        if(model.getParent() != null) {
            Parent parent= model.getParent();
            Artifact parentArt= artifactFactory.createArtifact(parent.getGroupId(), parent.getArtifactId(), parent.getVersion(), "compile", "pom");
            MavenProject parentProj= mavenProjectBuilder.buildFromRepository(parentArt, remoteRepositories, localRepository);
            Model parentModel= parentProj.getModel();
            
            if(parentModel.getParent() != null) {
                parentModel= checkModel(parentModel);
            }
            
            modelInheritanceAssembler.assembleModelInheritance(model, parentModel);
        }
        
        DefaultProjectBuilderConfiguration projectBuilderConfig= new DefaultProjectBuilderConfiguration();
        projectBuilderConfig.setExecutionProperties(model.getProperties());
        return modelInterpolator.interpolate(model, null, projectBuilderConfig, true);
    }
    
    private Logger getSLF4JLogger() {
        return new Logger() {
            public String getName() {
                return "ConfiguratorMojo";
            }

            public void debug(String message, Throwable t) {
                getLog().debug(message, t);
            }

            public void debug(String message) {
                getLog().debug(message);
            }

            public void error(String message, Throwable t) {
                getLog().error(message, t);
            }

            public void error(String message) {
                getLog().error(message);
            }

            public void info(String message, Throwable t) {
                getLog().info(message, t);
            }

            public void info(String message) {
                getLog().info(message);
            }

            public boolean isDebugEnabled() {
                return getLog().isDebugEnabled();
            }

            public boolean isErrorEnabled() {
                return getLog().isErrorEnabled();
            }

            public boolean isInfoEnabled() {
                return getLog().isInfoEnabled();
            }

            public boolean isTraceEnabled() {
                return getLog().isDebugEnabled();
            }

            public boolean isWarnEnabled() {
                return getLog().isWarnEnabled();
            }

            public void trace(String message, Throwable t) {
                getLog().debug(message, t);
            }

            public void trace(String message) {
                getLog().debug(message);
            }

            public void warn(String message, Throwable t) {
                getLog().warn(message, t);
            }

            public void warn(String message) {
                getLog().warn(message);
            }
        };
    }
}
