package com.botamochi.rcap.client.screen;

import mtr.data.NameColorDataBase;
import mtr.screen.DashboardList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 「会社」タブ用のダッシュボードリスト。
 * 駅や路線と同じUIを流用し、管理・編集・削除も同様にできる。
 */
public class CompanyDashboardList extends DashboardList {

    private boolean visible = false;

    public CompanyDashboardList(Screen parent) {
        // 会社データ用に初期化
        super(
                (data, idx) -> {/* onFind: 詳細など */},
                (data, idx) -> {/* onDrawArea: 会社範囲等（不要ならnullでOK） */},
                (data, idx) -> {/* onEdit: 編集画面を開くなど */},
                null, // onSort
                null, // onAdd
                (data, idx) -> {/* onDelete: 削除処理 */},
                () -> getCompanyList(),
                () -> "", // 検索文取得
                s -> {}   // 検索文セット
        );
        // リストサイズ・表示位置は親のDashboardScreenを参照し調整
        this.x = 0;
        this.y = 20; // タブ下
        this.width = 320;
        this.height = 240;
    }

    // 会社データのリスト取得（実装例）
    private static List<NameColorDataBase> getCompanyList() {
        // CompanyManagerやClientDataから会社リストを取得
        // ここでは空リストを返しているので、実際は会社データ構造を用意して返す
        return new ArrayList<>();
    }

    public void show() {
        visible = true;
    }

    public void hide() {
        visible = false;
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (visible) {
            super.render(matrices, MinecraftClient.getInstance().textRenderer);
        }
    }

    public void tick() {
        if (visible) {
            super.tick();
        }
    }
}