package com.oolonghoo.woomni.task;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.module.Module;

import java.util.Map;

public class AutoSaveTask implements Runnable {
    
    private final WooOmni plugin;
    
    public AutoSaveTask(WooOmni plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        boolean debug = plugin.getConfig().getBoolean("settings.debug", false);
        int savedModules = 0;
        long startTime = System.currentTimeMillis();
        
        for (Module module : plugin.getModuleManager().getLoadedModules().values()) {
            try {
                module.saveAll();
                savedModules++;
                if (debug) {
                    plugin.getLogger().info("[AutoSave] 模块 " + module.getName() + " 数据已保存");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("[AutoSave] 模块 " + module.getName() + " 保存失败: " + e.getMessage());
                if (debug) {
                    e.printStackTrace();
                }
            }
        }
        
        long elapsed = System.currentTimeMillis() - startTime;
        
        if (debug) {
            plugin.getLogger().info(String.format("[AutoSave] 已自动保存 %d 个模块数据，耗时 %dms", savedModules, elapsed));
        }
    }
}
