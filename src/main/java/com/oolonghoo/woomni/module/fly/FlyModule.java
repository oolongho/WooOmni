package com.oolonghoo.woomni.module.fly;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.listener.FlyListener;
import com.oolonghoo.woomni.module.Module;

public class FlyModule extends Module {
    
    private FlyDataManager dataManager;
    private FlyListener listener;
    
    public FlyModule(WooOmni plugin) {
        super(plugin, "fly");
    }
    
    @Override
    public void onEnable() {
        dataManager = new FlyDataManager(plugin);
        dataManager.initialize();
        
        listener = new FlyListener(plugin, dataManager);
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        
        log("Fly module enabled");
    }
    
    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.shutdown();
        }
        
        if (listener != null) {
            listener.unregister();
        }
        
        log("Fly module disabled");
    }
    
    @Override
    public void onReload() {
        if (dataManager != null) {
            dataManager.saveAll();
        }
        log("Fly module reloaded");
    }
    
    @Override
    public void saveAll() {
        if (dataManager != null) {
            dataManager.saveAll();
        }
    }
    
    public FlyDataManager getDataManager() {
        return dataManager;
    }
}
