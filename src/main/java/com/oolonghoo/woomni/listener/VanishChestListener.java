package com.oolonghoo.woomni.listener;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.module.vanish.VanishData;
import com.oolonghoo.woomni.module.vanish.VanishDataManager;
import com.oolonghoo.woomni.module.vanish.VanishHider;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 静默开箱监听器
 * 隐身玩家可以静默打开容器，不会发出声音和播放动画
 * 使用旁观者模式实现
 */
public class VanishChestListener implements Listener {
    
    private final WooOmni plugin;
    private final VanishDataManager dataManager;
    private final VanishHider hider;
    private final Map<UUID, PlayerState> playerStates = new ConcurrentHashMap<>();
    
    private final Collection<Material> chestMaterials = Arrays.asList(
        Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST,
        Material.BARREL, Material.SHULKER_BOX,
        Material.WHITE_SHULKER_BOX, Material.ORANGE_SHULKER_BOX,
        Material.MAGENTA_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX,
        Material.YELLOW_SHULKER_BOX, Material.LIME_SHULKER_BOX,
        Material.PINK_SHULKER_BOX, Material.GRAY_SHULKER_BOX,
        Material.LIGHT_GRAY_SHULKER_BOX, Material.CYAN_SHULKER_BOX,
        Material.PURPLE_SHULKER_BOX, Material.BLUE_SHULKER_BOX,
        Material.BROWN_SHULKER_BOX, Material.GREEN_SHULKER_BOX,
        Material.RED_SHULKER_BOX, Material.BLACK_SHULKER_BOX
    );
    
    public VanishChestListener(WooOmni plugin, VanishDataManager dataManager, VanishHider hider) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.hider = hider;
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChestInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        if (!hider.isVanished(player.getUniqueId())) {
            return;
        }
        
        VanishData data = dataManager.getIfPresent(player.getUniqueId());
        if (data == null || !data.hasSilentChest()) {
            return;
        }
        
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        if (player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        
        if (player.isSneaking() && player.getInventory().getItemInMainHand().getType().isBlock()) {
            return;
        }
        
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        
        if (!chestMaterials.contains(block.getType())) {
            return;
        }
        
        event.setCancelled(true);
        
        PlayerState state = new PlayerState(
            player.getAllowFlight(),
            player.isFlying(),
            player.getGameMode(),
            player.getLocation().clone()
        );
        playerStates.put(player.getUniqueId(), state);
        
        player.setGameMode(GameMode.SPECTATOR);
        
        if (block.getType() == Material.ENDER_CHEST) {
            player.openInventory(player.getEnderChest());
        } else {
            BlockState blockState = block.getState();
            if (blockState instanceof Container) {
                player.openInventory(((Container) blockState).getInventory());
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        PlayerState state = playerStates.get(uuid);
        if (state == null) {
            return;
        }
        
        new BukkitRunnable() {
            @Override
            public void run() {
                restorePlayerState(player, state);
                playerStates.remove(uuid);
            }
        }.runTaskLater(plugin, 1);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        PlayerState state = playerStates.remove(uuid);
        if (state != null) {
            restorePlayerState(player, state);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        PlayerState state = playerStates.get(uuid);
        if (state == null) {
            return;
        }
        
        Location from = event.getFrom();
        Location to = event.getTo();
        
        if (to != null && state.openLocation.distanceSquared(to) > 1) {
            player.closeInventory();
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        if (playerStates.containsKey(uuid) && event.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
            event.setCancelled(true);
        }
    }
    
    private void restorePlayerState(Player player, PlayerState state) {
        if (!player.isOnline()) {
            return;
        }
        
        player.setGameMode(state.gameMode);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    player.setAllowFlight(state.canFly);
                    player.setFlying(state.isFlying);
                }
            }
        }.runTaskLater(plugin, 1);
    }
    
    public boolean hasSilentlyOpenedChest(UUID uuid) {
        return playerStates.containsKey(uuid);
    }
    
    public void clearState(UUID uuid) {
        PlayerState state = playerStates.remove(uuid);
        if (state != null) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                restorePlayerState(player, state);
            }
        }
    }
    
    private static class PlayerState {
        final boolean canFly;
        final boolean isFlying;
        final GameMode gameMode;
        final Location openLocation;
        
        PlayerState(boolean canFly, boolean isFlying, GameMode gameMode, Location openLocation) {
            this.canFly = canFly;
            this.isFlying = isFlying;
            this.gameMode = gameMode;
            this.openLocation = openLocation;
        }
    }
}
