package com.botamochi.rcap.client.screen;

import com.botamochi.rcap.data.Company;
import com.botamochi.rcap.data.CompanyManager;
import mtr.data.NameColorDataBase;
import mtr.mappings.UtilitiesClient;
import mtr.screen.DashboardList;
import mtr.screen.DashboardScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;
import java.util.stream.Collectors;

public class CompanyDashboardList extends DashboardList implements Element, Drawable, Selectable {

    public CompanyDashboardList(DashboardScreen parentScreen) {
        super(
                (data, idx) -> {},
                null,
                (data, idx) -> {
                    if (data instanceof Company company) {
                        UtilitiesClient.setScreen(MinecraftClient.getInstance(), new EditCompanyScreen(company, parentScreen));
                    }
                },
                null,
                (data, idx) -> {
                    UtilitiesClient.setScreen(MinecraftClient.getInstance(), new EditCompanyScreen(
                            new Company(System.currentTimeMillis(), "", 0xFFFFFF), parentScreen
                    ));
                },
                (data, idx) -> {
                    if (data instanceof Company company) {
                        CompanyManager.COMPANY_LIST.remove(company);
                    }
                },
                CompanyDashboardList::getCompanyList,
                () -> "",
                s -> {}
        );
    }

    private static List<NameColorDataBase> getCompanyList() {
        return CompanyManager.COMPANY_LIST.stream().map(e -> (NameColorDataBase) e).collect(Collectors.toList());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        // 空実装でOK
    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        // 必要ならここに読み上げ情報を追加
    }
}