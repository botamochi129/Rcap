package com.botamochi.rcap.client.screen;

import com.botamochi.rcap.block.entity.RidingPosBlockEntity;
import com.botamochi.rcap.client.network.ClientNetworking;
import mtr.client.ClientData;
import mtr.data.Platform;
import mtr.data.RailwayData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class RidingPosScreen extends Screen {

    private static final int LIST_WIDTH = 300;
    private static final int ITEM_HEIGHT = 20;

    private final RidingPosBlockEntity blockEntity;
    private final BlockPos blockPos;

    private ScrollablePlatformList platformList;
    private ButtonWidget saveButton;

    private final Set<Long> selectedPlatformIds = new HashSet<>();
    private List<Platform> nearbyPlatforms = new ArrayList<>();

    public RidingPosScreen(RidingPosBlockEntity blockEntity) {
        super(Text.literal("乗車位置設定"));
        this.blockEntity = blockEntity;
        this.blockPos = blockEntity.getPos();
    }

    @Override
    protected void init() {
        selectedPlatformIds.clear();
        if (blockEntity.getPlatformId() != -1) {
            selectedPlatformIds.add(blockEntity.getPlatformId());
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        var world = mc.world;
        if (world == null) return;

        var station = RailwayData.getStation(ClientData.STATIONS, ClientData.DATA_CACHE, blockPos);
        if (station == null) return;

        nearbyPlatforms = new ArrayList<>(ClientData.DATA_CACHE.requestStationIdToPlatforms(station.id).values());
        nearbyPlatforms.sort(Comparator.comparing(p -> p.name));

        int listX = (width - LIST_WIDTH) / 2;
        int listY = 40;
        int listHeight = height - 100;

        platformList = new ScrollablePlatformList(mc, LIST_WIDTH, listHeight, listY, listY + listHeight, ITEM_HEIGHT);
        platformList.setRenderBackground(false);
        platformList.setLeftPos(listX);

        for (Platform platform : nearbyPlatforms) {
            boolean checked = selectedPlatformIds.contains(platform.id);
            platformList.addPublicEntry(new PlatformEntry(platform.name + "（ID: " + platform.id + "）", platform.id, checked));
        }

        addSelectableChild(platformList);

        int bx = (this.width - 100) / 2;
        int by = this.height - 40;
        saveButton = new ButtonWidget(bx, by, 100, 20, Text.literal("保存"), b -> closeWithSave());
        addDrawableChild(saveButton);
    }

    private void closeWithSave() {
        long selectedId = -1L;
        for (PlatformEntry entry : platformList.children()) {
            if (entry.checkbox.isChecked()) {
                selectedId = entry.platformId;
                break;
            }
        }

        // ここを直接 ClientNetworking の送信メソッドにする
        ClientNetworking.sendUpdatePlatformIdPacket(blockPos, selectedId);

        MinecraftClient.getInstance().setScreen(null);
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    /**
     * サーバから送られた platformId を GUI の選択チェックボックスに反映する
     */
    public void updateSelectedPlatform(long platformId) {
        platformList.children().forEach(entry -> {
            entry.checkbox.setChecked(entry.platformId == platformId);
        });
    }

    @Override
    public void close() {
        closeWithSave();
        super.close();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        drawCenteredText(matrices, textRenderer, title.getString(), width / 2, 15, 0xFFFFFF);

        // これを追加👇：スクロールリストの描画
        platformList.render(matrices, mouseX, mouseY, delta);

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    // ---------- スクロール対応リスト ----------
    private static class ScrollablePlatformList extends EntryListWidget<PlatformEntry> {
        public ScrollablePlatformList(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
            super(client, width, height, top, bottom, itemHeight);
        }

        public void addPublicEntry(PlatformEntry entry) {
            super.addEntry(entry); // protected → 公開
        }

        @Override
        protected int getScrollbarPositionX() {
            return getRowLeft() + LIST_WIDTH - 8;
        }

        @Override
        public int getRowWidth() {
            return LIST_WIDTH;
        }

        @Override
        public void appendNarrations(NarrationMessageBuilder builder) {
        }
    }

    public static class PlatformEntry extends EntryListWidget.Entry<PlatformEntry> {
        public final long platformId;
        public final MyCheckboxWidget checkbox;

        public PlatformEntry(String label, long platformId, boolean selected) {
            this.platformId = platformId;
            this.checkbox = new MyCheckboxWidget(0, 0, LIST_WIDTH - 10, ITEM_HEIGHT, Text.literal(label), selected);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return checkbox.mouseClicked(mouseX, mouseY, button);
        }

        public List<? extends Element> children() {
            return List.of(checkbox);
        }

        public List<? extends Selectable> selectableChildren() {
            return List.of(checkbox);
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth,
                           int entryHeight, int mouseX, int mouseY, boolean hovered, float delta) {
            checkbox.x = x + 5;
            checkbox.y = y;
            checkbox.render(matrices, mouseX, mouseY, delta);
        }
    }

    public static class MyCheckboxWidget extends CheckboxWidget {

        public MyCheckboxWidget(int x, int y, int width, int height, Text message, boolean checked) {
            super(x, y, width, height, message, checked);
        }

        public void setChecked(boolean value) {
            // Yarn mappingでは setChecked などが存在しないため「トグル処理」を真似る
            if (value != isChecked()) {
                // 状態を逆転させる＝ボタンを押された時と同じ処理
                this.onPress(); // これで内部 checked 状態が切り替わる
            }
        }

        public boolean isChecked() {
            return super.isChecked();
        }
    }
}
