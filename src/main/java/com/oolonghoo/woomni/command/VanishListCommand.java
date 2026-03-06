package com.oolonghoo.woomni.command;

import com.oolonghoo.woomni.Perms;
import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.config.MessageManager;
import com.oolonghoo.woomni.module.vanish.VanishHider;
import com.oolonghoo.woomni.module.vanish.VanishModule;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class VanishListCommand implements CommandExecutor, TabCompleter {
    
    private final WooOmni plugin;
    private final MessageManager msg;
    
    public VanishListCommand(WooOmni plugin) {
        this.plugin = plugin;
        this.msg = plugin.getMessageManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getModuleManager().isModuleLoaded("vanish")) {
            msg.send(sender, "general.module-not-found", "module", "vanish");
            return true;
        }
        
        if (!sender.hasPermission(Perms.Vanish.LIST)) {
            msg.send(sender, "general.no-permission");
            return true;
        }
        
        VanishModule vanishModule = (VanishModule) plugin.getModuleManager().getModule("vanish");
        VanishHider hider = vanishModule.getHider();
        
        List<String> vanishedNames = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (hider.isVanished(player.getUniqueId())) {
                vanishedNames.add(player.getName());
            }
        }
        
        if (vanishedNames.isEmpty()) {
            msg.send(sender, "vanish.list-empty");
        } else {
            String players = String.join(", ", vanishedNames);
            msg.send(sender, "vanish.list", 
                "count", String.valueOf(vanishedNames.size()),
                "players", players);
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
