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
            // 只在玩家有飞行能力时保存飞行状态
            boolean canFly = player.getAllowFlight();
            data.setFlying(canFly && player.isFlying());
            
            // 保存飞行速度，确保在有效范围内
            float flySpeed = player.getFlySpeed();
            if (flySpeed >= 0.0f && flySpeed <= 1.0f) {
                data.setFlySpeed(flySpeed);
            }
        }
        
        dataManager.removeFromCache(player.getUniqueId());
    }
    
    public void unregister() {
        org.bukkit.event.HandlerList.unregisterAll(this);
    }
}
