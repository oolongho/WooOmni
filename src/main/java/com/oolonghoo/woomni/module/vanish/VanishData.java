package com.oolonghoo.woomni.module.vanish;

import com.oolonghoo.woomni.user.UserData;

import java.util.UUID;

public class VanishData implements UserData {
    
    private final UUID uuid;
    private boolean vanished;
    private boolean nightVision;
    private boolean pickupItems;
    private boolean canTakeDamage;
    private boolean canDamageOthers;
    private boolean physicalCollision;
    private boolean silentChest;
    private boolean preventMobSpawn;
    private boolean showJoinMessage;
    private boolean showQuitMessage;
    private boolean bossbarEnabled;
    private boolean autoVanishJoin;
    private boolean dirty;
    
    public VanishData(UUID uuid) {
        this.uuid = uuid;
        this.vanished = false;
        this.nightVision = true;
        this.pickupItems = false;
        this.canTakeDamage = false;
        this.canDamageOthers = true;
        this.physicalCollision = false;
        this.silentChest = true;
        this.preventMobSpawn = true;
        this.showJoinMessage = true;
        this.showQuitMessage = true;
        this.bossbarEnabled = true;
        this.autoVanishJoin = false;
        this.dirty = false;
    }
    
    @Override
    public UUID getUuid() {
        return uuid;
    }
    
    public boolean isVanished() {
        return vanished;
    }
    
    public void setVanished(boolean vanished) {
        this.vanished = vanished;
        this.dirty = true;
    }
    
    public boolean hasNightVision() {
        return nightVision;
    }
    
    public void setNightVision(boolean nightVision) {
        this.nightVision = nightVision;
        this.dirty = true;
    }
    
    public boolean canPickupItems() {
        return pickupItems;
    }
    
    public void setPickupItems(boolean pickupItems) {
        this.pickupItems = pickupItems;
        this.dirty = true;
    }
    
    public boolean canTakeDamage() {
        return canTakeDamage;
    }
    
    public void setCanTakeDamage(boolean canTakeDamage) {
        this.canTakeDamage = canTakeDamage;
        this.dirty = true;
    }
    
    public boolean canDamageOthers() {
        return canDamageOthers;
    }
    
    public void setCanDamageOthers(boolean canDamageOthers) {
        this.canDamageOthers = canDamageOthers;
        this.dirty = true;
    }
    
    public boolean hasPhysicalCollision() {
        return physicalCollision;
    }
    
    public void setPhysicalCollision(boolean physicalCollision) {
        this.physicalCollision = physicalCollision;
        this.dirty = true;
    }
    
    public boolean hasSilentChest() {
        return silentChest;
    }
    
    public void setSilentChest(boolean silentChest) {
        this.silentChest = silentChest;
        this.dirty = true;
    }
    
    public boolean shouldPreventMobSpawn() {
        return preventMobSpawn;
    }
    
    public void setPreventMobSpawn(boolean preventMobSpawn) {
        this.preventMobSpawn = preventMobSpawn;
        this.dirty = true;
    }
    
    public boolean shouldShowJoinMessage() {
        return showJoinMessage;
    }
    
    public void setShowJoinMessage(boolean showJoinMessage) {
        this.showJoinMessage = showJoinMessage;
        this.dirty = true;
    }
    
    public boolean shouldShowQuitMessage() {
        return showQuitMessage;
    }
    
    public void setShowQuitMessage(boolean showQuitMessage) {
        this.showQuitMessage = showQuitMessage;
        this.dirty = true;
    }
    
    public boolean isBossbarEnabled() {
        return bossbarEnabled;
    }
    
    public void setBossbarEnabled(boolean bossbarEnabled) {
        this.bossbarEnabled = bossbarEnabled;
        this.dirty = true;
    }
    
    public boolean isAutoVanishJoin() {
        return autoVanishJoin;
    }
    
    public void setAutoVanishJoin(boolean autoVanishJoin) {
        this.autoVanishJoin = autoVanishJoin;
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
