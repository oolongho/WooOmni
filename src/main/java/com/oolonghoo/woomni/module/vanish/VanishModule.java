package com.oolonghoo.woomni.module.vanish;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.listener.VanishChestListener;
import com.oolonghoo.woomni.listener.VanishListener;
import com.oolonghoo.woomni.module.Module;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class VanishModule extends Module {
    
    private VanishSettings settings;
    private VanishDataManager dataManager;
    private VanishHider hider;
    private VanishBossBar bossBar;
    private VanishListener listener;
    private VanishChestListener chestListener;
    
    public VanishModule(WooOmni plugin) {
        super(plugin, "vanish");
    }
    
    @Override
    public void onEnable() {
        // 初始化设置
        settings = new VanishSettings(plugin);
        settings.initialize();
        log("配置加载完成");
        
        // 初始化数据管理器
        dataManager = new VanishDataManager(plugin);
        dataManager.initialize();
        log("数据管理器初始化完成");
        
        // 初始化隐藏管理器
        hider = new VanishHider(plugin, settings);
        log("隐藏管理器初始化完成");
        
        // 初始化BossBar管理器
        bossBar = new VanishBossBar(plugin, settings);
        log("BossBar管理器初始化完成");
        
        // 注册监听器
        listener = new VanishListener(plugin, dataManager, hider, bossBar, settings);
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        
        // 注册静默开箱子监听器
        chestListener = new VanishChestListener(plugin, dataManager, hider);
        plugin.getServer().getPluginManager().registerEvents(chestListener, plugin);
        log("事件监听器注册完成");
        
        // 恢复在线玩家的隐身状态
        restoreOnlinePlayers();
        
        log("模块启用完成");
    }
    
    @Override
    public void onDisable() {
        // 保存所有数据
        saveAll();
        
        // 清理BossBar
        if (bossBar != null) {
            bossBar.clearAll();
        }
        
        // 清理隐身状态
        if (hider != null) {
            hider.clearAll();
        }
        
        // 注销监听器
        if (listener != null) {
            listener.unregister();
        }
        
        // 关闭数据管理器
        if (dataManager != null) {
            dataManager.shutdown();
        }
        
        log("模块已禁用");
    }
    
    @Override
    public void onReload() {
        // 保存所有数据
        saveAll();
        
        // 重载配置
        if (settings != null) {
            settings.reload();
            log("配置已重载");
        }
        
        // 刷新BossBar
        if (bossBar != null) {
            bossBar.refreshAll();
        }
        
        // 刷新可见性
        if (hider != null) {
            hider.refreshAllVisibility();
        }
        
        log("模块重载完成");
    }
    
    @Override
    public void saveAll() {
        if (dataManager != null) {
            dataManager.saveAll();
        }
    }
    
    /**
     * 恢复在线玩家的隐身状态
     */
    private void restoreOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            
            // 异步加载数据
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                VanishData data = dataManager.getVanishData(uuid);
                
                if (data.isVanished() || data.isAutoVanishJoin()) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        if (player.isOnline()) {
                            // 恢复隐身状态
                            hider.hidePlayer(player);
                            
                            // 显示BossBar
                            if (data.isBossbarEnabled()) {
                                bossBar.showBossBar(player);
                            }
                            
                            // 设置夜视
                            if (data.hasNightVision()) {
                                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                                    org.bukkit.potion.PotionEffectType.NIGHT_VISION,
                                    Integer.MAX_VALUE, 0, false, false
                                ));
                            }
                        }
                    });
                }
            });
        }
    }
    
    /**
     * 设置玩家隐身状态
     * @param player 玩家
     * @param vanished 是否隐身
     */
    public void setVanished(Player player, boolean vanished) {
        UUID uuid = player.getUniqueId();
        VanishData data = dataManager.getVanishData(uuid);
        
        data.setVanished(vanished);
        
        if (vanished) {
            hider.hidePlayer(player);
            
            if (data.isBossbarEnabled()) {
                bossBar.showBossBar(player);
            }
            
            if (data.hasNightVision()) {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.NIGHT_VISION,
                    Integer.MAX_VALUE, 0, false, false
                ));
            }
        } else {
            hider.showPlayer(player);
            bossBar.removeBossBar(player);
            
            // 移除夜视效果
            player.removePotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION);
        }
        
        // 异步保存数据
        dataManager.saveVanishData(uuid);
    }
    
    /**
     * 切换玩家隐身状态
     * @param player 玩家
     * @return 新的隐身状态
     */
    public boolean toggleVanish(Player player) {
        boolean currentState = hider.isVanished(player);
        setVanished(player, !currentState);
        return !currentState;
    }
    
    /**
     * 检查玩家是否隐身
     * @param player 玩家
     * @return 是否隐身
     */
    public boolean isVanished(Player player) {
        return hider.isVanished(player);
    }
    
    /**
     * 检查玩家是否隐身
     * @param uuid 玩家UUID
     * @return 是否隐身
     */
    public boolean isVanished(UUID uuid) {
        return hider.isVanished(uuid);
    }
    
    /**
     * 获取数据管理器
     * @return 数据管理器
     */
    public VanishDataManager getDataManager() {
        return dataManager;
    }
    
    /**
     * 获取设置管理器
     * @return 设置管理器
     */
    public VanishSettings getSettings() {
        return settings;
    }
    
    /**
     * 获取隐藏管理器
     * @return 隐藏管理器
     */
    public VanishHider getHider() {
        return hider;
    }
    
    /**
     * 获取BossBar管理器
     * @return BossBar管理器
     */
    public VanishBossBar getBossBar() {
        return bossBar;
    }
}
