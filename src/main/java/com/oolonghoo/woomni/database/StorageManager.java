package com.oolonghoo.woomni.database;

import com.oolonghoo.woomni.WooOmni;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class StorageManager {
    
    private final WooOmni plugin;
    private final StorageType storageType;
    private HikariDataSource dataSource;
    private final String tablePrefix;
    
    public enum StorageType {
        SQLITE, MYSQL
    }
    
    public StorageManager(WooOmni plugin) {
        this.plugin = plugin;
        
        String typeStr = plugin.getConfig().getString("storage.type", "sqlite").toLowerCase();
        this.storageType = "mysql".equals(typeStr) ? StorageType.MYSQL : StorageType.SQLITE;
        this.tablePrefix = plugin.getConfig().getString("storage.mysql.table-prefix", "woomni_");
    }
    
    public void initialize() {
        if (storageType == StorageType.MYSQL) {
            initMySQL();
        } else {
            initSQLite();
        }
    }
    
    private void initMySQL() {
        String host = plugin.getConfig().getString("storage.mysql.host", "localhost");
        int port = plugin.getConfig().getInt("storage.mysql.port", 3306);
        String database = plugin.getConfig().getString("storage.mysql.database", "woomni");
        String user = plugin.getConfig().getString("storage.mysql.user", "root");
        String password = plugin.getConfig().getString("storage.mysql.password", "");
        int poolSize = plugin.getConfig().getInt("storage.mysql.pool-size", 10);
        
        HikariConfig config = new HikariConfig();
        config.setPoolName("[WooOmni-MySQL]");
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database 
            + "?useSSL=false&useUnicode=true&characterEncoding=UTF-8");
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(poolSize);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setMaxLifetime(1800000);
        config.setConnectionTimeout(10000);
        
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        
        dataSource = new HikariDataSource(config);
        plugin.getLogger().info("数据库连接成功 (MySQL)");
    }
    
    private void initSQLite() {
        File dbFile = new File(plugin.getDataFolder(), "woomni.db");
        if (!dbFile.getParentFile().exists()) {
            dbFile.getParentFile().mkdirs();
        }
        
        HikariConfig config = new HikariConfig();
        config.setPoolName("[WooOmni-SQLite]");
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(1);
        
        dataSource = new HikariDataSource(config);
        plugin.getLogger().info("数据库连接成功 (SQLite)");
    }
    
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Storage not initialized");
        }
        return dataSource.getConnection();
    }
    
    public StorageType getStorageType() {
        return storageType;
    }
    
    public String getTablePrefix() {
        return tablePrefix;
    }
    
    public boolean isMySQL() {
        return storageType == StorageType.MYSQL;
    }
    
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("数据库连接池已关闭");
        }
    }
    
    public WooOmni getPlugin() {
        return plugin;
    }
}
