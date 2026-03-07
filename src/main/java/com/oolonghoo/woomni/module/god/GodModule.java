package com.oolonghoo.woomni.module.god;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.listener.GodListener;
import com.oolonghoo.woomni.module.Module;

public class GodModule extends Module {
    
    private GodDataManager dataManager;
    private GodListener listener;
    
    public GodModule(WooOmni plugin) {
        super(plugin, "god");
    }
    
    @Override
    public void onEnable() {
        dataManager = new GodDataManager(plugin);
        dataManager.initialize();
        
        listener = new GodListener(plugin, dataManager);
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }
    
    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.shutdown();
        }
        
        if (listener != null) {
            listener.unregister();
        }
    }
    
    @Override
    public void onReload() {
        if (dataManager != null) {
            dataManager.saveAll();
        }
    }
    
    @Override
    public void saveAll() {
        if (dataManager != null) {
            dataManager.saveAll();
        }
    }
    
    public GodDataManager getDataManager() {
        return dataManager;
    }
    
    public GodListener getListener() {
        return listener;
    }
}
