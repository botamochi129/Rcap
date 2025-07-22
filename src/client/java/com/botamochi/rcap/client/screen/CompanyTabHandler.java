package com.botamochi.rcap.client.screen;

import mtr.client.IDrawing;
import mtr.screen.DashboardList;
import mtr.screen.DashboardScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.stream.Collectors;

import com.botamochi.rcap.data.Company;
import com.botamochi.rcap.data.CompanyManager;

public class CompanyTabHandler {

    private static boolean selected = false;
    private static int selectedIndex = -1;

    // GUI部品
    private static DashboardList dashboardList;
    private static TextFieldWidget textFieldName;
    private static mtr.screen.WidgetColorSelector colorSelector;
    private static ButtonWidget buttonAdd;
    private static TextFieldWidget textFieldAdd;

    public static boolean isSelected() {
        return selected;
    }

    public static void onTabClick(Runnable reInit) {
        selected = true;
        selectedIndex = -1;
        if (reInit != null) reInit.run();
    }

    public static void deactivate() {
        selected = false;
        selectedIndex = -1;
    }

    public static void init(DashboardScreen screen) {
        // == 会社タブの描画・ウィジェット登録 ==
        final int panelWidth = 180;
        final int yBase = 30;
        final int entryHeight = 22;
        final int maxListY = screen.height - 80;

        // 会社リスト
        ArrayList<CompanyEntry> list = CompanyManager.COMPANY_LIST.stream().map(CompanyEntry::new)
                .collect(Collectors.toCollection(ArrayList::new));
        if (dashboardList == null) {
            dashboardList = new DashboardList(
                    (data, index) -> {
                        // onFind: unused
                    },
                    (data, index) -> {
                        // onDraw: select
                        selectedIndex = index;
                        screen.init();
                    },
                    (data, index) -> {
                        // onEdit: same as select
                        selectedIndex = index;
                        screen.init();
                    },
                    null, // onSort
                    null, // onAdd
                    (data, index) -> {
                        // onDelete
                        if (index >= 0 && index < CompanyManager.COMPANY_LIST.size()) {
                            CompanyManager.COMPANY_LIST.remove(index);
                            if (selectedIndex == index) selectedIndex = -1;
                            screen.init();
                        }
                    },
                    () -> list,
                    () -> "", // search getter
                    s -> {}   // search setter
            );
        }
        dashboardList.y = yBase;
        dashboardList.width = panelWidth;
        dashboardList.height = maxListY - yBase;

        screen.addSelectableChild(dashboardList);

        // == 追加フォーム ==
        textFieldAdd = new TextFieldWidget(screen.textRenderer, 10, maxListY + 6, 100, 18, Text.literal("会社名"));
        screen.addDrawableChild(textFieldAdd);
        buttonAdd = new ButtonWidget(115, maxListY + 6, 50, 18, Text.literal("追加"), btn -> {
            String name = textFieldAdd.getText().trim();
            if (!name.isEmpty()) {
                CompanyManager.COMPANY_LIST.add(new Company(CompanyManager.getNextId(), name, WidgetColorSelector.getRandomColor()));
                textFieldAdd.setText("");
                selectedIndex = CompanyManager.COMPANY_LIST.size() - 1;
                // サーバーパケット送信：PacketGuiServer.sendCompanyAdd(...)
                screen.init();
            }
        });
        screen.addDrawableChild(buttonAdd);

        // == 編集欄（右ペイン）==
        if (selectedIndex >= 0 && selectedIndex < CompanyManager.COMPANY_LIST.size()) {
            Company sel = CompanyManager.COMPANY_LIST.get(selectedIndex);

            textFieldName = new TextFieldWidget(screen.textRenderer, panelWidth + 20, yBase + 6, 140, 20, Text.literal("会社名"));
            textFieldName.setText(sel.name);
            screen.addDrawableChild(textFieldName);

            colorSelector = new WidgetColorSelector(screen, true, () -> {});
            colorSelector.setColor(sel.color);
            IDrawing.setPositionAndWidth(colorSelector, panelWidth + 20, yBase + 32, 140);

            screen.addDrawableChild(colorSelector);

            ButtonWidget buttonSave = new ButtonWidget(panelWidth + 20, yBase + 60, 80, 18, Text.literal("保存"), btn -> {
                String newName = textFieldName.getText().trim();
                int newColor = colorSelector.getColor();
                if (!newName.isEmpty()) {
                    sel.name = newName;
                    sel.color = newColor;
                    // サーバーパケット送信：PacketGuiServer.sendCompanyEdit(...)
                    screen.init();
                }
            });
            screen.addDrawableChild(buttonSave);
        }
    }
}
