package com.oolonghoo.woomni.listener;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.module.god.GodData;
import com.oolonghoo.woomni.module.god.GodDataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GodListener implements Listener {
    
    private final WooOmni plugin;
    private final GodDataManager dataManager;
    
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
                    }
                });
            }
        });
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        GodData data = dataManager.getIfPresent(player.getUniqueId());
        if (data != null) {
            data.setGodMode(player.isInvulnerable());
        }
        
        dataManager.removeFromCache(player.getUniqueId());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        GodData data = dataManager.getIfPresent(player.getUniqueId());
        
        if (data != null && data.isGodMode()) {
            event.setCancelled(true);
        }
    }
    
    public void unregister() {
        org.bukkit.event.HandlerList.unregisterAll(this);
    }
}
