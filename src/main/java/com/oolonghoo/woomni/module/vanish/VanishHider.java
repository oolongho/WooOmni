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
    
    // 隐身玩家集合
    private final Set<UUID> vanishedPlayers = ConcurrentHashMap.newKeySet();
    
    // 可以看到隐身玩家的权限
    private static final String SEE_VANISH_PERMISSION = "wooomni.vanish.see";
    
    public VanishHider(WooOmni plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 隐藏玩家
     * @param player 要隐藏的玩家
     */
    public void hidePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        vanishedPlayers.add(uuid);
        
        // 对所有在线玩家隐藏此玩家
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) {
                continue;
            }
            
            // 检查其他玩家是否有权限看到隐身玩家
            if (!other.hasPermission(SEE_VANISH_PERMISSION)) {
                other.hidePlayer(plugin, player);
            }
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
        
        // 对所有在线玩家显示此玩家
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) {
                continue;
            }
            other.showPlayer(plugin, player);
        }
        
        plugin.getLogger().info("[Vanish] 玩家 " + player.getName() + " 已退出隐身状态");
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
        // 隐藏所有隐身玩家对新玩家可见
        for (UUID vanishedUuid : vanishedPlayers) {
            Player vanishedPlayer = Bukkit.getPlayer(vanishedUuid);
            if (vanishedPlayer != null && vanishedPlayer.isOnline()) {
                // 如果新玩家没有权限看到隐身玩家，则隐藏
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
            
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (other.equals(player)) {
                    continue;
                }
                
                if (isVanished) {
                    // 玩家处于隐身状态
                    if (other.hasPermission(SEE_VANISH_PERMISSION)) {
                        other.showPlayer(plugin, player);
                    } else {
                        other.hidePlayer(plugin, player);
                    }
                } else {
                    // 玩家不处于隐身状态，对所有人可见
                    other.showPlayer(plugin, player);
                }
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
        // 如果目标玩家不隐身，则可见
        if (!isVanished(target)) {
            return true;
        }
        
        // 如果观察者有权限看到隐身玩家，则可见
        if (viewer.hasPermission(SEE_VANISH_PERMISSION)) {
            return true;
        }
        
        // 否则不可见
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
        // 显示所有隐身玩家
        for (UUID uuid : vanishedPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                showPlayer(player);
            }
        }
        vanishedPlayers.clear();
    }
}
