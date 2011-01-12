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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.Expand;
import org.codehaus.plexus.util.FileUtils;

/**
 * Extracts all 'de.jiac.micro' dependencies to the classes folder.
 * 
 * @goal depextract
 * @requiresDependencyResolution runtime
 * @description Extracts 'de.jiac.micro' dependencies such that can be preverified and included into projects jar.
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ExtractionMojo extends AbstractMojo {
    public static final String EXTRACTED_DEPENDENCIES= "Extracted-Dependencies";
    
//    public static final String JIAC_MICRO_GROUP_ID= "de.jiac.micro";

    /**
     * The Maven project reference.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Directory containing the classes.
     * 
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     * @readonly
     */
    private File outputDirectory;
    
    public void execute() {
        getLog().debug("starting extraction");
        final HashSet<String> extractedDependencies= new HashSet<String>();
        
        for (Object artObj : project.getRuntimeArtifacts()) {
            Artifact art = (Artifact) artObj;
            
            File file= art.getFile();
            try {
                extractContentToTarget(file);
                extractedDependencies.add(art.getDependencyConflictId());
            } catch (Exception e) {
                getLog().debug("could not extract '" + file + "'", e);
            }
        }
        
        getPluginContext().put(EXTRACTED_DEPENDENCIES, extractedDependencies);
        getLog().debug("finished extraction");
    }
    
    private void extractContentToTarget(File file) throws Exception {
        getLog().info("Extracting '" + file.getName() + "' to classes folder...");
        
        Expand exp= new Expand();
        exp.setSrc(file);
        exp.setDest(outputDirectory);
        exp.setOverwrite(false);
        exp.execute();
        
        DirectoryScanner scanner= new DirectoryScanner();
        scanner.setExcludes(new String[] {"META-INF", "config", "**/*.xsd", "**/*.xml"});
        scanner.setIncludes(new String[]{"**/**"});
        scanner.setBasedir(outputDirectory);
        scanner.scan();
        
        for(String fString : scanner.getExcludedFiles()) {
            FileUtils.forceDelete(new File(outputDirectory, fString));
        }
        
        for(String fString : scanner.getExcludedDirectories()) {
            FileUtils.forceDelete(new File(outputDirectory, fString));
        }
    }
}
