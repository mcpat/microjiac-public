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

import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.Value;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class BasicGuesser implements Interpreter, Opcodes {
    public Value newValue(Type type) {
        if (type == null) {
            return RuntimeGuessValue.UNINITIALISED_VALUE;
        }
        switch (type.getSort()) {
            case Type.VOID:
                return null;
            case Type.BOOLEAN:
            case Type.CHAR:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
                return RuntimeGuessValue.UNSPECIFIED_INT_VALUE;
            case Type.FLOAT:
                return RuntimeGuessValue.UNSPECIFIED_FLOAT_VALUE;
            case Type.LONG:
                return RuntimeGuessValue.UNSPECIFIED_LONG_VALUE;
            case Type.DOUBLE:
                return RuntimeGuessValue.UNSPECIFIED_DOUBLE_VALUE;
            case Type.ARRAY:
            case Type.OBJECT:
                return new RuntimeGuessValue(type, null);
            default:
                throw new Error("Internal error");
        }
    }

    public Value newOperation(final AbstractInsnNode insn) {
        switch (insn.getOpcode()) {
            case ACONST_NULL:
                return RuntimeGuessValue.NULL_CONSTANT;
            case ICONST_M1:
                return RuntimeGuessValue.IM1_CONSTANT;
            case ICONST_0:
                return RuntimeGuessValue.I0_CONSTANT;
            case ICONST_1:
                return RuntimeGuessValue.I1_CONSTANT;
            case ICONST_2:
                return RuntimeGuessValue.I2_CONSTANT;
            case ICONST_3:
                return RuntimeGuessValue.I3_CONSTANT;
            case ICONST_4:
                return RuntimeGuessValue.I4_CONSTANT;
            case ICONST_5:
                return RuntimeGuessValue.I5_CONSTANT;
            case LCONST_0:
                return RuntimeGuessValue.L0_CONSTANT;
            case LCONST_1:
                return RuntimeGuessValue.L1_CONSTANT;
            case FCONST_0:
                return RuntimeGuessValue.F0_CONSTANT;
            case FCONST_1:
                return RuntimeGuessValue.F1_CONSTANT;
            case FCONST_2:
                return RuntimeGuessValue.F2_CONSTANT;
            case DCONST_0:
                return RuntimeGuessValue.D0_CONSTANT;
            case DCONST_1:
                return RuntimeGuessValue.D1_CONSTANT;
            case BIPUSH:
            case SIPUSH:
                return new RuntimeGuessValue(Type.INT_TYPE, Integer.valueOf(((IntInsnNode) insn).operand));
            case LDC:
                Object cst = ((LdcInsnNode) insn).cst;
                if (cst instanceof Integer) {
                    return new RuntimeGuessValue(Type.INT_TYPE, cst);
                } else if (cst instanceof Float) {
                    return new RuntimeGuessValue(Type.FLOAT_TYPE, cst);
                } else if (cst instanceof Long) {
                    return new RuntimeGuessValue(Type.LONG_TYPE, cst);
                } else if (cst instanceof Double) {
                    return new RuntimeGuessValue(Type.DOUBLE_TYPE, cst);
                } else if (cst instanceof Type) {
                    return new RuntimeGuessValue(Type.getObjectType("java/lang/Class"), ((Type) cst).getClassName());
                } else {
                    return new RuntimeGuessValue(Type.getType(cst.getClass()), cst);
                }
            case JSR:
                return RuntimeGuessValue.RETURNADDRESS_VALUE;
            case GETSTATIC:
                return newValue(Type.getType(((FieldInsnNode) insn).desc));
            case NEW:
                return newValue(Type.getObjectType(((TypeInsnNode) insn).desc));
            default:
                throw new Error("Internal error.");
        }
    }

    public Value copyOperation(final AbstractInsnNode insn, final Value value)
            throws AnalyzerException
    {
        return value;
    }

    public Value unaryOperation(final AbstractInsnNode insn, final Value value)
            throws AnalyzerException
    {
        switch (insn.getOpcode()) {
            case INEG:
            case IINC:
            case L2I:
            case F2I:
            case D2I:
            case I2B:
            case I2C:
            case I2S:
                return RuntimeGuessValue.UNSPECIFIED_INT_VALUE;
            case FNEG:
            case I2F:
            case L2F:
            case D2F:
                return RuntimeGuessValue.UNSPECIFIED_FLOAT_VALUE;
            case LNEG:
            case I2L:
            case F2L:
            case D2L:
                return RuntimeGuessValue.UNSPECIFIED_LONG_VALUE;
            case DNEG:
            case I2D:
            case L2D:
            case F2D:
                return RuntimeGuessValue.UNSPECIFIED_DOUBLE_VALUE;
            case IFEQ:
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE:
            case TABLESWITCH:
            case LOOKUPSWITCH:
            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN:
            case PUTSTATIC:
                return null;
            case GETFIELD:
                return newValue(Type.getType(((FieldInsnNode) insn).desc));
            case NEWARRAY:
                switch (((IntInsnNode) insn).operand) {
                    case T_BOOLEAN:
                        return newValue(Type.getType("[Z"));
                    case T_CHAR:
                        return newValue(Type.getType("[C"));
                    case T_BYTE:
                        return newValue(Type.getType("[B"));
                    case T_SHORT:
                        return newValue(Type.getType("[S"));
                    case T_INT:
                        return newValue(Type.getType("[I"));
                    case T_FLOAT:
                        return newValue(Type.getType("[F"));
                    case T_DOUBLE:
                        return newValue(Type.getType("[D"));
                    case T_LONG:
                        return newValue(Type.getType("[J"));
                    default:
                        throw new AnalyzerException("Invalid array type");
                }
            case ANEWARRAY:
                String desc = ((TypeInsnNode) insn).desc;
                return newValue(Type.getType("[" + Type.getObjectType(desc)));
            case ARRAYLENGTH:
                return RuntimeGuessValue.UNSPECIFIED_INT_VALUE;
            case ATHROW:
                return null;
            case CHECKCAST:
                desc = ((TypeInsnNode) insn).desc;
                return newValue(Type.getObjectType(desc));
            case INSTANCEOF:
                return RuntimeGuessValue.UNSPECIFIED_INT_VALUE;
            case MONITORENTER:
            case MONITOREXIT:
            case IFNULL:
            case IFNONNULL:
                return null;
            default:
                throw new Error("Internal error.");
        }
    }

    public Value binaryOperation(
        final AbstractInsnNode insn,
        final Value value1,
        final Value value2) throws AnalyzerException
    {
        switch (insn.getOpcode()) {
            case IALOAD:
            case BALOAD:
            case CALOAD:
            case SALOAD:
            case IADD:
            case ISUB:
            case IMUL:
            case IDIV:
            case IREM:
            case ISHL:
            case ISHR:
            case IUSHR:
            case IAND:
            case IOR:
            case IXOR:
                return RuntimeGuessValue.UNSPECIFIED_INT_VALUE;
            case FALOAD:
            case FADD:
            case FSUB:
            case FMUL:
            case FDIV:
            case FREM:
                return RuntimeGuessValue.UNSPECIFIED_FLOAT_VALUE;
            case LALOAD:
            case LADD:
            case LSUB:
            case LMUL:
            case LDIV:
            case LREM:
            case LSHL:
            case LSHR:
            case LUSHR:
            case LAND:
            case LOR:
            case LXOR:
                return RuntimeGuessValue.UNSPECIFIED_LONG_VALUE;
            case DALOAD:
            case DADD:
            case DSUB:
            case DMUL:
            case DDIV:
            case DREM:
                return RuntimeGuessValue.UNSPECIFIED_DOUBLE_VALUE;
            case AALOAD:
                return new RuntimeGuessValue(Type.getObjectType("java/lang/Object"), null); // FIXME: type!
            case LCMP:
            case FCMPL:
            case FCMPG:
            case DCMPL:
            case DCMPG:
                return RuntimeGuessValue.UNSPECIFIED_INT_VALUE;
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE:
            case IF_ACMPEQ:
            case IF_ACMPNE:
            case PUTFIELD:
                return null;
            default:
                throw new Error("Internal error.");
        }
    }

    public Value ternaryOperation(
        final AbstractInsnNode insn,
        final Value value1,
        final Value value2,
        final Value value3) throws AnalyzerException
    {
        return null;
    }

    public Value naryOperation(final AbstractInsnNode insn, final List values)
            throws AnalyzerException
    {
        if (insn.getOpcode() == MULTIANEWARRAY) {
            return newValue(Type.getType(((MultiANewArrayInsnNode) insn).desc));
        } else {
            return newValue(Type.getReturnType(((MethodInsnNode) insn).desc));
        }
    }

    public Value merge(final Value v, final Value w) {
        return internalMerge((RuntimeGuessValue) v, (RuntimeGuessValue) w);
    }
    
    private RuntimeGuessValue internalMerge(RuntimeGuessValue v, RuntimeGuessValue w) {
        if (!v.typeEquals(w)) {
            return RuntimeGuessValue.UNINITIALISED_VALUE;
        }
        
        if(v.valueEquals(w)) {
            return v;
        }
        
        return v.getValue() != null ? v : w;
    }
}
