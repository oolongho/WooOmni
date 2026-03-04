package com.oolonghoo.woomni.manager;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.module.Module;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ModuleManager {
    
    private final WooOmni plugin;
    private final Map<String, Supplier<Module>> moduleFactories = new LinkedHashMap<>();
    private final Map<String, Module> loadedModules = new HashMap<>();
    
    public ModuleManager(WooOmni plugin) {
        this.plugin = plugin;
    }
    
    public void registerModule(String name, Supplier<Module> factory) {
        moduleFactories.put(name.toLowerCase(), factory);
    }
    
    public void loadEnabledModules() {
        FileConfiguration modulesConfig = plugin.getModulesLoader().getConfig();
        List<String> enabledModules = new ArrayList<>();
        
        for (Map.Entry<String, Supplier<Module>> entry : moduleFactories.entrySet()) {
            String moduleName = entry.getKey();
            boolean enabled = modulesConfig.getBoolean("modules." + moduleName + ".enabled", true);
            
            if (enabled) {
                loadModule(moduleName);
                enabledModules.add(moduleName);
            }
        }
        
        if (!enabledModules.isEmpty()) {
            StringBuilder sb = new StringBuilder("已启用模块 ");
            for (String moduleName : enabledModules) {
                sb.append("[").append(moduleName).append("] ");
            }
            plugin.getLogger().info(sb.toString().trim());
        }
    }
    
    public void loadModule(String name) {
        name = name.toLowerCase();
        
        if (loadedModules.containsKey(name)) {
            plugin.getLogger().warning("模块 " + name + " 已经加载");
            return;
        }
        
        Supplier<Module> factory = moduleFactories.get(name);
        if (factory == null) {
            plugin.getLogger().warning("未知模块: " + name);
            return;
        }
        
        Module module = factory.get();
        module.onEnable();
        loadedModules.put(name, module);
    }
    
    public void unloadModule(String name) {
        name = name.toLowerCase();
        
        Module module = loadedModules.remove(name);
        if (module != null) {
            module.onDisable();
        }
    }
    
    public void reloadModule(String name) {
        name = name.toLowerCase();
        
        if (loadedModules.containsKey(name)) {
            unloadModule(name);
        }
        
        FileConfiguration modulesConfig = plugin.getModulesLoader().getConfig();
        boolean enabled = modulesConfig.getBoolean("modules." + name + ".enabled", true);
        
        if (enabled) {
            loadModule(name);
        }
    }
    
    public void reloadAllModules() {
        for (String moduleName : moduleFactories.keySet()) {
            reloadModule(moduleName);
        }
    }
    
    public void disableAllModules() {
        if (!loadedModules.isEmpty()) {
            List<String> disabledModules = new ArrayList<>(loadedModules.keySet());
            for (Map.Entry<String, Module> entry : loadedModules.entrySet()) {
                entry.getValue().onDisable();
            }
            loadedModules.clear();
            
            StringBuilder sb = new StringBuilder("已禁用模块 ");
            for (String moduleName : disabledModules) {
                sb.append("[").append(moduleName).append("] ");
            }
            plugin.getLogger().info(sb.toString().trim());
        }
    }
    
    public Module getModule(String name) {
        return loadedModules.get(name.toLowerCase());
    }
    
    public boolean isModuleLoaded(String name) {
        return loadedModules.containsKey(name.toLowerCase());
    }
    
    public Map<String, Module> getLoadedModules() {
        return new HashMap<>(loadedModules);
    }
}
