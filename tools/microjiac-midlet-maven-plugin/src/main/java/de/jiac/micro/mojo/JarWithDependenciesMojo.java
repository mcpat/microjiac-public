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

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.archiver.jar.JarArchiver;

import de.jiac.micro.config.generator.AbstractConfiguration;
import de.jiac.micro.config.generator.NodeConfiguration;
import de.jiac.micro.util.JavaApplicationDescriptor;
import de.jiac.micro.util.ReducedArchiver;

/**
 * This mojo contains several functions of the maven-jar plugin to create the .jar
 * file.
 * 
 * @goal depsjar
 * @description Creates a jar out of the projects classes.
 * @requiresProject
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class JarWithDependenciesMojo extends AbstractArchiveMojo {
    private static final String[] DEFAULT_EXCLUDES = new String[] { "**/package.html" };

    private static final String[] DEFAULT_INCLUDES = new String[] { "**/**" };
    
    /**
     * Directory containing the classes.
     * 
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     * @readonly
     */
    private File outputDirectory;

    /**
     * The Jar archiver.
     * 
     * @component role="org.codehaus.plexus.archiver.Archiver" roleHint="jar"
     * @required
     * @readonly
     */
    private JarArchiver jarArchiver;

    /**
     * The maven archiver to use.
     * 
     * @parameter
     */
    private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();
    
    /**
     * Generates the JAR.
     * 
     * @todo Add license files in META-INF directory.
     */
    public void execute() throws MojoExecutionException {
        getLog().debug("starting packaging");
        
        // complete jar
        getLog().debug("assembling complete jar");
        File completeFile = createArchive(null);
        getProject().getArtifact().setFile(completeFile);
        
        AbstractConfiguration[] configurations= (AbstractConfiguration[]) getPluginContext().get(ConfiguratorMojo.GENERATED_CONFIGURATIONS_KEY);
        
        // we need a separate jar for each node!
        for(AbstractConfiguration configuration : configurations) {
            if(!(configuration instanceof NodeConfiguration)) {
                continue;
            }
            
            JavaApplicationDescriptor descriptor= getDescriptor();
            descriptor.setNodeConfiguration(configuration.className);
            archive.addManifestEntries(descriptor.toMap());
            
            String classifier= configuration.className.substring(configuration.className.lastIndexOf('.') + 1);
            getLog().debug("assembling jar for node '" + classifier + "'");
            File jarFile= createArchive(classifier);
            getProjectHelper().attachArtifact(getProject(), "jar", classifier, jarFile);
        }
        
        getLog().debug("finished packaging");
    }

    private File createArchive(String classifier) throws MojoExecutionException {
        File jarFile = getJarFile(classifier);

        ReducedArchiver archiver = new ReducedArchiver();

        archiver.setArchiver(jarArchiver);
        archiver.setOutputFile(jarFile);

        try {
            if (!outputDirectory.exists()) {
                getLog().warn("JAR will be empty - no content was marked for inclusion!");
            } else {
                archiver.getArchiver().addDirectory(outputDirectory, DEFAULT_INCLUDES, DEFAULT_EXCLUDES);
            }

            archiver.createArchive(getProject(), archive);

            return jarFile;
        } catch (Exception e) {
            throw new MojoExecutionException("Error assembling JAR", e);
        }
    }
}
