package com.oolonghoo.woomni.module.vanish;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.database.AbstractUserDataManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class VanishDataManager extends AbstractUserDataManager<VanishData> {
    
    public VanishDataManager(WooOmni plugin) {
        super(plugin, "vanish_data");
    }
    
    @Override
    protected String getDataTypeName() {
        return "Vanish";
    }
    
    @Override
    protected String buildCreateTableSQL() {
        if (isMySQL()) {
            return "CREATE TABLE IF NOT EXISTS " + getFullTableName() + " (" +
                   "uuid VARCHAR(36) NOT NULL PRIMARY KEY, " +
                   "vanished TINYINT NOT NULL DEFAULT 0, " +
                   "night_vision TINYINT NOT NULL DEFAULT 1, " +
                   "pickup_items TINYINT NOT NULL DEFAULT 0, " +
                   "can_take_damage TINYINT NOT NULL DEFAULT 0, " +
                   "can_damage_others TINYINT NOT NULL DEFAULT 1, " +
                   "physical_collision TINYINT NOT NULL DEFAULT 0, " +
                   "silent_chest TINYINT NOT NULL DEFAULT 1, " +
                   "prevent_mob_spawn TINYINT NOT NULL DEFAULT 1, " +
                   "show_join_message TINYINT NOT NULL DEFAULT 1, " +
                   "show_quit_message TINYINT NOT NULL DEFAULT 1, " +
                   "bossbar_enabled TINYINT NOT NULL DEFAULT 1, " +
                   "auto_vanish_join TINYINT NOT NULL DEFAULT 0" +
                   ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        } else {
            return "CREATE TABLE IF NOT EXISTS " + getFullTableName() + " (" +
                   "uuid VARCHAR(36) NOT NULL PRIMARY KEY, " +
                   "vanished INTEGER NOT NULL DEFAULT 0, " +
                   "night_vision INTEGER NOT NULL DEFAULT 1, " +
                   "pickup_items INTEGER NOT NULL DEFAULT 0, " +
                   "can_take_damage INTEGER NOT NULL DEFAULT 0, " +
                   "can_damage_others INTEGER NOT NULL DEFAULT 1, " +
                   "physical_collision INTEGER NOT NULL DEFAULT 0, " +
                   "silent_chest INTEGER NOT NULL DEFAULT 1, " +
                   "prevent_mob_spawn INTEGER NOT NULL DEFAULT 1, " +
                   "show_join_message INTEGER NOT NULL DEFAULT 1, " +
                   "show_quit_message INTEGER NOT NULL DEFAULT 1, " +
                   "bossbar_enabled INTEGER NOT NULL DEFAULT 1, " +
                   "auto_vanish_join INTEGER NOT NULL DEFAULT 0)";
        }
    }
    
    @Override
    protected VanishData createDataInstance(UUID uuid) {
        return new VanishData(uuid);
    }
    
    @Override
    protected String buildSelectSQL() {
        return "SELECT vanished, night_vision, pickup_items, can_take_damage, can_damage_others, " +
               "physical_collision, silent_chest, prevent_mob_spawn, show_join_message, " +
               "show_quit_message, bossbar_enabled, auto_vanish_join FROM " + getFullTableName() + " WHERE uuid = ?";
    }
    
    @Override
    protected void mapResultSetToData(ResultSet rs, VanishData data) throws SQLException {
        data.setVanished(rs.getBoolean("vanished"));
        data.setNightVision(rs.getBoolean("night_vision"));
        data.setPickupItems(rs.getBoolean("pickup_items"));
        data.setCanTakeDamage(rs.getBoolean("can_take_damage"));
        data.setCanDamageOthers(rs.getBoolean("can_damage_others"));
        data.setPhysicalCollision(rs.getBoolean("physical_collision"));
        data.setSilentChest(rs.getBoolean("silent_chest"));
        data.setPreventMobSpawn(rs.getBoolean("prevent_mob_spawn"));
        data.setShowJoinMessage(rs.getBoolean("show_join_message"));
        data.setShowQuitMessage(rs.getBoolean("show_quit_message"));
        data.setBossbarEnabled(rs.getBoolean("bossbar_enabled"));
        data.setAutoVanishJoin(rs.getBoolean("auto_vanish_join"));
    }
    
    @Override
    protected String buildInsertSQL() {
        if (isMySQL()) {
            return "INSERT INTO " + getFullTableName() + " (uuid, vanished, night_vision, pickup_items, " +
                   "can_take_damage, can_damage_others, physical_collision, silent_chest, prevent_mob_spawn, " +
                   "show_join_message, show_quit_message, bossbar_enabled, auto_vanish_join) " +
                   "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                   "ON DUPLICATE KEY UPDATE vanished = ?, night_vision = ?, pickup_items = ?, " +
                   "can_take_damage = ?, can_damage_others = ?, physical_collision = ?, silent_chest = ?, " +
                   "prevent_mob_spawn = ?, show_join_message = ?, show_quit_message = ?, " +
                   "bossbar_enabled = ?, auto_vanish_join = ?";
        } else {
            return "INSERT OR REPLACE INTO " + getFullTableName() + " (uuid, vanished, night_vision, pickup_items, " +
                   "can_take_damage, can_damage_others, physical_collision, silent_chest, prevent_mob_spawn, " +
                   "show_join_message, show_quit_message, bossbar_enabled, auto_vanish_join) " +
                   "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement stmt, VanishData data) throws SQLException {
        stmt.setString(1, data.getUuid().toString());
        stmt.setBoolean(2, data.isVanished());
        stmt.setBoolean(3, data.hasNightVision());
        stmt.setBoolean(4, data.canPickupItems());
        stmt.setBoolean(5, data.canTakeDamage());
        stmt.setBoolean(6, data.canDamageOthers());
        stmt.setBoolean(7, data.hasPhysicalCollision());
        stmt.setBoolean(8, data.hasSilentChest());
        stmt.setBoolean(9, data.shouldPreventMobSpawn());
        stmt.setBoolean(10, data.shouldShowJoinMessage());
        stmt.setBoolean(11, data.shouldShowQuitMessage());
        stmt.setBoolean(12, data.isBossbarEnabled());
        stmt.setBoolean(13, data.isAutoVanishJoin());
        
        if (isMySQL()) {
            stmt.setBoolean(14, data.isVanished());
            stmt.setBoolean(15, data.hasNightVision());
            stmt.setBoolean(16, data.canPickupItems());
            stmt.setBoolean(17, data.canTakeDamage());
            stmt.setBoolean(18, data.canDamageOthers());
            stmt.setBoolean(19, data.hasPhysicalCollision());
            stmt.setBoolean(20, data.hasSilentChest());
            stmt.setBoolean(21, data.shouldPreventMobSpawn());
            stmt.setBoolean(22, data.shouldShowJoinMessage());
            stmt.setBoolean(23, data.shouldShowQuitMessage());
            stmt.setBoolean(24, data.isBossbarEnabled());
            stmt.setBoolean(25, data.isAutoVanishJoin());
        }
    }
    
    public VanishData getVanishData(UUID uuid) {
        return getData(uuid);
    }
    
    public void saveVanishData(UUID uuid) {
        saveData(uuid);
    }
}
