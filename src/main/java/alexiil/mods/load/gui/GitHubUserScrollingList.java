package alexiil.mods.load.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import alexiil.mods.load.git.GitHubUser;

public class GitHubUserScrollingList extends TextGuiScrollingList {
    public final List<GitHubUser> userList = new ArrayList<GitHubUser>();
    public final BaseConfig parent;

    public GitHubUserScrollingList(BaseConfig parent, int width, int height, int top, int bottom, int left) {
        super(Minecraft.getMinecraft(), width, height, top, bottom, left);
        this.parent = parent;
    }

    @Override
    protected int getSize() {
        return userList.size();
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
    protected void drawSlot(int index, int var2, int var3, int var4, Tessellator tess) {
        GitHubUser user = userList.get(index);
        parent.drawString(parent.getFontRenderer(), user.login, left + 3, var3, 0xFFFFFF);
    }
}
