package alexiil.mods.load.coremod;

import net.minecraft.launchwrapper.IClassTransformer;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import alexiil.mods.load.ProgressDisplayer;
import cpw.mods.fml.client.FMLClientHandler;

public class BetterLoadingScreenTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals("net.minecraft.client.Minecraft"))
            return transformMinecraft(basicClass);
        return basicClass;
    }

    private byte[] transformMinecraft(byte[] before) {
        boolean hasFoundFMLClientHandler = false;
        boolean hasFoundGL11 = false;
        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(before);
        reader.accept(classNode, 0);

        for (MethodNode m : classNode.methods) {
            if (hasFoundGL11)
                break;
            if (m.exceptions.size() == 1 && m.exceptions.get(0).equals(Type.getInternalName(LWJGLException.class))) {
                for (int i = 0; i < m.instructions.size(); i++) {
                    if (!hasFoundGL11) {
                        AbstractInsnNode node = m.instructions.get(i);
                        if (node instanceof MethodInsnNode) {
                            MethodInsnNode method = (MethodInsnNode) node;
                            if (method.owner.equals(Type.getInternalName(GL11.class)) && method.name.equals("glFlush")) {
                                hasFoundGL11 = true;
                                // This method throws an LWJGL exception, and calls GL11.glFlush(). This must be
                                // Minecraft.loadScreen()!
                                m.instructions.insertBefore(m.instructions.getFirst(), new InsnNode(Opcodes.RETURN));
                                // just return from the method, as if nothing happened
                                break;
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < m.instructions.size(); i++) {
                if (!hasFoundFMLClientHandler) {
                    AbstractInsnNode node = m.instructions.get(i);
                    if (node instanceof MethodInsnNode) {
                        MethodInsnNode method = (MethodInsnNode) node;
                        if (method.owner.equals(Type.getInternalName(FMLClientHandler.class)) && method.name.equals("instance")) {
                            MethodInsnNode newOne =
                                    new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(ProgressDisplayer.class),
                                            "minecraftDisplayFirstProgress", "()V", false);
                            m.instructions.insertBefore(method, newOne);
                            hasFoundFMLClientHandler = true;
                            break;
                        }
                    }
                }
            }
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(cw);
        return cw.toByteArray();
    }
}
