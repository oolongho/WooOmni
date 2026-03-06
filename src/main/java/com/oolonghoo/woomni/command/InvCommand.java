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
        if (!plugin.getModuleManager().isModuleLoaded("inventory")) {
            sender.sendMessage(msg.getWithPrefix("general.module-not-found", "module", "inventory"));
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(msg.getWithPrefix("general.player-only"));
            return true;
        }
        
        Player viewer = (Player) sender;
        
        if (!viewer.hasPermission(Perms.Inventory.INV_VIEW)) {
            viewer.sendMessage(msg.getWithPrefix("general.no-permission"));
            return true;
        }
        
        if (args.length == 0) {
            viewer.sendMessage(msg.getWithPrefix("inv.usage"));
            return true;
        }
        
        String targetName = args[0];
        
        // 禁止查看自己的背包
        if (targetName.equalsIgnoreCase(viewer.getName())) {
            viewer.sendMessage(msg.getWithPrefix("inv.cannot-view-self"));
            return true;
        }
        
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            viewer.sendMessage(msg.getWithPrefix("general.player-not-found", "player", targetName));
            return true;
        }
        
        InventoryModule inventoryModule = (InventoryModule) plugin.getModuleManager().getModule("inventory");
        
        boolean canEdit = viewer.hasPermission(Perms.Inventory.INV_EDIT);
        InvSeeGUI gui = new InvSeeGUI(
            inventoryModule.getSettings(),
            inventoryModule.getDataUtil(),
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
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(prefix)) {
                    if (!player.equals(sender)) {
                        completions.add(player.getName());
                    }
                }
            }
        }
        
        return completions;
    }
}
