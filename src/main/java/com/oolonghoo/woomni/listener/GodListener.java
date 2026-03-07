package com.oolonghoo.woomni.listener;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.module.god.GodData;
import com.oolonghoo.woomni.module.god.GodDataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GodListener implements Listener {
    
    private final WooOmni plugin;
    private final GodDataManager dataManager;
    private final Set<UUID> godModePlayers = ConcurrentHashMap.newKeySet();
    private int oxygenTaskId = -1;
    
    public GodListener(WooOmni plugin, GodDataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            GodData data = dataManager.getGodData(player.getUniqueId());
            
            if (data.isGodMode()) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (player.isOnline()) {
                        player.setInvulnerable(true);
                        godModePlayers.add(player.getUniqueId());
                        startOxygenTaskIfNeeded();
                    }
                });
            }
        });
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        godModePlayers.remove(uuid);
        
        GodData data = dataManager.getIfPresent(uuid);
        if (data != null) {
            boolean actualState = player.isInvulnerable();
            boolean recordedState = data.isGodMode();
            
            if (actualState != recordedState && plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().info("[God] 玩家 " + player.getName() + " 退出时状态不一致: " +
                    "记录=" + recordedState + ", 实际=" + actualState);
            }
        }
        
        dataManager.removeFromCache(uuid);
        
        if (godModePlayers.isEmpty()) {
            stopOxygenTask();
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        if (godModePlayers.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        if (godModePlayers.contains(player.getUniqueId())) {
            if (event.getFoodLevel() < player.getFoodLevel()) {
                event.setCancelled(true);
            }
        }
    }
    
    private void startOxygenTaskIfNeeded() {
        if (oxygenTaskId == -1 && !godModePlayers.isEmpty()) {
            oxygenTaskId = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
                if (godModePlayers.isEmpty()) {
                    stopOxygenTask();
                    return;
                }
                
                for (UUID uuid : godModePlayers) {
                    Player player = plugin.getServer().getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        if (player.getRemainingAir() < player.getMaximumAir()) {
                            player.setRemainingAir(player.getMaximumAir());
                        }
                    } else {
                        godModePlayers.remove(uuid);
                    }
                }
            }, 20L, 20L).getTaskId();
        }
    }
    
    private void stopOxygenTask() {
        if (oxygenTaskId != -1) {
            plugin.getServer().getScheduler().cancelTask(oxygenTaskId);
            oxygenTaskId = -1;
        }
    }
    
    public void addGodPlayer(UUID uuid) {
        if (godModePlayers.add(uuid)) {
            startOxygenTaskIfNeeded();
        }
    }
    
    public void removeGodPlayer(UUID uuid) {
        godModePlayers.remove(uuid);
        if (godModePlayers.isEmpty()) {
            stopOxygenTask();
        }
    }
    
    public boolean isGodPlayer(UUID uuid) {
        return godModePlayers.contains(uuid);
    }
    
    public void unregister() {
        org.bukkit.event.HandlerList.unregisterAll(this);
        stopOxygenTask();
        godModePlayers.clear();
    }
}
