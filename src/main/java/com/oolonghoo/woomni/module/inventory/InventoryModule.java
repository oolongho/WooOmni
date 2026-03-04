package com.oolonghoo.woomni.module.inventory;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.listener.EnderSeeListener;
import com.oolonghoo.woomni.listener.InvSeeListener;
import com.oolonghoo.woomni.module.Module;

/**
 * Inventory模块主类
 * 负责管理Inventory相关的功能
 */
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
        // 初始化设置
        settings = new InventorySettings(plugin);
        settings.initialize();
        log("配置加载完成");
        
        // 初始化离线玩家数据工具
        dataUtil = new OfflinePlayerDataUtil(plugin);
        log("离线玩家数据工具初始化完成");
        
        // 注册监听器
        invSeeListener = new InvSeeListener(plugin, dataUtil);
        enderSeeListener = new EnderSeeListener(plugin, dataUtil);
        plugin.getServer().getPluginManager().registerEvents(invSeeListener, plugin);
        plugin.getServer().getPluginManager().registerEvents(enderSeeListener, plugin);
        log("监听器注册完成");
        
        log("模块启用完成");
    }
    
    @Override
    public void onDisable() {
        // 保存所有数据
        saveAll();
        
        // 清理监听器
        if (invSeeListener != null) {
            invSeeListener.clearAll();
        }
        if (enderSeeListener != null) {
            enderSeeListener.clearAll();
        }
        
        log("模块已禁用");
    }
    
    @Override
    public void onReload() {
        // 保存所有数据
        saveAll();
        
        // 重载配置
        if (settings != null) {
            settings.reload();
            log("配置已重载");
        }
        
        log("模块重载完成");
    }
    
    @Override
    public void saveAll() {
        // Inventory模块暂时没有需要保存的数据
    }
    
    /**
     * 获取设置管理器
     * @return 设置管理器
     */
    public InventorySettings getSettings() {
        return settings;
    }
    
    /**
     * 获取离线玩家数据工具
     * @return 离线玩家数据工具
     */
    public OfflinePlayerDataUtil getDataUtil() {
        return dataUtil;
    }
    
    /**
     * 获取背包查看监听器
     * @return 背包查看监听器
     */
    public InvSeeListener getInvSeeListener() {
        return invSeeListener;
    }
    
    /**
     * 获取末影箱查看监听器
     * @return 末影箱查看监听器
     */
    public EnderSeeListener getEnderSeeListener() {
        return enderSeeListener;
    }
}
