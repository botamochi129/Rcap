package com.botamochi.rcap.client.screen;

import com.botamochi.rcap.data.Company;
import com.botamochi.rcap.data.CompanyManager;
import mtr.data.NameColorDataBase;
import mtr.screen.DashboardListSelectorScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class EditCompanyScreen extends Screen {
    private final Company editingCompany;
    private TextFieldWidget nameField;
    private TextFieldWidget colorField;
    private ButtonWidget routesButton;
    private ButtonWidget depotsButton;
    private ButtonWidget saveButton;

    public EditCompanyScreen(Company company) {
        super(Text.translatable(company == null ? "会社の追加" : "会社の編集"));
        this.editingCompany = company == null ? new Company(System.currentTimeMillis(), "", 0xFFFFFF) : company;
    }

    @Override
    protected void init() {
        nameField = new TextFieldWidget(this.textRenderer, 50, 40, 200, 20, Text.translatable("会社名"));
        nameField.setText(editingCompany.name);
        this.addDrawableChild(nameField);

        colorField = new TextFieldWidget(this.textRenderer, 50, 80, 100, 20, Text.translatable("会社カラー"));
        colorField.setText(String.format("#%06X", editingCompany.color));
        this.addDrawableChild(colorField);

        routesButton = new ButtonWidget(50, 120, 150, 20, Text.literal("保有路線を選択"), btn -> {
            // 路線一覧とeditingCompany.ownedRoutesをDashboardListSelectorScreenで編集
            this.client.setScreen(new DashboardListSelectorScreen(
                    () -> this.client.setScreen(this),
                    new ArrayList<>(/* 全路線リスト(NameColorDataBase) */),
                    editingCompany.ownedRoutes,
                    false, // 複数選択かどうか
                    false  // 重複選択可否
            ));
        });
        this.addDrawableChild(routesButton);

        depotsButton = new ButtonWidget(50, 160, 150, 20, Text.literal("保有車庫を選択"), btn -> {
            // 車庫一覧とeditingCompany.ownedDepotsをDashboardListSelectorScreenで編集
            this.client.setScreen(new DashboardListSelectorScreen(
                    () -> this.client.setScreen(this),
                    new ArrayList<>(/* 全車庫リスト(NameColorDataBase) */),
                    editingCompany.ownedDepots,
                    false,
                    false
            ));
        });
        this.addDrawableChild(depotsButton);

        saveButton = new ButtonWidget(50, 200, 80, 20, Text.literal("保存"), btn -> {
            editingCompany.name = nameField.getText();
            try {
                editingCompany.color = Integer.decode(colorField.getText());
            } catch (NumberFormatException ignored) {}
            if (!CompanyManager.COMPANY_LIST.contains(editingCompany)) {
                CompanyManager.COMPANY_LIST.add(editingCompany);
            }
            // サーバー同期が必要ならここでパケット送信
            this.client.setScreen(null); // ダッシュボードへ戻る
        });
        this.addDrawableChild(saveButton);
    }
}