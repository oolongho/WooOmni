package com.oolonghoo.woomni.module.vanish;

import com.oolonghoo.woomni.WooOmni;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BossBar管理器
 * 负责管理隐身玩家的BossBar显示
 * 使用Paper API的BossBar系统
 */
public class VanishBossBar {
    
    private final WooOmni plugin;
    private final VanishSettings settings;
    
    // 玩家UUID -> BossBar映射
    private final Map<UUID, BossBar> playerBossBars = new ConcurrentHashMap<>();
    
    public VanishBossBar(WooOmni plugin, VanishSettings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }
    
    /**
     * 为隐身玩家显示BossBar
     * @param player 隐身玩家
     */
    public void showBossBar(Player player) {
        if (!settings.isDefaultBossbarEnabled()) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        
        // 如果已经有BossBar，先移除
        removeBossBar(player);
        
        // 创建新的BossBar
        String title = settings.getBossbarTitle();
        BarColor color = settings.getBossbarColor();
        BarStyle style = settings.getBossbarStyle();
        
        // 转换颜色代码
        Component titleComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(title);
        String legacyTitle = LegacyComponentSerializer.legacySection().serialize(titleComponent);
        
        BossBar bossBar = Bukkit.createBossBar(legacyTitle, color, style);
        bossBar.addPlayer(player);
        bossBar.setVisible(true);
        bossBar.setProgress(1.0);
        
        playerBossBars.put(uuid, bossBar);
    }
    
    /**
     * 移除玩家的BossBar
     * @param player 玩家
     */
    public void removeBossBar(Player player) {
        UUID uuid = player.getUniqueId();
        BossBar bossBar = playerBossBars.remove(uuid);
        
        if (bossBar != null) {
            bossBar.removePlayer(player);
            bossBar.setVisible(false);
        }
    }
    
    /**
     * 更新BossBar标题
     * @param player 玩家
     * @param title 新标题
     */
    public void updateTitle(Player player, String title) {
        BossBar bossBar = playerBossBars.get(player.getUniqueId());
        if (bossBar != null) {
            Component titleComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(title);
            String legacyTitle = LegacyComponentSerializer.legacySection().serialize(titleComponent);
            bossBar.setTitle(legacyTitle);
        }
    }
    
    /**
     * 更新BossBar颜色
     * @param player 玩家
     * @param color 新颜色
     */
    public void updateColor(Player player, BarColor color) {
        BossBar bossBar = playerBossBars.get(player.getUniqueId());
        if (bossBar != null) {
            bossBar.setColor(color);
        }
    }
    
    /**
     * 更新BossBar样式
     * @param player 玩家
     * @param style 新样式
     */
    public void updateStyle(Player player, BarStyle style) {
        BossBar bossBar = playerBossBars.get(player.getUniqueId());
        if (bossBar != null) {
            bossBar.setStyle(style);
        }
    }
    
    /**
     * 检查玩家是否有BossBar
     * @param uuid 玩家UUID
     * @return 是否有BossBar
     */
    public boolean hasBossBar(UUID uuid) {
        return playerBossBars.containsKey(uuid);
    }
    
    /**
     * 检查玩家是否有BossBar
     * @param player 玩家
     * @return 是否有BossBar
     */
    public boolean hasBossBar(Player player) {
        return hasBossBar(player.getUniqueId());
    }
    
    /**
     * 刷新所有BossBar
     * 用于配置重载后更新
     */
    public void refreshAll() {
        String title = settings.getBossbarTitle();
        BarColor color = settings.getBossbarColor();
        BarStyle style = settings.getBossbarStyle();
        
        Component titleComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(title);
        String legacyTitle = LegacyComponentSerializer.legacySection().serialize(titleComponent);
        
        for (BossBar bossBar : playerBossBars.values()) {
            bossBar.setTitle(legacyTitle);
            bossBar.setColor(color);
            bossBar.setStyle(style);
        }
    }
    
    /**
     * 清理所有BossBar
     */
    public void clearAll() {
        for (Map.Entry<UUID, BossBar> entry : playerBossBars.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                entry.getValue().removePlayer(player);
            }
            entry.getValue().setVisible(false);
        }
        playerBossBars.clear();
    }
    
    /**
     * 获取当前显示BossBar的玩家数量
     * @return BossBar数量
     */
    public int getBossBarCount() {
        return playerBossBars.size();
    }
}
