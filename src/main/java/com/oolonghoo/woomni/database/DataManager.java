package com.oolonghoo.woomni.database;

import com.oolonghoo.woomni.WooOmni;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataManager {
    
    protected final WooOmni plugin;
    protected final StorageManager storageManager;
    protected final String tableName;
    
    protected DataManager(WooOmni plugin, String tableName) {
        this.plugin = plugin;
        this.storageManager = plugin.getStorageManager();
        this.tableName = tableName;
    }
    
    protected Connection getConnection() throws SQLException {
        return storageManager.getConnection();
    }
    
    protected String getFullTableName() {
        return storageManager.getTablePrefix() + tableName;
    }
    
    protected boolean isMySQL() {
        return storageManager.isMySQL();
    }
    
    protected abstract void createTable();
    
    public abstract void saveAll();
}
