package com.oolonghoo.woomni.module.inventory;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.listener.EnderSeeListener;
import com.oolonghoo.woomni.listener.InvSeeListener;
import com.oolonghoo.woomni.module.Module;

public class InventoryModule extends Module {
    
    private InventorySettings settings;
    private OfflinePlayerDataUtil dataUtil;
    private InvSeeListener invSeeListener;
    private EnderSeeListener enderSeeListener;
    
    public InventoryModule(WooOmni plugin) {
        super(plugin, "inventory");
    }
    
    @Override
    public void onEnable() {
        settings = new InventorySettings(plugin);
        settings.initialize();
        
        dataUtil = new OfflinePlayerDataUtil(plugin);
        
        invSeeListener = new InvSeeListener(plugin, dataUtil);
        enderSeeListener = new EnderSeeListener(plugin, dataUtil);
        plugin.getServer().getPluginManager().registerEvents(invSeeListener, plugin);
        plugin.getServer().getPluginManager().registerEvents(enderSeeListener, plugin);
    }
    
    @Override
    public void onDisable() {
        saveAll();
        
        if (invSeeListener != null) {
            invSeeListener.clearAll();
        }
        if (enderSeeListener != null) {
            enderSeeListener.clearAll();
        }
    }
    
    @Override
    public void onReload() {
        saveAll();
        
        if (settings != null) {
            settings.reload();
        }
    }
    
    @Override
    public void saveAll() {
    }
    
    public InventorySettings getSettings() {
        return settings;
    }
    
    public OfflinePlayerDataUtil getDataUtil() {
        return dataUtil;
    }
    
    public InvSeeListener getInvSeeListener() {
        return invSeeListener;
    }
    
    public EnderSeeListener getEnderSeeListener() {
        return enderSeeListener;
    }
}
