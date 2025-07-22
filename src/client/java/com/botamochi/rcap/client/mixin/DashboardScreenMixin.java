package com.botamochi.rcap.client.mixin;

import com.botamochi.rcap.data.CompanyManager;
import mtr.client.IDrawing;
import mtr.data.IGui;
import mtr.screen.DashboardList;
import mtr.screen.DashboardScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.lang.reflect.Field;


@Mixin(value = DashboardScreen.class, priority = 1001)
public abstract class DashboardScreenMixin extends Screen {

    @Unique
    private boolean companyTabSelected = false;

    @Unique
    private Button buttonTabCompany;

    @Unique private final DashboardList companyDashboardList =
            new DashboardList((data, index) -> {}, // onFind
                    (data, index) -> {}, // onDraw
                    (data, index) -> {}, // onEdit
                    null, null,
                    null, // onDelete
                    () -> CompanyManager.getDashboardEntries(), // your data
                    () -> "",
                    s -> {}
            );

    protected DashboardScreenMixin(Text title) {
        super(title);
    }

    /**
     * タブ追加（本家 init() 後）
     */
    @Inject(method = "init", at = @At("TAIL"))
    private void injectCompanyTab(CallbackInfo ci) {
        final DashboardScreen self = (DashboardScreen)(Object)this;

        int x = 3 * (DashboardScreen.PANEL_WIDTH / 3); // STATIONS, ROUTES, DEPOTS の次の位置
        int w = DashboardScreen.PANEL_WIDTH / 3;

        buttonTabCompany = UtilitiesClient.newButton(
                Text.literal("会社"),
                btn -> onSelectCompanyTab()
        );
        IDrawing.setPositionAndWidth(buttonTabCompany, x, 0, w);
        self.addDrawableChild(buttonTabCompany);

        if (companyTabSelected) {
            // 通常の初期化を避けて、自前のUIだけ描画
            companyDashboardList.x = 0;
            companyDashboardList.y = IGui.SQUARE_SIZE;
            companyDashboardList.width = DashboardScreen.PANEL_WIDTH;
            companyDashboardList.init(self::addDrawableChild);
        }
    }

    /**
     * render phase
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void renderCompanyTab(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (companyTabSelected) {
            renderBackground(matrices);
            matrices.push();
            matrices.translate(0, 0, 500);

            // 左パネル背景
            Gui.fill(matrices, 0, 0, DashboardScreen.PANEL_WIDTH, height, IGui.ARGB_BACKGROUND);

            textRenderer.draw(matrices, "会社管理", 10, 15, 0xFFFFFF);
            companyDashboardList.render(matrices, textRenderer);

            super.render(matrices, mouseX, mouseY, delta);
            matrices.pop();
            ci.cancel();
        }
    }

    // tick = データ更新
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void tickCompanyTab(CallbackInfo ci) {
        if (companyTabSelected) {
            companyDashboardList.tick();
            ci.cancel();
        }
    }

    // マウススクロール反映
    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void scrollCompanyTab(double mouseX, double mouseY, double amount, CallbackInfoReturnable<Boolean> cir) {
        if (companyTabSelected) {
            companyDashboardList.mouseScrolled(mouseX, mouseY, amount);
            cir.setReturnValue(true);
        }
    }

    /**
     * タブ切り替えロジック
     */
    @Unique
    private void onSelectCompanyTab() {
        companyTabSelected = true;

        // 既存タブを非表示に切り替える
        try {
            Field selectedTabField = DashboardScreen.class.getDeclaredField("selectedTab");
            selectedTabField.setAccessible(true);
            selectedTabField.set(this, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (buttonTabCompany != null) {
            buttonTabCompany.active = false;
        }
    }

    /**
     * 他タブが押されたら companyTabSelected を false に戻す
     */
    @Inject(method = "onSelectTab", at = @At("TAIL"))
    private void resetCompanyTab(CallbackInfo ci) {
        companyTabSelected = false;
        if (buttonTabCompany != null) {
            buttonTabCompany.active = true;
        }
    }
}
