package com.botamochi.rcap.client.mixin;

import com.botamochi.rcap.client.screen.CompanyTabHandler;
import mtr.client.IDrawing;
import mtr.packet.PacketTrainDataGuiClient;
import mtr.screen.DashboardScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import com.botamochi.rcap.data.Company;
import com.botamochi.rcap.data.CompanyManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collections;

@Mixin(DashboardScreen.class)
public abstract class DashboardScreenMixin extends Screen {

    protected DashboardScreenMixin(Text title) {
        super(title);
    }

    @Shadow private ButtonWidget buttonTabStations;
    @Shadow private ButtonWidget buttonTabRoutes;
    @Shadow private ButtonWidget buttonTabDepots;

    @Unique private ButtonWidget buttonTabCompanies;
    @Unique private boolean companyTabSelected = false;
    @Unique private int selectedCompanyIndex = -1;

    @Unique private TextFieldWidget textFieldCompanyName;
    @Unique private TextFieldWidget textFieldAddCompany;
    @Unique private ButtonWidget buttonAddCompany;

    @Inject(method = "init", at = @At("TAIL"))
    public void companyTabInit(CallbackInfo ci) {
        if (buttonTabCompanies == null) {
            buttonTabCompanies = new ButtonWidget(144, 0, 36, 20, Text.literal("会社"), btn -> {
                CompanyTabHandler.onTabClick(this::init);
                buttonTabStations.active = true;
                buttonTabRoutes.active = true;
                buttonTabDepots.active = true;
                buttonTabCompanies.active = false;
                this.init();
            });
        }
        addDrawableChild(buttonTabCompanies);

        if (CompanyTabHandler.isSelected()) {
            // 既存タブのUIを先に初期化・描画
            this.clearChildren();
            addDrawableChild(buttonTabStations);
            addDrawableChild(buttonTabRoutes);
            addDrawableChild(buttonTabDepots);
            addDrawableChild(buttonTabCompanies);
            // 会社タブのUI群
            CompanyTabHandler.init((DashboardScreen) (Object) this);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void updateCompanyTab(CallbackInfo ci) {
        if (CompanyTabHandler.isSelected()) {
            if (!buttonTabStations.active || !buttonTabRoutes.active || !buttonTabDepots.active) {
                CompanyTabHandler.deactivate();
                if (buttonTabCompanies != null) buttonTabCompanies.active = true;
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void companyTabRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (CompanyTabHandler.isSelected()) {
            matrices.push();
            matrices.translate(0, 0, 500);
            fill(matrices, 0, 30, 180, height - 40, 0xAA222222); // 会社リスト左ペイン
            fill(matrices, 200, 30, width - 10, height - 40, 0x22222222); // 編集右ペイン
            textRenderer.draw(matrices, "会社管理タブ", 10, 24, 0xFFFFFF);
            matrices.pop();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void updateTabSelection(CallbackInfo ci) {
        if (companyTabSelected) {
            if (!buttonTabStations.active || !buttonTabRoutes.active || !buttonTabDepots.active) {
                companyTabSelected = false;
                if (buttonTabCompanies != null) buttonTabCompanies.active = true;
            }
        }
    }

}
