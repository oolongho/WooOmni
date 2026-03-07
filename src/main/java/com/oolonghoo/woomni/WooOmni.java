package com.oolonghoo.woomni;

import com.oolonghoo.woomni.command.MainCommand;
import com.oolonghoo.woomni.config.ConfigLoader;
import com.oolonghoo.woomni.config.MessageManager;
import com.oolonghoo.woomni.database.StorageManager;
import com.oolonghoo.woomni.manager.ModuleManager;
import com.oolonghoo.woomni.module.fly.FlyModule;
import com.oolonghoo.woomni.module.god.GodModule;
import com.oolonghoo.woomni.module.inventory.InventoryModule;
import com.oolonghoo.woomni.module.nickname.NicknameModule;
import com.oolonghoo.woomni.module.vanish.VanishModule;
import com.oolonghoo.woomni.task.AutoSaveTask;
import org.bukkit.plugin.java.JavaPlugin;

public class WooOmni extends JavaPlugin {
    
    private static WooOmni instance;
    
    private ConfigLoader configLoader;
    private ConfigLoader modulesLoader;
    private MessageManager messageManager;
    private StorageManager storageManager;
    private ModuleManager moduleManager;
    private AutoSaveTask autoSaveTask;
    private int autoSaveTaskId = -1;
    
    @Override
    public void onEnable() {
        instance = this;
        
        configLoader = new ConfigLoader(this, "config.yml");
        configLoader.initialize();
        
        modulesLoader = new ConfigLoader(this, "modules.yml");
        modulesLoader.initialize();
        
        messageManager = new MessageManager(this);
        messageManager.initialize();
        
        storageManager = new StorageManager(this);
        storageManager.initialize();
        
        moduleManager = new ModuleManager(this);
        registerModules();
        moduleManager.loadEnabledModules();
        
        registerCommands();
        startAutoSave();
        
        getLogger().info("WooOmni v" + getPluginMeta().getVersion() + " 已启用!");
    }
    
    @Override
    public void onDisable() {
        stopAutoSave();
        
        if (moduleManager != null) {
            moduleManager.disableAllModules();
        }
        
        if (storageManager != null) {
            storageManager.shutdown();
        }
    }
    
    private void registerModules() {
        moduleManager.registerModule("fly", () -> new FlyModule(this));
        moduleManager.registerModule("god", () -> new GodModule(this));
        moduleManager.registerModule("vanish", () -> new VanishModule(this));
        moduleManager.registerModule("inventory", () -> new InventoryModule(this));
        moduleManager.registerModule("nickname", () -> new NicknameModule(this));
    }
    
    private void registerCommands() {
        MainCommand mainCommand = new MainCommand(this);
        getCommand("wooomni").setExecutor(mainCommand);
        getCommand("wooomni").setTabCompleter(mainCommand);
        
        getCommand("fly").setExecutor(mainCommand.getFlyCommand());
        getCommand("fly").setTabCompleter(mainCommand.getFlyCommand());
        
        getCommand("flyspeed").setExecutor(mainCommand.getFlySpeedCommand());
        getCommand("flyspeed").setTabCompleter(mainCommand.getFlySpeedCommand());
        
        getCommand("god").setExecutor(mainCommand.getGodCommand());
        getCommand("god").setTabCompleter(mainCommand.getGodCommand());
        
        getCommand("vanish").setExecutor(mainCommand.getVanishCommand());
        getCommand("vanish").setTabCompleter(mainCommand.getVanishCommand());
        
        getCommand("vanishlist").setExecutor(mainCommand.getVanishListCommand());
        getCommand("vanishlist").setTabCompleter(mainCommand.getVanishListCommand());
        
        getCommand("vanishedit").setExecutor(mainCommand.getVanishEditCommand());
        getCommand("vanishedit").setTabCompleter(mainCommand.getVanishEditCommand());
        
        getCommand("inv").setExecutor(mainCommand.getInvCommand());
        getCommand("inv").setTabCompleter(mainCommand.getInvCommand());
        
        getCommand("ender").setExecutor(mainCommand.getEnderCommand());
        getCommand("ender").setTabCompleter(mainCommand.getEnderCommand());
        
        getCommand("nickname").setExecutor(mainCommand.getNicknameCommand());
        getCommand("nickname").setTabCompleter(mainCommand.getNicknameCommand());
    }
    
    private void startAutoSave() {
        int interval = getConfig().getInt("settings.auto-save-interval", 300);
        if (interval > 0) {
            autoSaveTask = new AutoSaveTask(this);
            autoSaveTaskId = getServer().getScheduler().runTaskTimerAsynchronously(
                this, autoSaveTask, interval * 20L, interval * 20L
            ).getTaskId();
        }
    }
    
    private void stopAutoSave() {
        if (autoSaveTaskId != -1) {
            getServer().getScheduler().cancelTask(autoSaveTaskId);
            autoSaveTaskId = -1;
        }
        
        if (autoSaveTask != null) {
            autoSaveTask.run();
        }
    }
    
    public void reload() {
        configLoader.reload();
        modulesLoader.reload();
        messageManager.reload();
        moduleManager.reloadAllModules();
    }
    
    public void reloadModule(String moduleName) {
        moduleManager.reloadModule(moduleName);
    }
    
    public static WooOmni getInstance() {
        return instance;
    }
    
    public ConfigLoader getConfigLoader() {
        return configLoader;
    }
    
    public ConfigLoader getModulesLoader() {
        return modulesLoader;
    }
    
    public MessageManager getMessageManager() {
        return messageManager;
    }
    
    public StorageManager getStorageManager() {
        return storageManager;
    }
    
    public ModuleManager getModuleManager() {
        return moduleManager;
    }
}
