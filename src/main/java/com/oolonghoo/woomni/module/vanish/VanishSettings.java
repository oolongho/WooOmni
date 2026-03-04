package com.oolonghoo.woomni.module.vanish;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.config.ConfigLoader;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

/**
 * Vanish模块设置管理器
 * 负责加载和管理vanish.yml配置文件
 */
public class VanishSettings extends ConfigLoader {
    
    // 默认设置缓存
    private boolean defaultNightVision;
    private boolean defaultPickupItems;
    private boolean defaultCanTakeDamage;
    private boolean defaultCanDamageOthers;
    private boolean defaultPhysicalCollision;
    private boolean defaultSilentChest;
    private boolean defaultPreventMobSpawn;
    private boolean defaultShowJoinMessage;
    private boolean defaultShowQuitMessage;
    private boolean defaultBossbarEnabled;
    
    // BossBar设置
    private BarColor bossbarColor;
    private BarStyle bossbarStyle;
    private String bossbarTitle;
    
    // 自动隐身设置
    private boolean autoVanishEnabled;
    private String autoVanishPermission;
    
    // 消息设置
    private boolean fakeMessages;
    private boolean notifyAdmins;
    
    public VanishSettings(WooOmni plugin) {
        super(plugin, "settings/vanish.yml");
    }
    
    @Override
    protected void loadValues() {
        cache.clear();
        
        // 加载默认设置
        defaultNightVision = getCachedBoolean("default-settings.night-vision", true);
        defaultPickupItems = getCachedBoolean("default-settings.pickup-items", false);
        defaultCanTakeDamage = getCachedBoolean("default-settings.can-take-damage", false);
        defaultCanDamageOthers = getCachedBoolean("default-settings.can-damage-others", true);
        defaultPhysicalCollision = getCachedBoolean("default-settings.physical-collision", false);
        defaultSilentChest = getCachedBoolean("default-settings.silent-chest", true);
        defaultPreventMobSpawn = getCachedBoolean("default-settings.prevent-mob-spawn", true);
        defaultShowJoinMessage = getCachedBoolean("default-settings.show-join-message", true);
        defaultShowQuitMessage = getCachedBoolean("default-settings.show-quit-message", true);
        defaultBossbarEnabled = getCachedBoolean("default-settings.bossbar-enabled", true);
        
        // 加载BossBar设置
        bossbarColor = loadBarColor(getCachedString("bossbar.color", "RED"));
        bossbarStyle = loadBarStyle(getCachedString("bossbar.style", "SOLID"));
        bossbarTitle = getCachedString("bossbar.title", "&c你正处于隐身状态");
        
        // 加载自动隐身设置
        autoVanishEnabled = getCachedBoolean("auto-vanish.enabled", false);
        autoVanishPermission = getCachedString("auto-vanish.permission", "wooomni.vanish.autojoin");
        
        // 加载消息设置
        fakeMessages = getCachedBoolean("messages.fake-messages", true);
        notifyAdmins = getCachedBoolean("messages.notify-admins", true);
    }
    
    private BarColor loadBarColor(String colorName) {
        try {
            return BarColor.valueOf(colorName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("[Vanish] 无效的BossBar颜色: " + colorName + ", 使用默认值 RED");
            return BarColor.RED;
        }
    }
    
    private BarStyle loadBarStyle(String styleName) {
        try {
            return BarStyle.valueOf(styleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("[Vanish] 无效的BossBar样式: " + styleName + ", 使用默认值 SOLID");
            return BarStyle.SOLID;
        }
    }
    
    // 默认设置Getter
    public boolean isDefaultNightVision() {
        return defaultNightVision;
    }
    
    public boolean isDefaultPickupItems() {
        return defaultPickupItems;
    }
    
    public boolean isDefaultCanTakeDamage() {
        return defaultCanTakeDamage;
    }
    
    public boolean isDefaultCanDamageOthers() {
        return defaultCanDamageOthers;
    }
    
    public boolean isDefaultPhysicalCollision() {
        return defaultPhysicalCollision;
    }
    
    public boolean isDefaultSilentChest() {
        return defaultSilentChest;
    }
    
    public boolean isDefaultPreventMobSpawn() {
        return defaultPreventMobSpawn;
    }
    
    public boolean isDefaultShowJoinMessage() {
        return defaultShowJoinMessage;
    }
    
    public boolean isDefaultShowQuitMessage() {
        return defaultShowQuitMessage;
    }
    
    public boolean isDefaultBossbarEnabled() {
        return defaultBossbarEnabled;
    }
    
    // BossBar设置Getter
    public BarColor getBossbarColor() {
        return bossbarColor;
    }
    
    public BarStyle getBossbarStyle() {
        return bossbarStyle;
    }
    
    public String getBossbarTitle() {
        return bossbarTitle;
    }
    
    // 自动隐身设置Getter
    public boolean isAutoVanishEnabled() {
        return autoVanishEnabled;
    }
    
    public String getAutoVanishPermission() {
        return autoVanishPermission;
    }
    
    // 消息设置Getter
    public boolean isFakeMessagesEnabled() {
        return fakeMessages;
    }
    
    public boolean isNotifyAdminsEnabled() {
        return notifyAdmins;
    }
    
    /**
     * 应用默认设置到VanishData
     */
    public void applyDefaults(VanishData data) {
        data.setNightVision(defaultNightVision);
        data.setPickupItems(defaultPickupItems);
        data.setCanTakeDamage(defaultCanTakeDamage);
        data.setCanDamageOthers(defaultCanDamageOthers);
        data.setPhysicalCollision(defaultPhysicalCollision);
        data.setSilentChest(defaultSilentChest);
        data.setPreventMobSpawn(defaultPreventMobSpawn);
        data.setShowJoinMessage(defaultShowJoinMessage);
        data.setShowQuitMessage(defaultShowQuitMessage);
        data.setBossbarEnabled(defaultBossbarEnabled);
    }
}
