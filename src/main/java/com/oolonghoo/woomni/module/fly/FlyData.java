package com.oolonghoo.woomni.module.fly;

import com.oolonghoo.woomni.user.UserData;

import java.util.UUID;

public class FlyData implements UserData {
    
    private final UUID uuid;
    private boolean flying;
    private float flySpeed;
    private boolean dirty;
    
    public FlyData(UUID uuid) {
        this.uuid = uuid;
        this.flying = false;
        this.flySpeed = 0.1f;
        this.dirty = false;
    }
    
    @Override
    public UUID getUuid() {
        return uuid;
    }
    
    public boolean isFlying() {
        return flying;
    }
    
    public void setFlying(boolean flying) {
        this.flying = flying;
        this.dirty = true;
    }
    
    public float getFlySpeed() {
        return flySpeed;
    }
    
    public void setFlySpeed(float flySpeed) {
        this.flySpeed = flySpeed;
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
