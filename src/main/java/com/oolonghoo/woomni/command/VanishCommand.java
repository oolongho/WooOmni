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

/**
 * Vanish命令处理器
 * 处理 /vanish 命令
 */
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
        
        // 处理子命令
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "list":
                return handleList(sender);
            case "help":
                sendHelp(sender, label);
                return true;
            default:
                // 尝试作为玩家名处理
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
    }
    
    /**
     * 切换玩家隐身状态
     */
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
    
    /**
     * 处理list子命令
     */
    private boolean handleList(CommandSender sender) {
        if (!sender.hasPermission(Perms.Vanish.LIST)) {
            sender.sendMessage(msg.getWithPrefix("general.no-permission"));
            return true;
        }
        
        VanishModule vanishModule = (VanishModule) plugin.getModuleManager().getModule("vanish");
        List<String> vanishedPlayers = new ArrayList<>();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (vanishModule.isVanished(player)) {
                vanishedPlayers.add(player.getName());
            }
        }
        
        if (vanishedPlayers.isEmpty()) {
            sender.sendMessage(msg.getWithPrefix("vanish.list-empty"));
        } else {
            sender.sendMessage(msg.getWithPrefix("vanish.list", 
                "count", String.valueOf(vanishedPlayers.size()),
                "players", String.join(", ", vanishedPlayers)
            ));
        }
        
        return true;
    }
    
    /**
     * 发送帮助信息
     */
    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(msg.get("vanish.help.header"));
        sender.sendMessage(msg.get("vanish.help.toggle", "command", label));
        
        if (sender.hasPermission(Perms.Vanish.OTHERS)) {
            sender.sendMessage(msg.get("vanish.help.others", "command", label));
        }
        
        if (sender.hasPermission(Perms.Vanish.LIST)) {
            sender.sendMessage(msg.get("vanish.help.list", "command", label));
        }
        
        sender.sendMessage(msg.get("vanish.help.footer"));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            
            // 子命令补全
            if ("list".startsWith(prefix) && sender.hasPermission(Perms.Vanish.LIST)) {
                completions.add("list");
            }
            if ("help".startsWith(prefix)) {
                completions.add("help");
            }
            
            // 玩家名补全
            if (sender.hasPermission(Perms.Vanish.OTHERS)) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(prefix)) {
                        completions.add(player.getName());
                    }
                }
            }
        }
        
        return completions;
    }
}
