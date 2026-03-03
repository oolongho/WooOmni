package com.oolonghoo.woomni.user;

import java.util.UUID;

public interface UserData {
    
    UUID getUuid();
    
    boolean isDirty();
    
    void setDirty(boolean dirty);
}
