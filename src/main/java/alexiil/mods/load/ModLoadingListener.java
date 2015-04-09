package alexiil.mods.load;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.common.MinecraftForge;

import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ModLoadingListener {
    public enum State {
        CONSTRUCT("construction"), PRE_INIT("pre_initialization"), INIT("initialization"), POST_INIT("post_initialization"), LOAD_COMPLETE(
                "completed"), FINAL_LOADING("reloading_resource_packs", true);

        final String translatedName;
        /** If this state is only called once. This is false for all except for FINAL_LOADING */
        final boolean isLoneState;

        State(String name, boolean mods) {
            isLoneState = mods;
            String failure = name.replaceAll("\\_", " ");
            String[] split = failure.split(" ");
            failure = "";
            for (int i = 0; i < split.length; i++) {
                failure += i == 0 ? "" : " ";
                failure += split[i].substring(0, 1).toUpperCase().concat(split[i].substring(1));
            }
            translatedName = Translation.translate(name, failure);
        }

        State(String name) {
            this(name, false);
        }

        public String translate() {
            return translatedName;
        }
    }

    private static class ModStage {
        public final State state;

        @Override
        public String toString() {
            return "ModStage [state=" + state + ", index=" + index + "]";
        }

        public final int index;

        public ModStage(State state, int index) {
            this.state = state;
            this.index = index;
        }

        public ModStage getNext() {
            int ind = index + 1;
            State s = state;
            if (ind == listeners.size() || s.isLoneState) {
                ind = 0;
                int ord = s.ordinal() + 1;
                if (ord == State.values().length)
                    return null;
                s = State.values()[ord];
            }
            return new ModStage(s, ind);
        }

        public String getDisplayText() {
            if (state.isLoneState)
                return state.translate();
            return state.translate() + ": loading " + listeners.get(index).mod.getName();
        }

        public int getProgress() {
            int values = 100 / State.values().length;
            int part = (int) (state.ordinal() * values);
            int size = listeners.size();
            int percent = values * index / size;
            return part + percent;
        }
    }

    private static List<ModLoadingListener> listeners = new ArrayList<ModLoadingListener>();
    private static ModStage stage = null;

    private final ModContainer mod;

    public ModLoadingListener(ModContainer mod) {
        this.mod = mod;
        if (listeners.isEmpty())
            MinecraftForge.EVENT_BUS.register(this);
        listeners.add(this);
    }

    @Subscribe
    public void construct(FMLConstructionEvent event) {
        doProgress(State.CONSTRUCT, this);
    }

    @Subscribe
    public void preinit(FMLPreInitializationEvent event) {
        doProgress(State.PRE_INIT, this);
    }

    @Subscribe
    public void init(FMLInitializationEvent event) {
        doProgress(State.INIT, this);
    }

    @Subscribe
    public void postinit(FMLPostInitializationEvent event) {
        doProgress(State.POST_INIT, this);
    }

    @Subscribe
    public void loadComplete(FMLLoadCompleteEvent event) {
        doProgress(State.LOAD_COMPLETE, this);
    }

    public static void doProgress(State state, ModLoadingListener mod) {
        if (stage == null)
            if (mod == null)
                stage = new ModStage(state, 0);
            else
                stage = new ModStage(state, listeners.indexOf(mod));
        String text = stage.getDisplayText();
        float percent = stage.getProgress() / 100F;
        stage = stage.getNext();
        if (stage.state == State.FINAL_LOADING) {
            text = stage.getDisplayText();
            percent = stage.getProgress() / 100F;
        }
        ProgressDisplayer.displayProgress(text, percent);
    }
}
