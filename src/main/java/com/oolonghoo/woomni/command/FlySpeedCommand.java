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

public class FlySpeedCommand implements CommandExecutor, TabCompleter {
    
    private final WooOmni plugin;
    private final MessageManager msg;
    
    private static final float MIN_SPEED = 0.1f;
    private static final float MAX_SPEED = 10.0f;
    
    public FlySpeedCommand(WooOmni plugin) {
        this.plugin = plugin;
        this.msg = plugin.getMessageManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getModuleManager().isModuleLoaded("fly")) {
            sender.sendMessage(msg.getWithPrefix("general.module-not-found", "module", "fly"));
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(msg.getWithPrefix("help.flyspeed"));
            return true;
        }
        
        float speed;
        try {
            speed = Float.parseFloat(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(msg.getWithPrefix("fly.speed-invalid"));
            return true;
        }
        
        if (speed < MIN_SPEED || speed > MAX_SPEED) {
            sender.sendMessage(msg.getWithPrefix("fly.speed-invalid"));
            return true;
        }
        
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(msg.getWithPrefix("general.player-only"));
                return true;
            }
            
            if (!sender.hasPermission(Perms.Fly.SPEED)) {
                sender.sendMessage(msg.getWithPrefix("general.no-permission"));
                return true;
            }
            
            setFlySpeed((Player) sender, speed, sender);
            return true;
        }
        
        if (!sender.hasPermission(Perms.Fly.OTHERS)) {
            sender.sendMessage(msg.getWithPrefix("general.no-permission"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(msg.getWithPrefix("general.player-not-found", "player", args[1]));
            return true;
        }
        
        setFlySpeed(target, speed, sender);
        return true;
    }
    
    private void setFlySpeed(Player player, float speed, CommandSender sender) {
        FlyModule flyModule = (FlyModule) plugin.getModuleManager().getModule("fly");
        FlyDataManager dataManager = flyModule.getDataManager();
        FlyData data = dataManager.getFlyData(player.getUniqueId());
        
        float flySpeed = speed / 10.0f;
        player.setFlySpeed(flySpeed);
        data.setFlySpeed(flySpeed);
        dataManager.saveFlyData(player.getUniqueId());
        
        String speedStr = String.format("%.1f", speed);
        if (sender.equals(player)) {
            sender.sendMessage(msg.getWithPrefix("fly.speed-set-self", "speed", speedStr));
        } else {
            sender.sendMessage(msg.getWithPrefix("fly.speed-set", "player", player.getName(), "speed", speedStr));
            player.sendMessage(msg.getWithPrefix("fly.speed-set-self", "speed", speedStr));
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // 不再补全速度数值，让玩家自己输入
        if (args.length == 2 && sender.hasPermission(Perms.Fly.OTHERS)) {
            String prefix = args[1].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(prefix)) {
                    completions.add(player.getName());
                }
            }
        }
        
        return completions;
    }
}
