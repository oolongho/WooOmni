package com.oolonghoo.woomni.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ConfigLoader {
    
    protected final JavaPlugin plugin;
    protected final String fileName;
    protected final File configFile;
    protected FileConfiguration config;
    protected final Map<String, Object> cache = new HashMap<>();
    
    public ConfigLoader(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        this.configFile = new File(plugin.getDataFolder(), fileName);
    }
    
    public void initialize() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        boolean isCustomFile = !isBundledResource(fileName);
        
        if (!configFile.exists()) {
            if (isCustomFile) {
                plugin.getLogger().warning("Config file not found: " + fileName);
                try {
                    configFile.getParentFile().mkdirs();
                    configFile.createNewFile();
                } catch (IOException e) {
                    plugin.getLogger().severe("Failed to create config file: " + fileName);
                }
            } else {
                plugin.saveResource(fileName, false);
            }
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        
        if (!isCustomFile) {
            InputStream defaultStream = plugin.getResource(fileName);
            if (defaultStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8)
                );
                config.setDefaults(defaultConfig);
                config.options().copyDefaults(true);
                save();
            }
        }
        
        loadValues();
    }
    
    protected boolean isBundledResource(String name) {
        return plugin.getResource(name) != null;
    }
    
    protected void loadValues() {
        cache.clear();
    }
    
    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save config file: " + fileName);
            e.printStackTrace();
        }
    }
    
    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
        loadValues();
    }
    
    @SuppressWarnings("unchecked")
    protected <T> T getCached(String path, T defaultValue, Class<T> type) {
        if (cache.containsKey(path)) {
            return (T) cache.get(path);
        }
        T value = config.getObject(path, type, defaultValue);
        cache.put(path, value);
        return value;
    }
    
    protected String getCachedString(String path, String defaultValue) {
        if (cache.containsKey(path)) {
            return (String) cache.get(path);
        }
        String value = config.getString(path, defaultValue);
        cache.put(path, value);
        return value;
    }
    
    protected int getCachedInt(String path, int defaultValue) {
        if (cache.containsKey(path)) {
            return (Integer) cache.get(path);
        }
        int value = config.getInt(path, defaultValue);
        cache.put(path, value);
        return value;
    }
    
    protected boolean getCachedBoolean(String path, boolean defaultValue) {
        if (cache.containsKey(path)) {
            return (Boolean) cache.get(path);
        }
        boolean value = config.getBoolean(path, defaultValue);
        cache.put(path, value);
        return value;
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
    
    public String getFileName() {
        return fileName;
    }
}
