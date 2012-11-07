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
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Organization;

import de.jiac.micro.util.JavaApplicationDescriptor;

/**
 * @author Marcel Patzlaff
 *
 * @version $Revision$
 */
abstract class AbstractArchiveMojo extends AbstractPackagingMojo {

    /**
     * Information for the .jad file (Java Application Descriptor)
     * 
     * @parameter
     * @optional
     */
    private JavaApplicationDescriptor descriptor = new JavaApplicationDescriptor();

    private boolean _initialised= false;
    
    JavaApplicationDescriptor getDescriptor() {
        if(!_initialised) {
            descriptor.initialize(getLog());
            for (Object artObj : getProject().getCompileArtifacts()) {
                Artifact art = (Artifact) artObj;
                File file = art.getFile();

                if (file != null && file.isFile() && file.toString().endsWith("jar")) {
                    try {
                        JarFile jar = new JarFile(file);

                        Attributes attrs = jar.getManifest().getMainAttributes();
                        descriptor.refreshVersion(attrs);
                        jar.close();
                    } catch (Exception e) {
                        getLog().debug("current artifact is no valid .jar file: " + file, e);
                    }
                }
            }
            
            descriptor.setDefaultMidletName(getProject().getName());
            descriptor.setDefaultMidletVersion(getProject().getVersion());
            
            Organization org= getProject().getOrganization();
            descriptor.setDefaultMidletVendor(org != null ? org.getName() : null);
            
            _initialised= true;
        }
        
        return descriptor;
    }
}
