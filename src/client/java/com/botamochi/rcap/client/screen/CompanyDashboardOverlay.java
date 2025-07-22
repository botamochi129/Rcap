package com.botamochi.rcap.client.screen;

import com.botamochi.rcap.client.network.ClientNetworking;
import com.botamochi.rcap.data.Company;
import com.botamochi.rcap.data.CompanyListEntry;
import com.botamochi.rcap.data.CompanyManager;
import mtr.data.NameColorDataBase;
import mtr.screen.DashboardList;
import mtr.screen.DashboardScreen;
import mtr.screen.WidgetBetterTextField;
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

    private WidgetBetterTextField colorField;
    private ButtonWidget colorApplyButton;

    private Company selectedCompany = null;

    private boolean visible = false;

    private static final int PADDING = 4;
    private static final int FIELD_HEIGHT = 20;

    public CompanyDashboardOverlay(DashboardScreen screen) {
        this.screen = screen;
    }

    public void show() {
        if (visible) return;

        searchField = new WidgetBetterTextField("検索", 64);
        searchField.setX(PADDING);
        searchField.y = 24;
        screen.addDrawableChild(searchField);

        dashboardList = new DashboardList(
                this::onSelect,
                this::onClick,
                this::onEdit,
                () -> {},
                (item, index) -> {},
                (item, index) -> {},
                this::getCompanyList,
                searchField::getText,
                s -> {}
        );
        dashboardList.y = searchField.y + FIELD_HEIGHT + PADDING;
        dashboardList.width = DashboardScreen.PANEL_WIDTH;
        dashboardList.height = screen.height - dashboardList.y - 60;
        dashboardList.init(screen::addDrawableChild);

        addButton = new ButtonWidget(
                PADDING,
                screen.height - FIELD_HEIGHT - PADDING,
                120,
                FIELD_HEIGHT,
                Text.translatable("gui.rcap.add_company"),
                btn -> {
                    Company company = new Company(CompanyManager.getNextId(), "新しい会社", 0x808080);
                    CompanyManager.COMPANY_LIST.add(company);
                    ClientNetworking.sendCreateCompany(company);
                    updateList();
                }
        );
        screen.addDrawableChild(addButton);

        nameField = new WidgetBetterTextField("名前", 64);
        nameField.setX(140);
        nameField.y = screen.height - FIELD_HEIGHT - PADDING;
        screen.addDrawableChild(nameField);

        saveButton = new ButtonWidget(
                310,
                screen.height - FIELD_HEIGHT - PADDING,
                60,
                FIELD_HEIGHT,
                Text.translatable("gui.rcap.save"),
                btn -> {
                    if (selectedCompany != null) {
                        selectedCompany.name = nameField.getText();
                        try {
                            selectedCompany.color = Integer.decode(colorField.getText());
                        } catch(NumberFormatException ignored){}
                        ClientNetworking.sendUpdateCompany(selectedCompany);
                        updateList();
                    }
                });
        screen.addDrawableChild(saveButton);

        deleteButton = new ButtonWidget(
                380,
                screen.height - FIELD_HEIGHT - PADDING,
                60,
                FIELD_HEIGHT,
                Text.translatable("gui.rcap.delete"),
                btn -> {
                    if (selectedCompany != null) {
                        CompanyManager.COMPANY_LIST.remove(selectedCompany);
                        ClientNetworking.sendDeleteCompany(selectedCompany.id);
                        selectedCompany = null;
                        nameField.setText("");
                        colorField.setText("");
                        updateList();
                    }
                });
        screen.addDrawableChild(deleteButton);

        colorField = new WidgetBetterTextField("#808080", 7);
        colorField.setX(450);
        colorField.y = screen.height - FIELD_HEIGHT - PADDING;
        screen.addDrawableChild(colorField);

        colorApplyButton = new ButtonWidget(
                500,
                screen.height - FIELD_HEIGHT - PADDING,
                30,
                FIELD_HEIGHT,
                Text.of(">"),
                btn -> {
                    if (selectedCompany != null) {
                        try {
                            selectedCompany.color = Integer.decode(colorField.getText());
                            updateList();
                        } catch(NumberFormatException ignored){}
                    }
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
        screen.children().remove(colorField);
        screen.children().remove(colorApplyButton);
        screen.children().remove(dashboardList);

        visible = false;
        selectedCompany = null;
    }

    public void tick() {
        if (!visible) return;
        searchField.tick();
        nameField.tick();
        colorField.tick();
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
