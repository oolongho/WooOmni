package com.oolonghoo.woomni.command;

import com.oolonghoo.woomni.Perms;
import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.config.MessageManager;
import com.oolonghoo.woomni.module.inventory.InventoryModule;
import com.oolonghoo.woomni.module.inventory.InventorySettings;
import com.oolonghoo.woomni.module.inventory.gui.EnderSeeGUI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 末影箱查看命令
 * 允许管理员查看并编辑玩家的末影箱
 */
public class EnderCommand implements CommandExecutor, TabCompleter {
    
    private final WooOmni plugin;
    private final MessageManager msg;
    
    public EnderCommand(WooOmni plugin) {
        this.plugin = plugin;
        this.msg = plugin.getMessageManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 检查模块是否加载
        if (!plugin.getModuleManager().isModuleLoaded("inventory")) {
            sender.sendMessage(msg.getWithPrefix("general.module-not-found", "module", "inventory"));
            return true;
        }
        
        // 必须是玩家
        if (!(sender instanceof Player)) {
            sender.sendMessage(msg.getWithPrefix("general.player-only"));
            return true;
        }
        
        Player viewer = (Player) sender;
        
        // 检查权限
        if (!viewer.hasPermission(Perms.Inventory.ENDER_VIEW)) {
            viewer.sendMessage(msg.getWithPrefix("general.no-permission"));
            return true;
        }
        
        // 检查参数
        if (args.length == 0) {
            viewer.sendMessage(msg.getWithPrefix("ender.usage"));
            return true;
        }
        
        String targetName = args[0];
        
        // 尝试获取在线玩家
        Player onlineTarget = Bukkit.getPlayer(targetName);
        
        if (onlineTarget != null) {
            // 检查豁免权限
            if (onlineTarget.hasPermission(Perms.Inventory.ENDER_EXEMPT) && !viewer.hasPermission(Perms.Inventory.ENDER_EXEMPT)) {
                viewer.sendMessage(msg.getWithPrefix("ender.exempt", "player", onlineTarget.getName()));
                return true;
            }
            
            // 打开在线玩家的末影箱
            openEnderChest(viewer, onlineTarget, onlineTarget.getUniqueId(), onlineTarget.getName());
        } else {
            // 尝试获取离线玩家
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
            
            // 检查玩家是否曾经加入过服务器
            if (!offlineTarget.hasPlayedBefore()) {
                viewer.sendMessage(msg.getWithPrefix("general.player-not-found", "player", targetName));
                return true;
            }
            
            UUID targetUUID = offlineTarget.getUniqueId();
            String name = offlineTarget.getName() != null ? offlineTarget.getName() : targetName;
            
            // 打开离线玩家的末影箱
            openEnderChest(viewer, null, targetUUID, name);
        }
        
        return true;
    }
    
    /**
     * 打开末影箱GUI
     * @param viewer 查看者
     * @param target 目标玩家（可能为null，表示离线玩家）
     * @param targetUUID 目标玩家UUID
     * @param targetName 目标玩家名称
     */
    private void openEnderChest(Player viewer, Player target, UUID targetUUID, String targetName) {
        InventoryModule inventoryModule = (InventoryModule) plugin.getModuleManager().getModule("inventory");
        InventorySettings settings = inventoryModule.getSettings();
        
        // 检查编辑权限
        boolean canEdit = viewer.hasPermission(Perms.Inventory.ENDER_EDIT);
        
        // 创建并打开GUI
        EnderSeeGUI gui = new EnderSeeGUI(settings, targetUUID, targetName, target, canEdit);
        viewer.openInventory(gui.getInventory());
        
        viewer.sendMessage(msg.getWithPrefix("ender.opened", "player", targetName));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1 && sender.hasPermission(Perms.Inventory.ENDER_VIEW)) {
            String prefix = args[0].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(prefix)) {
                    completions.add(player.getName());
                }
            }
        }
        
        return completions;
    }
}
