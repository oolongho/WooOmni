package com.oolonghoo.woomni.command;

import com.oolonghoo.woomni.Perms;
import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.config.MessageManager;
import com.oolonghoo.woomni.listener.VanishGUIListener;
import com.oolonghoo.woomni.module.vanish.VanishModule;
import com.oolonghoo.woomni.module.vanish.gui.VanishEditGUI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class VanishEditCommand implements CommandExecutor, TabCompleter {
    
    private final WooOmni plugin;
    private final MessageManager msg;
    private VanishGUIListener guiListener;
    
    public VanishEditCommand(WooOmni plugin) {
        this.plugin = plugin;
        this.msg = plugin.getMessageManager();
    }
    
    public void setGuiListener(VanishGUIListener guiListener) {
        this.guiListener = guiListener;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getModuleManager().isModuleLoaded("vanish")) {
            sender.sendMessage(msg.getWithPrefix("general.module-not-found", "module", "vanish"));
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(msg.getWithPrefix("general.player-only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission(Perms.Vanish.EDIT)) {
            sender.sendMessage(msg.getWithPrefix("general.no-permission"));
            return true;
        }
        
        Player target;
        
        if (args.length > 0) {
            if (!player.hasPermission(Perms.Vanish.EDIT_OTHERS)) {
                sender.sendMessage(msg.getWithPrefix("general.no-permission"));
                return true;
            }
            
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(msg.getWithPrefix("general.player-not-found", "player", args[0]));
                return true;
            }
        } else {
            target = player;
        }
        
        openEditGUI(player, target);
        return true;
    }
    
    private void openEditGUI(Player viewer, Player target) {
        VanishModule vanishModule = (VanishModule) plugin.getModuleManager().getModule("vanish");
        
        VanishEditGUI gui = new VanishEditGUI(
            vanishModule.getDataManager(),
            vanishModule.getHider(),
            vanishModule.getBossBar(),
            target
        );
        
        if (guiListener != null) {
            guiListener.registerGUI(viewer, gui);
        }
        
        viewer.openInventory(gui.getInventory());
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1 && sender.hasPermission(Perms.Vanish.EDIT_OTHERS)) {
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
