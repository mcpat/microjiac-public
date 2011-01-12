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
import java.util.HashSet;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import de.jiac.micro.util.FileNameUtil;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
abstract class AbstractPackagingMojo extends AbstractMojo {
    /**
     * The Maven project reference.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Name of the generated JAR.
     * 
     * @parameter alias="jarName" expression="${project.build.finalName}"
     * @required
     * @readonly
     */
    private String finalName;
    
    /**
     * Directory containing the generated JAR.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     * @readonly
     */
    private File basedir;
    
    /**
     * The reference to a project helper. It is required for attaching all node
     * <code>.jar</code> files to the project.
     * 
     * @component
     * @required
     */
    private MavenProjectHelper projectHelper;
    
    MavenProject getProject() {
        return project;
    }
    
    File getBuildDirectory() {
        return basedir;
    }
    
    String getFinalName() {
        return finalName;
    }
    
    MavenProjectHelper getProjectHelper() {
        return projectHelper;
    }
    
    File getArtifactJarFile() {
        return getJarFile(null);
    }
    
    File getJarFile(String classifier) {
        if(classifier == null) {
            return new File(getBuildDirectory(), getFinalName() + ".jar");
        } else {
            return new File(getBuildDirectory(), getFinalName() + "-" + classifier + ".jar");
        }
    }
    
    File getJadFile(String classifier) {
        return new File(getBuildDirectory(), getFinalName() + "-" + classifier + ".jad");
    }
    
    File checkArtifactJarFile() throws MojoExecutionException {
        return checkJarFile(null);
    }
    
    File checkJarFile(String classifier) throws MojoExecutionException {
        File jarFile= getJarFile(classifier);
        
        if(!jarFile.exists() || !jarFile.isFile()) {
            throw new MojoExecutionException("there is no built artifact '" + jarFile.toString() + "'");
        }
        
        return jarFile;
    }
    
    File checkJadFile(String classifier) throws MojoExecutionException {
        File jadFile= getJadFile(classifier);
        
        if(!jadFile.exists() || !jadFile.isFile()) {
            throw new MojoExecutionException("there is no java application descriptor '" + jadFile.toString() + "'");
        }
        
        return jadFile;
    }
    
    String getClassPath(MavenProject p) throws MojoExecutionException {
        try {
            StringBuffer classPath = new StringBuffer();

            HashSet<Artifact> artifacts= new HashSet<Artifact>();
            artifacts.addAll(p.getCompileArtifacts());
            
            HashSet<String> extractedDependencies= (HashSet<String>) getPluginContext().get(ExtractionMojo.EXTRACTED_DEPENDENCIES);
            
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
                
                String classpathElement = FileNameUtil.getAbsolutPath(file);

                if (classPath.length() > 0) {
                    classPath.append(File.pathSeparator);
                }

                classPath.append(classpathElement);
            }

            getLog().debug("classpath: " + classPath);
            return classPath.toString();
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException("failed to resolve dependency", e);
        }
    }
}
