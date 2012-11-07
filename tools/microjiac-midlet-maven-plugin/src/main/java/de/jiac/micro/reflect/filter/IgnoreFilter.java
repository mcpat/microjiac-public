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
package de.jiac.micro.reflect.filter;

import java.lang.reflect.Modifier;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class IgnoreFilter implements IFilter {
    public int filter(Class clazz) {
        try {
            Class throwable= Class.forName("java.lang.Throwable", false, clazz.getClassLoader());
            if(throwable.isAssignableFrom(clazz)) {
                return IGNORE;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int mods= clazz.getModifiers();
        if(!Modifier.isPublic(mods)) {
            return IGNORE;
        }
        
        if(clazz.isAnonymousClass()) {
            return IGNORE;
        }
        
        
        return NONE;
    }
}
