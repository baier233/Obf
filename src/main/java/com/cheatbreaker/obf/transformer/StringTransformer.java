/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2018 CheatBreaker, LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.cheatbreaker.obf.transformer;

import com.cheatbreaker.obf.Obf;
import com.cheatbreaker.obf.utils.AsmUtils;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class StringTransformer extends Transformer {

    private static final int PARTITION_BITS = 10;
    private static final int PARTITION_SIZE = 1 << PARTITION_BITS;
    private static final int PARTITION_MASK = PARTITION_SIZE - 1;
    private List<String> strings = new ArrayList<>();
    private List<long[]> longs = new ArrayList<>();

    public StringTransformer(Obf obf) {
        super(obf);
    }

    private long[] stringToLongArray(String str) {
        byte[] bytes = str.getBytes();
        int length = (bytes.length + 7) / 8;
        long[] longArray = new long[length];

        for (int i = 0; i < bytes.length; i++) {
            int byteIndex = i / 8;
            int bitIndex = i % 8;
            long mask = (long) bytes[i] << (bitIndex * 8);
            longArray[byteIndex] |= mask;
        }

        return longArray;
    }

    @Override
    public void visit(ClassNode classNode) {

        for (MethodNode method : classNode.methods) {
            for (Iterator<AbstractInsnNode> iter = method.instructions.iterator(); iter.hasNext(); ) {
                AbstractInsnNode insn = iter.next();
                if (insn.getOpcode() == Opcodes.LDC) {
                    LdcInsnNode ldc = (LdcInsnNode) insn;
                    if (ldc.cst instanceof String) {
                        String string = (String) ldc.cst;

                        long[] longArray = stringToLongArray(string);
                        int id = longs.indexOf(longArray);
                        if (id == -1) {
                            id = longs.size();
                            longs.add(longArray);
                        }
                        System.out.println(longs.size());
                        int index = id & PARTITION_MASK;
                        int classId = id >> PARTITION_BITS;
                        int mask = (short) random.nextInt();
                        int a = (short) random.nextInt() & mask | index;
                        int b = (short) random.nextInt() & ~mask | index;
                        method.instructions.insertBefore(insn, new FieldInsnNode(Opcodes.GETSTATIC, "generated/Longs" + classId, "Longs", "[[J"));
                        method.instructions.insertBefore(insn, AsmUtils.pushInt(a));
                        method.instructions.insertBefore(insn, AsmUtils.pushInt(b));
                        method.instructions.insertBefore(insn, new InsnNode(Opcodes.IAND));
                        method.instructions.insertBefore(insn, new InsnNode(Opcodes.AALOAD));
                        method.instructions.insertBefore(insn,new MethodInsnNode(INVOKESTATIC, "generated/Longs" + classId, "decrypt", "([J)Ljava/lang/String;", false));
                        iter.remove();
                    }
                }
            }
        }
    }

    private void buildDecryptMethod(MethodNode decrypt){
        Label label0 = new Label();
        decrypt.visitLabel(label0);
        decrypt.visitLineNumber(29, label0);
        decrypt.visitTypeInsn(NEW, "java/lang/StringBuilder");
        decrypt.visitInsn(DUP);
        decrypt.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
        decrypt.visitVarInsn(ASTORE, 1);
        Label label1 = new Label();
        decrypt.visitLabel(label1);
        decrypt.visitLineNumber(30, label1);
        decrypt.visitVarInsn(ALOAD, 0);
        decrypt.visitVarInsn(ASTORE, 2);
        decrypt.visitVarInsn(ALOAD, 2);
        decrypt.visitInsn(ARRAYLENGTH);
        decrypt.visitVarInsn(ISTORE, 3);
        decrypt.visitInsn(ICONST_0);
        decrypt.visitVarInsn(ISTORE, 4);
        Label label2 = new Label();
        decrypt.visitLabel(label2);
        decrypt.visitFrame(Opcodes.F_FULL, 5, new Object[]{"[J", "java/lang/StringBuilder", "[J", Opcodes.INTEGER, Opcodes.INTEGER}, 0, new Object[]{});
        decrypt.visitVarInsn(ILOAD, 4);
        decrypt.visitVarInsn(ILOAD, 3);
        Label label3 = new Label();
        decrypt.visitJumpInsn(IF_ICMPGE, label3);
        decrypt.visitVarInsn(ALOAD, 2);
        decrypt.visitVarInsn(ILOAD, 4);
        decrypt.visitInsn(LALOAD);
        decrypt.visitVarInsn(LSTORE, 5);
        Label label4 = new Label();
        decrypt.visitLabel(label4);
        decrypt.visitLineNumber(31, label4);
        decrypt.visitInsn(ICONST_0);
        decrypt.visitVarInsn(ISTORE, 7);
        Label label5 = new Label();
        decrypt.visitLabel(label5);
        decrypt.visitFrame(Opcodes.F_APPEND, 2, new Object[]{Opcodes.LONG, Opcodes.INTEGER}, 0, null);
        decrypt.visitVarInsn(ILOAD, 7);
        decrypt.visitIntInsn(BIPUSH, 8);
        Label label6 = new Label();
        decrypt.visitJumpInsn(IF_ICMPGE, label6);
        Label label7 = new Label();
        decrypt.visitLabel(label7);
        decrypt.visitLineNumber(32, label7);
        decrypt.visitVarInsn(ILOAD, 7);
        decrypt.visitInsn(ICONST_3);
        decrypt.visitInsn(ISHL);
        decrypt.visitVarInsn(ISTORE, 8);
        Label label8 = new Label();
        decrypt.visitLabel(label8);
        decrypt.visitLineNumber(33, label8);
        decrypt.visitVarInsn(LLOAD, 5);
        decrypt.visitVarInsn(ILOAD, 8);
        decrypt.visitInsn(LSHR);
        decrypt.visitLdcInsn(255L);
        decrypt.visitInsn(LAND);
        decrypt.visitInsn(L2I);
        decrypt.visitInsn(I2C);
        decrypt.visitVarInsn(ISTORE, 9);
        Label label9 = new Label();
        decrypt.visitLabel(label9);
        decrypt.visitLineNumber(34, label9);
        decrypt.visitVarInsn(ILOAD, 9);
        Label label10 = new Label();
        decrypt.visitJumpInsn(IFEQ, label10);
        Label label11 = new Label();
        decrypt.visitLabel(label11);
        decrypt.visitLineNumber(35, label11);
        decrypt.visitVarInsn(ALOAD, 1);
        decrypt.visitVarInsn(ILOAD, 9);
        decrypt.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
        decrypt.visitInsn(POP);
        decrypt.visitLabel(label10);
        decrypt.visitLineNumber(31, label10);
        decrypt.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        decrypt.visitIincInsn(7, 1);
        decrypt.visitJumpInsn(GOTO, label5);
        decrypt.visitLabel(label6);
        decrypt.visitLineNumber(30, label6);
        decrypt.visitFrame(Opcodes.F_CHOP, 2, null, 0, null);
        decrypt.visitIincInsn(4, 1);
        decrypt.visitJumpInsn(GOTO, label2);
        decrypt.visitLabel(label3);
        decrypt.visitLineNumber(39, label3);
        decrypt.visitFrame(Opcodes.F_CHOP, 3, null, 0, null);
        decrypt.visitVarInsn(ALOAD, 1);
        decrypt.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        decrypt.visitInsn(ARETURN);
        Label label12 = new Label();
        decrypt.visitLabel(label12);
        decrypt.visitLocalVariable("shift", "I", null, label8, label10, 8);
        decrypt.visitLocalVariable("ch", "C", null, label9, label10, 9);
        decrypt.visitLocalVariable("count", "I", null, label5, label6, 7);
        decrypt.visitLocalVariable("num", "J", null, label4, label6, 5);
        decrypt.visitLocalVariable("longArray", "[J", null, label0, label12, 0);
        decrypt.visitLocalVariable("sb", "Ljava/lang/StringBuilder;", null, label1, label12, 1);


    }
    public void after() {
        for (int classId = 0; classId <= strings.size() >> PARTITION_BITS; classId++) {
            ClassNode classNode = new ClassNode();
            classNode.version = Opcodes.V1_8;
            classNode.access = Opcodes.ACC_PUBLIC;
            classNode.name = "generated/Longs" + classId;
            classNode.superName = "java/lang/Object";
            FieldNode fieldNode = new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "Longs", "[[J", null, null);
            classNode.fields.add(fieldNode);

            MethodNode clinit = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            MethodNode decrypt = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "decrypt", "([J)Ljava/lang/String;", null, null);


            classNode.methods.add(clinit);
            classNode.methods.add(decrypt);

            buildDecryptMethod(decrypt);

            int start = classId << PARTITION_BITS;
            int end = Math.min(start + PARTITION_SIZE, longs.size());
            clinit.instructions.add(AsmUtils.pushInt(end-start));
            clinit.instructions.add(new TypeInsnNode(Opcodes.ANEWARRAY, "[J"));

            for (int id = start; id < end ; id++) {
                clinit.instructions.add(new InsnNode(Opcodes.DUP));
                clinit.instructions.add(AsmUtils.pushInt(id & PARTITION_MASK ));
                long[] longArray = longs.get(id);
                clinit.instructions.add(AsmUtils.pushInt(longArray.length));
                clinit.visitIntInsn(NEWARRAY, T_LONG);
                for (int i = 0; i < longArray.length; i++) {
                    clinit.instructions.add(new InsnNode(Opcodes.DUP));
                    clinit.instructions.add(AsmUtils.pushInt(i & PARTITION_MASK ));
                    clinit.instructions.add(new LdcInsnNode(longArray[i]));
                    clinit.instructions.add(new InsnNode(Opcodes.LASTORE));
                }
                clinit.instructions.add(new InsnNode(Opcodes.AASTORE));
            }

            clinit.instructions.add(new FieldInsnNode(Opcodes.PUTSTATIC, classNode.name, "Longs", "[[J") );

            clinit.instructions.add(new InsnNode(Opcodes.RETURN));

            obf.addNewClass(classNode);
        }
    }


}
