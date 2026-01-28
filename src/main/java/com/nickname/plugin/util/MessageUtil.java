package com.nickname.plugin.util;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.protocol.MaybeBool;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Markup parser for styled messages.
 * Tags: <b>, <i>, <u>, <color:#HEX>, <gradient:#HEX:#HEX>
 */
public final class MessageUtil {

    private static final Pattern COLOR_PATTERN = Pattern.compile("<color:(#[0-9A-Fa-f]{6})>");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient:(#[0-9A-Fa-f]{6}):(#[0-9A-Fa-f]{6})>");

    private MessageUtil() {}

    @Nonnull
    public static Message parse(@Nonnull String input) {
        if (!input.contains("<")) return Message.raw(input);

        boolean bold = input.contains("<b>") || input.contains("<bold>");
        boolean italic = input.contains("<i>") || input.contains("<italic>");
        boolean underline = input.contains("<u>") || input.contains("<underline>");

        Matcher colorMatcher = COLOR_PATTERN.matcher(input);
        String color = colorMatcher.find() ? colorMatcher.group(1) : null;

        Matcher gradMatcher = GRADIENT_PATTERN.matcher(input);
        String grad1 = null, grad2 = null;
        if (gradMatcher.find()) {
            grad1 = gradMatcher.group(1);
            grad2 = gradMatcher.group(2);
        }

        String text = stripTags(input);
        if (text.isEmpty()) return Message.empty();

        if (grad1 != null && grad2 != null) {
            return buildGradient(text, grad1, grad2, bold, italic, underline);
        }
        return buildStyled(text, color, bold, italic, underline);
    }

    @Nonnull
    public static Message parseForUI(@Nonnull String input) {
        // Without color/gradient - use markupEnabled for native tag parsing
        if (!input.contains("<color:") && !input.contains("<gradient:")) {
            Message msg = Message.raw(input);
            msg.getFormattedMessage().markupEnabled = true;
            return msg;
        }

        // With gradient - use special UI gradient builder
        if (input.contains("<gradient:")) {
            return parseGradientForUI(input);
        }

        // With color only - try markupEnabled + color on same message
        Matcher colorMatcher = COLOR_PATTERN.matcher(input);
        if (colorMatcher.find()) {
            String color = colorMatcher.group(1);
            // Remove only color tags, keep style tags for markupEnabled
            String withStyleTags = input.replaceAll("<color:#[0-9A-Fa-f]{6}>", "")
                                        .replaceAll("</color>", "");

            Message msg = Message.raw(withStyleTags);
            msg.getFormattedMessage().markupEnabled = true;
            msg.color(color);
            return msg;
        }

        return parse(input);
    }

    @Nonnull
    public static Message parseForChat(@Nonnull String input) {
        return parse(input);
    }

    public static boolean hasMarkup(@Nonnull String input) {
        return input.contains("<") && input.contains(">");
    }

    @Nonnull
    public static String stripTags(@Nonnull String input) {
        return input.replaceAll("<[^>]+>", "").replaceAll("</[^>]+>", "");
    }

    @Nonnull
    private static Message buildStyled(String text, String color, boolean bold, boolean italic, boolean underline) {
        Message msg = Message.raw(text);
        if (bold) msg.bold(true);
        if (italic) msg.italic(true);
        if (color != null) msg.color(color);
        if (underline) msg.getFormattedMessage().underlined = MaybeBool.True;
        return msg;
    }

    @Nonnull
    private static Message buildGradient(String text, String c1, String c2, boolean bold, boolean italic, boolean underline) {
        int[] rgb1 = hexToRgb(c1), rgb2 = hexToRgb(c2);
        Message result = Message.empty();
        int len = text.length();

        for (int i = 0; i < len; i++) {
            float r = len > 1 ? (float) i / (len - 1) : 0;
            String hex = String.format("#%02X%02X%02X",
                (int)(rgb1[0] + r * (rgb2[0] - rgb1[0])),
                (int)(rgb1[1] + r * (rgb2[1] - rgb1[1])),
                (int)(rgb1[2] + r * (rgb2[2] - rgb1[2])));

            Message ch = Message.raw(String.valueOf(text.charAt(i)));
            if (bold) ch.bold(true);
            if (italic) ch.italic(true);
            ch.color(hex);
            if (underline) ch.getFormattedMessage().underlined = MaybeBool.True;
            result.insert(ch);
        }
        return result;
    }

    @Nonnull
    private static Message parseGradientForUI(@Nonnull String input) {
        // Gradient in UI: underline does NOT work with per-char coloring (engine limitation)
        // The UI draws a manual underline line instead - see NicknameEditorPage.updatePreview()
        return buildGradient(
            stripTags(input),
            extractGradientColor1(input),
            extractGradientColor2(input),
            input.contains("<b>") || input.contains("<bold>"),
            input.contains("<i>") || input.contains("<italic>"),
            false  // underline handled visually in UI, not through Message API
        );
    }

    private static String extractGradientColor1(String input) {
        Matcher m = GRADIENT_PATTERN.matcher(input);
        return m.find() ? m.group(1) : "#FFFFFF";
    }

    private static String extractGradientColor2(String input) {
        Matcher m = GRADIENT_PATTERN.matcher(input);
        return m.find() ? m.group(2) : "#FFFFFF";
    }

    private static int[] hexToRgb(String hex) {
        hex = hex.replace("#", "");
        return new int[] {
            Integer.parseInt(hex.substring(0, 2), 16),
            Integer.parseInt(hex.substring(2, 4), 16),
            Integer.parseInt(hex.substring(4, 6), 16)
        };
    }
}
