package com.botamochi.rcap.client.screen;

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

    private boolean visible = false;
    private int currentPage = 1;
    private int totalPages = 1;

    // 定数
    private static final int PADDING = 4;
    private static final int FIELD_HEIGHT = 20;

    public CompanyDashboardOverlay(DashboardScreen screen) {
        this.screen = screen;
    }

    public void show() {
        if (visible) return;

        // 検索欄（左上）
        searchField = new WidgetBetterTextField("検索", 64);
        searchField.setX(PADDING);
        searchField.y = 24;
        screen.addDrawableChild(searchField);

        // リスト（検索欄の下）
        dashboardList = new DashboardList(
                this::onSelected,
                this::onClicked,
                this::onEdit,
                () -> {}, (item, index) -> {}, (item, index) -> {},
                this::getPagedCompanyList,
                searchField::getText,
                s -> {}
        );
        dashboardList.y = searchField.y + FIELD_HEIGHT + PADDING;
        dashboardList.width = DashboardScreen.PANEL_WIDTH;
        dashboardList.height = screen.height - dashboardList.y - 28;
        dashboardList.init(screen::addDrawableChild);

        // 「会社を追加」ボタン（左下、MTR準拠）
        int buttonY = screen.height - FIELD_HEIGHT - PADDING;
        addButton = new ButtonWidget(
                PADDING,
                buttonY,
                120,
                FIELD_HEIGHT,
                Text.translatable("gui.rcap.add_company"),
                btn -> {
                    Company newCompany = new Company(CompanyManager.getNextId(), "新しい会社", 0x808080);
                    CompanyManager.COMPANY_LIST.add(newCompany);
                    updateList();
                }
        );
        screen.addDrawableChild(addButton);

        visible = true;
        updateList();
    }

    public void hide() {
        if (!visible) return;

        if (searchField != null) screen.children().remove(searchField);
        if (addButton != null) screen.children().remove(addButton);
        if (dashboardList != null) screen.children().remove(dashboardList);
        visible = false;
    }

    public void tick() {
        if (!visible) return;
        searchField.tick();
        dashboardList.tick();
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (!visible) return;
        searchField.render(matrices, mouseX, mouseY, delta);
        addButton.render(matrices, mouseX, mouseY, delta);
        dashboardList.render(matrices, MinecraftClient.getInstance().textRenderer);
    }

    private List<NameColorDataBase> getPagedCompanyList() {
        List<NameColorDataBase> filtered = getFilteredCompanyList();
        final int itemsPerPage = 10;
        totalPages = Math.max(1, (int) Math.ceil(filtered.size() / (float) itemsPerPage));
        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, filtered.size());
        return filtered.subList(start, end);
    }

    private List<NameColorDataBase> getFilteredCompanyList() {
        String keyword = searchField.getText().toLowerCase().trim();
        List<NameColorDataBase> list = new ArrayList<>();
        for (Company company : CompanyManager.COMPANY_LIST) {
            if (company.name.toLowerCase().contains(keyword)) {
                list.add(new CompanyListEntry(company));
            }
        }
        return list;
    }

    private void updateList() {
        dashboardList.setData(getPagedCompanyList(), true, false, false, false, false, false);
    }

    private void onSelected(NameColorDataBase item, int index) {}
    private void onClicked(NameColorDataBase item, int index) {}
    private void onEdit(NameColorDataBase item, int index) {}
}
