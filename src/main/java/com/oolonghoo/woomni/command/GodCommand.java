package com.oolonghoo.woomni.command;

import com.oolonghoo.woomni.Perms;
import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.config.MessageManager;
import com.oolonghoo.woomni.event.GodStatusChangeEvent;
import com.oolonghoo.woomni.module.god.GodData;
import com.oolonghoo.woomni.module.god.GodDataManager;
import com.oolonghoo.woomni.module.god.GodModule;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GodCommand implements CommandExecutor, TabCompleter {
    
    private final WooOmni plugin;
    private final MessageManager msg;
    
    public GodCommand(WooOmni plugin) {
        this.plugin = plugin;
        this.msg = plugin.getMessageManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getModuleManager().isModuleLoaded("god")) {
            if (!sender.hasPermission(Perms.God.BYPASS_DISABLED)) {
                sender.sendMessage(msg.getWithPrefix("general.module-not-found", "module", "god"));
                return true;
            }
            return handleBasicGod(sender, args);
        }
        
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(msg.getWithPrefix("general.player-only"));
                return true;
            }
            
            if (!sender.hasPermission(Perms.God.USE)) {
                sender.sendMessage(msg.getWithPrefix("general.no-permission"));
                return true;
            }
            
            toggleGod((Player) sender, sender);
            return true;
        }
        
        if (!sender.hasPermission(Perms.God.OTHERS)) {
            sender.sendMessage(msg.getWithPrefix("general.no-permission"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(msg.getWithPrefix("general.player-not-found", "player", args[0]));
            return true;
        }
        
        toggleGod(target, sender);
        return true;
    }
    
    private boolean handleBasicGod(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(msg.getWithPrefix("general.player-only"));
                return true;
            }
            
            Player player = (Player) sender;
            boolean oldState = player.isInvulnerable();
            boolean newState = !oldState;
            
            Player initiator = player;
            GodStatusChangeEvent event = new GodStatusChangeEvent(
                player.getUniqueId(), player.getName(), oldState, newState, initiator);
            Bukkit.getPluginManager().callEvent(event);
            
            if (event.isCancelled()) {
                return true;
            }
            
            player.setInvulnerable(newState);
            if (newState) {
                sender.sendMessage(msg.getWithPrefix("god.enabled-self"));
            } else {
                sender.sendMessage(msg.getWithPrefix("god.disabled-self"));
            }
            return true;
        }
        
        if (!sender.hasPermission(Perms.God.OTHERS)) {
            sender.sendMessage(msg.getWithPrefix("general.no-permission"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(msg.getWithPrefix("general.player-not-found", "player", args[0]));
            return true;
        }
        
        boolean oldState = target.isInvulnerable();
        boolean newState = !oldState;
        
        Player initiator = (sender instanceof Player) ? (Player) sender : null;
        GodStatusChangeEvent event = new GodStatusChangeEvent(
            target.getUniqueId(), target.getName(), oldState, newState, initiator);
        Bukkit.getPluginManager().callEvent(event);
        
        if (event.isCancelled()) {
            return true;
        }
        
        target.setInvulnerable(newState);
        if (newState) {
            sender.sendMessage(msg.getWithPrefix("god.enabled", "player", target.getName()));
            target.sendMessage(msg.getWithPrefix("god.enabled-self"));
        } else {
            sender.sendMessage(msg.getWithPrefix("god.disabled", "player", target.getName()));
            target.sendMessage(msg.getWithPrefix("god.disabled-self"));
        }
        return true;
    }
    
    private void toggleGod(Player player, CommandSender sender) {
        GodModule godModule = (GodModule) plugin.getModuleManager().getModule("god");
        GodDataManager dataManager = godModule.getDataManager();
        GodData data = dataManager.getGodData(player.getUniqueId());
        
        boolean oldState = player.isInvulnerable();
        boolean newState = !oldState;
        
        Player initiator = (sender instanceof Player) ? (Player) sender : null;
        GodStatusChangeEvent event = new GodStatusChangeEvent(
            player.getUniqueId(), player.getName(), oldState, newState, initiator);
        Bukkit.getPluginManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        player.setInvulnerable(newState);
        data.setGodMode(newState);
        dataManager.saveGodData(player.getUniqueId());
        
        if (sender.equals(player)) {
            if (newState) {
                sender.sendMessage(msg.getWithPrefix("god.enabled-self"));
            } else {
                sender.sendMessage(msg.getWithPrefix("god.disabled-self"));
            }
        } else {
            if (newState) {
                sender.sendMessage(msg.getWithPrefix("god.enabled", "player", player.getName()));
                player.sendMessage(msg.getWithPrefix("god.enabled-self"));
            } else {
                sender.sendMessage(msg.getWithPrefix("god.disabled", "player", player.getName()));
                player.sendMessage(msg.getWithPrefix("god.disabled-self"));
            }
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1 && sender.hasPermission(Perms.God.OTHERS)) {
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
