package com.botamochi.rcap.client.screen;

import com.botamochi.rcap.data.Company;
import com.botamochi.rcap.data.CompanyManager;
import mtr.screen.DashboardListSelectorScreen;
import mtr.screen.DashboardScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class CompanyDashboardScreen extends DashboardListSelectorScreen<Company> {

    private final DashboardScreen parent;

    public CompanyDashboardScreen(DashboardScreen parent) {
        // 第2引数がリストタイトル、第3引数以降は翻訳キー
        super(
                Text.translatable("rcap.dashboard.company"),
                Text.translatable("rcap.dashboard.company"),
                1001,
                false,
                true,
                false);
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        // "追加" ボタン
        addDrawableChild(
                new ButtonWidget(width - 105, 10, 100, 20, Text.translatable("rcap.button.add_company"), btn -> {
                    Company newCompany = new Company(System.currentTimeMillis(), "", 0xFFFFFF);
                    CompanyManager.addCompany(newCompany);
                    MinecraftClient.getInstance().setScreen(new EditCompanyScreen(newCompany, parent));
                })
        );

        // "戻る" ボタン
        addDrawableChild(
                new ButtonWidget(5, 10, 100, 20, Text.translatable("mtr.gui.back"), btn -> {
                    MinecraftClient.getInstance().setScreen(parent);
                })
        );
    }

    @Override
    protected void updateSelection() {
        // 選択時に編集画面へ
        selectedItem.ifPresent(company ->
                MinecraftClient.getInstance().setScreen(new EditCompanyScreen(company, parent))
        );
    }

    @Override
    protected java.util.List<Company> getList() {
        return CompanyManager.COMPANY_LIST;
    }
}
