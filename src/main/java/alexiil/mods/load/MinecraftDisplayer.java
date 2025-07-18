package alexiil.mods.load;

import java.awt.SplashScreen;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundEventAccessorComposite;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.config.Configuration;

import alexiil.mods.load.ProgressDisplayer.IDisplayer;
import alexiil.mods.load.json.Area;
import alexiil.mods.load.json.EPosition;
import alexiil.mods.load.json.EType;
import alexiil.mods.load.json.ImageRender;
import alexiil.mods.load.json.JsonConfig;

import cpw.mods.fml.client.FMLFileResourcePack;
import cpw.mods.fml.client.FMLFolderResourcePack;

public class MinecraftDisplayer implements IDisplayer {
    private static String sound;
    private static String defaultSound = "random.levelup";
    private static String fontTexture;
    private static String defaultFontTexture = "textures/font/ascii.png";
    private final boolean preview;
    private ImageRender[] images;
    private TextureManager textureManager = null;
    private Map<String, FontRenderer> fontRenderers = new HashMap<String, FontRenderer>();
    private FontRenderer fontRenderer = null;
    private ScaledResolution resolution = null;
    private Minecraft mc = null;
    private boolean callAgain = false;
    private IResourcePack myPack, folderPack;
    private float clearRed = 1, clearGreen = 1, clearBlue = 1;

    public static void playFinishedSound() {
        SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
        ResourceLocation location = new ResourceLocation(sound);
        SoundEventAccessorComposite snd = soundHandler.getSound(location);
        if (snd == null) {
            System.out.println("The sound given (" + sound + ") did not give a valid sound!");
            location = new ResourceLocation(defaultSound);
            snd = soundHandler.getSound(location);
        }
        if (snd == null) {
            System.out.println("Default sound did not give a valid sound!");
            return;
        }
        ISound sound = PositionedSoundRecord.func_147673_a(location);
        soundHandler.playSound(sound);
    }

    public MinecraftDisplayer() {
        this(false);
    }

    public MinecraftDisplayer(boolean preview) {
        this.preview = preview;
    }

    @SuppressWarnings("unchecked")
    private List<IResourcePack> getOnlyList() {
        Field[] flds = mc.getClass().getDeclaredFields();
        for (Field f : flds) {
            if (f.getType().equals(List.class) && !Modifier.isStatic(f.getModifiers())) {
                f.setAccessible(true);
                try {
                    return (List<IResourcePack>) f.get(mc);
                }
                catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public void openPreview(ImageRender[] renders) {
        mc = Minecraft.getMinecraft();
        images = renders;
    }

    // Minecraft's display hasn't been created yet, so don't bother trying
    // to do anything now
    @Override
    public void open(Configuration cfg) {
        mc = Minecraft.getMinecraft();
        // Open the normal config
        String comment4 = "What sound to play when loading is complete. Default is the level up sound (" + defaultSound + ")";
        sound = cfg.getString("sound", "general", defaultSound, comment4);

        comment4 = "What font texture to use? Special Cases:"
                + "\n - If you use the Russian mod \"Client Fixer\" then change this to \"textures/font/ascii_fat.png\"" + "\n";
        fontTexture = cfg.getString("font", "general", defaultFontTexture, comment4);

        // Open the special config directory
        File configDir = new File("./config/BetterLoadingScreen");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        // Add ourselves as a resource pack
        if (!preview) {
            if (!ProgressDisplayer.coreModLocation.isDirectory())
                myPack = new FMLFileResourcePack(ProgressDisplayer.modContainer);
            else
                myPack = new FMLFolderResourcePack(ProgressDisplayer.modContainer);
            List<IResourcePack> packList = getOnlyList();
            packList.add(myPack);

            File packFolder = new File(configDir, "resources");

            if (!packFolder.exists()) {
                packFolder.mkdirs();
            }

            writePackMeta(new File(packFolder, "pack.mcmeta"));
            File sampleImage = new File(new File(new File(packFolder, "assets"), "betterloadingscreen"), "textures");
            sampleImage.mkdirs();
            sampleImage = new File(sampleImage, "progressBars2.png");
            if (!sampleImage.exists()) {
                InputStream input = null;
                OutputStream output = null;
                try {
                    input = Lib.class.getResourceAsStream("/assets/betterloadingscreen/textures/progressBars.png");
                    if (input != null) {
                        output = new FileOutputStream(sampleImage);
                        byte[] buffer = new byte[4096];
                        int read;
                        while ((read = input.read(buffer)) > 0) {
                            output.write(buffer, 0, read);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (input != null) {
                            input.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        if (output != null) {
                            output.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            packList.add(folderPack = new FolderResourcePack(packFolder));

            mc.refreshResources();
        }

        // Image Config
        images = new ImageRender[6];
        String progress = "betterloadingscreen:textures/progressBars.png";
        String title = "textures/gui/title/mojang.png";
        images[0] = new ImageRender(title, EPosition.CENTER, EType.STATIC, new Area(0, 0, 256, 256), new Area(0, 0, 256, 256));
        images[1] = new ImageRender(fontTexture, EPosition.CENTER, EType.DYNAMIC_TEXT_STATUS, null, new Area(0, -30, 0, 0), "000000", null, "");
        images[2] = new ImageRender(fontTexture, EPosition.CENTER, EType.DYNAMIC_TEXT_PERCENTAGE, null, new Area(0, -40, 0, 0), "000000", null, "");
        images[3] = new ImageRender(progress, EPosition.CENTER, EType.STATIC, new Area(0, 10, 182, 5), new Area(0, -50, 182, 5));
        images[4] = new ImageRender(progress, EPosition.CENTER, EType.DYNAMIC_PERCENTAGE, new Area(0, 15, 182, 5), new Area(0, -50, 182, 5));
        images[5] = new ImageRender(null, null, EType.CLEAR_COLOUR, null, null, "ffffff", null, "");

        if (!preview) {
            SplashScreen splashScreen = SplashScreen.getSplashScreen();
            if (splashScreen != null)
                splashScreen.close();
        }

        ImageRender[] defaultImageRender = images;

        File imagesFile = new File(configDir, "images.json");
        JsonConfig<ImageRender[]> imagesConfig = new JsonConfig<ImageRender[]>(imagesFile, ImageRender[].class, images);
        images = imagesConfig.load();

        for (ImageRender ir : images) {
            if (ir.type == EType.CLEAR_COLOUR) {
                clearRed = ir.getRed();
                clearGreen = ir.getGreen();
                clearBlue = ir.getBlue();
            }
        }

        // Preset one is the default one
        definePreset(configDir, "preset one", defaultImageRender);

        // Preset two uses something akin to minecraft's loading screen when loading a world
        ImageRender[] presetData = new ImageRender[5];
        presetData[0] = new ImageRender("textures/gui/options_background.png", EPosition.CENTER, EType.STATIC, new Area(0, 0, 65536, 65536),
                new Area(0, 0, 8192, 8192), "404040", null, "Background image");
        presetData[1] = new ImageRender(fontTexture, EPosition.CENTER, EType.DYNAMIC_TEXT_STATUS, null, new Area(0, 0, 0, 0), "FFFFFF", null,
                "The current operation");
        presetData[2] = new ImageRender(fontTexture, EPosition.CENTER, EType.DYNAMIC_TEXT_PERCENTAGE, null, new Area(0, -10, 0, 0), "FFFFFF", null,
                "The overall percentage progress");
        presetData[3] = new ImageRender(fontTexture, EPosition.BOTTOM_CENTER, EType.STATIC_TEXT, null, new Area(0, 10, 0, 0), "FFDD49",
                "Better Loading Screen by AlexIIL", "Text at the bottom of the screen");
        presetData[4] = new ImageRender("", null, EType.CLEAR_COLOUR, null, null, "ffffff", null, "Background colour");
        definePreset(configDir, "preset two", presetData);

        // Preset three uses... idk, TODO: Preset 3 etc
    }

    private static void writePackMeta(File file) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(
                "{\n'pack': {\n\t'description': 'Better Loading Screen folder pack',"
                    + "\n\t'pack_format': 1\n}\n}\n".replace('\'', '"')
            );
            writer.close();
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void definePreset(File configDir, String name, ImageRender... images) {
        File presetFile = new File(configDir, name + ".json");
        JsonConfig<ImageRender[]> presetConfig = new JsonConfig<ImageRender[]>(presetFile, ImageRender[].class, images);
        presetConfig.createNew();
    }

    @Override
    public void displayProgress(String text, float percent) {
        resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

        preDisplayScreen();

        for (ImageRender image : images)
            if (image != null)
                drawImageRender(image, text, percent);

        postDisplayScreen();

        if (callAgain) {
            // For some reason, calling this again makes pre-init render properly. I have no idea why, it just does
            callAgain = false;
            displayProgress(text, percent);
        }
    }

    private FontRenderer fontRenderer(String fontTexture) {
        if (fontRenderers.containsKey(fontTexture))
            return fontRenderers.get(fontTexture);
        FontRenderer font = new FontRenderer(mc.gameSettings, new ResourceLocation(fontTexture), textureManager, false);
        font.onResourceManagerReload(mc.getResourceManager());
        if (!preview) {
            mc.refreshResources();
            font.onResourceManagerReload(mc.getResourceManager());
        }
        fontRenderers.put(fontTexture, font);
        return font;
    }

    public void drawImageRender(ImageRender render, String text, double percent) {
        int startX = render.transformX(resolution.getScaledWidth());
        int startY = render.transformY(resolution.getScaledHeight());
        int PWidth = 0;
        int PHeight = 0;
        if (render.position != null) {
            PWidth = render.position.width == 0 ? resolution.getScaledWidth() : render.position.width;
            PHeight = render.position.height == 0 ? resolution.getScaledHeight() : render.position.height;
        }
        GL11.glColor3f(render.getRed(), render.getGreen(), render.getBlue());
        switch (render.type) {
            case DYNAMIC_PERCENTAGE: {
                ResourceLocation res = new ResourceLocation(render.resourceLocation);
                textureManager.bindTexture(res);
                double visibleWidth = PWidth * percent;
                double textureWidth = render.texture.width * percent;
                drawRect(startX, startY, visibleWidth, PHeight, render.texture.x, render.texture.y, textureWidth, render.texture.height);
                break;
            }
            case DYNAMIC_TEXT_PERCENTAGE: {
                FontRenderer font = fontRenderer(render.resourceLocation);
                String percentage = (int) (percent * 100) + "%";
                int width = font.getStringWidth(percentage);
                startX = render.positionType.transformX(render.position.x, resolution.getScaledWidth() - width);
                startY = render.positionType.transformY(render.position.y, resolution.getScaledHeight() - font.FONT_HEIGHT);
                drawString(font, percentage, startX, startY, render.getColour());
                break;
            }
            case DYNAMIC_TEXT_STATUS: {
                FontRenderer font = fontRenderer(render.resourceLocation);
                int width = font.getStringWidth(text);
                startX = render.positionType.transformX(render.position.x, resolution.getScaledWidth() - width);
                startY = render.positionType.transformY(render.position.y, resolution.getScaledHeight() - font.FONT_HEIGHT);
                drawString(font, text, startX, startY, render.getColour());
                break;
            }
            case STATIC_TEXT: {
                FontRenderer font = fontRenderer(render.resourceLocation);
                int width = font.getStringWidth(render.text);
                int startX1 = render.positionType.transformX(render.position.x, resolution.getScaledWidth() - width);
                int startY1 = render.positionType.transformY(render.position.y, resolution.getScaledHeight() - font.FONT_HEIGHT);
                drawString(font, render.text, startX1, startY1, render.getColour());
                break;
            }
            case STATIC: {
                ResourceLocation res = new ResourceLocation(render.resourceLocation);
                textureManager.bindTexture(res);
                drawRect(startX, startY, PWidth, PHeight, render.texture.x, render.texture.y, render.texture.width, render.texture.height);
                break;
            }
            case CLEAR_COLOUR:// Ignore this, as its set elsewhere
                break;
        }
    }

    public void drawString(FontRenderer font, String text, int x, int y, int colour) {
        font.drawString(text, x, y, colour);
        GL11.glColor4f(1, 1, 1, 1);
    }

    public void drawRect(double x, double y, double drawnWidth, double drawnHeight, double u, double v, double uWidth, double vHeight) {
        float f = 1 / 256F;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + drawnHeight, 0, u * f, (v + vHeight) * f);
        tessellator.addVertexWithUV(x + drawnWidth, y + drawnHeight, 0, (u + uWidth) * f, (v + vHeight) * f);
        tessellator.addVertexWithUV(x + drawnWidth, y, 0, (u + uWidth) * f, v * f);
        tessellator.addVertexWithUV(x, y, 0, u * f, v * f);
        tessellator.draw();
    }

    private void preDisplayScreen() {
        if (textureManager == null) {
            if (preview) {
                textureManager = mc.renderEngine;
            }
            else {
                textureManager = mc.renderEngine = new TextureManager(mc.getResourceManager());
                mc.refreshResources();
                textureManager.onResourceManagerReload(mc.getResourceManager());
                mc.fontRenderer = new FontRenderer(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"), textureManager, false);
                if (mc.gameSettings.language != null) {
                    mc.fontRenderer.setUnicodeFlag(mc.func_152349_b());
                    LanguageManager lm = mc.getLanguageManager();
                    mc.fontRenderer.setBidiFlag(lm.isCurrentLanguageBidirectional());
                }
                mc.fontRenderer.onResourceManagerReload(mc.getResourceManager());
                callAgain = true;
            }
        }
        if (fontRenderer != mc.fontRenderer)
            fontRenderer = mc.fontRenderer;
        // if (textureManager != mc.renderEngine)
        // textureManager = mc.renderEngine;
        resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int i = resolution.getScaleFactor();
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

        GL11.glClearColor(clearRed, clearGreen, clearBlue, 1);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);

        GL11.glColor4f(1, 1, 1, 1);
    }

    public ImageRender[] getImageData() {
        return images;
    }

    private void postDisplayScreen() {
        mc.func_147120_f();
    }

    @Override
    public void close() {
        List<IResourcePack> packs = getOnlyList();
        packs.remove(myPack);
        packs.remove(folderPack);
    }
}
