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

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.maven.plugin.logging.Log;

import de.jiac.micro.mojo.version.ConfigurationVersion;
import de.jiac.micro.mojo.version.ProfileVersion;

/**
 * @author Marcel Patzlaff
 *
 * @version $Revision$
 */
public class VersionResolver {
    private final static String CONFIGURATION_ENTRY= "MicroEdition-Configuration";
    private final static String PROFILE_ENTRY= "MicroEdition-Profile";

    private final SortedSet<ProfileVersion> _profiles= new TreeSet<ProfileVersion>();
    private final SortedSet<ConfigurationVersion> _configurations= new TreeSet<ConfigurationVersion>();
    
    private final Log _log;
    
    public VersionResolver(Log log) {
        _log= log;
    }
    
    public void addProfile(String name, String version) {
        try {
            _profiles.add(new ProfileVersion(name, version));
        } catch (RuntimeException e) {
            _log.debug("'" + version + "' is no valid version number", e);
        }
    }
    
    public void addConfiguration(String name, String version) {
        try {
            _configurations.add(new ConfigurationVersion(name, version));
        } catch (RuntimeException e) {
            _log.debug("'" + version + "' is no valid version number", e);
        }
    }
    
    /**
     * @throws IllegalStateException
     *          if no profile and/or configuration could be found
     */
    public void addHighestCompatibleVersions(Map<String,String> descriptor) {
        if(_configurations.size() > 0) {
            descriptor.put(CONFIGURATION_ENTRY, _configurations.first().toString());
        } else {
            _log.warn("no J2ME configuration version found");
        }

        if(_profiles.size() > 0) {
            descriptor.put(PROFILE_ENTRY, _profiles.first().toString());
        } else {
            _log.warn("no J2ME profile version found");
        }
    }
}