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
package de.jiac.micro.reflect;

import java.io.PrintStream;
import java.lang.reflect.Method;

import org.objectweb.asm.Type;

import de.dailab.jiac.common.aamm.beans.IndexedPropertyDescriptor;
import de.dailab.jiac.common.aamm.beans.MappedPropertyDescriptor;
import de.dailab.jiac.common.aamm.beans.MethodDescriptor;
import de.dailab.jiac.common.aamm.beans.PropertyDescriptor;
import de.jiac.micro.reflect.ClassInfoReducer.ReducedClassInfo;

/**
 * TODO: Das Ding hier zu warten ist die Pest. Hat jemand eine Idee wie man die
 * Codeerzeugung besser machen koennte? -- marcel
 * 
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ReflectorGenerator extends GeneratorUtil {
    public static final String SUPER_CLASS = "ReflectorStub";
    public static final String MY_PACKAGE = "de.jiac.micro.internal.latebind";
    public static final String MY_NAME = "Reflector";

    public static void generateReflector(PrintStream out, ReducedClassInfo[] classes) {
        printHeader(out);
        out.println("package " + MY_PACKAGE + ";");
        out.println("public final class " + MY_NAME + " extends " + SUPER_CLASS + " {");
        generateWriteProperty(out, "\t", classes);
        generateInvokeMethodWithDescriptor(out, "\t", classes);
        generateInvokeMethod(out, "\t", classes);
        out.println('}');
        out.flush();
    }

    private static void generateWriteProperty(PrintStream out, String indent, ReducedClassInfo[] classes) {
        String objParam = "obj";
        String propParam = "prop";
        String argsParam = "args";
        String myIndent = indent + "\t\t";

        out.println(indent + "protected void writeProperty0(Object " + objParam + ", String " + propParam
                + ", Object[] " + argsParam + ") throws Exception {");
        for (ReducedClassInfo current : classes) {
            if (current.hasWritablePropertiesForMask()) {
                String cast = getJavaSourceClassName(current.getClassDescriptor().getClazz());
                out.println(indent + "\tif(" + objParam + " instanceof " + cast + "){");
                for (PropertyDescriptor pd : current.getPropertyDescriptors()) {
                    generatePropertyWriteClause(out, myIndent, cast, objParam, propParam, argsParam, pd);
                }
                out.println(indent + "\t}");
            }
        }
        out.println(indent + "\tthrow new IllegalArgumentException(\"property '\" + " + propParam
                + " + \"' is either not defined or not writable!\");");
        out.println(indent + "}");
    }

    private static void generateInvokeMethodWithDescriptor(PrintStream out, String indent, ReducedClassInfo[] classes) {
        String objParam = "obj";
        String clsParam = "cls";
        String mNameParam= "mName";
        String mDescrParam= "mDescr";
        String argumentsParam = "arguments";
        
        out.println(indent + "protected Object invokeMethodWithDescriptor0(Object " + objParam + ", Class " + clsParam + ", String " + mNameParam + ", String " + mDescrParam + ", Object[] " + argumentsParam + ") throws Exception {");

        for(ReducedClassInfo ci : classes) {
            if(ci.needsMethodsWithDescriptors()) {
                MethodDescriptor[] mds= ci.getMethodDescriptors();
                if(mds == null || mds.length <= 0) {
                    continue;
                }
                
                final String fqCls= getJavaSourceClassName(ci.getClassDescriptor().getClazz());
                out.println(indent + "\tif(" + fqCls + ".class.isAssignableFrom(" + clsParam + ")) {");
                
                for(MethodDescriptor md : mds) {
                    Method m= md.getMethod();
                    out.println(indent + "\t\tif(\"" + m.getName() + "\".equals(" + mNameParam + ") && \"" + Type.getMethodDescriptor(md.getMethod()) + "\".equals(" + mDescrParam + ")) {");
                    
                    StringBuilder call= new StringBuilder();
                    call.append("((" + fqCls + ")" + objParam + ")." + m.getName() + "(");
                    
                    Class<?>[] pts= m.getParameterTypes();
                    for(int i= 0; i < pts.length; ++i) {
                        insertFromObjectConversion(call, pts[i], argumentsParam + "[" + i + "]");
                        
                        if(i < pts.length - 1) {
                            call.append(',');
                        }
                    }
                    
                    call.append(")");
                    
                    Class<?> rt= m.getReturnType();
                    if(rt == void.class) {
                        out.append(indent + "\t\t\t").append(call).println(';');
                        out.println(indent + "\t\t\treturn null;");
                    } else {
                        out.append(indent + "\t\t\treturn ");
                        insertToObjectConversion(out, rt, call);
                        out.println(';');
                    }
                    
                    out.println(indent + "\t\t}");
                }
                
                out.println(indent + "\t}");
            }
        }
        
        out.println(indent + "\tthrow new IllegalArgumentException(\"method not found\");");
        out.println(indent + "}");
    }
    
    private static void generateInvokeMethod(PrintStream out, String indent, ReducedClassInfo[] classes) {
        String objParam = "obj";
        String nameParam = "name";
        String argumentsParam = "arguments";
        
        out.println(indent + "protected Object invokeMethod0(Object " + objParam + ", String " + nameParam + ", Object[] "+ argumentsParam + ") throws Exception {");
        // TODO
        out.println(indent + "\tthrow new RuntimeException(\"unsupported operation\");");
        out.println(indent + "}");
    }

    private static void generatePropertyWriteClause(PrintStream out, String indent, String cast, String objParam,
            String propParam, String argsParam, PropertyDescriptor pd) {
        if (!pd.isWritable()) {
            return;
        }

        Method writeMethod = pd.getWriteMethod();
        Method twoArgWriteMethod= null;
        
        if(pd instanceof IndexedPropertyDescriptor) {
            twoArgWriteMethod= ((IndexedPropertyDescriptor) pd).getIndexedWriteMethod();
        } else if(pd instanceof MappedPropertyDescriptor) {
            twoArgWriteMethod= ((MappedPropertyDescriptor) pd).getMappedWriteMethod();
        }
        
        
        out.println(indent + "if(\"" + pd.getName() + "\".equals(" + propParam + ")){");
        if (writeMethod != null) {
            out.println(indent + "\tif(" + argsParam + ".length == 1){");
            out.append(indent + "\t((" + cast + ")" + objParam + ")." + writeMethod.getName() + "(");
            Class<?> valueType = writeMethod.getParameterTypes()[0];
            if (valueType.isPrimitive()) {
                insertWrap(out, valueType, argsParam + "[0]");
                out.println(");");
            } else if (valueType.isArray()) {
                Class<?> compType = valueType.getComponentType();
                String valueCast = "[]";

                while (compType.isArray()) {
                    valueCast = valueCast + "[]";
                    compType= compType.getComponentType();
                }

                valueCast = getJavaSourceClassName(compType) + valueCast;
                out.println("(" + valueCast + ")" + argsParam + "[0]);");
            } else {
                out.println("(" + getJavaSourceClassName(valueType) + ")" + argsParam + "[0]);");
            }
            out.println(indent + "\t\treturn;");
            out.println(indent + "\t}");
        }

        if (twoArgWriteMethod != null) {
            out.println(indent + "\tif(" + argsParam + ".length == 2){");
            out.append(indent + "\t\t((" + cast + ")" + objParam + ")." + twoArgWriteMethod.getName());
            
            if(twoArgWriteMethod.getParameterTypes()[0] == int.class) {
                out.append("(((Integer)" + argsParam + "[0]).intValue(), ");
            } else {
                out.append("((String) " + argsParam + "[0], ");
            }
            
            Class<?> valueType = twoArgWriteMethod.getParameterTypes()[1];
            if (valueType.isPrimitive()) {
                insertWrap(out, valueType, argsParam + "[1]");
                out.println(");");
            } else if (valueType.isArray()) {
                Class<?> compType = valueType.getComponentType();
                String valueCast = "[]";

                while (compType.isArray()) {
                    valueCast = valueCast + "[]";
                    compType= compType.getComponentType();
                }

                valueCast = getJavaSourceClassName(compType) + valueCast;
                out.println("(" + valueCast + ")" + argsParam + "[1]);");
            } else {
                out.println("(" + getJavaSourceClassName(valueType) + ")" + argsParam + "[1]);");
            }
            out.println(indent + "\t\treturn;");
            out.println(indent + "\t}");
        }
        out.println(indent + "}");
    }
    
    private static void insertWrap(PrintStream out, Class<?> argType, String variable) {
        out.append(variable + ".getClass() == String.class ? ");

        if (argType == boolean.class) {
            out.append("(((String)" + variable + ").charAt(0) == 't' ? true : false) : " + "((Boolean)" + variable
                    + ").booleanValue()");
        } else if (argType == byte.class) {
            out.append("Byte.parseByte((String)" + variable + ") : " + "((Byte)" + variable + ").byteValue()");
        } else if (argType == char.class) {
            out.append("((String)" + variable + ").charAt(0) : " + "((Character)" + variable + ").charValue()");
        } else if (argType == double.class) {
            out.append("Double.parseDouble((String)" + variable + ") : " + "((Double)" + variable + ").doubleValue()");
        } else if (argType == float.class) {
            out.append("Float.parseFloat((String)" + variable + ") : " + "((Float)" + variable + ").floatValue()");
        } else if (argType == int.class) {
            out.append("Integer.parseInt((String)" + variable + ") : " + "((Integer)" + variable + ").intValue()");
        } else if (argType == long.class) {
            out.append("Long.parseLong((String)" + variable + ") : " + "((Long)" + variable + ").longValue()");
        } else if (argType == short.class) {
            out.append("Short.parseShort((String)" + variable + ") : " + "((Short)" + variable + ").shortValue()");
        }
    }
}
