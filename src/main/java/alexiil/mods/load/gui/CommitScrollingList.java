package alexiil.mods.load.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
import alexiil.mods.load.BetterLoadingScreen;
import alexiil.mods.load.git.Commit;
import alexiil.mods.load.git.Release;

public class CommitScrollingList extends TextGuiScrollingList {
    public final List<Commit> commitList = new ArrayList<Commit>();
    public final BaseConfig parent;

    public CommitScrollingList(BaseConfig parent, int width, int height, int top, int bottom, int left) {
        super(Minecraft.getMinecraft(), width, height, top, bottom, left);
        this.parent = parent;

        populateStrings();
    }

    private void populateStrings() {
        clearLines();

        List<Commit> commitList = BetterLoadingScreen.getCommits();
        for (Commit c : commitList) {
            boolean thisOne = c == BetterLoadingScreen.getCurrentCommit();
            Release release = null;
            for (Release rel : BetterLoadingScreen.getReleases()) {
                if (rel.commit.sha.equals(c.sha))
                    release = rel;
            }
            String date = c.commit.committer.date.split("T")[0];
            String[] dates = date.split("-");
            date = dates[2] + "/" + dates[1] + "/" + dates[0];

            String text = c.author.login + " " + date;
            LineInfo line0 = new LineInfo(text, thisOne ? 0xBFB23A : 0x00CAFF);
            LineInfo line1 = release == null ? null : new LineInfo(release.name, 0x11FF44, parent.getFontRenderer().getStringWidth(text) + 10);
            addLine(line0, line1);

            String message = c.commit.message;
            String[] strings = message.split("\n");
            for (int i = 0; i < strings.length; i++) {
                String s = strings[i];
                String nextLine = "";
                while (parent.getFontRenderer().getStringWidth(s) > this.listWidth - 10 && s != null) {
                    if (s.length() <= 10)
                        break;
                    nextLine = s.substring(s.length() - 1) + nextLine;
                    s = s.substring(0, s.length() - 1);
                }
                if (nextLine.length() > 0) {
                    strings = Arrays.copyOf(strings, strings.length + 1);
                    strings[i] = s;
                    strings[i + 1] = nextLine;
                }
            }
            for (String s : strings)
                addLine(new LineInfo(s, thisOne ? 0xFFDD49 : 0xFFFFFF, 4));
            addLine(new LineInfo(""));
        }
    }
}
