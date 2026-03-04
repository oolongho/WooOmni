package com.oolonghoo.woomni.command;

import com.oolonghoo.woomni.Perms;
import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.config.MessageManager;
import com.oolonghoo.woomni.module.vanish.VanishModule;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class VanishCommand implements CommandExecutor, TabCompleter {
    
    private final WooOmni plugin;
    private final MessageManager msg;
    
    public VanishCommand(WooOmni plugin) {
        this.plugin = plugin;
        this.msg = plugin.getMessageManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getModuleManager().isModuleLoaded("vanish")) {
            sender.sendMessage(msg.getWithPrefix("general.module-not-found", "module", "vanish"));
            return true;
        }
        
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(msg.getWithPrefix("general.player-only"));
                return true;
            }
            
            if (!sender.hasPermission(Perms.Vanish.USE)) {
                sender.sendMessage(msg.getWithPrefix("general.no-permission"));
                return true;
            }
            
            toggleVanish((Player) sender, sender);
            return true;
        }
        
        // 处理玩家名参数
        if (!sender.hasPermission(Perms.Vanish.OTHERS)) {
            sender.sendMessage(msg.getWithPrefix("general.no-permission"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(msg.getWithPrefix("general.player-not-found", "player", args[0]));
            return true;
        }
        
        toggleVanish(target, sender);
        return true;
    }
    
    private void toggleVanish(Player player, CommandSender sender) {
        VanishModule vanishModule = (VanishModule) plugin.getModuleManager().getModule("vanish");
        boolean newState = vanishModule.toggleVanish(player);
        
        if (sender.equals(player)) {
            if (newState) {
                sender.sendMessage(msg.getWithPrefix("vanish.enabled-self"));
            } else {
                sender.sendMessage(msg.getWithPrefix("vanish.disabled-self"));
            }
        } else {
            if (newState) {
                sender.sendMessage(msg.getWithPrefix("vanish.enabled", "player", player.getName()));
                player.sendMessage(msg.getWithPrefix("vanish.enabled-self"));
            } else {
                sender.sendMessage(msg.getWithPrefix("vanish.disabled", "player", player.getName()));
                player.sendMessage(msg.getWithPrefix("vanish.disabled-self"));
            }
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1 && sender.hasPermission(Perms.Vanish.OTHERS)) {
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
