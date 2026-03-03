package com.oolonghoo.woomni.module.god;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.database.AbstractUserDataManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class GodDataManager extends AbstractUserDataManager<GodData> {
    
    public GodDataManager(WooOmni plugin) {
        super(plugin, "god_data");
    }
    
    @Override
    protected String getDataTypeName() {
        return "God";
    }
    
    @Override
    protected String buildCreateTableSQL() {
        if (isMySQL()) {
            return "CREATE TABLE IF NOT EXISTS " + getFullTableName() + " (" +
                   "uuid VARCHAR(36) NOT NULL PRIMARY KEY, " +
                   "god_mode TINYINT NOT NULL DEFAULT 0" +
                   ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        } else {
            return "CREATE TABLE IF NOT EXISTS " + getFullTableName() + " (" +
                   "uuid VARCHAR(36) NOT NULL PRIMARY KEY, " +
                   "god_mode INTEGER NOT NULL DEFAULT 0)";
        }
    }
    
    @Override
    protected GodData createDataInstance(UUID uuid) {
        return new GodData(uuid);
    }
    
    @Override
    protected String buildSelectSQL() {
        return "SELECT god_mode FROM " + getFullTableName() + " WHERE uuid = ?";
    }
    
    @Override
    protected void mapResultSetToData(ResultSet rs, GodData data) throws SQLException {
        data.setGodMode(rs.getBoolean("god_mode"));
    }
    
    @Override
    protected String buildInsertSQL() {
        if (isMySQL()) {
            return "INSERT INTO " + getFullTableName() + " (uuid, god_mode) VALUES (?, ?) " +
                   "ON DUPLICATE KEY UPDATE god_mode = ?";
        } else {
            return "INSERT OR REPLACE INTO " + getFullTableName() + " (uuid, god_mode) VALUES (?, ?)";
        }
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement stmt, GodData data) throws SQLException {
        stmt.setString(1, data.getUuid().toString());
        stmt.setBoolean(2, data.isGodMode());
        
        if (isMySQL()) {
            stmt.setBoolean(3, data.isGodMode());
        }
    }
    
    public GodData getGodData(UUID uuid) {
        return getData(uuid);
    }
    
    public void saveGodData(UUID uuid) {
        saveData(uuid);
    }
}
