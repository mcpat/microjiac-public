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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

import de.jiac.micro.config.generator.AbstractConfiguration;
import de.jiac.micro.config.generator.NodeConfiguration;
import de.jiac.micro.util.FileNameUtil;

/**
 * Classes aimed to run on MIDlet based platforms (IMP/MIDP) have to be preverified.
 * For this task the <a href="http://java.sun.com/products/sjwtoolkit/">Sun Wireless Toolkit</a>
 * is expected to be present.
 * 
 * @goal preverify
 * @description Preverification of the classes (for IMP/MIDP usage)
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class PreverifyMojo extends AbstractPackagingMojo {
    private class LogWriter implements StreamConsumer {
        private final boolean _warn;
        
        protected LogWriter(boolean warn) {
            _warn= warn;
        }

        public void consumeLine(String line) {
            if(_warn) {
                getLog().warn(line);
            } else {
                getLog().info(line);
            }
        }
    }

    /**
     * The path to the native preverify command. 
     * 
     * @parameter
     * @optional
     */
    private File preverifyPath= null;
    
    /**
     * Indicates whether all versions of the jars should be kept for
     * comparison purposes.
     * 
     * @parameter default-value="false"
     */
    private boolean keepJars;
    
    /**
     * The main method of this MoJo
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        if(preverifyPath == null) {
            getLog().debug("skip native preverification");
            return;
        }
        
        getLog().debug("start native preverification");

        final File preverifyCmd= getAbsolutePreverifyCommand();
        
        StreamConsumer stdoutLogger= new LogWriter(false);
        StreamConsumer stderrLogger= new LogWriter(true);
        
        String classPath = getClassPath(getProject());
        AbstractConfiguration[] configurations= (AbstractConfiguration[]) getPluginContext().get(ConfiguratorMojo.GENERATED_CONFIGURATIONS_KEY);
        
        for(AbstractConfiguration configuration : configurations) {
            if(!(configuration instanceof NodeConfiguration)) {
                continue;
            }
            
            String classifier= configuration.className.substring(configuration.className.lastIndexOf('.') + 1);
            File oldJar= checkJarFile(classifier);
            
            if(keepJars) {
                try {
                    FileUtils.copyFile(oldJar, getJarFile(classifier + "-before_preverify"));
                } catch (IOException ioe) {
                    getLog().warn("could not keep old jar '" + oldJar.getAbsolutePath() + "'", ioe);
                }
            }
            
            getLog().info("Preverifying jar: " + FileNameUtil.getAbsolutPath(oldJar));
            
            Commandline commandLine= new Commandline(preverifyCmd.getPath() + " -classpath " + classPath + " -d " + FileNameUtil.getAbsolutPath(oldJar.getParentFile()) + " " + FileNameUtil.getAbsolutPath(oldJar));
            getLog().debug(commandLine.toString());
            
            try {
                if(CommandLineUtils.executeCommandLine(commandLine, stdoutLogger, stderrLogger) != 0) {
                    throw new MojoExecutionException("Preverification failed. Please read the log for details.");
                }
            } catch (CommandLineException cle) {
                throw new MojoExecutionException("could not execute preverify command", cle);
            }
        }

        getLog().debug("finished preverification");
    }

    private File getAbsolutePreverifyCommand() throws MojoFailureException {
        final String os= System.getProperty("os.name");
        getLog().debug("os name is: '" + os + "'");
        
        String preverify= "preverify";
        
        if(os.startsWith("Windows")) {
            getLog().debug("os is a windows platform");
            preverify= preverify + ".exe";
        } else {
            getLog().debug("os is not a windows platform");
        }
        
        File absolutePreverifyCommand= new File(preverifyPath, preverify);
        
        getLog().debug("absolute preverify command = " + absolutePreverifyCommand);

        if (!absolutePreverifyCommand.exists() || !absolutePreverifyCommand.isFile()) {
            throw new MojoFailureException("preverify cannot be found");
        }

        if(!absolutePreverifyCommand.canRead()) {
            throw new MojoFailureException("you have no access rights to the preverify command");
        }
        
        return absolutePreverifyCommand;
    }
}
