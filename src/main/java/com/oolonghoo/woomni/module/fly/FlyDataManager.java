package com.oolonghoo.woomni.module.fly;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.database.DataManager;
import com.oolonghoo.woomni.user.UserCacheManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class FlyDataManager extends DataManager {
    
    private final UserCacheManager<FlyData> cache;
    
    public FlyDataManager(WooOmni plugin) {
        super(plugin, "fly_data");
        this.cache = new UserCacheManager<FlyData>(plugin) {
            @Override
            protected FlyData loadFromStorage(UUID uuid) {
                return FlyDataManager.this.doLoadFromStorage(uuid);
            }
            
            @Override
            protected void saveData(FlyData data) {
                FlyDataManager.this.doSaveData(data);
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
                      "flying TINYINT NOT NULL DEFAULT 0, " +
                      "fly_speed FLOAT NOT NULL DEFAULT 0.1" +
                      ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            } else {
                sql = "CREATE TABLE IF NOT EXISTS " + getFullTableName() + " (" +
                      "uuid VARCHAR(36) NOT NULL PRIMARY KEY, " +
                      "flying INTEGER NOT NULL DEFAULT 0, " +
                      "fly_speed REAL NOT NULL DEFAULT 0.1)";
            }
            conn.createStatement().execute(sql);
            plugin.getLogger().info("Fly data table created/verified");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create fly data table: " + e.getMessage());
        }
    }
    
    private FlyData doLoadFromStorage(UUID uuid) {
        FlyData data = new FlyData(uuid);
        
        try (Connection conn = getConnection()) {
            String sql = "SELECT flying, fly_speed FROM " + getFullTableName() + " WHERE uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    data.setFlying(rs.getBoolean("flying"));
                    data.setFlySpeed(rs.getFloat("fly_speed"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load fly data for " + uuid + ": " + e.getMessage());
        }
        
        data.setDirty(false);
        return data;
    }
    
    private void doSaveData(FlyData data) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = getConnection()) {
                String sql;
                if (isMySQL()) {
                    sql = "INSERT INTO " + getFullTableName() + " (uuid, flying, fly_speed) VALUES (?, ?, ?) " +
                          "ON DUPLICATE KEY UPDATE flying = ?, fly_speed = ?";
                } else {
                    sql = "INSERT OR REPLACE INTO " + getFullTableName() + " (uuid, flying, fly_speed) VALUES (?, ?, ?)";
                }
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, data.getUuid().toString());
                    stmt.setBoolean(2, data.isFlying());
                    stmt.setFloat(3, data.getFlySpeed());
                    
                    if (isMySQL()) {
                        stmt.setBoolean(4, data.isFlying());
                        stmt.setFloat(5, data.getFlySpeed());
                    }
                    
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to save fly data for " + data.getUuid() + ": " + e.getMessage());
            }
        });
        
        data.setDirty(false);
    }
    
    public FlyData getFlyData(UUID uuid) {
        return cache.get(uuid);
    }
    
    public FlyData getIfPresent(UUID uuid) {
        return cache.getIfPresent(uuid);
    }
    
    public void removeFromCache(UUID uuid) {
        cache.invalidate(uuid);
    }
    
    public void saveFlyData(UUID uuid) {
        FlyData data = cache.getIfPresent(uuid);
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
