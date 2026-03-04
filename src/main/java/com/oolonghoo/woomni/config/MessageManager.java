package com.oolonghoo.woomni.config;

import com.oolonghoo.woomni.WooOmni;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {
    
    private final WooOmni plugin;
    private File langFile;
    private FileConfiguration langConfig;
    private String prefix;
    
    public MessageManager(WooOmni plugin) {
        this.plugin = plugin;
    }
    
    public void initialize() {
        String language = plugin.getConfig().getString("settings.language", "zh-CN");
        loadLanguageFile(language);
        prefix = LegacyComponentSerializer.legacyAmpersand().serialize(
            LegacyComponentSerializer.legacyAmpersand().deserialize(
                langConfig.getString("prefix", "&8[&6WooOmni&8]&r ")));
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
        prefix = LegacyComponentSerializer.legacyAmpersand().serialize(
            LegacyComponentSerializer.legacyAmpersand().deserialize(
                langConfig.getString("prefix", "&8[&6WooOmni&8]&r ")));
    }
    
    public String get(String key, Object... args) {
        String message = langConfig.getString(key, key);
        message = LegacyComponentSerializer.legacyAmpersand().serialize(
            LegacyComponentSerializer.legacyAmpersand().deserialize(message));
        return formatPlaceholders(message, args);
    }
    
    public String getWithPrefix(String key, Object... args) {
        return prefix + get(key, args);
    }
    
    public String getPrefix() {
        return prefix;
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
}
