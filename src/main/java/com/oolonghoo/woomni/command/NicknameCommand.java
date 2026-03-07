package com.oolonghoo.woomni.command;

import com.oolonghoo.woomni.Perms;
import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.config.MessageManager;
import com.oolonghoo.woomni.module.nickname.NickData;
import com.oolonghoo.woomni.module.nickname.NicknameModule;
import com.oolonghoo.woomni.module.nickname.NicknameSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class NicknameCommand implements CommandExecutor, TabCompleter {
    
    private final WooOmni plugin;
    private final MessageManager msg;
    
    public NicknameCommand(WooOmni plugin) {
        this.plugin = plugin;
        this.msg = plugin.getMessageManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getModuleManager().isModuleLoaded("nickname")) {
            msg.send(sender, "general.module-not-found", "module", "nickname");
            return true;
        }
        
        NicknameModule module = (NicknameModule) plugin.getModuleManager().getModule("nickname");
        
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                msg.send(sender, "general.player-only");
                return true;
            }
            
            if (!sender.hasPermission(Perms.Nickname.USE)) {
                msg.send(sender, "general.no-permission");
                return true;
            }
            
            module.openAnvilGUI((Player) sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "set":
                return handleSet(sender, args, module);
            case "clear":
                return handleClear(sender, args, module);
            case "check":
                return handleCheck(sender, args, module);
            default:
                msg.send(sender, "nickname.usage");
                return true;
        }
    }
    
    private boolean handleSet(CommandSender sender, String[] args, NicknameModule module) {
        if (args.length < 2) {
            msg.send(sender, "nickname.usage-set");
            return true;
        }
        
        String nickname;
        Player target;
        Player initiator;
        boolean isSettingOthers = false;
        
        if (args.length >= 3 && sender.hasPermission(Perms.Nickname.SET_OTHERS)) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                msg.send(sender, "general.player-not-found", "player", args[1]);
                return true;
            }
            nickname = args[2];
            initiator = (sender instanceof Player) ? (Player) sender : null;
            isSettingOthers = true;
        } else if (sender instanceof Player) {
            target = (Player) sender;
            nickname = args[1];
            initiator = target;
            
            if (!sender.hasPermission(Perms.Nickname.SET)) {
                msg.send(sender, "general.no-permission");
                return true;
            }
        } else {
            msg.send(sender, "general.player-only");
            return true;
        }
        
        String validationResult = validateNickname(nickname, target, module);
        if (validationResult != null) {
            msg.send(sender, validationResult);
            return true;
        }
        
        if (!isSettingOthers && module.isEconomyEnabled()) {
            double cost = module.getSetCost(target);
            if (cost > 0) {
                if (!module.canAfford(target)) {
                    msg.send(sender, "nickname.not-enough-money", "cost", module.formatCost(cost));
                    return true;
                }
                
                msg.send(sender, "nickname.cost-confirm", "cost", module.formatCost(cost), "nickname", nickname);
            }
        }
        
        String processedNick = processNickname(nickname, target, module);
        
        if (!isSettingOthers && module.isEconomyEnabled()) {
            double cost = module.getSetCost(target);
            if (cost > 0 && !module.chargePlayer(target)) {
                msg.send(sender, "nickname.payment-failed");
                return true;
            }
        }
        
        module.setNickname(target, processedNick, initiator);
        
        if (sender.equals(target)) {
            msg.send(sender, "nickname.set-self", "nickname", processedNick);
        } else {
            msg.send(sender, "nickname.set", "player", target.getName(), "nickname", processedNick);
            msg.send(target, "nickname.set-self", "nickname", processedNick);
        }
        
        return true;
    }
    
    private boolean handleClear(CommandSender sender, String[] args, NicknameModule module) {
        Player target;
        Player initiator;
        
        if (args.length >= 2 && sender.hasPermission(Perms.Nickname.CLEAR_OTHERS)) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                msg.send(sender, "general.player-not-found", "player", args[1]);
                return true;
            }
            initiator = (sender instanceof Player) ? (Player) sender : null;
        } else if (sender instanceof Player) {
            target = (Player) sender;
            initiator = target;
            
            if (!sender.hasPermission(Perms.Nickname.CLEAR)) {
                msg.send(sender, "general.no-permission");
                return true;
            }
        } else {
            msg.send(sender, "general.player-only");
            return true;
        }
        
        NickData data = module.getDataManager().getNickData(target.getUniqueId());
        if (!data.hasNickname()) {
            msg.send(sender, "nickname.no-nickname", "player", target.getName());
            return true;
        }
        
        module.clearNickname(target, initiator);
        
        if (sender.equals(target)) {
            msg.send(sender, "nickname.cleared-self");
        } else {
            msg.send(sender, "nickname.cleared", "player", target.getName());
            msg.send(target, "nickname.cleared-self");
        }
        
        return true;
    }
    
    private boolean handleCheck(CommandSender sender, String[] args, NicknameModule module) {
        Player target;
        
        if (args.length >= 2 && sender.hasPermission(Perms.Nickname.CHECK_OTHERS)) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                msg.send(sender, "general.player-not-found", "player", args[1]);
                return true;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
            
            if (!sender.hasPermission(Perms.Nickname.CHECK)) {
                msg.send(sender, "general.no-permission");
                return true;
            }
        } else {
            msg.send(sender, "general.player-only");
            return true;
        }
        
        String nickname = module.getNickname(target.getUniqueId());
        if (nickname == null || nickname.isEmpty()) {
            msg.send(sender, "nickname.no-nickname", "player", target.getName());
        } else {
            if (sender.equals(target)) {
                msg.send(sender, "nickname.check-self", "realname", target.getName(), "nickname", nickname);
            } else {
                msg.send(sender, "nickname.check", "player", target.getName(), "realname", target.getName(), "nickname", nickname);
            }
        }
        
        return true;
    }
    
    private String validateNickname(String nickname, Player player, NicknameModule module) {
        NicknameSettings settings = module.getSettings();
        
        String stripped = ChatColor.stripColor(nickname.replace("&", "§"));
        
        if (!player.hasPermission(Perms.Nickname.BYPASS_LENGTH)) {
            if (stripped.length() < settings.getMinLength()) {
                return msg.get("nickname.invalid-length", "min", String.valueOf(settings.getMinLength()), "max", String.valueOf(settings.getMaxLength()));
            }
            if (stripped.length() > settings.getMaxLength()) {
                return msg.get("nickname.invalid-length", "min", String.valueOf(settings.getMinLength()), "max", String.valueOf(settings.getMaxLength()));
            }
        }
        
        if (!player.hasPermission(Perms.Nickname.BYPASS_REGEX)) {
            Pattern regex = settings.getNickRegex();
            if (!regex.matcher(stripped).matches()) {
                return msg.get("nickname.invalid-chars");
            }
        }
        
        if (!player.hasPermission(Perms.Nickname.BYPASS_BLACKLIST)) {
            if (settings.isBlacklisted(nickname)) {
                return msg.get("nickname.blacklisted");
            }
        }
        
        String lowerNick = stripped.toLowerCase();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player)) continue;
            
            String onlineNick = module.getNickname(online.getUniqueId());
            if (onlineNick != null) {
                String strippedOnline = ChatColor.stripColor(onlineNick.replace("&", "§"));
                if (lowerNick.equals(strippedOnline.toLowerCase())) {
                    return msg.get("nickname.already-used");
                }
            }
            
            if (lowerNick.equals(online.getName().toLowerCase())) {
                return msg.get("nickname.already-used");
            }
        }
        
        return null;
    }
    
    private String processNickname(String nickname, Player player, NicknameModule module) {
        NicknameSettings settings = module.getSettings();
        
        if (settings.isAllowColors()) {
            if (settings.isColorPermission() && !player.hasPermission(Perms.Nickname.COLORS)) {
                return ChatColor.stripColor(nickname.replace("&", "§"));
            }
            return nickname.replace("&", "§");
        }
        
        return ChatColor.stripColor(nickname.replace("&", "§"));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            for (String sub : new String[]{"set", "clear", "check"}) {
                if (sub.startsWith(prefix)) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            String prefix = args[1].toLowerCase();
            
            if (subCommand.equals("set") && sender.hasPermission(Perms.Nickname.SET_OTHERS)) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(prefix)) {
                        completions.add(player.getName());
                    }
                }
            } else if ((subCommand.equals("clear") && sender.hasPermission(Perms.Nickname.CLEAR_OTHERS)) ||
                       (subCommand.equals("check") && sender.hasPermission(Perms.Nickname.CHECK_OTHERS))) {
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
