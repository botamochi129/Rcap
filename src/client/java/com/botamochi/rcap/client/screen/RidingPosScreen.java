package com.botamochi.rcap.client.screen;

import com.botamochi.rcap.screen.RidingPosScreenHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class RidingPosScreen extends HandledScreen<RidingPosScreenHandler> {

    private TextFieldWidget platformIdField;

    public RidingPosScreen(RidingPosScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.passEvents = false;
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - 150) / 2;
        int y = (height - 20) / 2;

        // プラットフォームIDの入力欄
        platformIdField = new TextFieldWidget(textRenderer, x, y, 150, 20, Text.literal("Platform ID"));
        platformIdField.setText(Long.toString(handler.getPlatformId()));

        addSelectableChild(platformIdField);

        // 保存ボタン
        addDrawableChild(new ButtonWidget(x, y + 25, 150, 20, Text.literal("保存"), button -> {
            try {
                long newId = Long.parseLong(platformIdField.getText());
                handler.setPlatformId(newId);
                // サーバへパケット送る必要あり（下参照）
                client.player.closeHandledScreen();
            } catch (NumberFormatException e) {
                // 入力が数字でない場合のエラーハンドリング
            }
        }));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        platformIdField.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return platformIdField.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }
}
