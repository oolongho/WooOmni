package com.oolonghoo.woomni.module.nickname;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {
    
    private static Economy economy = null;
    private static boolean enabled = false;
    
    public static boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        
        economy = rsp.getProvider();
        enabled = economy != null;
        return enabled;
    }
    
    public static boolean isEnabled() {
        return enabled && economy != null;
    }
    
    public static Economy getEconomy() {
        return economy;
    }
    
    public static double getBalance(Player player) {
        if (!isEnabled()) return 0;
        return economy.getBalance(player);
    }
    
    public static boolean hasEnough(Player player, double amount) {
        if (!isEnabled()) return true;
        if (amount <= 0) return true;
        return economy.has(player, amount);
    }
    
    public static boolean hasEnough(Player player, int amount) {
        return hasEnough(player, (double) amount);
    }
    
    public static boolean withdraw(Player player, double amount) {
        if (!isEnabled()) return true;
        if (amount <= 0) return true;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }
    
    public static boolean withdraw(Player player, int amount) {
        return withdraw(player, (double) amount);
    }
    
    public static boolean deposit(Player player, double amount) {
        if (!isEnabled()) return true;
        if (amount <= 0) return true;
        return economy.depositPlayer(player, amount).transactionSuccess();
    }
    
    public static String format(double amount) {
        if (!isEnabled()) return String.format("%.2f", amount);
        return economy.format(amount);
    }
    
    public static String format(int amount) {
        return format((double) amount);
    }
}
