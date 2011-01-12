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
package de.jiac.micro.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.jar.Attributes;

import org.apache.maven.plugin.logging.Log;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 * 
 * @todo strip version number 
 */
public class JavaApplicationDescriptor {
    private final static String NODE_CONFIGURATION_KEY= "MicroJIAC-Node-Configuration";
    
    private final static List<String> CONFIGURATIONS;
    private final static List<String> PROFILES;

    private final static String VERSION = "API-Specification-Version";
    private final static String API = "API";

    static {
        CONFIGURATIONS = new LinkedList<String>();
        CONFIGURATIONS.add("CDC");
        CONFIGURATIONS.add("CLDC");

        PROFILES = new LinkedList<String>();
        PROFILES.add("MIDP");
        PROFILES.add("IMP");
    }

    /**
     * The name of the MIDlet collection
     */
    private String midletName= null;

    /**
     * The version of the MIDlet collection
     */
    private String midletVersion= null;

    /**
     * The name of the vendor of this midlet suite.
     */
    private String midletVendor= null;

    private String midletIcon= "";
    
    /**
     * The name and the version of the profile - e.g. <pre>MIDP-2.0</pre>
     */
    private String j2meProfile= null;

    /**
     * The name and the version of the configuration - e.g. <pre>CLDC-1.1</pre>
     */
    private String j2meConfiguration= null;
    
    
    private Log _log;
    private Map<String,String> _configuration= null;
    private VersionResolver _resolver;
    
    public JavaApplicationDescriptor() {}
    
    public void setJ2meConfiguration(String configuration) {
        j2meConfiguration = configuration;
    }

    public void setJ2meProfile(String profile) {
        j2meProfile = profile;
    }

    public void setDefaultMidletName(String midletName) {
        if(this.midletName != null || midletName == null) {
            return;
        }
        
        this.midletName = midletName;
    }

    public void setDefaultMidletVendor(String midletVendor) {
        if(this.midletVendor != null || midletVendor == null) {
            return;
        }
        
        this.midletVendor = midletVendor;
    }

    public void setDefaultMidletVersion(String midletVersion) {
        if(this.midletVersion != null || midletVersion == null) {
            return;
        }
        
        this.midletVersion = midletVersion;
    }

    public void initialize(Log log) {
        _log= log;
        _resolver= new VersionResolver(_log);
        
        if(j2meConfiguration != null) {
            StringTokenizer tokens= new StringTokenizer(j2meConfiguration, "-");
            
            if(tokens.countTokens() == 2) {
                _resolver.addConfiguration(tokens.nextToken(), tokens.nextToken());
            }
        }
        
        if(j2meProfile != null) {
            StringTokenizer tokens= new StringTokenizer(j2meConfiguration, "-");
            
            if(tokens.countTokens() == 2) {
                _resolver.addProfile(tokens.nextToken(), tokens.nextToken());
            }
        }
    }
    
    public void refreshVersion(Attributes attributes) {
        String value= attributes.getValue(API);
        
        if(value == null)
            return;
        
        if(PROFILES.contains(value)) {
            _resolver.addProfile(value, attributes.getValue(VERSION));
        } else if(CONFIGURATIONS.contains(value)) {
            _resolver.addConfiguration(value, attributes.getValue(VERSION));
        }
    }
    
    public void writeDescriptor(File file) throws IOException {
        _log.info("Building jad: " + file.toString());
        
        PrintWriter jadWriter= new PrintWriter(new FileOutputStream(file));
        
        Map<String,String> props= getProperties();
        List<String> keys= new LinkedList<String>();
        keys.addAll(props.keySet());
        
        Collections.sort(keys);
        
        for(String key : keys) {
            jadWriter.append(key).append(": ").append(props.get(key)).println();
        }
        
        jadWriter.close();
    }
    
    public void setJarFile(File jarFile) {
        getProperties().put("MIDlet-Jar-Size", Long.toString(jarFile.length()));
        getProperties().put("MIDlet-Jar-URL", jarFile.getName());
    }
    
    public void setNodeConfiguration(String fullQualifiedClassName) {
        getProperties().put(NODE_CONFIGURATION_KEY, fullQualifiedClassName);
    }
    
    public Map<String,String> toMap() {
        Map<String, String> result= new HashMap<String, String>();
        result.putAll(getProperties());
        return result;
    }
    /**
     * @throws IllegalStateException
     *          if no profile and/or configuration could be found
     */
    private Map<String,String> getProperties() {
        if(_configuration == null) {
            _configuration= new HashMap<String, String>();
            _configuration.put("MIDlet-1", midletName + "," + midletIcon + ",de.jiac.micro.internal.NodeLauncher");
            
            if(midletIcon != null && midletIcon.length() > 0) {
                insertNonNull(_configuration, "MIDlet-Icon", midletIcon);
            }
            insertNonNull(_configuration, "MIDlet-Name", midletName);
            insertNonNull(_configuration, "MIDlet-Vendor", midletVendor);
            insertNonNull(_configuration, "MIDlet-Version", midletVersion);
            
            _resolver.addHighestCompatibleVersions(_configuration);
        }
        
        return _configuration;
    }
    
    private void insertNonNull(Map<String,String> map, String key, String value) {
        if(value == null) {
            throw new RuntimeException(key + " have to be specified");
        }
        
        map.put(key, value);
    }
}
