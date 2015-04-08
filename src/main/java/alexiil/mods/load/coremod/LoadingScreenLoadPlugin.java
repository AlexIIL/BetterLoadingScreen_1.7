package alexiil.mods.load.coremod;

import java.util.Map;

import alexiil.mods.load.ProgressDisplayer;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("1.7.10")
public class LoadingScreenLoadPlugin implements
		cpw.mods.fml.relauncher.IFMLLoadingPlugin {
	// The only reason this coremod exists is this static method: its the first
	// time our code is called
	static {
		ProgressDisplayer.start();
	}

	@Override
	public String[] getASMTransformerClass() {
		return new String[0];
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
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

}
