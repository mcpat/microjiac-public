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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Builds the obfuscated and preverified JAR and its Java Application
 * Descriptor JAD
 * 
 * @goal midletjar
 * @execute phase="package" lifecycle="midletPackaging"
 * @description Packages this J2ME project and creates its application descriptor
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class MIDletJarMojo extends AbstractPackagingMojo {
    /**
     * @parameter expression="${executedProject}"
     */
    private MavenProject executedProject;
    
    public void execute() throws MojoExecutionException {
        File jarFile= checkArtifactJarFile();
        getProject().getArtifact().setFile(jarFile);
        executedProject.getArtifact().setFile(jarFile);
    }
}
