package com.oolonghoo.woomni.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * 昵称变更事件
 */
public class NicknameChangeEvent extends Event implements Cancellable {
    
    private static final HandlerList HANDLERS = new HandlerList();
    
    private final UUID playerUUID;
    private final String playerName;
    private final String oldNickname;
    private final String newNickname;
    private final Player initiator;
    private boolean cancelled = false;
    
    public NicknameChangeEvent(UUID playerUUID, String playerName, String oldNickname, String newNickname, Player initiator) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.oldNickname = oldNickname;
        this.newNickname = newNickname;
        this.initiator = initiator;
    }
    
    public UUID getPlayerUUID() {
        return playerUUID;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public String getOldNickname() {
        return oldNickname;
    }
    
    public String getNewNickname() {
        return newNickname;
    }
    
    public Player getInitiator() {
        return initiator;
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
    
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
