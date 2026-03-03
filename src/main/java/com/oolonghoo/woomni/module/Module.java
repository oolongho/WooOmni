package com.oolonghoo.woomni.module;

import com.oolonghoo.woomni.WooOmni;

public abstract class Module {
    
    protected final WooOmni plugin;
    protected final String name;
    
    public Module(WooOmni plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }
    
    public abstract void onEnable();
    
    public abstract void onDisable();
    
    public abstract void onReload();
    
    public void saveAll() {
    }
    
    public String getName() {
        return name;
    }
    
    protected void log(String message) {
        plugin.getLogger().info("[" + name + "] " + message);
    }
    
    protected void logWarning(String message) {
        plugin.getLogger().warning("[" + name + "] " + message);
    }
}
