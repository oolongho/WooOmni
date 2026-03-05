package com.oolonghoo.woomni.listener;

import com.oolonghoo.woomni.module.vanish.gui.VanishEditGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VanishGUIListener implements Listener {
    
    private final Map<UUID, VanishEditGUI> openGUIs = new HashMap<>();
    
    public void registerGUI(Player player, VanishEditGUI gui) {
        openGUIs.put(player.getUniqueId(), gui);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        InventoryHolder holder = event.getInventory().getHolder();
        
        if (holder instanceof VanishEditGUI) {
            event.setCancelled(true);
            
            VanishEditGUI gui = (VanishEditGUI) holder;
            int slot = event.getRawSlot();
            
            gui.handleClick(slot, player);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        openGUIs.remove(player.getUniqueId());
    }
    
    public void clearAll() {
        openGUIs.clear();
    }
}
