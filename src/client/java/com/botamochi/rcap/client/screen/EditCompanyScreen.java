package com.botamochi.rcap.client.screen;

import com.botamochi.rcap.data.Company;
import com.botamochi.rcap.data.CompanyManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.client.util.math.MatrixStack;

public class EditCompanyScreen extends Screen {

    private final Screen parent;
    private final Company editingCompany;

    private TextFieldWidget nameField;

    private final CompanyDashboardList companyDashboardList;

    public EditCompanyScreen(Screen parent, CompanyDashboardList companyDashboardList, Company editingCompany) {
        super(Text.translatable("rcap.edit_company.title"));
        this.parent = parent;
        this.editingCompany = editingCompany;
        this.companyDashboardList = companyDashboardList; // 保存しておく！
    }

    @Override
    protected void init() {
        nameField = new TextFieldWidget(textRenderer, width / 2 - 100, height / 4, 200, 20, Text.translatable("rcap.edit_company.name"));
        nameField.setText(editingCompany.name);
        addSelectableChild(nameField);
        addDrawableChild(nameField);

        addDrawableChild(new ButtonWidget(width / 2 - 100, height / 4 + 40, 200, 20, Text.translatable("gui.done"), button -> {
            editingCompany.name = nameField.getText();
            CompanyManager.COMPANY_LIST.add(editingCompany);

            companyDashboardList.resetData(); // ← エラー解消＆リストに反映される！

            client.setScreen(parent);
        }));

        addDrawableChild(new ButtonWidget(width / 2 - 100, height / 4 + 70, 200, 20, Text.translatable("gui.cancel"), button -> {
            client.setScreen(parent);
        }));
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
