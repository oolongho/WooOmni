package com.oolonghoo.woomni.command;

import com.oolonghoo.woomni.Perms;
import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.config.MessageManager;
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
            // 检查 bypass 权限
            if (!sender.hasPermission(Perms.Fly.BYPASS_DISABLED)) {
                sender.sendMessage(msg.getWithPrefix("general.module-not-found", "module", "fly"));
                return true;
            }
            // 有 bypass 权限，允许使用基本功能
            return handleBasicFly(sender, args);
        }
        
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(msg.getWithPrefix("general.player-only"));
                return true;
            }
            
            if (!sender.hasPermission(Perms.Fly.USE)) {
                sender.sendMessage(msg.getWithPrefix("general.no-permission"));
                return true;
            }
            
            toggleFly((Player) sender, sender);
            return true;
        }
        
        if (!sender.hasPermission(Perms.Fly.OTHERS)) {
            sender.sendMessage(msg.getWithPrefix("general.no-permission"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(msg.getWithPrefix("general.player-not-found", "player", args[0]));
            return true;
        }
        
        toggleFly(target, sender);
        return true;
    }
    
    /**
     * 处理模块未加载时的基本飞行功能（需要 bypass 权限）
     */
    private boolean handleBasicFly(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(msg.getWithPrefix("general.player-only"));
                return true;
            }
            
            Player player = (Player) sender;
            boolean newState = !player.getAllowFlight();
            player.setAllowFlight(newState);
            if (newState) {
                player.setFlying(true);
                sender.sendMessage(msg.getWithPrefix("fly.enabled-self"));
            } else {
                player.setFlying(false);
                sender.sendMessage(msg.getWithPrefix("fly.disabled-self"));
            }
            return true;
        }
        
        if (!sender.hasPermission(Perms.Fly.OTHERS)) {
            sender.sendMessage(msg.getWithPrefix("general.no-permission"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(msg.getWithPrefix("general.player-not-found", "player", args[0]));
            return true;
        }
        
        boolean newState = !target.getAllowFlight();
        target.setAllowFlight(newState);
        if (newState) {
            target.setFlying(true);
            sender.sendMessage(msg.getWithPrefix("fly.enabled", "player", target.getName()));
            target.sendMessage(msg.getWithPrefix("fly.enabled-self"));
        } else {
            target.setFlying(false);
            sender.sendMessage(msg.getWithPrefix("fly.disabled", "player", target.getName()));
            target.sendMessage(msg.getWithPrefix("fly.disabled-self"));
        }
        return true;
    }
    
    private void toggleFly(Player player, CommandSender sender) {
        FlyModule flyModule = (FlyModule) plugin.getModuleManager().getModule("fly");
        FlyDataManager dataManager = flyModule.getDataManager();
        FlyData data = dataManager.getFlyData(player.getUniqueId());
        
        boolean newState = !player.getAllowFlight();
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
                sender.sendMessage(msg.getWithPrefix("fly.enabled-self"));
            } else {
                sender.sendMessage(msg.getWithPrefix("fly.disabled-self"));
            }
        } else {
            if (newState) {
                sender.sendMessage(msg.getWithPrefix("fly.enabled", "player", player.getName()));
                player.sendMessage(msg.getWithPrefix("fly.enabled-self"));
            } else {
                sender.sendMessage(msg.getWithPrefix("fly.disabled", "player", player.getName()));
                player.sendMessage(msg.getWithPrefix("fly.disabled-self"));
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
