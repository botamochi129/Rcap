package com.botamochi.rcap.client.screen;

import com.botamochi.rcap.client.RcapNetworkingClient;
import com.botamochi.rcap.screen.RidingPosBlockScreenHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

import java.util.List;

public class RidingPosBlockScreen extends HandledScreen<RidingPosBlockScreenHandler> {

    private final List<String> platformNameList;
    private final List<Integer> platformIdList;
    private int selectedIndex = -1;

    public RidingPosBlockScreen(RidingPosBlockScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        // ここでMTR依存API等で駅のplatform名・IDリストを取得してセットするのが理想
        // 例:
        // this.platformNameList = MtrApiHelper.getNearbyPlatformNames(handler.context);
        // this.platformIdList = MtrApiHelper.getNearbyPlatformIds(handler.context);
        this.platformNameList = java.util.Collections.singletonList("Platform 1");
        this.platformIdList = java.util.Collections.singletonList(1);
    }

    @Override
    protected void init() {
        super.init();
        int y = this.y + 30;
        for (int i = 0; i < platformNameList.size(); i++) {
            int idx = i;
            String name = platformNameList.get(i);
            this.addDrawableChild(new ButtonWidget(this.x + 30, y + idx * 24, 120, 20, Text.literal(name), btn -> {
                this.selectedIndex = idx;
            }));
        }
        this.addDrawableChild(new ButtonWidget(this.x + 160, this.y + 30, 60, 20, Text.literal("決定"), btn -> {
            if (selectedIndex >= 0 && selectedIndex < platformIdList.size()) {
                int selectedPlatformId = platformIdList.get(selectedIndex);
                handler.context.run((world, pos) -> {
                    RcapNetworkingClient.sendRidingPlatform(pos, selectedPlatformId);
                });
            }
            this.close();
        }));
    }

    @Override
    protected void drawBackground(net.minecraft.client.util.math.MatrixStack matrices, float delta, int mouseX, int mouseY) {}
}