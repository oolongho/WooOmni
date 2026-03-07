package com.oolonghoo.woomni.listener;

import com.oolonghoo.woomni.module.nickname.NicknameModule;
import com.oolonghoo.woomni.module.nickname.gui.NicknameAnvilGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.AnvilInventory;

import java.util.UUID;

public class NicknameListener implements Listener {
    
    private final NicknameModule module;
    
    public NicknameListener(NicknameModule module) {
        this.module = module;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        module.getDataManager().getNickData(uuid);
        
        module.applyDisplayName(player);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        module.getDataManager().saveNickData(uuid);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        NicknameAnvilGUI gui = NicknameAnvilGUI.getOpenGUI(player.getUniqueId());
        if (gui == null) return;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        
        NicknameAnvilGUI gui = NicknameAnvilGUI.getOpenGUI(player.getUniqueId());
        if (gui != null && event.getInventory() instanceof AnvilInventory) {
        }
    }
}
