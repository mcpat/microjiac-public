/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Config.
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
package de.jiac.micro.config.analysis;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.Value;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class RuntimeGuessValue implements Value {
    public static final RuntimeGuessValue UNINITIALISED_VALUE= new RuntimeGuessValue(null, null);
    public static final RuntimeGuessValue RETURNADDRESS_VALUE= new RuntimeGuessValue(null, null);
    public static final RuntimeGuessValue NULL_CONSTANT= new RuntimeGuessValue(Type.getObjectType("java/lang/Object"), null);
    
    public static final RuntimeGuessValue IM1_CONSTANT= new RuntimeGuessValue(Type.INT_TYPE, Integer.valueOf(-1));
    public static final RuntimeGuessValue I0_CONSTANT= new RuntimeGuessValue(Type.INT_TYPE, Integer.valueOf(0));
    public static final RuntimeGuessValue I1_CONSTANT= new RuntimeGuessValue(Type.INT_TYPE, Integer.valueOf(1));
    public static final RuntimeGuessValue I2_CONSTANT= new RuntimeGuessValue(Type.INT_TYPE, Integer.valueOf(2));
    public static final RuntimeGuessValue I3_CONSTANT= new RuntimeGuessValue(Type.INT_TYPE, Integer.valueOf(3));
    public static final RuntimeGuessValue I4_CONSTANT= new RuntimeGuessValue(Type.INT_TYPE, Integer.valueOf(4));
    public static final RuntimeGuessValue I5_CONSTANT= new RuntimeGuessValue(Type.INT_TYPE, Integer.valueOf(5));
    
    public static final RuntimeGuessValue L0_CONSTANT= new RuntimeGuessValue(Type.LONG_TYPE, Long.valueOf(0L));
    public static final RuntimeGuessValue L1_CONSTANT= new RuntimeGuessValue(Type.LONG_TYPE, Long.valueOf(1L));
    
    public static final RuntimeGuessValue F0_CONSTANT= new RuntimeGuessValue(Type.FLOAT_TYPE, Float.valueOf(0F));
    public static final RuntimeGuessValue F1_CONSTANT= new RuntimeGuessValue(Type.FLOAT_TYPE, Float.valueOf(1F));
    public static final RuntimeGuessValue F2_CONSTANT= new RuntimeGuessValue(Type.FLOAT_TYPE, Float.valueOf(2F));
    
    public static final RuntimeGuessValue D0_CONSTANT= new RuntimeGuessValue(Type.DOUBLE_TYPE, Double.valueOf(0D));
    public static final RuntimeGuessValue D1_CONSTANT= new RuntimeGuessValue(Type.DOUBLE_TYPE, Double.valueOf(1D));
    
    public static final RuntimeGuessValue UNSPECIFIED_INT_VALUE = new RuntimeGuessValue(Type.INT_TYPE, null);
    public static final RuntimeGuessValue UNSPECIFIED_FLOAT_VALUE = new RuntimeGuessValue(Type.FLOAT_TYPE, null);
    public static final RuntimeGuessValue UNSPECIFIED_LONG_VALUE = new RuntimeGuessValue(Type.LONG_TYPE, null);
    public static final RuntimeGuessValue UNSPECIFIED_DOUBLE_VALUE = new RuntimeGuessValue(Type.DOUBLE_TYPE, null);
    
    private final Type _type;
    private final Object _value;
    
    public RuntimeGuessValue(Type type, Object value) {
        _type= type;
        _value= value;
    }
    
    public Type getType() {
        return _type;
    }
    
    public int getSize() {
        return _type == Type.LONG_TYPE || _type == Type.DOUBLE_TYPE ? 2 : 1;
    }
    
    public Object getValue() {
        return _value;
    }
    
    public boolean equals(final Object value) {
        if (value == this) {
            return true;
        } else if (value instanceof RuntimeGuessValue) {
            RuntimeGuessValue other= (RuntimeGuessValue) value;
            return typeEquals(other) && valueEquals(other);
        } else {
            return false;
        }
    }
    
    public boolean typeEquals(final RuntimeGuessValue other) {
        if (_type == null) {
            return other.getType() == null;
        } else {
            return _type.equals(other.getType());
        }
    }
    
    public boolean valueEquals(final RuntimeGuessValue other) {
        if (_value == null) {
            return other.getValue() == null;
        } else {
            return _value.equals(other.getValue());
        }
    }

    public int hashCode() {
        int valHash= _value == null ? 0 : _value.hashCode();
        return _type == null ? 0 : valHash ^ _type.hashCode();
    }
    
    public String toString() {
        return "{" + String.valueOf(_type) + " = " + String.valueOf(_value) + "}";
    }
}
