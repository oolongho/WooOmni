package com.oolonghoo.woomni.module.nickname;

import com.oolonghoo.woomni.user.UserData;

import java.util.UUID;

public class NickData implements UserData {
    
    private final UUID uuid;
    private String nickname;
    private String strippedName;
    private boolean dirty;
    
    public NickData(UUID uuid) {
        this.uuid = uuid;
        this.nickname = null;
        this.strippedName = null;
        this.dirty = false;
    }
    
    @Override
    public UUID getUuid() {
        return uuid;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
        this.strippedName = stripColors(nickname);
        this.dirty = true;
    }
    
    public String getStrippedName() {
        return strippedName;
    }
    
    public boolean hasNickname() {
        return nickname != null && !nickname.isEmpty();
    }
    
    @Override
    public boolean isDirty() {
        return dirty;
    }
    
    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
    
    private String stripColors(String text) {
        if (text == null) return null;
        return text.replaceAll("[&§][0-9a-fk-orA-FK-OR]", "");
    }
}
