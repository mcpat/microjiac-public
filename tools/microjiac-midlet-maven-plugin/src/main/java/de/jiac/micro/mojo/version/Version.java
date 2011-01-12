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
package de.jiac.micro.mojo.version;

/**
 * @author Marcel Patzlaff
 *
 * @version $Revision$
 */
abstract class Version implements Comparable {
    final String name;
    final int major;
    final int minor;
    
    private static int[] getMajorMinorVersion(String version) {
        int point= version.indexOf('.');
        int major;
        int minor;
        if(point > 0 && point < version.length() - 1) {
            major= Integer.parseInt(version.substring(0, point));
            minor= Integer.parseInt(version.substring(point + 1));
        } else {
            major= Integer.parseInt(version);
            minor= 0;
        }
            
        return new int[] {major, minor};
    }
    
    Version(String name, String version) {
        this(name, getMajorMinorVersion(version));
    }
    
    private Version(String name, int[] version) {
        this.name= name;
        this.major= version[0];
        this.minor= version[1];
    }
    
    public final boolean equals(final Object obj) {
        if(obj == null) {
            return false;
        }
        
        if(!obj.getClass().equals(getClass())) {
            return false;
        }
        
        Version other= (Version) obj;
        return other.name.equals(name) && other.major == major && other.minor == minor;
    }
    
    public final int hashCode() {
        return name.hashCode() ^ (major << 8) ^ minor;
    }

    public final int compareTo(Object o) {
        if(o == null) {
            return 1;
        }
        
        if(!o.getClass().equals(getClass())) {
            return 1;
        }
        
        return saveCompareTo((Version) o);
    }

    public String toString() {
        return name + '-' + major + '.' + minor;
    }
    
    abstract int saveCompareTo(Version o);
}
