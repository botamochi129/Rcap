package com.botamochi.rcap.client.mixin;

import com.botamochi.rcap.client.screen.CompanyDashboardOverlay;
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

    protected DashboardScreenMixin(Text title) {
        super(title);
    }

    @Shadow @Final protected ButtonWidget buttonTabStations;
    @Shadow @Final protected ButtonWidget buttonTabRoutes;
    @Shadow @Final protected ButtonWidget buttonTabDepots;

    @Unique private ButtonWidget companyButton;
    @Unique private CompanyDashboardOverlay companyOverlay;
    @Unique private boolean companyTabSelected = false;

    @Inject(method = "init", at = @At("TAIL"))
    private void injectCompanyTab(CallbackInfo ci) {
        int tabWidth = DashboardScreen.PANEL_WIDTH / 4;

        buttonTabStations.x = 0;
        buttonTabStations.setWidth(tabWidth);
        buttonTabRoutes.x = tabWidth;
        buttonTabRoutes.setWidth(tabWidth);
        buttonTabDepots.x = tabWidth * 2;
        buttonTabDepots.setWidth(tabWidth);

        companyButton = new ButtonWidget(tabWidth * 3, 0, tabWidth, 20,
                Text.translatable("gui.rcap.companies"),
                btn -> selectCompanyTab());
        this.addDrawableChild(companyButton);

        companyOverlay = new CompanyDashboardOverlay((DashboardScreen) (Object) this);
    }

    @Unique
    private void selectCompanyTab() {
        companyTabSelected = true;

        // 他タブをactive戻す
        buttonTabStations.active = true;
        buttonTabRoutes.active = true;
        buttonTabDepots.active = true;
        companyButton.active = false;

        if (companyOverlay != null) {
            companyOverlay.show();
        }
    }

    // 他のタブが押されたら会社UIを非表示
    @Inject(method = "onSelectTab", at = @At("HEAD"), remap = false)
    private void onOtherTabSelected(CallbackInfo ci) {
        if (companyTabSelected) {
            companyTabSelected = false;
            companyButton.active = true;
            if (companyOverlay != null) {
                companyOverlay.hide();
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickCompanyTab(CallbackInfo ci) {
        if (companyTabSelected && companyOverlay != null) {
            companyOverlay.tick();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderCompanyTab(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (companyTabSelected && companyOverlay != null) {
            companyOverlay.render(matrices, mouseX, mouseY, delta);
        }
    }
}
