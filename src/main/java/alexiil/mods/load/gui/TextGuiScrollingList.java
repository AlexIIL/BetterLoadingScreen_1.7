package alexiil.mods.load.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import cpw.mods.fml.client.GuiScrollingList;

public abstract class TextGuiScrollingList extends GuiScrollingList {
    public static class LineInfo {
        public final String text;
        public final int colour;
        public final int xOffset;

        public LineInfo(String text, int colour, int xOffset) {
            this.text = text;
            this.colour = colour;
            this.xOffset = xOffset;
        }

        public LineInfo(String text, int colour) {
            this(text, colour, 0);
        }

        public LineInfo(String text) {
            this(text, 0xFFFFFF, 0);
        }
    }

    private static final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

    private final List<List<LineInfo>> strings = new ArrayList<List<LineInfo>>();

    public TextGuiScrollingList(Minecraft client, int width, int height, int top, int bottom, int left) {
        super(client, width, height, top, bottom, left, fontRenderer.FONT_HEIGHT);
    }

    protected void addLine(LineInfo... lis) {
        ArrayList<LineInfo> lineInfos = new ArrayList<LineInfo>();
        for (LineInfo l : lis)
            if (l != null)
                lineInfos.add(l);
        strings.add(lineInfos);
    }

    protected void clearLines() {
        strings.clear();
    }

    @Override
    protected int getSize() {
        return strings.size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick) {}

    @Override
    protected boolean isSelected(int index) {
        return false;
    }

    @Override
    protected void drawBackground() {}

    @Override
    protected void drawSlot(int index, int var2, int var3, int var4, Tessellator var5) {
        List<LineInfo> lis = strings.get(index);
        for (LineInfo li : lis)
            fontRenderer.drawString(li.text, left + 3 + li.xOffset, var3, li.colour);
    }
}
