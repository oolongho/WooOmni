package com.oolonghoo.woomni.listener;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.module.fly.FlyData;
import com.oolonghoo.woomni.module.fly.FlyDataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class FlyListener implements Listener {
    
    private final WooOmni plugin;
    private final FlyDataManager dataManager;
    
    public FlyListener(WooOmni plugin, FlyDataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            FlyData data = dataManager.getFlyData(player.getUniqueId());
            
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) {
                    return;
                }
                
                if (data.isFlying()) {
                    player.setAllowFlight(true);
                    player.setFlying(true);
                }
                player.setFlySpeed(data.getFlySpeed());
            });
        });
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        FlyData data = dataManager.getIfPresent(player.getUniqueId());
        if (data != null) {
            data.setFlying(player.isFlying());
            data.setFlySpeed(player.getFlySpeed());
        }
        
        dataManager.removeFromCache(player.getUniqueId());
    }
    
    public void unregister() {
        org.bukkit.event.HandlerList.unregisterAll(this);
    }
}
