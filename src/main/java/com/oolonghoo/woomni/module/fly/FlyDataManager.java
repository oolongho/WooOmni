package com.oolonghoo.woomni.module.fly;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.database.AbstractUserDataManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class FlyDataManager extends AbstractUserDataManager<FlyData> {
    
    public FlyDataManager(WooOmni plugin) {
        super(plugin, "fly_data");
    }
    
    @Override
    protected String getDataTypeName() {
        return "Fly";
    }
    
    @Override
    protected String buildCreateTableSQL() {
        if (isMySQL()) {
            return "CREATE TABLE IF NOT EXISTS " + getFullTableName() + " (" +
                   "uuid VARCHAR(36) NOT NULL PRIMARY KEY, " +
                   "flying TINYINT NOT NULL DEFAULT 0, " +
                   "fly_speed FLOAT NOT NULL DEFAULT 0.1" +
                   ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        } else {
            return "CREATE TABLE IF NOT EXISTS " + getFullTableName() + " (" +
                   "uuid VARCHAR(36) NOT NULL PRIMARY KEY, " +
                   "flying INTEGER NOT NULL DEFAULT 0, " +
                   "fly_speed REAL NOT NULL DEFAULT 0.1)";
        }
    }
    
    @Override
    protected FlyData createDataInstance(UUID uuid) {
        return new FlyData(uuid);
    }
    
    @Override
    protected String buildSelectSQL() {
        return "SELECT flying, fly_speed FROM " + getFullTableName() + " WHERE uuid = ?";
    }
    
    @Override
    protected void mapResultSetToData(ResultSet rs, FlyData data) throws SQLException {
        data.setFlying(rs.getBoolean("flying"));
        data.setFlySpeed(rs.getFloat("fly_speed"));
    }
    
    @Override
    protected String buildInsertSQL() {
        if (isMySQL()) {
            return "INSERT INTO " + getFullTableName() + " (uuid, flying, fly_speed) VALUES (?, ?, ?) " +
                   "ON DUPLICATE KEY UPDATE flying = ?, fly_speed = ?";
        } else {
            return "INSERT OR REPLACE INTO " + getFullTableName() + " (uuid, flying, fly_speed) VALUES (?, ?, ?)";
        }
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement stmt, FlyData data) throws SQLException {
        stmt.setString(1, data.getUuid().toString());
        stmt.setBoolean(2, data.isFlying());
        stmt.setFloat(3, data.getFlySpeed());
        
        if (isMySQL()) {
            stmt.setBoolean(4, data.isFlying());
            stmt.setFloat(5, data.getFlySpeed());
        }
    }
    
    public FlyData getFlyData(UUID uuid) {
        return getData(uuid);
    }
    
    public void saveFlyData(UUID uuid) {
        saveData(uuid);
    }
}
