package com.oolonghoo.woomni.util;

import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

public final class EconomyUtil {
    
    private static Economy vaultEconomy = null;
    private static PlayerPointsAPI playerPointsAPI = null;
    private static boolean vaultChecked = false;
    private static boolean playerPointsChecked = false;
    
    private EconomyUtil() {}
    
    public static boolean setupVault() {
        if (vaultChecked) return vaultEconomy != null;
        vaultChecked = true;
        
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        
        vaultEconomy = rsp.getProvider();
        return vaultEconomy != null;
    }
    
    public static boolean setupPlayerPoints() {
        if (playerPointsChecked) return playerPointsAPI != null;
        playerPointsChecked = true;
        
        if (Bukkit.getPluginManager().getPlugin("PlayerPoints") == null) {
            return false;
        }
        
        try {
            PlayerPoints plugin = (PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints");
            if (plugin == null) {
                return false;
            }
            playerPointsAPI = plugin.getAPI();
            return playerPointsAPI != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean hasVault() {
        if (!vaultChecked) setupVault();
        return vaultEconomy != null;
    }
    
    public static boolean hasPlayerPoints() {
        if (!playerPointsChecked) setupPlayerPoints();
        return playerPointsAPI != null;
    }
    
    public static double getVaultBalance(UUID uuid) {
        if (!hasVault()) return 0;
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        return vaultEconomy.getBalance(player);
    }
    
    public static String getVaultBalanceFormatted(UUID uuid) {
        if (!hasVault()) return null;
        double balance = getVaultBalance(uuid);
        return vaultEconomy.format(balance);
    }
    
    public static int getPlayerPoints(UUID uuid) {
        if (!hasPlayerPoints()) return 0;
        return playerPointsAPI.look(uuid);
    }
    
    public static String getPlayerPointsFormatted(UUID uuid) {
        if (!hasPlayerPoints()) return null;
        int points = getPlayerPoints(uuid);
        return String.format("%,d", points);
    }
    
    public static void reset() {
        vaultEconomy = null;
        playerPointsAPI = null;
        vaultChecked = false;
        playerPointsChecked = false;
    }
}
