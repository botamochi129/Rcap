package com.botamochi.rcap.client.screen;

import com.botamochi.rcap.data.Company;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.mtr.mod.screen.DashboardList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CompanyTab extends DashboardList {

    private final List<Company> companies = new ArrayList<>();
    private Company selectedCompany;
    private final ButtonWidget createCompanyButton;
    private final List<Drawable> drawables = new ArrayList<>();
    private final List<Element> children = new ArrayList<>();

    public CompanyTab() {
        super(0, "会社", 0xFFFFFF);
        createCompanyButton = ButtonWidget.builder(Text.of("会社を作成"), button -> {
            Company newCompany = new Company(UUID.randomUUID().toString(), "新しい会社");
            companies.add(newCompany);
            selectedCompany = newCompany;
        }).dimensions(10, 10, 120, 20).build();
    }

    // MTRのDashboardScreenから呼び出される
    public void init(Screen screen) {
        drawables.clear();
        children.clear();

        drawables.add(createCompanyButton);
        children.add(createCompanyButton);
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        createCompanyButton.render(matrices, mouseX, mouseY, delta);

        int y = 50;
        for (Company company : companies) {
            screenText(matrices, Text.of(company.getName()), 20, y, 0xFFFFFF);
            y += 15;
        }
    }

    @Override
    public String title() {
        return "会社";
    }

    public List<Drawable> getDrawables() {
        return drawables;
    }

    public List<Element> getChildren() {
        return children;
    }
}
