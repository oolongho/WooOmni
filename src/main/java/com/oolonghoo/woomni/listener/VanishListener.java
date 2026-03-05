package com.oolonghoo.woomni.listener;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.module.vanish.VanishBossBar;
import com.oolonghoo.woomni.module.vanish.VanishData;
import com.oolonghoo.woomni.module.vanish.VanishDataManager;
import com.oolonghoo.woomni.module.vanish.VanishHider;
import com.oolonghoo.woomni.module.vanish.VanishSettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;

/**
 * Vanish模块事件监听器
 * 处理玩家加入、退出、物品拾取、伤害、怪物生成等事件
 */
public class VanishListener implements Listener {
    
    private final WooOmni plugin;
    private final VanishDataManager dataManager;
    private final VanishHider hider;
    private final VanishBossBar bossBar;
    private final VanishSettings settings;
    
    public VanishListener(WooOmni plugin, VanishDataManager dataManager, VanishHider hider, 
                          VanishBossBar bossBar, VanishSettings settings) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.hider = hider;
        this.bossBar = bossBar;
        this.settings = settings;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // 异步加载数据
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            VanishData data = dataManager.getVanishData(uuid);
            
            // 检查自动隐身
            if (settings.isAutoVanishEnabled() && player.hasPermission(settings.getAutoVanishPermission())) {
                data.setAutoVanishJoin(true);
            }
            
            // 如果玩家应该隐身
            if (data.isVanished() || data.isAutoVanishJoin()) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (player.isOnline()) {
                        // 设置隐身状态
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
                        
                        // 处理加入消息
                        if (settings.isFakeMessagesEnabled() && data.shouldShowJoinMessage()) {
                            event.joinMessage(null);
                        }
                    }
                });
            }
            
            // 更新其他玩家对此玩家的可见性
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    hider.onPlayerJoin(player);
                }
            });
        });
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // 先获取隐身状态，在 hider 清理之前
        boolean wasVanished = hider.isVanished(uuid);
        
        VanishData data = dataManager.getIfPresent(uuid);
        if (data != null) {
            // 保存当前隐身状态
            data.setVanished(wasVanished);
            
            // 处理退出消息
            if (settings.isFakeMessagesEnabled() && wasVanished && data.shouldShowQuitMessage()) {
                event.quitMessage(null);
            }
        }
        
        // 清理（顺序很重要：先清理 hider，再清理 bossBar，最后保存数据）
        hider.onPlayerQuit(player);
        bossBar.removeBossBar(player);
        dataManager.removeFromCache(uuid);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        VanishData data = dataManager.getIfPresent(player.getUniqueId());
        
        if (data != null && hider.isVanished(player) && !data.canPickupItems()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        VanishData data = dataManager.getIfPresent(player.getUniqueId());
        
        if (data != null && hider.isVanished(player) && !data.canTakeDamage()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player damager = (Player) event.getDamager();
        VanishData data = dataManager.getIfPresent(damager.getUniqueId());
        
        if (data != null && hider.isVanished(damager) && !data.canDamageOthers()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof Monster)) {
            return;
        }
        
        // 检查附近是否有隐身玩家
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!hider.isVanished(player)) {
                continue;
            }
            
            VanishData data = dataManager.getIfPresent(player.getUniqueId());
            if (data == null || !data.shouldPreventMobSpawn()) {
                continue;
            }
            
            // 检查距离（128格内）
            double distance = event.getLocation().distanceSquared(player.getLocation());
            if (distance < 16384) { // 128^2
                event.setCancelled(true);
                return;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // 静默开箱功能已由 VanishChestListener 处理
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        
        if (hider.isVanished(player)) {
            // 传送后刷新可见性
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    hider.refreshAllVisibility();
                }
            }, 1L);
        }
    }
    
    public void unregister() {
        org.bukkit.event.HandlerList.unregisterAll(this);
    }
}
