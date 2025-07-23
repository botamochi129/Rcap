package com.botamochi.rcap.mixin;

import com.botamochi.rcap.client.screen.CompanyDashboardScreen;
import mtr.screen.DashboardList;
import mtr.screen.DashboardScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DashboardScreen.class)
public abstract class DashboardScreenMixin extends Screen {

    @Shadow @Final private DashboardList dashboardList;

    protected DashboardScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void rcap$addCompanyTab(CallbackInfo ci) {
        // タブを追加する（他タブに続く位置に配置される）
        dashboardList.addButton(
                Text.translatable("rcap.dashboard.company"), // タブに使う文字列（翻訳対応）
                1001, // タブの内部ID（被らなければ何でもOK）
                () -> MinecraftClient.getInstance().setScreen(new CompanyDashboardScreen((DashboardScreen)(Object)this))
        );
    }
}
