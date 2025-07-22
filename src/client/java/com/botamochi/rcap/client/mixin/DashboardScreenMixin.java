package com.botamochi.rcap.client.mixin;

import com.botamochi.rcap.data.Company;
import com.botamochi.rcap.data.CompanyManager;
import com.botamochi.rcap.client.screen.CompanyEntry;
import mtr.client.IDrawing;
import mtr.screen.DashboardList;
import mtr.screen.DashboardScreen;
import mtr.screen.WidgetColorSelector;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.Collectors;

@Mixin(DashboardScreen.class)
public abstract class DashboardScreenMixin extends Screen {

    protected DashboardScreenMixin(Text title) {
        super(title);
    }

    // ==== 本家タブ ====
    private ButtonWidget buttonTabStations;
    private ButtonWidget buttonTabRoutes;
    private ButtonWidget buttonTabDepots;

    // ==== 会社タブ ====
    @Unique private ButtonWidget buttonTabCompanies;
    @Unique private boolean companyTabSelected = false;
    @Unique private int selectedCompanyIndex = -1;

    // ==== GUIパーツ ====
    @Unique private DashboardList companyDashboardList;
    @Unique private TextFieldWidget textFieldCompanyName;
    @Unique private WidgetColorSelector colorSelector;
    @Unique private ButtonWidget buttonAddCompany;
    @Unique private TextFieldWidget textFieldAddCompany;

    // ==== Mixin タブ設置 ====
    @Inject(method = "init", at = @At("TAIL"))
    private void addCompanyTab(CallbackInfo ci) {
        DashboardScreen self = (DashboardScreen)(Object)this;

        // === 他タブの位置参照 ===
        buttonTabStations = getButtonByText("gui.mtr.stations");
        buttonTabRoutes = getButtonByText("gui.mtr.routes");
        buttonTabDepots = getButtonByText("gui.mtr.depots");

        // === タブ追加 ===
        if (buttonTabCompanies == null) {
            buttonTabCompanies = new ButtonWidget(144, 0, 36, 20, Text.literal("会社"), btn -> {
                companyTabSelected = true;
                buttonTabStations.active = true;
                buttonTabRoutes.active = true;
                buttonTabDepots.active = true;
                buttonTabCompanies.active = false;
                selectedCompanyIndex = -1;
                this.init();
            });
        }
        addDrawableChild(buttonTabCompanies);

        // === 他タブが選択されていれば描画しない ===
        if (!companyTabSelected) return;

        // 描画内容クリア
        this.clearChildren();

        // 再描画：全タブ
        addDrawableChild(buttonTabStations);
        addDrawableChild(buttonTabRoutes);
        addDrawableChild(buttonTabDepots);
        addDrawableChild(buttonTabCompanies);

        // === リスト装備 ===
        companyDashboardList = new DashboardList(
                (data, index) -> {}, // onFind
                (data, index) -> { selectedCompanyIndex = index; this.init(); }, // onDraw
                (data, index) -> { selectedCompanyIndex = index; this.init(); }, // onEdit
                null, null,
                (data, index) -> {
                    CompanyManager.COMPANY_LIST.remove(index);
                    selectedCompanyIndex = -1;
                    this.init();
                },
                () -> CompanyManager.COMPANY_LIST.stream().map(CompanyEntry::new).collect(Collectors.toList()),
                () -> "",
                s -> {}
        );
        companyDashboardList.y = 30;
        companyDashboardList.width = 180;
        companyDashboardList.height = height - 80;
        companyDashboardList.init(this::addDrawableChild);

        // === 会社追加エリア ===
        textFieldAddCompany = new TextFieldWidget(textRenderer, 10, height - 40, 100, 20, Text.literal("会社名"));
        addDrawableChild(textFieldAddCompany);
        buttonAddCompany = new ButtonWidget(115, height - 40, 50, 20, Text.literal("追加"), btn -> {
            String name = textFieldAddCompany.getText().trim();
            if (!name.isEmpty()) {
                CompanyManager.COMPANY_LIST.add(new Company(CompanyManager.getNextId(), name));
                textFieldAddCompany.setText("");
                selectedCompanyIndex = CompanyManager.COMPANY_LIST.size() - 1;
                this.init(); // 再描画
            }
        });
        addDrawableChild(buttonAddCompany);

        // === 編集（右ペイン） ===
        if (selectedCompanyIndex >= 0 && selectedCompanyIndex < CompanyManager.COMPANY_LIST.size()) {
            final Company selected = CompanyManager.COMPANY_LIST.get(selectedCompanyIndex);

            textFieldCompanyName = new TextFieldWidget(textRenderer, 200, 40, 140, 20, Text.literal("会社名"));
            textFieldCompanyName.setText(selected.name);
            addDrawableChild(textFieldCompanyName);

            DashboardScreen dashboardScreen = (DashboardScreen)(Object)this;
            colorSelector = new WidgetColorSelector(dashboardScreen, true, () -> {});
            colorSelector.setColor(selected.color);
            IDrawing.setPositionAndWidth(colorSelector, 200, 70, 140);
            addDrawableChild(colorSelector);

            ButtonWidget buttonSave = new ButtonWidget(200, 100, 80, 20, Text.literal("保存"), saveBtn -> {
                selected.name = textFieldCompanyName.getText();
                selected.color = colorSelector.getColor();
                this.init();
            });
            addDrawableChild(buttonSave);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderCompanyTab(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!companyTabSelected) return;

        matrices.push();
        matrices.translate(0, 0, 500);
        fill(matrices, 0, 30, 180, height - 40, 0xAA222222);
        fill(matrices, 200, 30, width - 10, height - 40, 0x22000000);
        textRenderer.draw(matrices, "会社タブ", 10, 24, 0xFFFFFF);
        matrices.pop();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void checkTab(CallbackInfo ci) {
        if (companyTabSelected) {
            if (buttonTabStations != null && !buttonTabStations.active) {
                companyTabSelected = false;
                if (buttonTabCompanies != null) buttonTabCompanies.active = true;
            }
        }
    }

    // === タブ取得（本家 GUI 固定） ===
    @Unique
    private ButtonWidget getButtonByText(String translationKey) {
        return this.children().stream()
                .filter(e -> e instanceof ButtonWidget)
                .map(e -> (ButtonWidget) e)
                .filter(btn -> btn.getMessage().getString().equals(Text.translatable(translationKey).getString()))
                .findFirst().orElse(null);
    }
}
