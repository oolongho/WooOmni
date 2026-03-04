package com.oolonghoo.woomni.command;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.config.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainCommand implements CommandExecutor, TabCompleter {
    
    private final WooOmni plugin;
    private final MessageManager msg;
    private final FlyCommand flyCommand;
    private final FlySpeedCommand flySpeedCommand;
    private final GodCommand godCommand;
    private final VanishCommand vanishCommand;
    private final VanishEditCommand vanishEditCommand;
    
    public MainCommand(WooOmni plugin) {
        this.plugin = plugin;
        this.msg = plugin.getMessageManager();
        this.flyCommand = new FlyCommand(plugin);
        this.flySpeedCommand = new FlySpeedCommand(plugin);
        this.godCommand = new GodCommand(plugin);
        this.vanishCommand = new VanishCommand(plugin);
        this.vanishEditCommand = new VanishEditCommand(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                return handleReload(sender, args);
            case "help":
                sendHelp(sender, label);
                return true;
            default:
                sender.sendMessage(msg.getWithPrefix("general.unknown-command"));
                return true;
        }
    }
    
    private boolean handleReload(CommandSender sender, String[] args) {
        if (!sender.hasPermission("wooomni.reload")) {
            sender.sendMessage(msg.getWithPrefix("general.no-permission"));
            return true;
        }
        
        if (args.length > 1) {
            String moduleName = args[1].toLowerCase();
            plugin.reloadModule(moduleName);
            sender.sendMessage(msg.getWithPrefix("general.module-reloaded", "module", moduleName));
        } else {
            plugin.reload();
            sender.sendMessage(msg.getWithPrefix("general.reload-success"));
        }
        
        return true;
    }
    
    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(msg.get("help.header"));
        sender.sendMessage(msg.get("help.fly"));
        sender.sendMessage(msg.get("help.flyspeed"));
        sender.sendMessage(msg.get("help.god"));
        sender.sendMessage(msg.get("help.vanish"));
        if (sender.hasPermission("wooomni.reload")) {
            sender.sendMessage(msg.get("help.reload"));
        }
        sender.sendMessage(msg.get("help.footer"));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>(Arrays.asList("help"));
            if (sender.hasPermission("wooomni.reload")) {
                subCommands.add("reload");
            }
            
            String prefix = args[0].toLowerCase();
            for (String sub : subCommands) {
                if (sub.startsWith(prefix)) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("reload")) {
            String prefix = args[1].toLowerCase();
            for (String moduleName : plugin.getModuleManager().getLoadedModules().keySet()) {
                if (moduleName.startsWith(prefix)) {
                    completions.add(moduleName);
                }
            }
        }
        
        return completions;
    }
    
    public FlyCommand getFlyCommand() {
        return flyCommand;
    }
    
    public FlySpeedCommand getFlySpeedCommand() {
        return flySpeedCommand;
    }
    
    public GodCommand getGodCommand() {
        return godCommand;
    }
    
    public VanishCommand getVanishCommand() {
        return vanishCommand;
    }
    
    public VanishEditCommand getVanishEditCommand() {
        return vanishEditCommand;
    }
}
