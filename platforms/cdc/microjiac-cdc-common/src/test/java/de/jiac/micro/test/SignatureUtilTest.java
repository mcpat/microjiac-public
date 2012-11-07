/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC CDC-Common.
 *
 * Copyright (c) 2007-2012 DAI-Labor, Technische Universität Berlin
 *
 * This library includes software developed at DAI-Labor, Technische
 * Universität Berlin (http://www.dai-labor.de)
 *
 * This library is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * $Id$ 
 */
package de.jiac.micro.test;

import com.github.libxjava.lang.SimpleClassLoader;

import de.jiac.micro.internal.util.SignatureUtil;

import junit.framework.TestCase;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class SignatureUtilTest extends TestCase {
    public void testSignatureConversion() throws Exception {
        SimpleClassLoader classLoader= new SimpleClassLoader();
        
        String signature= "([Ljava/lang/Long;[[IBCDFIJLjava/lang/Object;SZ)V";
        
        Class[] expected= new Class[] {
            void.class,
            Long[].class,
            int[][].class,
            byte.class,
            char.class,
            double.class,
            float.class,
            int.class,
            long.class,
            Object.class,
            short.class,
            boolean.class
        };
        
        Class[] classes= SignatureUtil.getSignatureClasses(classLoader, signature);
        assertEquals("wrong number of signature classes", expected.length, classes.length);
        for(int i= 0; i < expected.length; i++) {
            assertEquals("invalid signature class at " + i, expected[i], classes[i]);
        }
        
        signature= "()Lyou/are/Doomed;";
        try {
            SignatureUtil.getSignatureClasses(classLoader, signature);
            fail("signature conversion must fail: " + signature);
        } catch (ClassNotFoundException cnfe) {
            // success
        }
    }
    
    public void testDescriptorCreation() throws Exception {
        Class[] arguments= new Class[] {
            Long[].class,
            int[][].class,
            byte.class,
            char.class,
            double.class,
            float.class,
            int.class,
            long.class,
            Object.class,
            short.class,
            boolean.class
        };
        
        String expected= "([Ljava/lang/Long;[[IBCDFIJLjava/lang/Object;SZ)V";
        String signature= SignatureUtil.getMethodDescriptor(arguments, void.class);
        assertEquals("invalid signature", expected, signature);
    }
}
