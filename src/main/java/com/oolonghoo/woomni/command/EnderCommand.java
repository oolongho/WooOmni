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
 * 无参数时打开自己的末影箱
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
        if (!plugin.getModuleManager().isModuleLoaded("inventory")) {
            sender.sendMessage(msg.getWithPrefix("general.module-not-found", "module", "inventory"));
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(msg.getWithPrefix("general.player-only"));
            return true;
        }
        
        Player viewer = (Player) sender;
        
        if (!viewer.hasPermission(Perms.Inventory.ENDER_VIEW)) {
            viewer.sendMessage(msg.getWithPrefix("general.no-permission"));
            return true;
        }
        
        // 无参数时打开自己的末影箱
        if (args.length == 0) {
            viewer.openInventory(viewer.getEnderChest());
            return true;
        }
        
        String targetName = args[0];
        
        // 禁止查看自己的末影箱（管理界面）
        if (targetName.equalsIgnoreCase(viewer.getName())) {
            viewer.openInventory(viewer.getEnderChest());
            return true;
        }
        
        Player onlineTarget = Bukkit.getPlayer(targetName);
        
        if (onlineTarget != null) {
            openEnderChest(viewer, onlineTarget, onlineTarget.getUniqueId(), onlineTarget.getName());
        } else {
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
            
            if (!offlineTarget.hasPlayedBefore()) {
                viewer.sendMessage(msg.getWithPrefix("general.player-not-found", "player", targetName));
                return true;
            }
            
            UUID targetUUID = offlineTarget.getUniqueId();
            String name = offlineTarget.getName() != null ? offlineTarget.getName() : targetName;
            
            openEnderChest(viewer, null, targetUUID, name);
        }
        
        return true;
    }
    
    private void openEnderChest(Player viewer, Player target, UUID targetUUID, String targetName) {
        InventoryModule inventoryModule = (InventoryModule) plugin.getModuleManager().getModule("inventory");
        InventorySettings settings = inventoryModule.getSettings();
        
        boolean canEdit = viewer.hasPermission(Perms.Inventory.ENDER_EDIT);
        
        EnderSeeGUI gui = new EnderSeeGUI(settings, inventoryModule.getDataUtil(), targetUUID, targetName, target, canEdit);
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
