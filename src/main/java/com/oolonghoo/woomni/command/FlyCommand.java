package com.oolonghoo.woomni.command;

import com.oolonghoo.woomni.Perms;
import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.config.MessageManager;
import com.oolonghoo.woomni.event.FlyStatusChangeEvent;
import com.oolonghoo.woomni.module.fly.FlyData;
import com.oolonghoo.woomni.module.fly.FlyDataManager;
import com.oolonghoo.woomni.module.fly.FlyModule;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FlyCommand implements CommandExecutor, TabCompleter {
    
    private final WooOmni plugin;
    private final MessageManager msg;
    
    public FlyCommand(WooOmni plugin) {
        this.plugin = plugin;
        this.msg = plugin.getMessageManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getModuleManager().isModuleLoaded("fly")) {
            if (!sender.hasPermission(Perms.Fly.BYPASS_DISABLED)) {
                msg.send(sender, "general.module-not-found", "module", "fly");
                return true;
            }
            return handleBasicFly(sender, args);
        }
        
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                msg.send(sender, "general.player-only");
                return true;
            }
            
            if (!sender.hasPermission(Perms.Fly.USE)) {
                msg.send(sender, "general.no-permission");
                return true;
            }
            
            toggleFly((Player) sender, sender);
            return true;
        }
        
        if (!sender.hasPermission(Perms.Fly.OTHERS)) {
            msg.send(sender, "general.no-permission");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            msg.send(sender, "general.player-not-found", "player", args[0]);
            return true;
        }
        
        toggleFly(target, sender);
        return true;
    }
    
    private boolean handleBasicFly(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                msg.send(sender, "general.player-only");
                return true;
            }
            
            Player player = (Player) sender;
            boolean oldState = player.getAllowFlight();
            boolean newState = !oldState;
            
            FlyStatusChangeEvent event = new FlyStatusChangeEvent(
                player.getUniqueId(), player.getName(), oldState, newState, player);
            Bukkit.getPluginManager().callEvent(event);
            
            if (event.isCancelled()) {
                return true;
            }
            
            player.setAllowFlight(newState);
            if (newState) {
                player.setFlying(true);
                msg.send(sender, "fly.enabled-self");
            } else {
                player.setFlying(false);
                msg.send(sender, "fly.disabled-self");
            }
            return true;
        }
        
        if (!sender.hasPermission(Perms.Fly.OTHERS)) {
            msg.send(sender, "general.no-permission");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            msg.send(sender, "general.player-not-found", "player", args[0]);
            return true;
        }
        
        Player initiator = (sender instanceof Player) ? (Player) sender : null;
        boolean oldState = target.getAllowFlight();
        boolean newState = !oldState;
        
        FlyStatusChangeEvent event = new FlyStatusChangeEvent(
            target.getUniqueId(), target.getName(), oldState, newState, initiator);
        Bukkit.getPluginManager().callEvent(event);
        
        if (event.isCancelled()) {
            return true;
        }
        
        target.setAllowFlight(newState);
        if (newState) {
            target.setFlying(true);
            msg.send(sender, "fly.enabled", "player", target.getName());
            msg.send(target, "fly.enabled-self");
        } else {
            target.setFlying(false);
            msg.send(sender, "fly.disabled", "player", target.getName());
            msg.send(target, "fly.disabled-self");
        }
        return true;
    }
    
    private void toggleFly(Player player, CommandSender sender) {
        FlyModule flyModule = (FlyModule) plugin.getModuleManager().getModule("fly");
        FlyDataManager dataManager = flyModule.getDataManager();
        FlyData data = dataManager.getFlyData(player.getUniqueId());
        
        boolean oldState = player.getAllowFlight();
        boolean newState = !oldState;
        
        Player initiator = (sender instanceof Player) ? (Player) sender : null;
        FlyStatusChangeEvent event = new FlyStatusChangeEvent(
            player.getUniqueId(), player.getName(), oldState, newState, initiator);
        Bukkit.getPluginManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        player.setAllowFlight(newState);
        
        if (newState) {
            player.setFlying(true);
            data.setFlying(true);
        } else {
            player.setFlying(false);
            data.setFlying(false);
        }
        
        dataManager.saveFlyData(player.getUniqueId());
        
        if (sender.equals(player)) {
            if (newState) {
                msg.send(sender, "fly.enabled-self");
            } else {
                msg.send(sender, "fly.disabled-self");
            }
        } else {
            if (newState) {
                msg.send(sender, "fly.enabled", "player", player.getName());
                msg.send(player, "fly.enabled-self");
            } else {
                msg.send(sender, "fly.disabled", "player", player.getName());
                msg.send(player, "fly.disabled-self");
            }
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1 && sender.hasPermission(Perms.Fly.OTHERS)) {
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
