package com.nickname.plugin.hooks;

import com.hypixel.hytale.server.core.Message;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

public class TinyMessageHook {

    private static boolean available = false;
    private static Method parseMethod = null;

    public static void init() {
        try {
            Class<?> tinyMsgClass = Class.forName("fi.sulku.hytale.TinyMsg");
            parseMethod = tinyMsgClass.getMethod("parse", String.class);
            available = true;
            System.out.println("[NicknameChanger] TinyMessage integration enabled!");
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            available = false;
            System.out.println("[NicknameChanger] TinyMessage not found, integration disabled.");
        }
    }

    public static boolean isAvailable() {
        return available;
    }

    @Nonnull
    public static Message parse(@Nonnull String input) {
        if (!available || parseMethod == null) {
            return Message.raw(input);
        }
        try {
            Object result = parseMethod.invoke(null, input);
            if (result instanceof Message) {
                return (Message) result;
            }
            return Message.raw(input);
        } catch (Exception e) {
            return Message.raw(input);
        }
    }

    public static boolean hasColorTags(@Nonnull String input) {
        return input.contains("<") && input.contains(">");
    }
}
