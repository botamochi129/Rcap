package com.botamochi.rcap.client.mixin;

import com.botamochi.rcap.client.screen.CompanyDashboardList;
import mtr.screen.DashboardList;
import mtr.screen.DashboardListSelectorScreen;
import mtr.screen.DashboardScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
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

    // 本家のリストWidget（通常: dashboardList, dashboardListSelectorScreen など）
    @Shadow protected DashboardList dashboardList;
    @Shadow protected DashboardListSelectorScreen dashboardListSelectorScreen;

    @Unique private ButtonWidget buttonTabCompany;
    @Unique private CompanyDashboardList companyDashboardList;
    @Unique private boolean isCompanyTab = false;

    @Inject(method = "init", at = @At("TAIL"))
    private void injectCompanyTab(CallbackInfo ci) {
        int tabCount = 4;
        int tabWidth = DashboardScreen.PANEL_WIDTH / tabCount;

        buttonTabStations.x = 0;
        buttonTabStations.setWidth(tabWidth);
        buttonTabRoutes.x = tabWidth;
        buttonTabRoutes.setWidth(tabWidth);
        buttonTabDepots.x = tabWidth * 2;
        buttonTabDepots.setWidth(tabWidth);

        if (buttonTabCompany == null) {
            buttonTabCompany = new ButtonWidget(
                    tabWidth * 3, 0, tabWidth, 20,
                    Text.translatable("gui.rcap.companies"),
                    btn -> selectCompanyTab()
            );
        }
        this.addDrawableChild(buttonTabCompany);

        if (companyDashboardList == null) {
            companyDashboardList = new CompanyDashboardList((DashboardScreen)(Object)this);
        }
    }

    private void selectCompanyTab() {
        isCompanyTab = true;
        buttonTabStations.active = true;
        buttonTabRoutes.active = true;
        buttonTabDepots.active = true;
        buttonTabCompany.active = false;

        // 他タブのリストをremove
        if (dashboardList != null) this.remove(dashboardList);
        if (dashboardListSelectorScreen != null) this.remove(dashboardListSelectorScreen);

        // 会社リストだけadd
        if (!this.children().contains(companyDashboardList)) {
            this.addDrawableChild(companyDashboardList);
        }
    }

    @Inject(method = "onSelectTab", at = @At("HEAD"), remap = false)
    private void onOtherTabSelected(CallbackInfo ci) {
        if (isCompanyTab) {
            isCompanyTab = false;
            if (buttonTabCompany != null) buttonTabCompany.active = true;
            // 会社リストWidgetをremove
            this.remove(companyDashboardList);
        }
    }
}