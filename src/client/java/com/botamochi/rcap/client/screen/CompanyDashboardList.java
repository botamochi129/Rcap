package com.botamochi.rcap.client.screen;

import com.botamochi.rcap.client.mixin.DashboardScreenMixin;
import com.botamochi.rcap.data.Company;
import com.botamochi.rcap.data.CompanyManager;
import mtr.client.ClientData;
import mtr.data.NameColorDataBase;
import mtr.screen.DashboardList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;
import java.util.stream.Collectors;

public class CompanyDashboardList extends DashboardList {

    private final Screen parentScreen;
    private boolean visible = false;

    public CompanyDashboardList(Screen parentScreen) {
        super(
                (data, index) -> {},

                (data, index) -> {
                    if (data instanceof Company company && parentScreen instanceof DashboardScreenMixin dashboardMixin) {
                        MinecraftClient.getInstance().setScreen(
                                new EditCompanyScreen(parentScreen, dashboardMixin.getCompanyDashboardList(), company)
                        );
                    }
                },

                (data, index) -> {
                    if (data instanceof Company company && parentScreen instanceof DashboardScreenMixin dashboardMixin) {
                        MinecraftClient.getInstance().setScreen(
                                new EditCompanyScreen(parentScreen, dashboardMixin.getCompanyDashboardList(), company)
                        );
                    }
                },

                () -> {},   // onSort
                null,       // onAdd

                // ✅ ダミー Runnable で delay しないでリセットする → this 使用禁止状態ではこのくらいが限界
                (data, index) -> {
                    if (data instanceof Company company) {
                        CompanyManager.COMPANY_LIST.removeIf(c -> c.id == company.id);
                        MinecraftClient.getInstance().submit(() -> {
                            // UI スレッドで遅延実行することで安全に this.use できるようになる
                            MinecraftClient.getInstance().execute(() -> {
                                MinecraftClient.getInstance().setScreen(MinecraftClient.getInstance().currentScreen); // force redraw
                            });
                        });
                    }
                },

                // データ
                () -> CompanyManager.COMPANY_LIST.stream()
                        .map(c -> (NameColorDataBase) c)
                        .collect(Collectors.toList()),

                // 検索
                () -> ClientData.DASHBOARD_SEARCH,
                s -> ClientData.DASHBOARD_SEARCH = s
        );

        this.parentScreen = parentScreen;
        this.x = 0;
        this.y = 20;
        this.width = 144;
        this.height = MinecraftClient.getInstance().getWindow().getScaledHeight() - 32;

        resetData();
    }

    public void resetData() {
        setData(
                CompanyManager.COMPANY_LIST.stream()
                        .map(c -> (NameColorDataBase) c)
                        .collect(Collectors.toList()),
                false, true, true, false, false, true
        );
    }

    public void renderCompanyList(MatrixStack matrices, TextRenderer font) {
        if (visible) {
            super.render(matrices, font);
            renderExtras(matrices, font);
        }
    }

    private void renderExtras(MatrixStack matrices, TextRenderer font) {
        int itemsToShow = (height - 24) / 20;
        int count = 0;

        for (NameColorDataBase data : CompanyManager.COMPANY_LIST) {
            if (!(data instanceof Company company)) continue;
            if (count >= itemsToShow) break;

            int drawY = y + 6 + 24 + 20 * count;

            font.drawWithShadow(matrices, "路線数: " + company.ownedRoutes.size(), x + 8, drawY + 10, 0xAAAAAA);

            count++;
        }
    }

    public void tickCompanyList() {
        if (visible) {
            super.tick();
        }
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }
}
