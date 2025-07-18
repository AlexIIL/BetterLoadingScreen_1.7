package alexiil.mods.load.coremod;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class BetterLoadingScreenTransformer implements IClassTransformer, Opcodes {

    private static final String LWJGL_EXCEPTION = "org/lwjgl/LWJGLException";
    private static final String GL11 = "org/lwjgl/opengl/GL11";
    private static final String FML_CLIENT_HANDLER = "cpw/mods/fml/client/FMLClientHandler";
    private static final String PROGRESS_DISPLAYER = "alexiil/mods/load/ProgressDisplayer";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        try {
            if (transformedName.equals("net.minecraft.client.Minecraft"))
                return transformMinecraft(basicClass);
            if (name.equals("com.mumfrey.liteloader.client.api.ObjectFactoryClient"))
                return transformObjectFactoryClient(basicClass);
            if (name.equals("lumien.resourceloader.ResourceLoader"))
                return transformResourceLoader(basicClass);
        }
        catch (Throwable t) {
            System.out.println("An issue occoured while transforming " + transformedName);
            t.printStackTrace();
        }
        return basicClass;
    }

    private byte[] transformObjectFactoryClient(byte[] before) {
        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(before);
        reader.accept(classNode, 0);

        for (MethodNode m : classNode.methods) {
            if (m.name.equals("preBeginGame")) {
                m.instructions.clear();
                m.instructions.add(new TypeInsnNode(NEW, "alexiil/mods/load/LiteLoaderProgress"));
                m.instructions.add(new MethodInsnNode(INVOKESPECIAL, "alexiil/mods/load/LiteLoaderProgress", "<init>", "()V", false));
                m.instructions.add(new InsnNode(RETURN));
            }
        }

        ClassWriter cw = new ClassWriter(0);
        classNode.accept(cw);
        return cw.toByteArray();
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
            if (m.exceptions.size() == 1 && m.exceptions.get(0).equals(LWJGL_EXCEPTION)) {
                for (int i = 0; i < m.instructions.size(); i++) {
                    if (!hasFoundGL11) {
                        AbstractInsnNode node = m.instructions.get(i);
                        if (node instanceof MethodInsnNode) {
                            MethodInsnNode method = (MethodInsnNode) node;
                            if (method.owner.equals(GL11) && method.name.equals("glFlush")) {
                                hasFoundGL11 = true;
                                // This method throws an LWJGL exception, and calls GL11.glFlush(). This must be
                                // Minecraft.loadScreen()!
                                m.instructions.insertBefore(m.instructions.getFirst(), new InsnNode(RETURN));
                                // just return from the method, as if nothing happened
                                break;
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < m.instructions.size(); i++) {
                /* LiteLoader disabling -NOTE TO ANYONE FROM LITELOADER OR ANYONE ELSE: I am disabling liteloader's
                 * overlay simply because otherwise it switches between liteloader's bar and mine. I can safely assume
                 * that people won't wont this, and as my progress bar is the entire mod, they can disable this
                 * behaviour by removing my mod (as all my mod does is just add a loading bar) */
                AbstractInsnNode node = m.instructions.get(i);
                if (node instanceof MethodInsnNode) {
                    MethodInsnNode method = (MethodInsnNode) node;
                    if (method.owner.equals("com/mumfrey/liteloader/client/gui/startup/LoadingBar")) {
                        m.instructions.remove(method);
                        continue;
                    }
                }
                // LiteLoader removing end

                if (!hasFoundFMLClientHandler) {
                    if (node instanceof MethodInsnNode) {
                        MethodInsnNode method = (MethodInsnNode) node;
                        if (method.owner.equals(FML_CLIENT_HANDLER) && method.name.equals("instance")) {
                            MethodInsnNode newOne =
                                    new MethodInsnNode(INVOKESTATIC, PROGRESS_DISPLAYER, "minecraftDisplayFirstProgress",
                                            "()V", false);
                            m.instructions.insertBefore(method, newOne);
                            hasFoundFMLClientHandler = true;
                        }
                    }
                }
            }
        }

        ClassWriter cw = new ClassWriter(0);
        classNode.accept(cw);
        System.out.println("Transformed Minecraft");
        return cw.toByteArray();
    }

    private byte[] transformResourceLoader(byte[] before) {
        if (before == null) {
            return null;
        }

        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(before);
        reader.accept(classNode, 0);

        for (MethodNode m : classNode.methods) {
            if (m.name.equals("preInit")) {
                m.visibleAnnotations.remove(0);// Remove @Mod.EventHandler
            }
        }

        for (FieldNode f : classNode.fields) {
            if (f.name.equals("INSTANCE"))
                f.visibleAnnotations.remove(0);// Remove @Mod.Instance("ResourceLoader")
        }

        ClassWriter cw = new ClassWriter(0);
        classNode.accept(cw);
        byte[] arr = cw.toByteArray();
        System.out.println("Transformed ResourceLoader!");
        return arr;
    }
}
