/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Config.
 *
 * Copyright (c) 2007-2011 DAI-Labor, Technische Universität Berlin
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

import java.util.HashSet;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.commons.EmptyVisitor;


/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
class ComponentAnalyser implements ClassVisitor, FieldVisitor, Opcodes {
    protected final static HashSet<String> HANDLE_SOURCES;
    
    static {
        HANDLE_SOURCES= new HashSet<String>();
        HANDLE_SOURCES.add("de/jiac/micro/core/scope/Scope");
        HANDLE_SOURCES.add("de/jiac/micro/core/IAgent");
        HANDLE_SOURCES.add("de/jiac/micro/core/INode");
        HANDLE_SOURCES.add("de/jiac/micro/core/IContainer");
    }
    
    private static abstract class MethodAnalyser extends EmptyVisitor {
        protected AnalyzerAdapter stackAnalyzer;
        
        protected MethodAnalyser() {}
        
        protected final MethodVisitor reset(String owner, int access, String name, String desc) {
            stackAnalyzer= new AnalyzerAdapter(owner, access, name, desc, this);
            return stackAnalyzer;
        }
    }
    
    private final class HandleGetterVisitor extends MethodAnalyser {
        protected HandleGetterVisitor() {}

        @Override
        public void visitInsn(int opcode) {
            if(opcode == ARETURN) {
                List stack= stackAnalyzer.stack;
                Object s= stack.get(stack.size() - 1);
                
                if(s instanceof Type) {
                    providedHandleClassName= ((Type) s).getClassName();
                } else {
                    providedHandleClassName= (String) s;
                }
            }
        }
    }
    
    private final class NormalMethodVisitor extends MethodAnalyser {
        private String _handleClassName;
        private boolean _awaitClassName;
        
        protected NormalMethodVisitor() {}
        
        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            if(_awaitClassName) {
                return;
            }
            
            if(opcode == GETSTATIC && name.startsWith("class$") && desc.equals("Ljava/lang/Class;")) {
                // class version < 49.0
                _awaitClassName= true;
            }
        }

        @Override
        public void visitLdcInsn(Object cst) {
            if(_awaitClassName) {
                _handleClassName= (String) cst;
                _awaitClassName= false;
            } else if(cst instanceof Type) {
                _handleClassName= ((Type) cst).getClassName();
            }
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            if(HANDLE_SOURCES.contains(owner) && desc.equals("(Ljava/lang/Class;)Lde/jiac/micro/core/IHandle;")) {
                if((opcode == INVOKESTATIC && name.equals("getScopeHandle")) || (opcode == INVOKEINTERFACE && name.equals("getHandle"))) {
                    requiredHandles.add(_handleClassName);
                    _handleClassName= null;
                }
            }
        }
    }
    
    private final HandleGetterVisitor _handleGetterVisitor= new HandleGetterVisitor();
    private final NormalMethodVisitor _normalMethodVisitor= new NormalMethodVisitor();
    
    private String _componentClassName;
    
    
    protected HashSet<String> requiredHandles= new HashSet<String>();
    protected String providedHandleClassName;
    
    public void clear() {
        requiredHandles= new HashSet<String>();
        providedHandleClassName= null;
    }
    
    /////////////////////
    // CLASS VISITOR
    ///////////////////
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        _componentClassName= name;
    }


    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return this;
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access) {}

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor result;
        
        if(desc.equals("()Lde/jiac/micro/core/IHandle;") && (name.equals("getHandle") || name.equals("getScopeHandle"))) {
            result= _handleGetterVisitor.reset(_componentClassName, access, name, desc);
        } else {
            result= _normalMethodVisitor.reset(_componentClassName, access, name, desc);
        }
        
        return result;
    }

    public void visitOuterClass(String owner, String name, String desc) {}

    public void visitSource(String source, String debug) {}
    
    /////////////////////
    // GENERIC
    ///////////////////
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return null;
    }
    
    public void visitAttribute(Attribute attr) {}
    
    public void visitEnd() {}
}
