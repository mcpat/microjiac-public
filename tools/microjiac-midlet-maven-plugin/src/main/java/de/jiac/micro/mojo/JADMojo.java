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
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;

import de.jiac.micro.config.generator.AbstractConfiguration;
import de.jiac.micro.config.generator.NodeConfiguration;
import de.jiac.micro.util.JavaApplicationDescriptor;

/**
 * Creates the Java Application Descriptor (.jad) for the J2ME application.
 * 
 * @goal jad
 * @description Creates a java descriptor for this J2ME project
 * @requiresProject
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class JADMojo extends AbstractArchiveMojo {
    /**
     * Generates the JAD.
     */
    public void execute() throws MojoExecutionException {
        getLog().debug("starting packaging");
        
        AbstractConfiguration[] configurations= (AbstractConfiguration[]) getPluginContext().get(ConfiguratorMojo.GENERATED_CONFIGURATIONS_KEY);
        
        try {
            for(AbstractConfiguration configuration : configurations) {
                if(!(configuration instanceof NodeConfiguration)) {
                    continue;
                }
                
                String classifier= configuration.className.substring(configuration.className.lastIndexOf('.') + 1);
                File jarFile= checkJarFile(classifier);
                JavaApplicationDescriptor descriptor= getDescriptor();
                descriptor.setNodeConfiguration(configuration.className);
                descriptor.setJarFile(jarFile);
                File jadFile= getJadFile(classifier);
                getDescriptor().writeDescriptor(jadFile);
                getProjectHelper().attachArtifact(getProject(), "jad", classifier, jadFile);
            }
        } catch (IOException ioe) {
            throw new MojoExecutionException("could not create .jad file", ioe);
        } catch (RuntimeException e) {
            throw new MojoExecutionException("could not create .jad file", e);
        }
        getLog().debug("finished packaging");
    }
}
