package com.oolonghoo.woomni.config;

import com.oolonghoo.woomni.WooOmni;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageManager {
    
    private final WooOmni plugin;
    private File langFile;
    private FileConfiguration langConfig;
    private Component prefixComponent;
    private String prefixString;
    
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    
    public MessageManager(WooOmni plugin) {
        this.plugin = plugin;
    }
    
    public void initialize() {
        String language = plugin.getConfig().getString("settings.language", "zh-CN");
        loadLanguageFile(language);
        updatePrefix();
    }
    
    private void updatePrefix() {
        String prefixText = langConfig.getString("prefix", "&8[&6WooOmni&8]&r ");
        this.prefixComponent = SERIALIZER.deserialize(prefixText);
        this.prefixString = SERIALIZER.serialize(prefixComponent);
    }
    
    private void loadLanguageFile(String language) {
        langFile = new File(plugin.getDataFolder(), "lang/" + language + ".yml");
        
        if (!langFile.exists()) {
            langFile.getParentFile().mkdirs();
            InputStream defaultStream = plugin.getResource("lang/" + language + ".yml");
            if (defaultStream != null) {
                plugin.saveResource("lang/" + language + ".yml", false);
            } else {
                try {
                    langFile.createNewFile();
                } catch (IOException e) {
                    plugin.getLogger().severe("创建语言文件失败: " + language);
                }
            }
        }
        
        langConfig = YamlConfiguration.loadConfiguration(langFile);
        
        InputStream defaultStream = plugin.getResource("lang/zh-CN.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defaultStream, StandardCharsets.UTF_8)
            );
            langConfig.setDefaults(defaultConfig);
        }
    }
    
    public void reload() {
        String language = plugin.getConfig().getString("settings.language", "zh-CN");
        loadLanguageFile(language);
        updatePrefix();
    }
    
    /**
     * 获取消息字符串（已转换颜色代码）
     */
    public String get(String key, Object... args) {
        String message = langConfig.getString(key, key);
        message = formatPlaceholders(message, args);
        return SERIALIZER.serialize(SERIALIZER.deserialize(message));
    }
    
    /**
     * 获取消息组件（用于 Adventure 发送）
     */
    public Component getComponent(String key, Object... args) {
        String message = langConfig.getString(key, key);
        message = formatPlaceholders(message, args);
        return SERIALIZER.deserialize(message);
    }
    
    /**
     * 获取带前缀的消息字符串
     */
    public String getWithPrefix(String key, Object... args) {
        return prefixString + get(key, args);
    }
    
    /**
     * 获取带前缀的消息组件
     */
    public Component getWithPrefixComponent(String key, Object... args) {
        return prefixComponent.append(getComponent(key, args));
    }
    
    /**
     * 发送消息给命令发送者（支持控制台和玩家）
     */
    public void send(CommandSender sender, String key, Object... args) {
        sender.sendMessage(getWithPrefixComponent(key, args));
    }
    
    /**
     * 发送消息给玩家（推荐使用此方法）
     */
    public void send(Player player, String key, Object... args) {
        player.sendMessage(getWithPrefixComponent(key, args));
    }
    
    /**
     * 发送无前缀消息给玩家
     */
    public void sendNoPrefix(Player player, String key, Object... args) {
        player.sendMessage(getComponent(key, args));
    }
    
    public String getPrefix() {
        return prefixString;
    }
    
    public Component getPrefixComponent() {
        return prefixComponent;
    }
    
    private String formatPlaceholders(String message, Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }
        
        Map<String, String> placeholders = new HashMap<>();
        for (int i = 0; i < args.length - 1; i += 2) {
            if (args[i] != null && args[i + 1] != null) {
                placeholders.put(String.valueOf(args[i]), String.valueOf(args[i + 1]));
            }
        }
        
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        
        return message;
    }
    
    public FileConfiguration getLangConfig() {
        return langConfig;
    }
    
    /**
     * 获取字符串列表（已转换颜色代码）
     */
    public List<String> getList(String key) {
        List<String> list = langConfig.getStringList(key);
        if (list.isEmpty()) {
            list = java.util.Collections.singletonList("&cMissing: " + key);
        }
        for (int i = 0; i < list.size(); i++) {
            list.set(i, SERIALIZER.serialize(SERIALIZER.deserialize(list.get(i))));
        }
        return list;
    }
    
    /**
     * 获取组件列表
     */
    public List<Component> getComponentList(String key) {
        List<String> strings = langConfig.getStringList(key);
        if (strings.isEmpty()) {
            strings = java.util.Collections.singletonList("&cMissing: " + key);
        }
        List<Component> components = new java.util.ArrayList<>();
        for (String str : strings) {
            components.add(SERIALIZER.deserialize(str));
        }
        return components;
    }
}
