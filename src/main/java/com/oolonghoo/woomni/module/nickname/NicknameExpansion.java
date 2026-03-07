package com.oolonghoo.woomni.module.nickname;

import com.oolonghoo.woomni.WooOmni;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NicknameExpansion extends PlaceholderExpansion {
    
    private final WooOmni plugin;
    private final NicknameModule module;
    
    public NicknameExpansion(WooOmni plugin, NicknameModule module) {
        this.plugin = plugin;
        this.module = module;
    }
    
    @Override
    public @NotNull String getIdentifier() {
        return "wooomni";
    }
    
    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }
    
    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null) return "";
        
        if (params.equals("player_name")) {
            String displayName = module.getDisplayName(offlinePlayer.getUniqueId());
            return displayName != null ? displayName : offlinePlayer.getName();
        }
        
        if (params.equals("player_nickname")) {
            String nickname = module.getNickname(offlinePlayer.getUniqueId());
            return nickname != null ? nickname : "";
        }
        
        if (params.equals("player_realname")) {
            return offlinePlayer.getName() != null ? offlinePlayer.getName() : "";
        }
        
        return null;
    }
    
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        return onRequest(player, params);
    }
}
