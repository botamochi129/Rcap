package com.botamochi.rcap.client.screen;

import com.botamochi.rcap.data.Company;
import com.botamochi.rcap.data.CompanyManager;
import com.botamochi.rcap.network.RcapServerPackets;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

import java.util.List;
import io.netty.buffer.Unpooled;

public class CompanyManagerScreen extends Screen {

    private List<Company> companies;
    private TextFieldWidget nameField;
    private int selectedIndex = -1;

    private ButtonWidget addButton;
    private ButtonWidget saveButton;

    public CompanyManagerScreen() {
        super(Text.literal("会社管理"));
        this.companies = CompanyManager.COMPANY_LIST;
    }

    public static void open() {
        MinecraftClient.getInstance().setScreen(new CompanyManagerScreen());
    }

    @Override
    protected void init() {
        int centerX = width / 2;

        nameField = new TextFieldWidget(textRenderer, centerX - 70, 40, 140, 20, Text.literal("会社名"));
        addSelectableChild(nameField);

        addButton = addDrawableChild(new ButtonWidget(centerX - 75, 70, 60, 20, Text.literal("追加"), btn -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                long id = CompanyManager.getNextId();
                Company newCompany = new Company(id, name);
                CompanyManager.COMPANY_LIST.add(newCompany);
                nameField.setText("");
            }
        }));

        saveButton = addDrawableChild(new ButtonWidget(centerX + 15, 70, 60, 20, Text.literal("保存"), btn -> {
            if (selectedIndex >= 0 && selectedIndex < companies.size()) {
                Company selected = companies.get(selectedIndex);
                selected.name = nameField.getText();
                sendUpdatePacket(selected);
            }
        }));
    }

    private void sendUpdatePacket(Company company) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeLong(company.id);
        buf.writeString(company.name);
        buf.writeInt(company.color); // まだ固定ですが、後でカラー選択追加予定
        ClientPlayNetworking.send(RcapServerPackets.UPDATE_COMPANY, buf);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        int y = 110;
        for (int i = 0; i < companies.size(); i++) {
            Company c = companies.get(i);
            if (i == selectedIndex) {
                fill(matrices, width / 2 - 80, y - 2, width / 2 + 80, y + 10, 0x663366FF);
            }
            textRenderer.draw(matrices, c.name, width / 2 - 70, y, 0xFFFFFF);
            y += 15;
        }

        nameField.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int y = 110;
        for (int i = 0; i < companies.size(); i++) {
            if (mouseX > width / 2 - 80 && mouseX < width / 2 + 80 &&
                    mouseY > y - 2 && mouseY < y + 10) {
                selectedIndex = i;
                nameField.setText(companies.get(i).name);
                return true;
            }
            y += 15;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
