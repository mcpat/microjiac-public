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
package de.jiac.micro.reflect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;

import org.apache.maven.plugin.logging.Log;
import org.objectweb.asm.Type;

import de.dailab.jiac.common.aamm.beans.MethodDescriptor;
import de.jiac.micro.reflect.ClassInfoReducer.ReducedClassInfo;
import de.jiac.micro.reflect.filter.IFilter;
import de.jiac.micro.reflect.filter.ServiceFilter;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class ServiceContextGenerator extends GeneratorUtil {
    private static final String EMULATION_CLASS_PREFIX= "ContextFor_";
    private static final String SUPER_CLASS= "de.jiac.micro.ext.service.impl.EmulatedProxyServiceContext";
        
    public static void generateContexts(Log logger, File outputFolder, ReducedClassInfo[] classes) throws IOException {
        for(ReducedClassInfo rci : classes) {
            Class<?> cls= rci.getClassDescriptor().getClazz();
            if((rci.mask & IFilter.CONTEXT) != 0 && !cls.getName().equals(ServiceFilter.ISERVICE_CLASS_NAME)) {
                
                StringBuilder contextName= new StringBuilder(cls.getName().replace('$', '_'));
                int dot= contextName.lastIndexOf(".");
                contextName.insert(dot < 0 ? 0 : dot + 1, EMULATION_CLASS_PREFIX);
                
                logger.info("generate context: " + contextName);

                File packageFolder= outputFolder;
                String packStr= null;
                if(dot > 0) {
                    packStr= contextName.substring(0, dot);
                    packageFolder= new File(outputFolder, packStr.replace('.', File.separatorChar));
                }
                
                if(!packageFolder.exists()) {
                    packageFolder.mkdirs();
                }
                
                String clsName= contextName.substring(dot < 0 ? 0 : dot + 1);
                
                File contextFile= new File(packageFolder, clsName + ".java");
                contextFile.createNewFile();
                
                PrintStream out= new PrintStream(new FileOutputStream(contextFile));
                generateContext(out, packStr, clsName, rci);
                out.flush();
                out.close();
            }
        }
    }

    private static void generateContext(PrintStream out, String pack, String name, ReducedClassInfo rci) {
        printHeader(out);
        
        if(pack != null) {
            out.println("package " + pack + ";");
        }
        String serviceName= getJavaSourceClassName(rci.getClassDescriptor().getClazz());
        out.println("public final class " + name + " extends " + SUPER_CLASS + " implements " + serviceName + " {");
        
        // constructor
        out.println("\tpublic " + name + "() {");
        out.println("\t\tsuper(" + serviceName + ".class);");
        out.println("\t}");

        // methods
        for(MethodDescriptor md : rci.originalInfo.getMethodDescriptors()) {
            Method m= md.getMethod();
            String mDescr= Type.getMethodDescriptor(m);
            
            if(!m.getName().equals("doGetDescription") && !mDescr.equals("()Ljava/lang/String;")) {
                Type rType= Type.getReturnType(m);
                Class<?>[] aClasses= m.getParameterTypes();
                Type[] aTypes= Type.getArgumentTypes(m);
                
                out.append("\tpublic " + rType.getClassName() + " " + m.getName() + "(");
                
                for(int i= 0; i < aTypes.length; ++i) {
                    out.append(aTypes[i].getClassName() + " arg" + i);
                    
                    if(i < aTypes.length - 1) {
                        out.append(", ");
                    }
                }
                out.println(") {");
                
                if(!m.getName().startsWith("do")) {
                    out.println("\t\tthrow new RuntimeException(\"unsupported operation " + m.getName() + "\");");
                } else {
                    StringBuilder call= new StringBuilder();
                    
                    call.append("searchAndInvoke(\"" + m.getName() + "\", \"" + mDescr + "\", ");
                    
                    if(aClasses.length <= 0) {
                        call.append("null)");
                    } else {
                        call.append("new Object[]{");
                        for(int i= 0; i < aClasses.length; ++i) {
                            insertToObjectConversion(call, aClasses[i], "arg" + i);
                            
                            if(i < aClasses.length - 1) {
                                call.append(", ");
                            }
                        }
                        call.append("})");
                    }
                    
                    if(rType.getSort() != Type.VOID) {
                        out.print("\t\treturn ");
                        insertFromObjectConversion(out, m.getReturnType(), call);
                        out.println(";");
                    } else {
                        out.append("\t\t").append(call);
                        out.println(";");
                    }
                }
                
                out.println("\t}");
            }
        }
        
        out.println("}");
    }
}
