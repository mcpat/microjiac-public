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

import java.util.HashSet;
import java.util.List;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.Value;

import de.jiac.micro.config.analysis.ClassInfoPool.ClassInfoCreator;


/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
final class MethodAnalyser extends MethodAdapter {
    protected final static HashSet<String> HANDLE_SOURCES;
    
    protected final static MethodKey GET_HANDLE= new MethodKey("getHandle", "()Lde/jiac/micro/core/IHandle;");
    protected final static MethodKey GET_NODE_HANDLE= new MethodKey("getNodeHandle", "()Lde/jiac/micro/core/IHandle;");
    protected final static MethodKey ADD_HANDLES_ON= new MethodKey("addHandlesOn", "(Lde/jiac/micro/core/scope/AgentScope;)V");
    
    static {
        HANDLE_SOURCES= new HashSet<String>();
        HANDLE_SOURCES.add("de/jiac/micro/core/scope/Scope");
        HANDLE_SOURCES.add("de/jiac/micro/core/IAgent");
        HANDLE_SOURCES.add("de/jiac/micro/core/INode");
        HANDLE_SOURCES.add("de/jiac/micro/core/IContainer");
        HANDLE_SOURCES.add("de/jiac/micro/core/AbstractContainer");
    }
    
    protected final ClassInfoCreator parent;
    protected final MethodKey methodKey;
    
    MethodAnalyser(ClassInfoCreator parent, int access, String name, String desc, String signature, String[] exceptions) {
        super(new MethodNode(access, name, desc, signature, exceptions));
        this.parent= parent;
        this.methodKey= new MethodKey(name, desc);
    }
    
    @Override
    public void visitEnd() {
        final HashSet<String> obtainedHandles= new HashSet<String>();
        final ClassInfo ci= parent.classInfo;
        final MethodNode method= (MethodNode) mv;
        
        ci.referencedClassesInMethods.put(methodKey, new HashSet<String>());
        
        // insert argument types
        processType(Type.getReturnType(method.desc));
        for(Type type : Type.getArgumentTypes(method.desc)) {
            processType(type);
        }
        
        Interpreter interpreter= new BasicGuesser() {
            @Override
            public Value newOperation(AbstractInsnNode insn) {
                final int op= insn.getOpcode();
                String className= null;
                
                if(op == GETSTATIC) {
                    className= ((FieldInsnNode) insn).owner.replace('/', '.');
                } else if(op == NEW) {
                    className= Type.getObjectType(((TypeInsnNode) insn).desc).getClassName();
                }
                
                parent.registerDependencyForMethod(methodKey, className);
                return super.newOperation(insn);
            }
            
            @Override
            public Value unaryOperation(AbstractInsnNode insn, Value value) throws AnalyzerException {
                if(insn.getOpcode() == ARETURN) {
                    if(methodKey.equals(GET_HANDLE) || methodKey.equals(GET_NODE_HANDLE)) {
                        RuntimeGuessValue guessedValue= (RuntimeGuessValue) value;
                        Type type= guessedValue.getType();
                        
                        if(type != null && guessedValue != RuntimeGuessValue.NULL_CONSTANT) {
                            parent.classInfo.directHandle= type.getClassName();
                        }
                    }
                }
                return super.unaryOperation(insn, value);
            }

            @Override
            public Value naryOperation(AbstractInsnNode insn, List values) throws AnalyzerException {
                if(insn instanceof MethodInsnNode) {
                    MethodInsnNode minsn= (MethodInsnNode) insn;
                    final int opCode= minsn.getOpcode();
                    if( minsn.desc.endsWith(")Ljava/lang/Class;") && (
                            minsn.name.equals("class$") || 
                            (minsn.owner.equals("java/lang/Class") && minsn.name.equals("forName")) ||
                            (minsn.owner.endsWith("ClassLoader") && minsn.name.equals("loadClass"))
                    )) {
                        // pattern for class loading
                        RuntimeGuessValue guessedValue= (RuntimeGuessValue) values.get(opCode == Opcodes.INVOKESTATIC ? 0 : 1);
                        String className= (String) guessedValue.getValue();
                        
                        if(className != null) {
                            return new RuntimeGuessValue(Type.getObjectType("java/lang/Class"), className);
                        }
                    } else if(HANDLE_SOURCES.contains(minsn.owner) && minsn.desc.equals("(Ljava/lang/Class;)Lde/jiac/micro/core/IHandle;") && (minsn.name.equals("getHandle") || minsn.name.equals("getScopeHandle"))) {
                        // pattern for obtaining a handle
                        RuntimeGuessValue guessedValue= (RuntimeGuessValue) values.get(opCode == Opcodes.INVOKESTATIC ? 0 : 1);
                        String className= (String) guessedValue.getValue();
                        
                        if(className != null) {
                            obtainedHandles.add(className);
                            parent.registerDependencyForMethod(methodKey, className);
                            String internalName= className.replace('.', '/');
                            return new RuntimeGuessValue(Type.getObjectType(internalName), null);
                        }
                    } else if(HANDLE_SOURCES.contains(minsn.owner) && minsn.name.equals("addHandle") && minsn.desc.equals("(Lde/jiac/micro/core/IHandle;)V")) {
                        // pattern for adding a handle explicitly
                        RuntimeGuessValue guessValue= (RuntimeGuessValue) values.get(1);
                        Type type= guessValue.getType();
                        
                        if(type != null) {
                            parent.registerDependencyForMethod(methodKey, type.getClassName());
                            parent.classInfo.indirectHandles.add(type.getClassName());
                        }
                    }
                }
                
                return super.naryOperation(insn, values);
            }
        };
        
        Analyzer a= new Analyzer(interpreter);
        try {
            a.analyze(parent.owner, method);
        } catch (AnalyzerException e) {
            e.printStackTrace();
        }
        
        if(obtainedHandles.size() > 0) {
            ci.referencedHandlesInMethods.put(methodKey, obtainedHandles);
        }
    }
    
    private void processType(Type type) {
        if(type.getSort() == Type.ARRAY) {
            type= type.getElementType();
        }
        
        if(type.getSort() == Type.OBJECT) {
            parent.registerDependencyForMethod(methodKey, type.getClassName());
        }
    }
}
