package com.oolonghoo.woomni.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * 飞行状态变更事件
 */
public class FlyStatusChangeEvent extends Event implements Cancellable {
    
    private static final HandlerList HANDLERS = new HandlerList();
    
    private final UUID playerUUID;
    private final String playerName;
    private final boolean oldState;
    private final boolean newState;
    private final Player initiator;
    private boolean cancelled = false;
    
    public FlyStatusChangeEvent(UUID playerUUID, String playerName, boolean oldState, boolean newState, Player initiator) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.oldState = oldState;
        this.newState = newState;
        this.initiator = initiator;
    }
    
    public UUID getPlayerUUID() {
        return playerUUID;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public boolean getOldState() {
        return oldState;
    }
    
    public boolean getNewState() {
        return newState;
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
