package alexiil.mods.load;

import java.io.IOException;
import java.lang.reflect.Field;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import alexiil.mods.load.ProgressDisplayer.IDisplayer;

import com.google.common.base.Throwables;

public class MinecraftDisplayer implements IDisplayer {
    private static final ResourceLocation locationMojangPng = new ResourceLocation("textures/gui/title/mojang.png");
    private static final ResourceLocation locationBetterLoadingScreen = new ResourceLocation("betterLoadingScreen:textures/gui/screen.png");
    private TextureManager textureManager = null;
    private ResourceLocation mojangPng = null;
    private FontRenderer fontRenderer = null;
    private ScaledResolution resolution = null;
    private Framebuffer framebuffer = null;

    // Minecraft's display hasn't been created yet, so don't bother trying
    // to do anything now
    @Override
    public void open() {}

    @Override
    public void displayProgress(String text, float percent) {
        System.out.println(text + "|" + percent);
        preDisplayScreen();
        fontRenderer.drawString(text, 10, (int) percent * 10, 0);
        postDisplayScreen();
    }

    private void preDisplayScreen() {
        Minecraft mc = Minecraft.getMinecraft();
        if (textureManager == null) {
            textureManager = mc.renderEngine = new TextureManager(mc.getResourceManager());
            fontRenderer = new FontRenderer(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"), textureManager, false);
            mc.fontRenderer = fontRenderer;
        }
        resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int i = resolution.getScaleFactor();
        framebuffer = new Framebuffer(resolution.getScaledWidth() * i, resolution.getScaledHeight() * i, true);
        framebuffer.bindFramebuffer(false);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, (double) resolution.getScaledWidth(), (double) resolution.getScaledHeight(), 0.0D, 1000.0D, 3000.0D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        try {
            DefaultResourcePack drp = getMinecraftField("mcDefaultResourcePack");
            DynamicTexture tex = new DynamicTexture(ImageIO.read(drp.getInputStream(locationMojangPng)));
            mojangPng = textureManager.getDynamicTextureLocation("logo", tex);
            textureManager.bindTexture(mojangPng);
        }
        catch (IOException ioexception) {
            // logger.error("Unable to load logo: " + locationMojangPng, ioexception);
        }

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorOpaque_I(16777215);
        tessellator.addVertexWithUV(0.0D, (double) mc.displayHeight, 0.0D, 0.0D, 0.0D);
        tessellator.addVertexWithUV((double) mc.displayWidth, (double) mc.displayHeight, 0.0D, 0.0D, 0.0D);
        tessellator.addVertexWithUV((double) mc.displayWidth, 0.0D, 0.0D, 0.0D, 0.0D);
        tessellator.addVertexWithUV(0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
        tessellator.draw();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        tessellator.setColorOpaque_I(16777215);
        short short1 = 256;
        short short2 = 256;
        mc.scaledTessellator((resolution.getScaledWidth() - short1) / 2, (resolution.getScaledHeight() - short2) / 2, 0, 0, short1, short2);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_FOG);

        framebuffer.unbindFramebuffer();
        framebuffer.framebufferRender(resolution.getScaledWidth() * i, resolution.getScaledHeight() * i);
    }

    private void postDisplayScreen() {
        Minecraft mc = Minecraft.getMinecraft();
        Tessellator tessellator = Tessellator.instance;
        int i = resolution.getScaleFactor();

        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
        GL11.glFlush();
        mc.func_147120_f();
    }

    @SuppressWarnings("unchecked")
    private <T> T getMinecraftField(String name) {
        try {
            Field fld = Minecraft.class.getDeclaredField(name);
            fld.setAccessible(true);
            return (T) fld.get(Minecraft.getMinecraft());
        }
        catch (Throwable t) {
            Throwables.propagate(t);
        }
        return null;
    }

    @Override
    public void close() {}
}
