package larrytllama.pvcmappermod.utils;

import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
//? if <26.1 {
import net.minecraft.client.gui.GuiGraphics;
//?} else {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;*///?}

public class GraphicsHelper {

    public static void drawString(/*? if <26.1 {*/GuiGraphics/*?} else {*//*GuiGraphicsExtractor*//*?}*/ graphics, Font font, String text, int x, int y, int color) {
        //? if <26.1 {
        graphics.drawString(font, text, x, y, color, false);
        //?} else {
        /*graphics.text(font, text, x, y, color, false);*///?}
    }

    public static void drawString(/*? if <26.1 {*/GuiGraphics/*?} else {*//*GuiGraphicsExtractor*//*?}*/ graphics, Font font, Component text, int x, int y, int color) {
        //? if <26.1 {
        graphics.drawString(font, text, x, y, color, false);
        //?} else {
        /*graphics.text(font, text, x, y, color, false);*///?}
    }

    public static void drawString(/*? if <26.1 {*/GuiGraphics/*?} else {*//*GuiGraphicsExtractor*//*?}*/ graphics, Font font, FormattedCharSequence text, int x, int y, int color) {
        //? if <26.1 {
        graphics.drawString(font, text, x, y, color, false);
        //?} else {
        /*graphics.text(font, text, x, y, color, false);*///?}
    }

    public static void drawStringWithShadow(/*? if <26.1 {*/GuiGraphics/*?} else {*//*GuiGraphicsExtractor*//*?}*/ graphics, Font font, String text, int x, int y, int color) {
        //? if <26.1 {
        graphics.drawString(font, text, x, y, color, true);
        //?} else {
        /*graphics.text(font, text, x, y, color, true);*///?}
    }

    public static void drawStringWithShadow(/*? if <26.1 {*/GuiGraphics/*?} else {*//*GuiGraphicsExtractor*//*?}*/ graphics, Font font, Component text, int x, int y, int color) {
        //? if <26.1 {
        graphics.drawString(font, text, x, y, color, true);
        //?} else {
        /*graphics.text(font, text, x, y, color, true);*///?}
    }

    public static void drawCenteredString(/*? if <26.1 {*/GuiGraphics/*?} else {*//*GuiGraphicsExtractor*//*?}*/ graphics, Font font, Component text, int x, int y, int color) {
        //? if <26.1 {
        graphics.drawCenteredString(font, text, x, y, color);
        //?} else {
        /*int width = font.width(text);
        graphics.text(font, text, x - (width / 2), y, color, true);*///?}
    }

    public static void drawCenteredString(/*? if <26.1 {*/GuiGraphics/*?} else {*//*GuiGraphicsExtractor*//*?}*/ graphics, Font font, String text, int x, int y, int color) {
        //? if <26.1 {
        graphics.drawCenteredString(font, text, x, y, color);
        //?} else {
        /*int width = font.width(text);
        graphics.text(font, text, x - (width / 2), y, color, true);*///?}
    }


    public static void renderItem(/*? if <26.1 {*/GuiGraphics/*?} else {*//*GuiGraphicsExtractor*//*?}*/ graphics, ItemStack stack, int x, int y) {
        //? if <26.1 {
        graphics.renderItem(stack, x, y);
        //?} else {
        /*graphics.fakeItem(stack, x, y);*///?}
    }

    public static void drawTooltipBackground(/*? if <26.1 {*/GuiGraphics/*?} else {*//*GuiGraphicsExtractor*//*?}*/ graphics, int x, int y, int width, int height) {
        //? if <26.1 {
        TooltipRenderUtil.renderTooltipBackground(graphics, x, y, width, height, null);
        //?} else {
        /*TooltipRenderUtil.extractTooltipBackground(graphics, x, y, width, height, null);*///?}
    }

    public static void vLine(/*? if <26.1 {*/GuiGraphics/*?} else {*//*GuiGraphicsExtractor*//*?}*/ graphics, int x, int y1, int y2, int color) {
        //? if <26.1 {
        graphics.vLine(x, y1, y2, color);
        //?} else {
        /*graphics.fill(x, y1, x + 1, y2, color);*///?}
    }

    public static void hLine(/*? if <26.1 {*/GuiGraphics/*?} else {*//*GuiGraphicsExtractor*//*?}*/ graphics, int x1, int x2, int y, int color) {
        //? if <26.1 {
        graphics.hLine(x1, x2, y, color);
        //?} else {
        /*graphics.fill(x1, y, x2, y + 1, color);*///?}
    }
}
