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
package de.jiac.micro.util;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.archiver.ManifestConfiguration;
import org.apache.maven.archiver.ManifestSection;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.archiver.jar.Manifest;
import org.codehaus.plexus.archiver.jar.ManifestException;

/**
 *
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ReducedArchiver {
    private JarArchiver archiver;

    private File archiveFile;

    /**
     * Return a pre-configured manifest
     *
     * @todo Add user attributes list and user groups list
     */
    public Manifest getManifest(MavenProject project, ManifestConfiguration config) throws ManifestException, DependencyResolutionRequiredException {
        // Added basic entries
        Manifest m = new Manifest();
        Manifest.Attribute buildAttr = new Manifest.Attribute("Built-By", System.getProperty("user.name"));
        m.addConfiguredAttribute(buildAttr);
        Manifest.Attribute createdAttr = new Manifest.Attribute("Created-By", "Apache Maven");
        m.addConfiguredAttribute(createdAttr);

        if (config.getPackageName() != null) {
            Manifest.Attribute packageAttr = new Manifest.Attribute("Package", config.getPackageName());
            m.addConfiguredAttribute(packageAttr);
        }

        Manifest.Attribute buildJdkAttr = new Manifest.Attribute("Build-Jdk", System.getProperty("java.version"));
        m.addConfiguredAttribute(buildJdkAttr);

        if (config.isAddClasspath()) {
            StringBuffer classpath = new StringBuffer();
            List artifacts = project.getRuntimeClasspathElements();
            String classpathPrefix = config.getClasspathPrefix();

            for (Iterator iter = artifacts.iterator(); iter.hasNext();) {
                File f = new File((String) iter.next());
                if (f.isFile()) {
                    if (classpath.length() > 0) {
                        classpath.append(" ");
                    }

                    classpath.append(classpathPrefix);
                    classpath.append(f.getName());
                }
            }

            if (classpath.length() > 0) {
                Manifest.Attribute classpathAttr = new Manifest.Attribute("Class-Path", classpath.toString());
                m.addConfiguredAttribute(classpathAttr);
            }
        }

        String mainClass = config.getMainClass();
        if (mainClass != null && !"".equals(mainClass)) {
            Manifest.Attribute mainClassAttr = new Manifest.Attribute("Main-Class", mainClass);
            m.addConfiguredAttribute(mainClassAttr);
        }
        return m;
    }

    public JarArchiver getArchiver() {
        return archiver;
    }

    public void setArchiver(JarArchiver archiver) {
        this.archiver = archiver;
    }

    public void setOutputFile(File outputFile) {
        archiveFile = outputFile;
    }

    public void createArchive(MavenProject project, MavenArchiveConfiguration archiveConfiguration) throws ArchiverException, ManifestException, IOException,
            DependencyResolutionRequiredException {
        // ----------------------------------------------------------------------
        // We want to add the metadata for the project to the JAR in two forms:
        //
        // The first form is that of the POM itself. Applications that wish to
        // access the POM for an artifact using maven tools they can.
        //
        // The second form is that of a properties file containing the basic
        // top-level POM elements so that applications that wish to access
        // POM information without the use of maven tools can do so.
        // ----------------------------------------------------------------------

        // we have to clone the project instance so we can write out the pom with the deployment version,
        // without impacting the main project instance...
        MavenProject workingProject = new MavenProject(project);

        if (workingProject.getArtifact().isSnapshot()) {
            workingProject.setVersion(workingProject.getArtifact().getVersion());
        }

        // ----------------------------------------------------------------------
        // Create the manifest
        // ----------------------------------------------------------------------

        File manifestFile = archiveConfiguration.getManifestFile();

        if (manifestFile != null) {
            archiver.setManifest(manifestFile);
        }

        Manifest manifest = getManifest(workingProject, archiveConfiguration.getManifest());

        // any custom manifest entries in the archive configuration manifest?
        if (!archiveConfiguration.isManifestEntriesEmpty()) {
            Map entries = archiveConfiguration.getManifestEntries();
            Set entrySet= entries.entrySet();
            
            for (Iterator iter = entrySet.iterator(); iter.hasNext();) {
                Map.Entry entry= (Map.Entry) iter.next();
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                Manifest.Attribute attr = new Manifest.Attribute(key, value);
                manifest.addConfiguredAttribute(attr);
            }
        }

        // any custom manifest sections in the archive configuration manifest?
        if (!archiveConfiguration.isManifestSectionsEmpty()) {
            List sections = archiveConfiguration.getManifestSections();
            for (Iterator iter = sections.iterator(); iter.hasNext();) {
                ManifestSection section = (ManifestSection) iter.next();
                Manifest.Section theSection = new Manifest.Section();
                theSection.setName(section.getName());

                if (!section.isManifestEntriesEmpty()) {
                    Map entries = section.getManifestEntries();
                    Set keys = entries.keySet();
                    for (Iterator it = keys.iterator(); it.hasNext();) {
                        String key = (String) it.next();
                        String value = (String) entries.get(key);
                        Manifest.Attribute attr = new Manifest.Attribute(key, value);
                        theSection.addConfiguredAttribute(attr);
                    }
                }

                manifest.addConfiguredSection(theSection);
            }
        }

        // Configure the jar
        archiver.addConfiguredManifest(manifest);

        archiver.setCompress(archiveConfiguration.isCompress());

        archiver.setIndex(archiveConfiguration.isIndex());

        archiver.setDestFile(archiveFile);

        // create archive
        archiver.createArchive();
    }
}
