package com.botamochi.rcap.client.screen;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import com.botamochi.rcap.client.screen.CompanyDashboardList;
import net.minecraft.client.font.TextRenderer;

public class CompanyDashboardListWrapper implements Drawable, Element, Selectable {

    private final CompanyDashboardList inner;
    private final TextRenderer font;

    public CompanyDashboardListWrapper(CompanyDashboardList inner, TextRenderer font) {
        this.inner = inner;
        this.font = font;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (inner.isVisible()) {
            // 独自の描画メソッドを通す
            inner.renderCompanyList(matrices, font);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        // 必要ならカーソル判定
        return inner.isVisible();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        // DashboardList は void 型なので、booleanが必要なラッパーで false を返す
        inner.mouseScrolled(mouseX, mouseY, amount);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (inner.handleCompanyClick(mouseX, mouseY, button)) {
            return true;
        }

        // 🧠 その他のクリック（＝項目自体のクリック）は DashboardList に渡す
        return Element.super.mouseClicked(mouseX, mouseY, button);
    }


    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {}
}
