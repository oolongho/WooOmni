package com.oolonghoo.woomni.listener;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.module.vanish.VanishData;
import com.oolonghoo.woomni.module.vanish.VanishDataManager;
import com.oolonghoo.woomni.module.vanish.VanishHider;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 静默开箱监听器
 * 隐身玩家可以静默打开容器，不会发出声音和播放动画
 */
public class VanishChestListener implements Listener {
    
    private final WooOmni plugin;
    private final VanishDataManager dataManager;
    private final VanishHider hider;
    private final Map<UUID, GameMode> savedGameModes = new ConcurrentHashMap<>();
    
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
        
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        
        if (!isChestType(block.getType())) {
            return;
        }
        
        event.setCancelled(true);
        
        // 静默打开容器 - 不使用旁观者模式，直接打开
        // 这样玩家可以正常操作物品
        if (block.getType() == Material.ENDER_CHEST) {
            player.openInventory(player.getEnderChest());
        } else {
            BlockState state = block.getState();
            if (state instanceof Container) {
                player.openInventory(((Container) state).getInventory());
            }
        }
        
        // 取消开箱声音
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0, 0);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        if (savedGameModes.containsKey(uuid)) {
            GameMode savedMode = savedGameModes.remove(uuid);
            player.setGameMode(savedMode);
        }
    }
    
    private boolean isChestType(Material material) {
        return material == Material.CHEST || 
               material == Material.TRAPPED_CHEST || 
               material == Material.ENDER_CHEST ||
               material == Material.BARREL ||
               material == Material.SHULKER_BOX ||
               material == Material.WHITE_SHULKER_BOX ||
               material == Material.ORANGE_SHULKER_BOX ||
               material == Material.MAGENTA_SHULKER_BOX ||
               material == Material.LIGHT_BLUE_SHULKER_BOX ||
               material == Material.YELLOW_SHULKER_BOX ||
               material == Material.LIME_SHULKER_BOX ||
               material == Material.PINK_SHULKER_BOX ||
               material == Material.GRAY_SHULKER_BOX ||
               material == Material.LIGHT_GRAY_SHULKER_BOX ||
               material == Material.CYAN_SHULKER_BOX ||
               material == Material.PURPLE_SHULKER_BOX ||
               material == Material.BLUE_SHULKER_BOX ||
               material == Material.BROWN_SHULKER_BOX ||
               material == Material.GREEN_SHULKER_BOX ||
               material == Material.RED_SHULKER_BOX ||
               material == Material.BLACK_SHULKER_BOX;
    }
    
    public void clearState(UUID uuid) {
        savedGameModes.remove(uuid);
    }
}
