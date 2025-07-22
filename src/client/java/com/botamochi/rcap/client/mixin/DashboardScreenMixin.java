package com.botamochi.rcap.client.mixin;

import com.botamochi.rcap.client.screen.CompanyDashboardList;
import mtr.screen.DashboardScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DashboardScreen.class)
public abstract class DashboardScreenMixin extends Screen {

    protected DashboardScreenMixin(Text title) { super(title); }

    @Shadow @Final protected ButtonWidget buttonTabStations;
    @Shadow @Final protected ButtonWidget buttonTabRoutes;
    @Shadow @Final protected ButtonWidget buttonTabDepots;

    // 会社タブ関連
    @Unique private ButtonWidget buttonTabCompany;
    @Unique private CompanyDashboardList companyDashboardList;
    @Unique private boolean isCompanyTab = false;

    @Inject(method = "init", at = @At("TAIL"))
    private void injectCompanyTab(CallbackInfo ci) {
        // タブを4分割
        int tabCount = 4;
        int tabWidth = DashboardScreen.PANEL_WIDTH / tabCount;
        // 既存タブの位置・サイズ調整（yarn: ButtonWidgetのx/wなどはpublic）
        buttonTabStations.x = 0;
        buttonTabStations.setWidth(tabWidth);
        buttonTabRoutes.x = tabWidth;
        buttonTabRoutes.setWidth(tabWidth);
        buttonTabDepots.x = tabWidth * 2;
        buttonTabDepots.setWidth(tabWidth);

        // 会社タブ追加（すでに作成済みならスキップ）
        if (buttonTabCompany == null) {
            buttonTabCompany = new ButtonWidget(
                    tabWidth * 3, // x
                    0,            // y
                    tabWidth,     // width
                    20,           // height
                    Text.translatable("gui.rcap.companies"), // ボタン表示テキスト
                    btn -> {
                        isCompanyTab = true;
                        setCompanyTabActive();
                    }
            );
        }
        this.addDrawableChild(buttonTabCompany);

        // 会社用DashboardList生成（初回のみ）
        if (companyDashboardList == null) {
            companyDashboardList = new CompanyDashboardList(this); // 会社用リストWidget（後述）
        }
    }

    @Inject(method = "onSelectTab", at = @At("HEAD"), remap = false)
    private void onOtherTabSelected(CallbackInfo ci) {
        if (isCompanyTab) {
            isCompanyTab = false;
            if (companyDashboardList != null) {
                companyDashboardList.hide();
            }
            if (buttonTabCompany != null) {
                buttonTabCompany.active = true;
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickCompanyTab(CallbackInfo ci) {
        if (isCompanyTab && companyDashboardList != null) {
            companyDashboardList.tick();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderCompanyTab(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (isCompanyTab && companyDashboardList != null) {
            companyDashboardList.render(matrices, mouseX, mouseY, delta);
        }
    }

    @Unique
    private void setCompanyTabActive() {
        // 他タブをactiveに、会社タブは非active
        buttonTabStations.active = true;
        buttonTabRoutes.active = true;
        buttonTabDepots.active = true;
        buttonTabCompany.active = false;
        if (companyDashboardList != null) companyDashboardList.show();
    }
}