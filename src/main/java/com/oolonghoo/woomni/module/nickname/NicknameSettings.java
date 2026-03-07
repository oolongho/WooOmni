package com.oolonghoo.woomni.module.nickname;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.config.ConfigLoader;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class NicknameSettings {
    
    private final WooOmni plugin;
    private final ConfigLoader configLoader;
    
    private int maxLength;
    private int minLength;
    private String prefix;
    private boolean allowColors;
    private boolean colorPermission;
    private Pattern nickRegex;
    private List<String> blacklist;
    
    private boolean changeDisplayName;
    private boolean changePlayerListName;
    
    private boolean economyEnabled;
    private double firstSetCost;
    private double changeCost;
    
    public NicknameSettings(WooOmni plugin) {
        this.plugin = plugin;
        this.configLoader = new ConfigLoader(plugin, "settings/nickname.yml");
    }
    
    public void initialize() {
        configLoader.initialize();
        loadSettings();
    }
    
    public void reload() {
        configLoader.reload();
        loadSettings();
    }
    
    private void loadSettings() {
        FileConfiguration config = configLoader.getConfig();
        
        maxLength = config.getInt("max-length", 16);
        minLength = config.getInt("min-length", 1);
        prefix = config.getString("prefix", "~");
        allowColors = config.getBoolean("allow-colors", true);
        colorPermission = config.getBoolean("color-permission", true);
        
        String regexStr = config.getString("regex", "[a-zA-Z0-9_\\u4e00-\\u9fa5]+");
        try {
            nickRegex = Pattern.compile(regexStr);
        } catch (Exception e) {
            nickRegex = Pattern.compile("[a-zA-Z0-9_\\u4e00-\\u9fa5]+");
        }
        
        blacklist = new ArrayList<>();
        List<String> list = config.getStringList("blacklist");
        if (list != null) {
            blacklist.addAll(list);
        }
        if (!blacklist.contains("admin")) {
            blacklist.add("admin");
        }
        if (!blacklist.contains("moderator")) {
            blacklist.add("moderator");
        }
        
        changeDisplayName = config.getBoolean("display.change-display-name", true);
        changePlayerListName = config.getBoolean("display.change-player-list-name", true);
        
        economyEnabled = config.getBoolean("economy.enabled", false);
        firstSetCost = config.getDouble("economy.first-set-cost", 100.0);
        changeCost = config.getDouble("economy.change-cost", 50.0);
    }
    
    public int getMaxLength() {
        return maxLength;
    }
    
    public int getMinLength() {
        return minLength;
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public boolean isAllowColors() {
        return allowColors;
    }
    
    public boolean isColorPermission() {
        return colorPermission;
    }
    
    public Pattern getNickRegex() {
        return nickRegex;
    }
    
    public List<String> getBlacklist() {
        return blacklist;
    }
    
    public boolean isBlacklisted(String name) {
        String stripped = stripColors(name).toLowerCase();
        for (String blocked : blacklist) {
            if (stripped.contains(blocked.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isChangeDisplayName() {
        return changeDisplayName;
    }
    
    public boolean isChangePlayerListName() {
        return changePlayerListName;
    }
    
    public boolean isEconomyEnabled() {
        return economyEnabled;
    }
    
    public double getFirstSetCost() {
        return firstSetCost;
    }
    
    public double getChangeCost() {
        return changeCost;
    }
    
    private String stripColors(String text) {
        if (text == null) return "";
        return text.replaceAll("[&§][0-9a-fk-orA-FK-OR]", "");
    }
}
