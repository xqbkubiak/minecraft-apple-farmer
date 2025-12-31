package com.bkubiak.applebot;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class XqbkScreen extends Screen {

    private final AppleBotClientBase bot;

    // Panel Colors
    private static final int COLOR_OVERLAY = 0xAA000000;
    private static final int COLOR_PANEL_TOP = 0xE6081810;
    private static final int COLOR_PANEL_BOTTOM = 0xE6040C08;
    private static final int COLOR_ACCENT = 0xFF00DD55;

    // Row Colors
    private static final int COLOR_YELLOW = 0xFFFFDD00;
    private static final int COLOR_MAGENTA = 0xFFFF55FF;
    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_GRAY = 0xFFAAAAAA;
    private static final int COLOR_GREEN = 0xFF55FF55;
    private static final int COLOR_RED = 0xFFFF5555;
    private static final int COLOR_CYAN = 0xFF55FFFF;

    // UI Elements
    private static final int COLOR_ROW_LINE = 0xFF1A2A1A;
    private static final int COLOR_BORDER = 0xFF00AA44;
    private static final int COLOR_HEADER_BG = 0x40005500;

    private int panelX, panelY, panelW, panelH;
    private boolean isCompact = false;
    // Layout variables calculated in init()
    private int headerH, tableHeadH, rowH, gap, inputHeight, socialH;

    private TextFieldWidget repairCommandField;

    public XqbkScreen(AppleBotClientBase bot) {
        super(Text.of("BK-Apple - Control Panel"));
        this.bot = bot;
    }

    protected void applyBlur() {
        // Disable MC 1.21 blur
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Don't draw default background
    }

    @Override
    protected void init() {
        // Panel width
        panelW = Math.min(380, this.width - 20);
        panelX = (this.width - panelW) / 2;

        int margin = 12;

        isCompact = this.height < 310;

        if (isCompact) {
            headerH = 32;
            tableHeadH = 12;
            rowH = 13;
            gap = 2;
            inputHeight = 16;
            socialH = 14;
        } else {
            headerH = 46;
            tableHeadH = 18;
            rowH = 18;
            gap = 8;
            inputHeight = 24;
            socialH = 18;
        }

        int layoutBtnHeight = isCompact ? 18 : 24;
        int contentHeight = headerH + tableHeadH + (rowH * 6) + gap + inputHeight + gap + inputHeight + gap
                + inputHeight + gap
                + layoutBtnHeight + gap + socialH;
        panelH = contentHeight;
        panelY = (this.height - panelH) / 2;

        if (panelY < 5)
            panelY = 5;
        if (panelY + panelH > this.height - 5) {
            panelH = this.height - panelY - 5;
        }

        int tableEndY = panelY + headerH + tableHeadH + (rowH * 6);
        int repairSectionY = tableEndY + gap;
        int storageSectionY = repairSectionY + inputHeight + gap;
        int restockSectionY = storageSectionY + inputHeight + gap;
        int btnY = restockSectionY + inputHeight + gap;
        int socialY = btnY + 24 + gap;

        int btnHeight = isCompact ? 18 : 24;
        int btnAreaWidth = panelW - (margin * 2);
        int numButtons = 4;
        int btnGap = 6;
        int btnWidth = (btnAreaWidth - (btnGap * (numButtons - 1))) / numButtons;
        int startX = panelX + margin;

        this.addDrawableChild(new GreenButton(
                startX, btnY, btnWidth, btnHeight,
                "> Start", button -> {
                    bot.setRunning(true);
                    this.client.setScreen(null);
                }, COLOR_GREEN));

        this.addDrawableChild(new GreenButton(
                startX + btnWidth + gap, btnY, btnWidth, btnHeight,
                "# Stop", button -> {
                    bot.setRunning(false);
                }, COLOR_RED));

        this.addDrawableChild(new GreenButton(
                startX + (btnWidth + gap) * 2, btnY, btnWidth, btnHeight,
                "x Clear", button -> {
                    bot.setRunning(false);
                    bot.setRepairMode(0);
                    bot.setAutoEat(false);
                }, COLOR_RED));

        this.addDrawableChild(new GreenButton(
                startX + (btnWidth + gap) * 3, btnY, btnWidth, btnHeight,
                "Close", button -> {
                    this.client.setScreen(null);
                }, COLOR_GRAY));

        int modeBtnWidth = 90;
        this.addDrawableChild(new GreenButton(
                panelX + panelW - modeBtnWidth - margin, repairSectionY, modeBtnWidth, 16,
                "Mode: " + bot.getRepairModeName(), button -> {
                    bot.cycleRepairMode();
                    button.setMessage(Text.of("Mode: " + bot.getRepairModeName()));
                }, bot.getRepairMode() == 0 ? COLOR_RED : COLOR_GREEN));

        int eatBtnWidth = 70;
        this.addDrawableChild(new GreenButton(
                panelX + panelW - modeBtnWidth - eatBtnWidth - margin - 5, repairSectionY, eatBtnWidth, 16,
                "Eat: " + (bot.isAutoEat() ? "ON" : "OFF"), button -> {
                    bot.setAutoEat(!bot.isAutoEat());
                    button.setMessage(Text.of("Eat: " + (bot.isAutoEat() ? "ON" : "OFF")));
                }, bot.isAutoEat() ? COLOR_GREEN : COLOR_RED));

        int fieldWidth = 90;
        repairCommandField = new TextFieldWidget(
                this.textRenderer,
                panelX + margin + 70,
                repairSectionY,
                fieldWidth,
                16,
                Text.of(""));
        repairCommandField.setText(bot.getRepairCommand());
        repairCommandField.setMaxLength(20);
        repairCommandField.setChangedListener(text -> {
            bot.setRepairCommand(text);
        });
        this.addDrawableChild(repairCommandField);

        int storageBtnWidth = (panelW - (margin * 2) - 10) / 2;

        this.addDrawableChild(new GreenButton(
                panelX + margin, storageSectionY, storageBtnWidth, 16,
                "Storage: " + (bot.isStorageMode() ? "ON" : "OFF"), button -> {
                    bot.setStorageMode(!bot.isStorageMode());
                    button.setMessage(Text.of("Storage: " + (bot.isStorageMode() ? "ON" : "OFF")));
                }, bot.isStorageMode() ? COLOR_GREEN : COLOR_RED));

        this.addDrawableChild(new GreenButton(
                panelX + margin + storageBtnWidth + 10, storageSectionY, storageBtnWidth, 16,
                "Cycles: " + bot.getStorageCycles(), button -> {
                    bot.cycleStorageCycles();
                    button.setMessage(Text.of("Cycles: " + bot.getStorageCycles()));
                }, COLOR_GRAY));

        this.addDrawableChild(new GreenButton(
                panelX + margin, restockSectionY, panelW - (margin * 2), 16,
                bot.t("restock") + ": " + (bot.isRestockMode() ? "ON" : "OFF"), button -> {
                    bot.setRestockMode(!bot.isRestockMode());
                    button.setMessage(Text.of(bot.t("restock") + ": " + (bot.isRestockMode() ? "ON" : "OFF")));
                }, bot.isRestockMode() ? COLOR_GREEN : COLOR_RED));

        int socialBtnWidth = 80;
        int socialGap = 10;
        int socialTotalWidth = socialBtnWidth * 2 + socialGap;
        int socialStartX = panelX + (panelW - socialTotalWidth) / 2;

        this.addDrawableChild(new GreenButton(
                socialStartX, socialY, socialBtnWidth, 16,
                "\uD83D\uDCBB GitHub", button -> {
                    Util.getOperatingSystem().open("https://github.com/xqbkubiak");
                }, 0xFF333333));

        this.addDrawableChild(new GreenButton(
                socialStartX + socialBtnWidth + socialGap, socialY, socialBtnWidth, 16,
                "\uD83D\uDCAC Discord", button -> {
                    Util.getOperatingSystem().open("https://dc.bkubiak.dev");
                }, 0xFF5865F2));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, COLOR_OVERLAY);
        drawGradientPanel(context, panelX, panelY, panelW, panelH);
        drawBorder(context, panelX, panelY, panelW, panelH, COLOR_BORDER);

        int headerY = panelY + (isCompact ? 8 : 12);
        context.fill(panelX + 4, headerY - 4, panelX + panelW - 4, headerY + 12, COLOR_HEADER_BG);
        context.drawTextWithShadow(this.textRenderer, Text.of("§2BK-§aApple"), panelX + 15, headerY, COLOR_ACCENT);
        context.drawTextWithShadow(this.textRenderer, Text.of("- AFK Farmer"), panelX + 72, headerY, COLOR_WHITE);

        String statusText = bot.isRunning() ? bot.t("active") : bot.t("inactive");
        int statusColor = bot.isRunning() ? COLOR_GREEN : COLOR_RED;
        context.drawTextWithShadow(this.textRenderer, Text.of("\u2022 " + statusText), panelX + panelW - 95,
                headerY, statusColor);

        String playerName = client.player != null ? client.player.getName().getString() : "---";
        context.drawTextWithShadow(this.textRenderer, Text.of(bot.t("profile") + ": " + playerName), panelX + 15,
                headerY + (isCompact ? 14 : 18), COLOR_GRAY);

        int tableHeaderY = panelY + headerH;
        context.drawTextWithShadow(this.textRenderer, Text.of("#"), panelX + 15, tableHeaderY, 0xFF666666);
        context.drawTextWithShadow(this.textRenderer, Text.of(bot.t("function")), panelX + 35, tableHeaderY,
                0xFF666666);
        context.drawTextWithShadow(this.textRenderer, Text.of(bot.t("status")), panelX + panelW - 80, tableHeaderY,
                0xFF666666);
        context.fill(panelX + 8, tableHeaderY + 12, panelX + panelW - 8, tableHeaderY + 13, COLOR_ROW_LINE);

        int rowStartY = tableHeaderY + tableHeadH;
        drawTableRow(context, 1, bot.t("delay"), bot.getTickDelay() + " " + bot.t("ticks"),
                COLOR_MAGENTA, COLOR_CYAN, rowStartY);
        drawTableRow(context, 2, bot.t("repair_mode"), bot.getRepairModeName(),
                COLOR_WHITE, bot.getRepairMode() == 0 ? COLOR_RED : COLOR_GREEN, rowStartY + rowH);
        drawTableRow(context, 3, bot.t("auto_eat"), bot.isAutoEat() ? "ON" : "OFF",
                COLOR_YELLOW, bot.isAutoEat() ? COLOR_GREEN : COLOR_RED, rowStartY + rowH * 2);

        int slot = client.player != null ? bot.getSelectedSlot(client.player) + 1 : 0;
        drawTableRow(context, 4, bot.t("slot"), String.valueOf(slot),
                COLOR_GRAY, COLOR_WHITE, rowStartY + rowH * 3);
        drawTableRow(context, 5, bot.t("durability"), getDurabilityString(),
                COLOR_MAGENTA, COLOR_CYAN, rowStartY + rowH * 4);
        drawTableRow(context, 6, bot.t("cycles"), String.valueOf(bot.getTotalCycles()),
                0xFFFFAA00, COLOR_WHITE, rowStartY + rowH * 5);

        int tableEndY = panelY + headerH + tableHeadH + (rowH * 6);
        int repairSectionY = tableEndY + gap;
        int textY = repairSectionY + (isCompact ? 4 : 5);
        context.drawTextWithShadow(this.textRenderer, Text.of(bot.t("command") + ":"), panelX + 15, textY,
                COLOR_GRAY);

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawGradientPanel(DrawContext context, int x, int y, int w, int h) {
        for (int i = 0; i < h; i++) {
            float ratio = (float) i / h;
            int color = interpolateColor(COLOR_PANEL_TOP, COLOR_PANEL_BOTTOM, ratio);
            context.fill(x, y + i, x + w, y + i + 1, color);
        }
    }

    private int interpolateColor(int c1, int c2, float ratio) {
        int a1 = (c1 >> 24) & 0xFF, r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int a2 = (c2 >> 24) & 0xFF, r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;
        int a = (int) (a1 + (a2 - a1) * ratio);
        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y, x + 1, y + h, color);
        context.fill(x + w - 1, y, x + w, y + h, color);
    }

    private void drawTableRow(DrawContext context, int num, String label, String value, int labelColor, int valueColor,
            int y) {
        context.drawTextWithShadow(this.textRenderer, Text.of(String.valueOf(num)), panelX + 15, y, COLOR_GRAY);
        context.drawTextWithShadow(this.textRenderer, Text.of(label), panelX + 35, y, labelColor);
        context.drawTextWithShadow(this.textRenderer, Text.of(value), panelX + panelW - 80, y, valueColor);
    }

    private String getDurabilityString() {
        if (client.player == null)
            return "---";
        var stack = client.player.getMainHandStack();
        if (stack.isEmpty())
            return "---";
        int max = stack.getMaxDamage();
        if (max == 0)
            return "---";
        int current = max - stack.getDamage();
        return current + "/" + max;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static class GreenButton extends ButtonWidget {
        protected final int borderColor;

        public GreenButton(int x, int y, int width, int height, String text, PressAction onPress, int borderColor) {
            super(x, y, width, height, Text.of(text), onPress, DEFAULT_NARRATION_SUPPLIER);
            this.borderColor = borderColor;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int bgColor = this.isHovered() ? 0xFF1A3A1A : 0xFF0A1A0A;
            context.fill(getX(), getY(), getX() + width, getY() + height, bgColor);

            int colorToDraw = getBorderColor();
            context.fill(getX(), getY(), getX() + width, getY() + 1, colorToDraw);
            context.fill(getX(), getY() + height - 1, getX() + width, getY() + height, colorToDraw);
            context.fill(getX(), getY(), getX() + 1, getY() + height, colorToDraw);
            context.fill(getX() + width - 1, getY(), getX() + width, getY() + height, colorToDraw);

            var tr = net.minecraft.client.MinecraftClient.getInstance().textRenderer;
            Text msg = getMessage();
            int textWidth = tr.getWidth(msg);
            int textX = getX() + (width - textWidth) / 2;
            int textY = getY() + (height - 8) / 2;
            int textColor = this.isHovered() ? 0xFFFFFFFF : colorToDraw;
            context.drawTextWithShadow(tr, msg, textX, textY, textColor);
        }

        protected int getBorderColor() {
            return borderColor;
        }
    }

    private static class StatusButton extends GreenButton {
        private final java.util.function.Supplier<String> textSupplier;
        private final java.util.function.Supplier<Integer> colorSupplier;

        public StatusButton(int x, int y, int width, int height, java.util.function.Supplier<String> textSupplier,
                PressAction onPress, java.util.function.Supplier<Integer> colorSupplier) {
            super(x, y, width, height, "", onPress, 0);
            this.textSupplier = textSupplier;
            this.colorSupplier = colorSupplier;
        }

        @Override
        public Text getMessage() {
            return Text.of(textSupplier.get());
        }

        @Override
        protected int getBorderColor() {
            return colorSupplier.get();
        }
    }
}
