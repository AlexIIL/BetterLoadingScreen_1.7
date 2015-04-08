package alexiil.mods.load;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class ProgressDisplayer {
    public interface IDisplayer {
        void open();

        void displayProgress(String text, float percent);

        void close();
    }

    public static class FrameDisplayer implements IDisplayer {
        private LoadingFrame frame = null;

        @Override
        public void open() {
            frame = LoadingFrame.openWindow();
            if (frame != null) {
                frame.setMessage("Minecraft Forge Starting");
                frame.setProgressIncrementing(0, 20, 4000);
            }
        }

        @Override
        public void displayProgress(String text, float percent) {
            if (frame == null)
                return;
            frame.setMessage(text);
            frame.setProgress(percent * 100F);
            frame.repaint();
        }

        @Override
        public void close() {
            if (frame != null)
                frame.dispose();
        }
    }

    private static IDisplayer displayer;

    public static void start() {
//        String comment = "Whether or not to use minecraft's display to show the progress. This looks better, but there is a possibilty of not being ";
//        comment += "compatible, so if you do have nay strange crash reports or compatability issues, try setting this to false";
//        Configuration cfg = new Configuration(new File("./config/betterloadingscreen.cfg"));
//        if (cfg.getBoolean("useMinecraft", "general", true, comment))
//            displayer = new MinecraftDisplayer();
//        else
            displayer = new FrameDisplayer();
//        cfg.save();
        displayer.open();
    }

    public static void displayProgress(String text, float percent) {
        displayer.displayProgress(text, percent);
    }

    public static void close() {
        if (displayer == null)
            return;
        displayer.close();
        displayer = null;
    }
}
