package com.oolonghoo.woomni.module.nickname;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.event.NicknameChangeEvent;
import com.oolonghoo.woomni.module.Module;
import com.oolonghoo.woomni.module.nickname.gui.NicknameAnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class NicknameModule extends Module {
    
    private NicknameSettings settings;
    private NickDataManager dataManager;
    private NicknameExpansion expansion;
    private boolean vaultEnabled;
    
    public NicknameModule(WooOmni plugin) {
        super(plugin, "nickname");
    }
    
    @Override
    public void onEnable() {
        settings = new NicknameSettings(plugin);
        settings.initialize();
        
        dataManager = new NickDataManager(plugin);
        dataManager.initialize();
        
        if (settings.isEconomyEnabled()) {
            vaultEnabled = VaultHook.setupEconomy();
            if (!vaultEnabled) {
                plugin.getLogger().warning("[nickname] Vault not found, economy features disabled");
            }
        } else {
            vaultEnabled = false;
        }
        
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            expansion = new NicknameExpansion(plugin, this);
            expansion.register();
        }
    }
    
    @Override
    public void onDisable() {
        saveAll();
        
        if (expansion != null) {
            expansion.unregister();
            expansion = null;
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
        
        if (settings.isEconomyEnabled() && !vaultEnabled) {
            vaultEnabled = VaultHook.setupEconomy();
        }
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            applyDisplayName(player);
        }
    }
    
    @Override
    public void saveAll() {
        if (dataManager != null) {
            dataManager.saveAll();
        }
    }
    
    public boolean isEconomyEnabled() {
        return settings.isEconomyEnabled() && vaultEnabled;
    }
    
    public double getSetCost(Player player) {
        if (!isEconomyEnabled()) return 0;
        
        boolean hasNickname = hasNickname(player.getUniqueId());
        return hasNickname ? settings.getChangeCost() : settings.getFirstSetCost();
    }
    
    public boolean canAfford(Player player) {
        if (!isEconomyEnabled()) return true;
        
        double cost = getSetCost(player);
        return VaultHook.hasEnough(player, cost);
    }
    
    public boolean chargePlayer(Player player) {
        if (!isEconomyEnabled()) return true;
        
        double cost = getSetCost(player);
        if (cost <= 0) return true;
        
        return VaultHook.withdraw(player, cost);
    }
    
    public String formatCost(double amount) {
        if (!isEconomyEnabled()) return "0";
        return VaultHook.format(amount);
    }
    
    public void setNickname(Player player, String nickname) {
        setNickname(player, nickname, player);
    }
    
    public void setNickname(Player target, String nickname, Player initiator) {
        NickData data = dataManager.getNickData(target.getUniqueId());
        String oldNickname = data.getNickname();
        
        NicknameChangeEvent event = new NicknameChangeEvent(
            target.getUniqueId(), target.getName(), oldNickname, nickname, initiator);
        Bukkit.getPluginManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        data.setNickname(nickname);
        dataManager.saveNickData(target.getUniqueId());
        
        applyDisplayName(target);
    }
    
    public void clearNickname(Player player) {
        clearNickname(player, player);
    }
    
    public void clearNickname(Player target, Player initiator) {
        setNickname(target, null, initiator);
    }
    
    public void applyDisplayName(Player player) {
        String nickname = getNickname(player.getUniqueId());
        
        if (nickname != null && !nickname.isEmpty()) {
            String displayName = settings.getPrefix() + nickname;
            
            if (settings.isChangeDisplayName()) {
                player.setDisplayName(displayName);
            }
            
            if (settings.isChangePlayerListName()) {
                player.setPlayerListName(displayName);
            }
        } else {
            if (settings.isChangeDisplayName()) {
                player.setDisplayName(player.getName());
            }
            
            if (settings.isChangePlayerListName()) {
                player.setPlayerListName(player.getName());
            }
        }
    }
    
    public String getNickname(UUID uuid) {
        NickData data = dataManager.getIfPresent(uuid);
        if (data != null && data.hasNickname()) {
            return data.getNickname();
        }
        return null;
    }
    
    public String getDisplayName(UUID uuid) {
        String nickname = getNickname(uuid);
        if (nickname != null && !nickname.isEmpty()) {
            return settings.getPrefix() + nickname;
        }
        Player player = Bukkit.getPlayer(uuid);
        return player != null ? player.getName() : null;
    }
    
    public String getRealName(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null ? player.getName() : null;
    }
    
    public boolean hasNickname(UUID uuid) {
        NickData data = dataManager.getIfPresent(uuid);
        return data != null && data.hasNickname();
    }
    
    public NicknameSettings getSettings() {
        return settings;
    }
    
    public NickDataManager getDataManager() {
        return dataManager;
    }
    
    public void openAnvilGUI(Player player) {
        NickData data = dataManager.getNickData(player.getUniqueId());
        String currentNick = data.getNickname();
        
        NicknameAnvilGUI gui = new NicknameAnvilGUI(this, player, currentNick);
        gui.open();
    }
}
