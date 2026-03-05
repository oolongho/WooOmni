package com.oolonghoo.woomni.listener;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.module.vanish.VanishBossBar;
import com.oolonghoo.woomni.module.vanish.VanishData;
import com.oolonghoo.woomni.module.vanish.VanishDataManager;
import com.oolonghoo.woomni.module.vanish.VanishHider;
import com.oolonghoo.woomni.module.vanish.VanishSettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            VanishData data = dataManager.getVanishData(uuid);
            
            if (settings.isAutoVanishEnabled() && player.hasPermission(settings.getAutoVanishPermission())) {
                data.setAutoVanishJoin(true);
            }
            
            if (data.isVanished() || data.isAutoVanishJoin()) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (player.isOnline()) {
                        hider.hidePlayer(player);
                        
                        if (data.isBossbarEnabled()) {
                            bossBar.showBossBar(player);
                        }
                        
                        if (data.hasNightVision()) {
                            player.addPotionEffect(new PotionEffect(
                                PotionEffectType.NIGHT_VISION,
                                Integer.MAX_VALUE, 0, false, false
                            ));
                        }
                        
                        // 设置不可见状态（对生物也有效）
                        player.setInvisible(true);
                        
                        // 处理加入消息 - 如果 shouldShowJoinMessage 为 false，则隐藏消息
                        if (settings.isFakeMessagesEnabled() && !data.shouldShowJoinMessage()) {
                            event.joinMessage(null);
                        }
                    }
                });
            }
            
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
        
        boolean wasVanished = hider.isVanished(uuid);
        
        VanishData data = dataManager.getIfPresent(uuid);
        if (data != null) {
            data.setVanished(wasVanished);
            
            // 处理退出消息 - 如果 shouldShowQuitMessage 为 false，则隐藏消息
            if (settings.isFakeMessagesEnabled() && wasVanished && !data.shouldShowQuitMessage()) {
                event.quitMessage(null);
            }
        }
        
        // 清除不可见状态
        player.setInvisible(false);
        
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
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!hider.isVanished(player)) {
                continue;
            }
            
            VanishData data = dataManager.getIfPresent(player.getUniqueId());
            if (data == null || !data.shouldPreventMobSpawn()) {
                continue;
            }
            
            double distance = event.getLocation().distanceSquared(player.getLocation());
            if (distance < 16384) {
                event.setCancelled(true);
                return;
            }
        }
    }
    
    /**
     * 防止生物发现隐身玩家
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        Entity target = event.getTarget();
        if (!(target instanceof Player)) {
            return;
        }
        
        Player player = (Player) target;
        
        if (hider.isVanished(player)) {
            VanishData data = dataManager.getIfPresent(player.getUniqueId());
            if (data != null && data.shouldPreventMobSpawn()) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        
        if (hider.isVanished(player)) {
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
