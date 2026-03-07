package com.oolonghoo.woomni.module.nickname;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerPointsHook {
    
    private static PlayerPointsAPI api = null;
    private static boolean enabled = false;
    
    public static boolean setupPlayerPoints() {
        if (Bukkit.getPluginManager().getPlugin("PlayerPoints") == null) {
            return false;
        }
        
        try {
            PlayerPoints plugin = (PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints");
            if (plugin == null) {
                return false;
            }
            
            api = plugin.getAPI();
            enabled = api != null;
            return enabled;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean isEnabled() {
        return enabled && api != null;
    }
    
    public static PlayerPointsAPI getAPI() {
        return api;
    }
    
    public static int getPoints(UUID uuid) {
        if (!isEnabled()) return 0;
        return api.look(uuid);
    }
    
    public static int getPoints(Player player) {
        return getPoints(player.getUniqueId());
    }
    
    public static boolean hasEnough(UUID uuid, int amount) {
        if (!isEnabled()) return true;
        if (amount <= 0) return true;
        return api.look(uuid) >= amount;
    }
    
    public static boolean hasEnough(Player player, int amount) {
        return hasEnough(player.getUniqueId(), amount);
    }
    
    public static boolean takePoints(UUID uuid, int amount) {
        if (!isEnabled()) return true;
        if (amount <= 0) return true;
        return api.take(uuid, amount);
    }
    
    public static boolean takePoints(Player player, int amount) {
        return takePoints(player.getUniqueId(), amount);
    }
    
    public static boolean givePoints(UUID uuid, int amount) {
        if (!isEnabled()) return true;
        if (amount <= 0) return true;
        return api.give(uuid, amount);
    }
    
    public static boolean givePoints(Player player, int amount) {
        return givePoints(player.getUniqueId(), amount);
    }
    
    public static String format(int amount) {
        return String.format("%,d", amount);
    }
}
