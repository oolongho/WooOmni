package com.oolonghoo.woomni.manager;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.module.Module;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.LinkedHashMap;
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
        
        for (Map.Entry<String, Supplier<Module>> entry : moduleFactories.entrySet()) {
            String moduleName = entry.getKey();
            boolean enabled = modulesConfig.getBoolean("modules." + moduleName + ".enabled", true);
            
            if (enabled) {
                loadModule(moduleName);
            }
        }
    }
    
    public void loadModule(String name) {
        name = name.toLowerCase();
        
        if (loadedModules.containsKey(name)) {
            plugin.getLogger().warning("Module " + name + " is already loaded");
            return;
        }
        
        Supplier<Module> factory = moduleFactories.get(name);
        if (factory == null) {
            plugin.getLogger().warning("Unknown module: " + name);
            return;
        }
        
        Module module = factory.get();
        module.onEnable();
        loadedModules.put(name, module);
        plugin.getLogger().info("Module " + name + " enabled");
    }
    
    public void unloadModule(String name) {
        name = name.toLowerCase();
        
        Module module = loadedModules.remove(name);
        if (module != null) {
            module.onDisable();
            plugin.getLogger().info("Module " + name + " disabled");
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
        for (Map.Entry<String, Module> entry : loadedModules.entrySet()) {
            entry.getValue().onDisable();
            plugin.getLogger().info("Module " + entry.getKey() + " disabled");
        }
        loadedModules.clear();
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
