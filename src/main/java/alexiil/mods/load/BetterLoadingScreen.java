package alexiil.mods.load;

import java.lang.reflect.Field;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import alexiil.mods.load.ModLoadingListener.State;

import com.google.common.eventbus.EventBus;

import cpw.mods.fml.common.FMLModContainer;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = Lib.Mod.ID)
public class BetterLoadingScreen {
    @Instance(Lib.Mod.ID)
    public static BetterLoadingScreen instance;

    @EventHandler
    public void construct(FMLConstructionEvent event) {
        ModLoadingListener thisListener = null;
        for (ModContainer mod : Loader.instance().getActiveModList()) {
            if (mod instanceof FMLModContainer) {
                EventBus bus = null;
                try {
                    // Its a bit questionable to be changing FML itself, but reflection is better than ASM transforming
                    // forge
                    Field f = FMLModContainer.class.getDeclaredField("eventBus");
                    f.setAccessible(true);
                    bus = (EventBus) f.get(mod);
                }
                catch (Throwable t) {
                    t.printStackTrace();
                }
                if (bus != null) {
                    if (mod.getModId().equals(Lib.Mod.ID)) {
                        thisListener = new ModLoadingListener(mod);
                        bus.register(thisListener);
                    }
                    else
                        bus.register(new ModLoadingListener(mod));
                }
            }
        }
        if (thisListener != null)
            ModLoadingListener.doProgress(State.CONSTRUCT, thisListener);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(instance);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void guiOpen(GuiOpenEvent event) {
        if (event.gui != null && event.gui instanceof GuiMainMenu)
            ProgressDisplayer.close();
    }

    @EventHandler
    @SideOnly(Side.SERVER)
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
        ProgressDisplayer.close();
    }
}
