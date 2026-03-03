package com.oolonghoo.woomni.task;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.module.Module;

public class AutoSaveTask implements Runnable {
    
    private final WooOmni plugin;
    
    public AutoSaveTask(WooOmni plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        for (Module module : plugin.getModuleManager().getLoadedModules().values()) {
            module.saveAll();
        }
        
        if (plugin.getConfig().getBoolean("settings.debug", false)) {
            plugin.getLogger().info("Auto-saved all module data");
        }
    }
}
