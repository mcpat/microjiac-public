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
package de.jiac.micro.reflect.test;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.maven.plugin.logging.SystemStreamLog;

import de.jiac.micro.reflect.ClassCollector;
import de.jiac.micro.reflect.ClassInfoReducer.ReducedClassInfo;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ClassCollectorTest {
    public static void main(String[] args) throws Exception {
        URLClassLoader myLoader= (URLClassLoader) ClassCollectorTest.class.getClassLoader();
        
        URL[] urls= myLoader.getURLs();
        File[] files= new File[1];
        files[0]= new File(urls[0].getFile());
        
//        for(int i= 0; i < urls.length; ++i) {
//            files[i]= new File(urls[i].getFile());
//        }
        
        ClassCollector collector= new ClassCollector();
        collector.initialise(files, myLoader);
        
        collector.process(new SystemStreamLog());
        
        ReducedClassInfo[] rci= collector.getClassInfosForSourceGeneration();
        for(int i= 0; i < rci.length; ++i) {
            System.out.println(rci[i]);
        }
    }
}
