package alexiil.mods.load.coremod;

import java.io.File;
import java.util.Map;

import alexiil.mods.load.ProgressDisplayer;
import alexiil.mods.load.Translation;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions({ "alexiil.mods.load.coremod" })
@IFMLLoadingPlugin.SortingIndex(Integer.MAX_VALUE - 80)
// A big number
public class LoadingScreenLoadPlugin implements cpw.mods.fml.relauncher.IFMLLoadingPlugin {
    static {
        ProgressDisplayer.start();
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { "alexiil.mods.load.coremod.BetterLoadingScreenTransformer" };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        Translation.addTranslations((File) data.get("coremodLocation"));
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

}
