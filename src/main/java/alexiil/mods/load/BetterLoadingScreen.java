package alexiil.mods.load;

import java.lang.reflect.Field;

import org.apache.logging.log4j.LogManager;

import com.google.common.eventbus.EventBus;

import cpw.mods.fml.common.FMLModContainer;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLConstructionEvent;

@Mod(modid = Lib.Mod.ID)
public class BetterLoadingScreen {
    @Instance(Lib.Mod.ID)
    public static BetterLoadingScreen instance;

    private org.apache.logging.log4j.Logger log = null;

    @EventHandler
    public void construct(FMLConstructionEvent event) {
        log = LogManager.getLogger(Lib.Mod.ID);
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
                    bus.register(new ModLoadingListener(mod));
                }
            }
        }
    }
}
