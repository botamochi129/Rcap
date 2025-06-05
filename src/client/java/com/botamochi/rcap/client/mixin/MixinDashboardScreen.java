package com.botamochi.rcap.client.mixin;

import com.botamochi.rcap.client.screen.CompanyTab;
import org.mtr.mod.screen.DashboardListItem;
import org.mtr.mod.screen.DashboardScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.List;

@Mixin(DashboardScreen.class)
public abstract class MixinDashboardScreen {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectCompanyTab(CallbackInfo ci) {
        DashboardScreen screen = (DashboardScreen) (Object) this;
        try {
            Field tabsField = DashboardScreen.class.getDeclaredField("tabs");
            tabsField.setAccessible(true);
            List<DashboardListItem> tabs = (List<DashboardListItem>) tabsField.get(screen);
            tabs.add(new CompanyTab());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
