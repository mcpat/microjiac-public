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
package de.jiac.micro.test;

/**
 *
 * @author Marcel Patzlaff
 */
public class DummyClass {
    private int _number= -1;
    private String _string= null;
    
    public int getNumber() {
        return _number;
    }
    
    public void setNumber(int value) {
        _number= value;
    }
    
    public String getString() {
        return _string;
    }
    
    public void setString(String value) {
        _string= value;
    }
    
    protected void invisibleMethod() {}
    
    public byte overloadedMethod(Object val) {
        System.out.println("Object: aufgerufen mit " + val);
        return 1;
    }
    
    public byte overloadedMethod(Long val) {
        System.out.println("Long: aufgerufen mit " + val);
        return 2;
    }
    
    public byte overloadedMethod(short[] val) {
        System.out.println("short[]: aufgerufen mit " + val);
        return 3;
    }
    
    public byte overloadedMethod(float val) {
        System.out.println("float: aufgerufen mit " + val);
        return 4;
    }
}
