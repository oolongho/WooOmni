package com.oolonghoo.woomni.database;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.user.UserCacheManager;
import com.oolonghoo.woomni.user.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public abstract class AbstractUserDataManager<T extends UserData> extends DataManager {
    
    private final UserCacheManager<T> cache;
    
    protected AbstractUserDataManager(WooOmni plugin, String tableName) {
        super(plugin, tableName);
        this.cache = new UserCacheManager<T>(plugin) {
            @Override
            protected T loadFromStorage(UUID uuid) {
                return AbstractUserDataManager.this.doLoadFromStorage(uuid);
            }
            
            @Override
            protected void saveData(UserData data) {
                AbstractUserDataManager.this.doSaveData((T) data);
            }
        };
    }
    
    public void initialize() {
        createTable();
    }
    
    @Override
    protected void createTable() {
        try (Connection conn = getConnection()) {
            String sql = buildCreateTableSQL();
            conn.createStatement().execute(sql);
            plugin.getLogger().info(getDataTypeName() + " data table created/verified");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create " + getDataTypeName() + " data table: " + e.getMessage());
        }
    }
    
    protected abstract String buildCreateTableSQL();
    
    protected abstract String getDataTypeName();
    
    private T doLoadFromStorage(UUID uuid) {
        T data = createDataInstance(uuid);
        
        try (Connection conn = getConnection()) {
            String sql = buildSelectSQL();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    mapResultSetToData(rs, data);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load " + getDataTypeName() + " data for " + uuid + ": " + e.getMessage());
        }
        
        data.setDirty(false);
        return data;
    }
    
    protected abstract T createDataInstance(UUID uuid);
    
    protected abstract String buildSelectSQL();
    
    protected abstract void mapResultSetToData(ResultSet rs, T data) throws SQLException;
    
    private void doSaveData(T data) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = getConnection()) {
                String sql = buildInsertSQL();
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    setInsertParameters(stmt, data);
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to save " + getDataTypeName() + " data for " + data.getUuid() + ": " + e.getMessage());
            }
        });
        
        data.setDirty(false);
    }
    
    protected abstract String buildInsertSQL();
    
    protected abstract void setInsertParameters(PreparedStatement stmt, T data) throws SQLException;
    
    public T getData(UUID uuid) {
        return cache.get(uuid);
    }
    
    public T getIfPresent(UUID uuid) {
        return cache.getIfPresent(uuid);
    }
    
    public void removeFromCache(UUID uuid) {
        cache.invalidate(uuid);
    }
    
    public void saveData(UUID uuid) {
        T data = cache.getIfPresent(uuid);
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
