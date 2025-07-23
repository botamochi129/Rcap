package com.botamochi.rcap.client.screen;

import com.botamochi.rcap.data.Company;
import mtr.screen.DashboardScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class EditCompanyScreen extends Screen {

    private final Company company;
    private final Screen parent;
    private TextFieldWidget nameField;

    public EditCompanyScreen(Company company, Screen parent) {
        super(Text.translatable("rcap.edit_company.title"));
        this.company = company;
        this.parent = parent;
    }

    @Override
    protected void init() {
        nameField = new TextFieldWidget(textRenderer, width / 2 - 100, 60, 200, 20, company.name);
        addDrawableChild(nameField);

        addDrawableChild(new ButtonWidget(width / 2 - 105, 100, 100, 20, Text.translatable("mtr.gui.save"), btn -> {
            company.name = nameField.getText();
            MinecraftClient.getInstance().setScreen(parent);
        }));

        addDrawableChild(new ButtonWidget(width / 2 + 5, 100, 100, 20, Text.translatable("mtr.gui.cancel"), btn -> {
            MinecraftClient.getInstance().setScreen(parent);
        }));
    }
}
