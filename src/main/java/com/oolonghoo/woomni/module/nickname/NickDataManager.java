package com.oolonghoo.woomni.module.nickname;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.database.AbstractUserDataManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class NickDataManager extends AbstractUserDataManager<NickData> {
    
    public NickDataManager(WooOmni plugin) {
        super(plugin, "nickname_data");
    }
    
    @Override
    protected String getDataTypeName() {
        return "Nickname";
    }
    
    @Override
    protected String buildCreateTableSQL() {
        if (isMySQL()) {
            return "CREATE TABLE IF NOT EXISTS " + getFullTableName() + " (" +
                   "uuid VARCHAR(36) NOT NULL PRIMARY KEY, " +
                   "nickname VARCHAR(64)" +
                   ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        } else {
            return "CREATE TABLE IF NOT EXISTS " + getFullTableName() + " (" +
                   "uuid VARCHAR(36) NOT NULL PRIMARY KEY, " +
                   "nickname TEXT)";
        }
    }
    
    @Override
    protected NickData createDataInstance(UUID uuid) {
        return new NickData(uuid);
    }
    
    @Override
    protected String buildSelectSQL() {
        return "SELECT nickname FROM " + getFullTableName() + " WHERE uuid = ?";
    }
    
    @Override
    protected void mapResultSetToData(ResultSet rs, NickData data) throws SQLException {
        String nickname = rs.getString("nickname");
        if (nickname != null && !nickname.isEmpty()) {
            data.setNickname(nickname);
            data.setDirty(false);
        }
    }
    
    @Override
    protected String buildInsertSQL() {
        if (isMySQL()) {
            return "INSERT INTO " + getFullTableName() + " (uuid, nickname) VALUES (?, ?) " +
                   "ON DUPLICATE KEY UPDATE nickname = ?";
        } else {
            return "INSERT OR REPLACE INTO " + getFullTableName() + " (uuid, nickname) VALUES (?, ?)";
        }
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement stmt, NickData data) throws SQLException {
        stmt.setString(1, data.getUuid().toString());
        stmt.setString(2, data.getNickname());
        
        if (isMySQL()) {
            stmt.setString(3, data.getNickname());
        }
    }
    
    public NickData getNickData(UUID uuid) {
        return getData(uuid);
    }
    
    public void saveNickData(UUID uuid) {
        saveData(uuid);
    }
}
