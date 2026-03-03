package com.oolonghoo.woomni.module.god;

import com.oolonghoo.woomni.user.UserData;

import java.util.UUID;

public class GodData implements UserData {
    
    private final UUID uuid;
    private boolean godMode;
    private boolean dirty;
    
    public GodData(UUID uuid) {
        this.uuid = uuid;
        this.godMode = false;
        this.dirty = false;
    }
    
    @Override
    public UUID getUuid() {
        return uuid;
    }
    
    public boolean isGodMode() {
        return godMode;
    }
    
    public void setGodMode(boolean godMode) {
        this.godMode = godMode;
        this.dirty = true;
    }
    
    @Override
    public boolean isDirty() {
        return dirty;
    }
    
    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
