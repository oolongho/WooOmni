package com.oolonghoo.woomni.util;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.config.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

public final class GUILocale {
    
    private static WooOmni plugin;
    private static MessageManager msg;
    
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacySection();
    
    private GUILocale() {}
    
    public static void init(WooOmni pluginInstance) {
        plugin = pluginInstance;
        msg = plugin.getMessageManager();
    }
    
    public static String get(String key) {
        return msg.getRaw(key);
    }
    
    public static String get(String key, String... replacements) {
        String text = msg.getRaw(key);
        if (text == null) return key;
        
        for (int i = 0; i < replacements.length - 1; i += 2) {
            text = text.replace("%" + replacements[i] + "%", replacements[i + 1]);
        }
        return text;
    }
    
    public static Component getComponent(String key) {
        String text = get(key);
        if (text == null) return Component.text(key);
        return SERIALIZER.deserialize(text);
    }
    
    public static Component getComponent(String key, NamedTextColor color) {
        String text = get(key);
        if (text == null) return Component.text(key, color);
        return SERIALIZER.deserialize(text).color(color);
    }
    
    public static Component getComponent(String key, String... replacements) {
        String text = get(key, replacements);
        if (text == null) return Component.text(key);
        return SERIALIZER.deserialize(text);
    }
    
    public static Component getComponent(String key, NamedTextColor color, String... replacements) {
        String text = get(key, replacements);
        if (text == null) return Component.text(key, color);
        return SERIALIZER.deserialize(text).color(color);
    }
    
    public static String color(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
