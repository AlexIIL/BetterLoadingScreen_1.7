package alexiil.mods.load.gui;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import alexiil.mods.load.BetterLoadingScreen;

import cpw.mods.fml.client.IModGuiFactory;

public class ConfigGuiFactory implements IModGuiFactory {
    @Override
    public void initialize(Minecraft minecraftInstance) {}

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        if (BetterLoadingScreen.couldGetVersionHistory()) {
            return BaseConfig.class;
        } else {
            return ActualConfig.class;
        }
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }

}
