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
package de.jiac.micro.reflect.filter;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public interface IFilter {
    int NONE= 0x00;
    int PROPERTIES= 0x01;
    int METHODS= 0x02;
    int METHODS_WITH_DESCRIPTORS= 0x04;
    int CONTEXT= 0x08;
    int DELAY= 0x10;
    int IGNORE= -1;

    int filter(Class<?> clazz);
}
