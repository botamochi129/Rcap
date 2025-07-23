package com.botamochi.rcap.client.screen;

import com.botamochi.rcap.data.Company;
import com.botamochi.rcap.data.CompanyManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.io.File;

public class EditCompanyScreen extends Screen {

    private final Screen parent;
    private final CompanyDashboardList dashboardList;
    private final Company company;

    private TextFieldWidget nameField;
    private ButtonWidget colorButton;

    public EditCompanyScreen(Screen parent, CompanyDashboardList dashboardList, Company company) {
        super(Text.literal("会社編集"));
        this.parent = parent;
        this.dashboardList = dashboardList;
        this.company = company;
    }

    @Override
    protected void init() {
        final int centerX = width / 2;
        final int startY = height / 4;

        nameField = new TextFieldWidget(textRenderer, centerX - 100, startY, 200, 20, Text.literal("会社名"));
        nameField.setText(company.name);
        addDrawableChild(nameField);
        addSelectableChild(nameField);

        colorButton = new ButtonWidget(centerX - 100, startY + 30, 200, 20, getColorLabel(company.color), button -> {
            company.color = nextColor(company.color);
            colorButton.setMessage(getColorLabel(company.color));
        });
        addDrawableChild(colorButton);

        addDrawableChild(new ButtonWidget(centerX - 100, startY + 60, 98, 20, Text.literal("保存"), button -> {
            company.name = nameField.getText().trim();

            if (!CompanyManager.COMPANY_LIST.contains(company)) {
                CompanyManager.COMPANY_LIST.add(company);
            }

            File saveFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "rcap/companies.dat");
            CompanyManager.save(saveFile);

            dashboardList.resetData();
            MinecraftClient.getInstance().setScreen(parent);
        }));

        addDrawableChild(new ButtonWidget(centerX + 2, startY + 60, 98, 20, Text.literal("キャンセル"), button -> {
            MinecraftClient.getInstance().setScreen(parent);
        }));
    }

    private Text getColorLabel(int color) {
        return Text.literal(String.format("色: #%06X", color & 0xFFFFFF));
    }

    private int nextColor(int current) {
        int[] presets = { 0xFF0000, 0x00FF00, 0x0000FF, 0xFFFFFF, 0xFFFF00 };
        for (int i = 0; i < presets.length; i++) {
            if ((current & 0xFFFFFF) == presets[i]) {
                return presets[(i + 1) % presets.length];
            }
        }
        return presets[0];
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        drawCenteredText(matrices, textRenderer, title, width / 2, 20, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
