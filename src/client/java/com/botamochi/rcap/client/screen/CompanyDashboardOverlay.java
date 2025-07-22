package com.botamochi.rcap.client.screen;

import com.botamochi.rcap.client.network.ClientNetworking;
import com.botamochi.rcap.data.Company;
import com.botamochi.rcap.data.CompanyListEntry;
import com.botamochi.rcap.data.CompanyManager;
import mtr.data.NameColorDataBase;
import mtr.screen.DashboardList;
import mtr.screen.DashboardScreen;
import mtr.screen.WidgetBetterTextField;
import mtr.screen.WidgetColorSelector;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class CompanyDashboardOverlay {

    private final DashboardScreen screen;

    private DashboardList dashboardList;
    private WidgetBetterTextField searchField;
    private ButtonWidget addButton;

    // 編集UI
    private WidgetBetterTextField nameField;
    private ButtonWidget saveButton;
    private ButtonWidget deleteButton;

    private Company selectedCompany = null;

    private boolean visible = false;

    private WidgetBetterTextField colorField;
    private ButtonWidget colorApplyButton;

    public CompanyDashboardOverlay(DashboardScreen screen) {
        this.screen = screen;
    }

    public void show() {
        if (visible) return;

        final int spacing = 4;

        // 検索欄
        searchField = new WidgetBetterTextField("検索", 64);
        searchField.setX(spacing);
        searchField.y = 24;
        screen.addDrawableChild(searchField);

        // リスト
        dashboardList = new DashboardList(
                this::onSelect,
                this::onClick,
                this::onEdit,
                () -> {},
                (item, index) -> {},
                (item, index) -> {},
                this::getCompanyList,
                searchField::getText,
                text -> {}
        );
        dashboardList.y = 50;
        dashboardList.width = DashboardScreen.PANEL_WIDTH;
        dashboardList.height = screen.height - 140;
        dashboardList.init(screen::addDrawableChild);

        // 会社を追加
        addButton = new ButtonWidget(spacing, screen.height - 32, 120, 20,
                Text.translatable("gui.rcap.add_company"),
                btn -> {
                    Company company = new Company(CompanyManager.getNextId(), "新しい会社", 0x808080);
                    CompanyManager.COMPANY_LIST.add(company);
                    ClientNetworking.sendCreateCompany(company);
                    updateList();
                });
        screen.addDrawableChild(addButton);

        // 編集欄
        nameField = new WidgetBetterTextField("名前", 64);
        nameField.setX(140);
        nameField.y = screen.height - 80;
        screen.addDrawableChild(nameField);

        saveButton = new ButtonWidget(310, screen.height - 80, 60, 20,
                Text.translatable("gui.rcap.save"),
                btn -> {
                    if (selectedCompany != null) {
                        selectedCompany.name = nameField.getText();
                        selectedCompany.color = Integer.decode(colorField.getText());
                        ClientNetworking.sendUpdateCompany(selectedCompany);
                        updateList();
                    }
                });
        screen.addDrawableChild(saveButton);

        deleteButton = new ButtonWidget(380, screen.height - 80, 60, 20,
                Text.translatable("gui.rcap.delete"),
                btn -> {
                    if (selectedCompany != null) {
                        CompanyManager.COMPANY_LIST.remove(selectedCompany);
                        ClientNetworking.sendDeleteCompany(selectedCompany.id);
                        selectedCompany = null;
                        nameField.setText("");
                        updateList();
                    }
                });
        screen.addDrawableChild(deleteButton);

        //color
        colorField = new WidgetBetterTextField("#808080", 7);
        colorField.setX(550);
        colorField.y = screen.height - 80;
        screen.addDrawableChild(colorField);

        colorApplyButton = new ButtonWidget(620, screen.height - 80, 30, 20, Text.of(">"), btn -> {
            try {
                int parsedColor = Integer.decode(colorField.getText());
                if (selectedCompany != null) {
                    selectedCompany.color = parsedColor;
                }
            } catch (NumberFormatException e) {}
        });
        screen.addDrawableChild(colorApplyButton);

        visible = true;
        updateList();
    }

    public void hide() {
        if (!visible) return;

        screen.children().remove(searchField);
        screen.children().remove(addButton);
        screen.children().remove(nameField);
        screen.children().remove(saveButton);
        screen.children().remove(deleteButton);
        if (colorField != null) screen.children().remove(colorField);
        if (colorApplyButton != null) screen.children().remove(colorApplyButton);
        screen.children().remove(dashboardList);

        visible = false;
    }

    public void tick() {
        if (!visible) return;
        searchField.tick();
        nameField.tick();
        dashboardList.tick();
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (!visible) return;
        searchField.render(matrices, mouseX, mouseY, delta);
        nameField.render(matrices, mouseX, mouseY, delta);
        addButton.render(matrices, mouseX, mouseY, delta);
        saveButton.render(matrices, mouseX, mouseY, delta);
        deleteButton.render(matrices, mouseX, mouseY, delta);
        colorField.render(matrices, mouseX, mouseY, delta);
        colorApplyButton.render(matrices, mouseX, mouseY, delta);
        dashboardList.render(matrices, MinecraftClient.getInstance().textRenderer);
    }

    private void onSelect(NameColorDataBase item, int index) {
        if (item instanceof CompanyListEntry entry) {
            selectedCompany = entry.company;
            nameField.setText(selectedCompany.name);
            colorField.setText(String.format("#%06X", selectedCompany.color));
        }
    }

    private void onClick(NameColorDataBase item, int index) {
        onSelect(item, index);
    }

    private void onEdit(NameColorDataBase item, int index) {
        onSelect(item, index);
    }

    private void updateList() {
        dashboardList.setData(getCompanyList(), true, false, false, false, false, false);
    }

    private List<NameColorDataBase> getCompanyList() {
        List<NameColorDataBase> list = new ArrayList<>();
        for (Company company : CompanyManager.COMPANY_LIST) {
            list.add(new CompanyListEntry(company));
        }
        return list;
    }
}
