package com.oolonghoo.woomni.module.vanish;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.listener.VanishChestListener;
import com.oolonghoo.woomni.listener.VanishGUIListener;
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
    private VanishGUIListener guiListener;
    
    public VanishModule(WooOmni plugin) {
        super(plugin, "vanish");
    }
    
    @Override
    public void onEnable() {
        settings = new VanishSettings(plugin);
        settings.initialize();
        
        dataManager = new VanishDataManager(plugin);
        dataManager.initialize();
        
        hider = new VanishHider(plugin, dataManager);
        
        bossBar = new VanishBossBar(plugin, settings);
        
        listener = new VanishListener(plugin, dataManager, hider, bossBar, settings);
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        
        chestListener = new VanishChestListener(plugin, dataManager, hider);
        plugin.getServer().getPluginManager().registerEvents(chestListener, plugin);
        
        guiListener = new VanishGUIListener();
        plugin.getServer().getPluginManager().registerEvents(guiListener, plugin);
        
        restoreOnlinePlayers();
    }
    
    @Override
    public void onDisable() {
        saveAll();
        
        if (bossBar != null) {
            bossBar.clearAll();
        }
        
        if (hider != null) {
            hider.clearAll();
        }
        
        if (listener != null) {
            listener.unregister();
        }
        
        if (dataManager != null) {
            dataManager.shutdown();
        }
    }
    
    @Override
    public void onReload() {
        saveAll();
        
        if (settings != null) {
            settings.reload();
        }
        
        if (bossBar != null) {
            bossBar.refreshAll();
        }
        
        if (hider != null) {
            hider.refreshAllVisibility();
        }
    }
    
    @Override
    public void saveAll() {
        if (dataManager != null) {
            dataManager.saveAll();
        }
    }
    
    private void restoreOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                VanishData data = dataManager.getVanishData(uuid);
                
                if (data.isVanished() || data.isAutoVanishJoin()) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        Player onlinePlayer = Bukkit.getPlayer(uuid);
                        if (onlinePlayer != null && onlinePlayer.isOnline()) {
                            hider.hidePlayer(onlinePlayer);
                            
                            if (data.isBossbarEnabled()) {
                                bossBar.showBossBar(onlinePlayer);
                            }
                            
                            if (data.hasNightVision()) {
                                onlinePlayer.addPotionEffect(new org.bukkit.potion.PotionEffect(
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
            
            player.removePotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION);
        }
        
        dataManager.saveVanishData(uuid);
    }
    
    public boolean toggleVanish(Player player) {
        boolean currentState = hider.isVanished(player);
        setVanished(player, !currentState);
        return !currentState;
    }
    
    public boolean isVanished(Player player) {
        return hider.isVanished(player);
    }
    
    public boolean isVanished(UUID uuid) {
        return hider.isVanished(uuid);
    }
    
    public VanishDataManager getDataManager() {
        return dataManager;
    }
    
    public VanishSettings getSettings() {
        return settings;
    }
    
    public VanishHider getHider() {
        return hider;
    }
    
    public VanishBossBar getBossBar() {
        return bossBar;
    }
    
    public VanishGUIListener getGuiListener() {
        return guiListener;
    }
}
