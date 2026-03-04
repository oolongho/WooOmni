package com.oolonghoo.woomni.command;

import com.oolonghoo.woomni.Perms;
import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.config.MessageManager;
import com.oolonghoo.woomni.module.inventory.InventoryModule;
import com.oolonghoo.woomni.module.inventory.gui.InvSeeGUI;
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
 * 背包查看命令
 * 实现 /inv [player] 命令功能
 */
public class InvCommand implements CommandExecutor, TabCompleter {
    
    private final WooOmni plugin;
    private final MessageManager msg;
    
    public InvCommand(WooOmni plugin) {
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
        if (!viewer.hasPermission(Perms.Inventory.INV_VIEW)) {
            viewer.sendMessage(msg.getWithPrefix("general.no-permission"));
            return true;
        }
        
        // 检查参数
        if (args.length == 0) {
            viewer.sendMessage(msg.getWithPrefix("inv.usage"));
            return true;
        }
        
        // 获取目标玩家
        String targetName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        
        // 检查目标玩家是否存在
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            viewer.sendMessage(msg.getWithPrefix("general.player-not-found", "player", targetName));
            return true;
        }
        
        // 检查豁免权限
        if (target.isOnline()) {
            Player onlineTarget = target.getPlayer();
            if (onlineTarget != null && onlineTarget.hasPermission(Perms.Inventory.INV_EXEMPT)) {
                viewer.sendMessage(msg.getWithPrefix("inv.exempt", "player", targetName));
                return true;
            }
        }
        
        // 获取模块和设置
        InventoryModule inventoryModule = (InventoryModule) plugin.getModuleManager().getModule("inventory");
        
        // 创建并打开GUI
        boolean canEdit = viewer.hasPermission(Perms.Inventory.INV_EDIT);
        InvSeeGUI gui = new InvSeeGUI(
            inventoryModule.getSettings(),
            target.getUniqueId(),
            targetName,
            target.isOnline(),
            canEdit
        );
        
        viewer.openInventory(gui.getInventory());
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1 && sender.hasPermission(Perms.Inventory.INV_VIEW)) {
            String prefix = args[0].toLowerCase();
            
            // 在线玩家
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(prefix)) {
                    // 排除有豁免权限的玩家（如果发送者不是自己）
                    if (!player.hasPermission(Perms.Inventory.INV_EXEMPT) || 
                        sender instanceof Player && player.equals(sender)) {
                        completions.add(player.getName());
                    }
                }
            }
        }
        
        return completions;
    }
}
