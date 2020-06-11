package club.sk1er.mods.sk1eroldanimations.asm;

import club.sk1er.mods.sk1eroldanimations.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

public class GuiIngameForgeTransformer implements ITransformer {

    @Override
    public String[] getClassName() {
        return new String[]{"net.minecraftforge.client.GuiIngameForge"};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {
            String methodName = mapMethodName(classNode, methodNode);

            if (methodName.equals("renderHealth")) {
                int highlightIndex = -1;
                for (LocalVariableNode localVariableNode : methodNode.localVariables) {
                    if (localVariableNode.name.equals("highlight")) {
                        highlightIndex = localVariableNode.index;
                        break;
                    }
                }
                ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                LabelNode veryEnd = new LabelNode();
                boolean foundFirst = false;
                while (iterator.hasNext()) {
                    AbstractInsnNode node = iterator.next();

                    if (node.getOpcode() == Opcodes.INVOKESTATIC) {
                        String nodeName = mapMethodNameFromNode(node);
                        if (!foundFirst && (nodeName.equals("ceiling_float_int") || nodeName.equals("func_76123_f"))) {
                            foundFirst = true;
                            AbstractInsnNode pos = node.getNext().getNext().getNext();
                            methodNode.instructions.insert(pos, jumpIfOldHealth(veryEnd));
                        }
                    } else if (node.getOpcode() == Opcodes.ISTORE && ((VarInsnNode) node).var == highlightIndex) {
                        methodNode.instructions.insertBefore(node, veryEnd);
                    }
                }
            } else if (methodNode.name.equals("renderHUDText")) {
                ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();

                while (iterator.hasNext()) {
                    AbstractInsnNode next = iterator.next();

                    if (next instanceof MethodInsnNode) {
                        if (next.getOpcode() == Opcodes.INVOKESTATIC) {
                            String methodInsnName = mapMethodNameFromNode(next);

                            if (methodInsnName.equals("drawRect") || methodInsnName.equals("func_73734_a")) {
                                AbstractInsnNode check = next;

                                boolean firstCall = false;

                                for (int i = 0; i < 12; ++i) {
                                    check = check.getPrevious();
                                }

                                if (check.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                                    firstCall = true;
                                }

                                AbstractInsnNode current = next;
                                if (firstCall) {
                                    for (int i = 0; i < 20; ++i) {
                                        next = next.getPrevious();
                                    }

                                    LabelNode ifne = new LabelNode();
                                    methodNode.instructions.insertBefore(next, createLabel(ifne));
                                    methodNode.instructions.insertBefore(current.getNext(), setLabel(ifne));
                                } else {
                                    for (int i = 0; i < 19; ++i) {
                                        next = next.getPrevious();
                                    }

                                    LabelNode ifne = new LabelNode();
                                    methodNode.instructions.insertBefore(next, createLabel(ifne));
                                    methodNode.instructions.insertBefore(current.getNext(), setLabel(ifne));
                                }
                            }
                            // todo: fix i am in a major state of Pain
                        }/* else if (next.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                            String methodInsnName = mapMethodNameFromNode(next);
                            if ((methodInsnName.equals("drawString") || methodInsnName.equals("func_78276_b"))) {
                                int msgIndex = -1;

                                for (LocalVariableNode variableNode : methodNode.localVariables) {
                                    if (variableNode.name.equals("msg")) {
                                        msgIndex = variableNode.index;
                                        break;
                                    }
                                }

                                boolean left = false;

                                if (next.getPrevious().getPrevious().getPrevious().getOpcode() == Opcodes.ICONST_2) {
                                    left = true;
                                }

                                for (int i = 0; i < 6; ++i) {
                                    methodNode.instructions.remove(next.getPrevious());
                                }

                                methodNode.instructions.insertBefore(next, useShadow(msgIndex, left));
                                methodNode.instructions.remove(next);
                            }
                        }*/
                    }
                }
            }
        }
    }

    private InsnList useShadow(int msgIndex, boolean left) {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraftforge/client/GuiIngameForge", "fontrenderer", "Lnet/minecraft/client/gui/FontRenderer;"));
        list.add(new VarInsnNode(Opcodes.ALOAD, msgIndex));

        if (left) {
            list.add(new InsnNode(Opcodes.FCONST_2));
        } else {
            list.add(new VarInsnNode(Opcodes.ILOAD, 10));
            list.add(new InsnNode(Opcodes.I2F));
        }

        list.add(new VarInsnNode(Opcodes.ILOAD, 6));
        list.add(new InsnNode(Opcodes.I2F));
        list.add(new LdcInsnNode(14737632));
        list.add(new FieldInsnNode(Opcodes.GETSTATIC, getConfigClass(), "oldDebugScreen", "Z"));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/client/gui/FontRenderer", "func_175065_a", "(Ljava/lang/String;FFIZ)I", false));
        return list;
    }

    private InsnList setLabel(LabelNode ifne) {
        InsnList list = new InsnList();
        list.add(ifne);
        return list;
    }

    private InsnList createLabel(LabelNode ifne) {
        InsnList list = new InsnList();
        list.add(new FieldInsnNode(Opcodes.GETSTATIC, getConfigClass(), "oldDebugScreen", "Z"));
        list.add(new JumpInsnNode(Opcodes.IFNE, ifne));
        return list;
    }

    public InsnList jumpIfOldHealth(LabelNode veryEnd) {
        InsnList list = new InsnList();
        LabelNode after = new LabelNode();
        list.add(new FieldInsnNode(Opcodes.GETSTATIC, getConfigClass(), "oldHealth", "Z"));
        list.add(new JumpInsnNode(Opcodes.IFEQ, after));
        list.add(new InsnNode(Opcodes.ICONST_0));
        list.add(new JumpInsnNode(Opcodes.GOTO, veryEnd));
        list.add(after);
        return list;
    }
}
