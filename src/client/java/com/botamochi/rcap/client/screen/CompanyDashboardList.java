package com.botamochi.rcap.client.screen;

import com.botamochi.rcap.data.Company;
import com.botamochi.rcap.data.CompanyManager;
import mtr.client.ClientData;
import mtr.data.NameColorDataBase;
import mtr.screen.DashboardList;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;

import java.util.stream.Collectors;

public class CompanyDashboardList extends DashboardList {

    private boolean visible = false;

    public CompanyDashboardList() {
        super(
                (data, index) -> {},
                (data, index) -> {},
                (data, index) -> {},
                () -> {},
                null,
                (data, index) -> {},
                () -> CompanyManager.COMPANY_LIST.stream()
                        .map(c -> (NameColorDataBase) c)
                        .collect(Collectors.toList()),
                () -> ClientData.DASHBOARD_SEARCH,
                s -> ClientData.DASHBOARD_SEARCH = s
        );

        this.x = 0;
        this.y = 20;
        this.width = 144;
        resetData();
    }

    public void resetData() {
        final var data = CompanyManager.COMPANY_LIST.stream()
                .map(c -> (NameColorDataBase) c)
                .collect(Collectors.toList());

        setData(data, false, false, false, false, false, false);
    }

    public void renderCompanyList(MatrixStack matrices, TextRenderer font) {
        if (visible) {
            super.render(matrices, font);
        }
    }

    public void tickCompanyList() {
        if (visible) {
            super.tick();
        }
    }

    public void hide() {
        visible = false;
    }

    public void show() {
        visible = true;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
