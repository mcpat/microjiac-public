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
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;

import proguard.Configuration;
import proguard.ConfigurationParser;
import proguard.ProGuard;
import de.jiac.micro.config.generator.AbstractConfiguration;
import de.jiac.micro.config.generator.NodeConfiguration;
import de.jiac.micro.util.FileNameUtil;

/**
 * Reduces the size of class files. <a href="http://proguard.sourceforge.net/">ProGuard</a> is
 * needed so that this mojo can do its task.
 * 
 * @goal proguard
 * @requiresDependencyResolution compile
 * @description Reduces the size of class files and removes unused classes.
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ProGuardMojo extends AbstractPackagingMojo {
    /**
     * This parameter indicates whether obfuscation should be started or not.
     * 
     * @parameter default-value="true"
     */
    private boolean obfuscate;

    /**
     * Indicates whether all versions of the jars should be kept for
     * comparison purposes.
     * 
     * @parameter default-value="false"
     */
    private boolean keepJars;

    /**
     * Further configuration to be used by ProGuard
     * 
     * @parameter
     * @optional
     */
    private File additionalObfuscationSettings= null;
    
    /**
     * The path to the native preverifier command.
     * 
     * @parameter
     * @optional
     */
    private File preverifyPath= null;
    
    public void execute() throws MojoExecutionException {
        AbstractConfiguration[] configurations= (AbstractConfiguration[]) getPluginContext().get(ConfiguratorMojo.GENERATED_CONFIGURATIONS_KEY);
        
        for(AbstractConfiguration configuration : configurations) {
            if(!(configuration instanceof NodeConfiguration)) {
                continue;
            }
            
            String classifier= configuration.className.substring(configuration.className.lastIndexOf('.') + 1);
            File jar = checkJarFile(classifier);
            File copyJar = getJarFile(classifier + "-before_obfuscation");
            try {
                FileUtils.copyFile(jar, copyJar);
            } catch (IOException ioe) {
                throw new MojoExecutionException("could not copy jar '" + jar.getAbsolutePath() + "'", ioe);
            }

            jar.delete();

            String libraryPath = getClassPath(getProject());
            String inJars = FileNameUtil.getAbsolutPath(copyJar);
            String outJars = FileNameUtil.getAbsolutPath(jar);
            File mappingFile = new File(jar.getParent(), classifier + "-mappings.txt");

            // first merge all options
            List<String> mergeList= new LinkedList<String>();
            
            mergeList.add("-keep public class " + configuration.className);
            for(String agentConfigurationName : ((NodeConfiguration) configuration).fullQualifiedAgentConfigurationNames) {
                mergeList.add("-keep public class " + agentConfigurationName);
            }
            
            if(!obfuscate) {
                getLog().info("obfuscation and optimisation is skipped");
                mergeList.add("-dontshrink");
                mergeList.add("-dontoptimize");
                mergeList.add("-dontobfuscate");
            }
            
            if(preverifyPath == null) {
                getLog().info("use ProGuard preverifier");
                mergeList.add("-microedition");
            }
            
            if(additionalObfuscationSettings != null) {
                mergeList.add("@" + FileNameUtil.getAbsolutPath(additionalObfuscationSettings));
            }
            mergeList.add("-injars");
            mergeList.add(inJars);
            mergeList.add("-outjars");
            mergeList.add(outJars);
            
            if(libraryPath.length() > 0) {
                mergeList.add("-libraryjars");
                mergeList.add(libraryPath);
            }
            mergeList.add("-printmapping");
            mergeList.add(FileNameUtil.getAbsolutPath(mappingFile));
            
            Configuration proguardConfiguration;

            try {
                getLog().info("load and merge obfuscation settings");
                proguardConfiguration= loadAndMergeConfigurations(mergeList.toArray(new String[mergeList.size()]));
            } catch (Exception e) {
                throw new MojoExecutionException("could not merge obfuscation settings", e);
            }
            
            try {
                getLog().info("start ProGuard");
                new ProGuard(proguardConfiguration).execute();
                getLog().info("ProGuard finished");
            } catch (IOException ioe) {
                throw new MojoExecutionException("could not execute obfuscator", ioe);
            }
            
            if (!keepJars) {
                copyJar.delete();
            }
        }
    }
    
    private Configuration loadAndMergeConfigurations(String[] options) throws Exception {
        // load default settings
        URL url= getClass().getClassLoader().getResource("proguard/default-4.3.pro");
        ConfigurationParser parser= new ConfigurationParser(url);
        Configuration configuration= new Configuration();
        parser.parse(configuration);
        
        // load additional settings
        parser= new ConfigurationParser(options);
        parser.parse(configuration);
        
        return configuration;
    }
}
