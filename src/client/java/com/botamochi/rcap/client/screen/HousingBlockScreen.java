package com.botamochi.rcap.client.screen;

import com.botamochi.rcap.client.RcapNetworkingClient;
import com.botamochi.rcap.screen.HousingBlockScreenHandler;
import com.botamochi.rcap.screen.OfficeBlockScreenHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class HousingBlockScreen extends HandledScreen<HousingBlockScreenHandler> {
    private TextFieldWidget textField;

    public HousingBlockScreen(HousingBlockScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        textField = new TextFieldWidget(this.textRenderer, this.x + 30, this.y + 40, 60, 20, Text.empty());
        this.addDrawableChild(textField);
        this.addDrawableChild(new ButtonWidget(this.x + 100, this.y + 40, 40, 20, Text.literal("決定"), btn -> {

            int value = 1;
            try {
                value = Integer.parseInt(textField.getText());
            } catch (NumberFormatException ignored) {}
            int finalValue = value;
            handler.context.run((world, pos) -> {
                RcapNetworkingClient.sendOfficeEmployees(pos, finalValue);
            });
            this.close();
        }));
    }

    @Override
    protected void drawBackground(net.minecraft.client.util.math.MatrixStack matrices, float delta, int mouseX, int mouseY) {}
}
