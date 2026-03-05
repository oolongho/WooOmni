package com.oolonghoo.woomni.module.vanish;

import com.oolonghoo.woomni.WooOmni;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 玩家隐藏管理器
 * 负责处理玩家的显示/隐藏逻辑
 * 使用Paper API的hidePlayer/showPlayer方法
 */
public class VanishHider {
    
    private final WooOmni plugin;
    private final VanishDataManager dataManager;
    
    private final Set<UUID> vanishedPlayers = ConcurrentHashMap.newKeySet();
    
    private static final String SEE_VANISH_PERMISSION = "wooomni.vanish.see";
    
    public VanishHider(WooOmni plugin, VanishDataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }
    
    /**
     * 隐藏玩家
     * @param player 要隐藏的玩家
     */
    public void hidePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        vanishedPlayers.add(uuid);
        
        VanishData data = dataManager.getIfPresent(uuid);
        boolean hideFromTab = data == null || data.shouldHideFromTab();
        
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) {
                continue;
            }
            
            if (!other.hasPermission(SEE_VANISH_PERMISSION)) {
                other.hidePlayer(plugin, player);
            }
        }
        
        if (hideFromTab) {
            hideFromPlayerList(player);
        }
        
        plugin.getLogger().info("[Vanish] 玩家 " + player.getName() + " 已进入隐身状态");
    }
    
    /**
     * 显示玩家
     * @param player 要显示的玩家
     */
    public void showPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        vanishedPlayers.remove(uuid);
        
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) {
                continue;
            }
            other.showPlayer(plugin, player);
        }
        
        showInPlayerList(player);
        
        plugin.getLogger().info("[Vanish] 玩家 " + player.getName() + " 已退出隐身状态");
    }
    
    /**
     * 从Tab列表隐藏玩家
     * @param player 要隐藏的玩家
     */
    private void hideFromPlayerList(Player player) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) {
                continue;
            }
            if (!other.hasPermission(SEE_VANISH_PERMISSION)) {
                other.hidePlayer(plugin, player);
            }
        }
    }
    
    /**
     * 在Tab列表显示玩家
     * @param player 要显示的玩家
     */
    private void showInPlayerList(Player player) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) {
                continue;
            }
            other.showPlayer(plugin, player);
        }
    }
    
    /**
     * 更新玩家的Tab列表可见性
     * @param player 目标玩家
     * @param hide 是否隐藏
     */
    public void updateTabVisibility(Player player, boolean hide) {
        if (hide && isVanished(player)) {
            hideFromPlayerList(player);
        } else {
            showInPlayerList(player);
        }
    }
    
    /**
     * 检查玩家是否处于隐身状态
     * @param uuid 玩家UUID
     * @return 是否隐身
     */
    public boolean isVanished(UUID uuid) {
        return vanishedPlayers.contains(uuid);
    }
    
    /**
     * 检查玩家是否处于隐身状态
     * @param player 玩家
     * @return 是否隐身
     */
    public boolean isVanished(Player player) {
        return isVanished(player.getUniqueId());
    }
    
    /**
     * 获取所有隐身玩家UUID
     * @return 隐身玩家集合（不可修改）
     */
    public Set<UUID> getVanishedPlayers() {
        return Collections.unmodifiableSet(vanishedPlayers);
    }
    
    /**
     * 当新玩家加入时更新可见性
     * @param newPlayer 新加入的玩家
     */
    public void onPlayerJoin(Player newPlayer) {
        for (UUID vanishedUuid : vanishedPlayers) {
            Player vanishedPlayer = Bukkit.getPlayer(vanishedUuid);
            if (vanishedPlayer != null && vanishedPlayer.isOnline()) {
                if (!newPlayer.hasPermission(SEE_VANISH_PERMISSION)) {
                    newPlayer.hidePlayer(plugin, vanishedPlayer);
                }
            }
        }
    }
    
    /**
     * 当玩家退出时清理
     * @param player 退出的玩家
     */
    public void onPlayerQuit(Player player) {
        vanishedPlayers.remove(player.getUniqueId());
    }
    
    /**
     * 更新所有隐身玩家的可见性
     * 用于权限变更或重载时刷新
     */
    public void refreshAllVisibility() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean isVanished = vanishedPlayers.contains(player.getUniqueId());
            
            VanishData data = dataManager.getIfPresent(player.getUniqueId());
            boolean hideFromTab = data == null || data.shouldHideFromTab();
            
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (other.equals(player)) {
                    continue;
                }
                
                if (isVanished) {
                    if (other.hasPermission(SEE_VANISH_PERMISSION)) {
                        other.showPlayer(plugin, player);
                    } else {
                        other.hidePlayer(plugin, player);
                    }
                } else {
                    other.showPlayer(plugin, player);
                }
            }
            
            if (isVanished && hideFromTab) {
                hideFromPlayerList(player);
            } else {
                showInPlayerList(player);
            }
        }
    }
    
    /**
     * 检查玩家是否可以看到目标玩家
     * @param viewer 观察者
     * @param target 目标玩家
     * @return 是否可见
     */
    public boolean canSee(Player viewer, Player target) {
        if (!isVanished(target)) {
            return true;
        }
        
        if (viewer.hasPermission(SEE_VANISH_PERMISSION)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 获取隐身玩家数量
     * @return 隐身玩家数量
     */
    public int getVanishedCount() {
        return vanishedPlayers.size();
    }
    
    /**
     * 清理所有隐身状态
     */
    public void clearAll() {
        for (UUID uuid : vanishedPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                showPlayer(player);
            }
        }
        vanishedPlayers.clear();
    }
}
