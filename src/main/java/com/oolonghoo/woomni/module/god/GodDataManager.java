package com.oolonghoo.woomni.module.god;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.database.DataManager;
import com.oolonghoo.woomni.user.UserCacheManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class GodDataManager extends DataManager {
    
    private final UserCacheManager<GodData> cache;
    
    public GodDataManager(WooOmni plugin) {
        super(plugin, "god_data");
        this.cache = new UserCacheManager<GodData>(plugin) {
            @Override
            protected GodData loadFromStorage(UUID uuid) {
                return GodDataManager.this.doLoadFromStorage(uuid);
            }
            
            @Override
            protected void saveData(GodData data) {
                GodDataManager.this.doSaveData(data);
            }
        };
    }
    
    public void initialize() {
        createTable();
    }
    
    @Override
    protected void createTable() {
        try (Connection conn = getConnection()) {
            String sql;
            if (isMySQL()) {
                sql = "CREATE TABLE IF NOT EXISTS " + getFullTableName() + " (" +
                      "uuid VARCHAR(36) NOT NULL PRIMARY KEY, " +
                      "god_mode TINYINT NOT NULL DEFAULT 0" +
                      ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            } else {
                sql = "CREATE TABLE IF NOT EXISTS " + getFullTableName() + " (" +
                      "uuid VARCHAR(36) NOT NULL PRIMARY KEY, " +
                      "god_mode INTEGER NOT NULL DEFAULT 0)";
            }
            conn.createStatement().execute(sql);
            plugin.getLogger().info("God data table created/verified");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create god data table: " + e.getMessage());
        }
    }
    
    private GodData doLoadFromStorage(UUID uuid) {
        GodData data = new GodData(uuid);
        
        try (Connection conn = getConnection()) {
            String sql = "SELECT god_mode FROM " + getFullTableName() + " WHERE uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    data.setGodMode(rs.getBoolean("god_mode"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load god data for " + uuid + ": " + e.getMessage());
        }
        
        data.setDirty(false);
        return data;
    }
    
    private void doSaveData(GodData data) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = getConnection()) {
                String sql;
                if (isMySQL()) {
                    sql = "INSERT INTO " + getFullTableName() + " (uuid, god_mode) VALUES (?, ?) " +
                          "ON DUPLICATE KEY UPDATE god_mode = ?";
                } else {
                    sql = "INSERT OR REPLACE INTO " + getFullTableName() + " (uuid, god_mode) VALUES (?, ?)";
                }
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, data.getUuid().toString());
                    stmt.setBoolean(2, data.isGodMode());
                    
                    if (isMySQL()) {
                        stmt.setBoolean(3, data.isGodMode());
                    }
                    
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to save god data for " + data.getUuid() + ": " + e.getMessage());
            }
        });
        
        data.setDirty(false);
    }
    
    public GodData getGodData(UUID uuid) {
        return cache.get(uuid);
    }
    
    public GodData getIfPresent(UUID uuid) {
        return cache.getIfPresent(uuid);
    }
    
    public void removeFromCache(UUID uuid) {
        cache.invalidate(uuid);
    }
    
    public void saveGodData(UUID uuid) {
        GodData data = cache.getIfPresent(uuid);
        if (data != null && data.isDirty()) {
            cache.saveAll();
        }
    }
    
    @Override
    public void saveAll() {
        cache.saveAll();
    }
    
    public void shutdown() {
        cache.shutdown();
    }
}
