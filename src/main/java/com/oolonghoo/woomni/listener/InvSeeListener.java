package com.oolonghoo.woomni.listener;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.config.MessageManager;
import com.oolonghoo.woomni.module.inventory.OfflinePlayerDataUtil;
import com.oolonghoo.woomni.module.inventory.gui.InvSeeGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 背包查看GUI监听器
 */
public class InvSeeListener implements Listener {
    
    private final WooOmni plugin;
    private final MessageManager msg;
    private final OfflinePlayerDataUtil dataUtil;
    
    private final Map<UUID, InvSeeGUI> openGUIs = new ConcurrentHashMap<>();
    
    public InvSeeListener(WooOmni plugin, OfflinePlayerDataUtil dataUtil) {
        this.plugin = plugin;
        this.msg = plugin.getMessageManager();
        this.dataUtil = dataUtil;
    }
    
    public void registerGUI(Player player, InvSeeGUI gui) {
        openGUIs.put(player.getUniqueId(), gui);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player viewer = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        Inventory topInventory = event.getView().getTopInventory();
        
        if (!(topInventory.getHolder() instanceof InvSeeGUI)) {
            return;
        }
        
        InvSeeGUI gui = (InvSeeGUI) topInventory.getHolder();
        int rawSlot = event.getRawSlot();
        
        if (rawSlot >= 0 && rawSlot < InvSeeGUI.GUI_SIZE) {
            handleGUIClick(event, viewer, gui, rawSlot);
            return;
        }
        
        if (!gui.canEdit()) {
            if (clickedInventory != null && clickedInventory.equals(viewer.getInventory())) {
                if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    event.setCancelled(true);
                    msg.send(viewer, "inv.no-edit");
                }
            }
        }
    }
    
    private void handleGUIClick(InventoryClickEvent event, Player viewer, InvSeeGUI gui, int slot) {
        if (InvSeeGUI.isButtonSlot(slot)) {
            event.setCancelled(true);
            handleButtonClick(viewer, gui, slot);
            return;
        }
        
        if (InvSeeGUI.isFillerSlot(slot, gui.getViewType())) {
            event.setCancelled(true);
            return;
        }
        
        if (InvSeeGUI.isArmorSlot(slot)) {
            if (!gui.canEdit()) {
                event.setCancelled(true);
                msg.send(viewer, "inv.no-edit");
                return;
            }
            handleArmorClick(event, viewer, gui, slot);
            return;
        }
        
        if (InvSeeGUI.isInventorySlot(slot)) {
            if (!gui.canEdit()) {
                event.setCancelled(true);
                msg.send(viewer, "inv.no-edit");
                return;
            }
            handleInventoryClick(event, viewer, gui, slot);
        }
    }
    
    private void handleButtonClick(Player viewer, InvSeeGUI gui, int slot) {
        switch (slot) {
            case InvSeeGUI.SLOT_COPY:
                handleCopy(viewer, gui);
                break;
            case InvSeeGUI.SLOT_CLEAR:
                handleClear(viewer, gui);
                break;
            case InvSeeGUI.SLOT_TOGGLE:
                handleToggle(viewer, gui);
                break;
            case InvSeeGUI.SLOT_INFO:
            case InvSeeGUI.SLOT_DATA:
                break;
        }
    }
    
    private void handleCopy(Player viewer, InvSeeGUI gui) {
        Player target = Bukkit.getPlayer(gui.getTargetUUID());
        if (target == null) {
            msg.send(viewer, "inv.player-offline", "player", gui.getTargetName());
            return;
        }
        
        if (gui.getViewType() == InvSeeGUI.ViewType.INVENTORY) {
            viewer.getInventory().setContents(target.getInventory().getContents());
            msg.send(viewer, "inv.copied", "player", gui.getTargetName());
        } else {
            viewer.getEnderChest().setContents(target.getEnderChest().getContents());
            msg.send(viewer, "inv.copied-ender", "player", gui.getTargetName());
        }
    }
    
    private void handleClear(Player viewer, InvSeeGUI gui) {
        if (!gui.canEdit()) {
            msg.send(viewer, "inv.no-edit");
            return;
        }
        
        Player target = Bukkit.getPlayer(gui.getTargetUUID());
        if (target == null) {
            msg.send(viewer, "inv.player-offline", "player", gui.getTargetName());
            return;
        }
        
        if (gui.getViewType() == InvSeeGUI.ViewType.INVENTORY) {
            target.getInventory().clear();
            target.getInventory().setHelmet(null);
            target.getInventory().setChestplate(null);
            target.getInventory().setLeggings(null);
            target.getInventory().setBoots(null);
            target.getInventory().setItemInOffHand(null);
        } else {
            target.getEnderChest().clear();
        }
        
        msg.send(viewer, "inv.cleared", "player", gui.getTargetName());
        refreshGUI(viewer, gui);
    }
    
    private void handleToggle(Player viewer, InvSeeGUI gui) {
        // 切换视图前先保存当前修改
        Player target = Bukkit.getPlayer(gui.getTargetUUID());
        syncToTarget(target, gui);
        
        InvSeeGUI.ViewType newType = gui.getViewType() == InvSeeGUI.ViewType.INVENTORY 
            ? InvSeeGUI.ViewType.ENDER_CHEST 
            : InvSeeGUI.ViewType.INVENTORY;
        
        InvSeeGUI newGui = new InvSeeGUI(
            gui.getSettings(),
            gui.getDataUtil(),
            gui.getTargetUUID(),
            gui.getTargetName(),
            gui.isOnline(),
            gui.canEdit(),
            newType
        );
        
        viewer.openInventory(newGui.getInventory());
        registerGUI(viewer, newGui);
    }
    
    private void handleArmorClick(InventoryClickEvent event, Player viewer, InvSeeGUI gui, int slot) {
        Player target = Bukkit.getPlayer(gui.getTargetUUID());
        if (target == null) {
            event.setCancelled(true);
            msg.send(viewer, "inv.player-offline", "player", gui.getTargetName());
            return;
        }
        
        int playerSlot = InvSeeGUI.armorSlotToPlayerSlot(slot);
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();
        
        // 检查是否点击的是空槽位占位符
        boolean isEmptySlotPlaceholder = clickedItem != null && 
            clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE;
        
        event.setCancelled(true);
        
        switch (event.getAction()) {
            case PICKUP_ALL:
            case PICKUP_SOME:
            case PICKUP_HALF:
            case PICKUP_ONE:
                if (!isEmptySlotPlaceholder && clickedItem != null && clickedItem.getType() != Material.AIR) {
                    viewer.setItemOnCursor(clickedItem.clone());
                    target.getInventory().setItem(playerSlot, null);
                }
                break;
            case PLACE_ALL:
            case PLACE_SOME:
            case PLACE_ONE:
                if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                    int amountToPlace = event.getAction() == InventoryAction.PLACE_ONE ? 1 : cursorItem.getAmount();
                    ItemStack toPlace = cursorItem.clone();
                    toPlace.setAmount(amountToPlace);
                    target.getInventory().setItem(playerSlot, toPlace);
                    
                    ItemStack newCursor = cursorItem.clone();
                    newCursor.setAmount(cursorItem.getAmount() - amountToPlace);
                    viewer.setItemOnCursor(newCursor.getAmount() > 0 ? newCursor : null);
                }
                break;
            case SWAP_WITH_CURSOR:
                if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                    if (!isEmptySlotPlaceholder) {
                        viewer.setItemOnCursor(clickedItem != null && clickedItem.getType() != Material.AIR ? clickedItem.clone() : null);
                    } else {
                        viewer.setItemOnCursor(null);
                    }
                    target.getInventory().setItem(playerSlot, cursorItem.clone());
                }
                break;
            case MOVE_TO_OTHER_INVENTORY:
                return;
            default:
                break;
        }
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> refreshGUI(viewer, gui), 1L);
    }
    
    private void handleInventoryClick(InventoryClickEvent event, Player viewer, InvSeeGUI gui, int slot) {
        Player target = Bukkit.getPlayer(gui.getTargetUUID());
        if (target == null) {
            event.setCancelled(true);
            msg.send(viewer, "inv.player-offline", "player", gui.getTargetName());
            return;
        }
        
        int playerSlot = InvSeeGUI.guiSlotToPlayerSlot(slot);
        if (playerSlot < 0) {
            event.setCancelled(true);
            return;
        }
        
        switch (event.getAction()) {
            case PICKUP_ALL:
            case PICKUP_SOME:
            case PICKUP_HALF:
            case PICKUP_ONE:
            case PLACE_ALL:
            case PLACE_SOME:
            case PLACE_ONE:
            case SWAP_WITH_CURSOR:
            case HOTBAR_SWAP:
            case HOTBAR_MOVE_AND_READD:
                break;
            case MOVE_TO_OTHER_INVENTORY:
                handleShiftClick(event, viewer, target, playerSlot, gui);
                return;
            default:
                break;
        }
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            syncToTarget(target, gui);
            refreshGUI(viewer, gui);
        }, 1L);
    }
    
    private void handleShiftClick(InventoryClickEvent event, Player viewer, Player target, int playerSlot, InvSeeGUI gui) {
        event.setCancelled(true);
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        
        if (gui.getViewType() == InvSeeGUI.ViewType.INVENTORY) {
            target.getInventory().setItem(playerSlot, null);
        } else {
            target.getEnderChest().setItem(playerSlot, null);
        }
        viewer.getInventory().addItem(clickedItem.clone());
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> refreshGUI(viewer, gui), 1L);
    }
    
    private void syncToTarget(Player target, InvSeeGUI gui) {
        if (target == null) {
            saveOfflineInventory(gui);
            return;
        }
        
        Inventory guiInventory = gui.getInventory();
        
        if (gui.getViewType() == InvSeeGUI.ViewType.INVENTORY) {
            for (int guiSlot = InvSeeGUI.SLOT_INVENTORY_START; guiSlot < InvSeeGUI.GUI_SIZE; guiSlot++) {
                int playerSlot = InvSeeGUI.guiSlotToPlayerSlot(guiSlot);
                if (playerSlot >= 0) {
                    ItemStack item = guiInventory.getItem(guiSlot);
                    target.getInventory().setItem(playerSlot, item);
                }
            }
            
            int[] armorSlots = {InvSeeGUI.SLOT_HELMET, InvSeeGUI.SLOT_CHESTPLATE, 
                               InvSeeGUI.SLOT_LEGGINGS, InvSeeGUI.SLOT_BOOTS, InvSeeGUI.SLOT_OFFHAND};
            for (int slot : armorSlots) {
                int playerSlot = InvSeeGUI.armorSlotToPlayerSlot(slot);
                if (playerSlot >= 0) {
                    ItemStack item = guiInventory.getItem(slot);
                    target.getInventory().setItem(playerSlot, item);
                }
            }
        } else {
            for (int i = 0; i < 27; i++) {
                int guiSlot = InvSeeGUI.SLOT_INVENTORY_START + i;
                ItemStack item = guiInventory.getItem(guiSlot);
                target.getEnderChest().setItem(i, item);
            }
        }
    }
    
    private void saveOfflineInventory(InvSeeGUI gui) {
        if (gui.isOnline() || dataUtil == null) {
            return;
        }
        
        Inventory guiInventory = gui.getInventory();
        
        if (gui.getViewType() == InvSeeGUI.ViewType.INVENTORY) {
            ItemStack[] contents = new ItemStack[41];
            
            for (int guiSlot = InvSeeGUI.SLOT_INVENTORY_START; guiSlot < InvSeeGUI.GUI_SIZE; guiSlot++) {
                int playerSlot = InvSeeGUI.guiSlotToPlayerSlot(guiSlot);
                if (playerSlot >= 0 && playerSlot < 36) {
                    contents[playerSlot] = guiInventory.getItem(guiSlot);
                }
            }
            
            contents[36] = guiInventory.getItem(InvSeeGUI.SLOT_BOOTS);
            contents[37] = guiInventory.getItem(InvSeeGUI.SLOT_LEGGINGS);
            contents[38] = guiInventory.getItem(InvSeeGUI.SLOT_CHESTPLATE);
            contents[39] = guiInventory.getItem(InvSeeGUI.SLOT_HELMET);
            contents[40] = guiInventory.getItem(InvSeeGUI.SLOT_OFFHAND);
            
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                boolean success = dataUtil.saveOfflineInventory(gui.getTargetUUID(), contents);
                if (success) {
                    plugin.getLogger().info("已保存离线玩家 " + gui.getTargetName() + " 的背包数据");
                }
            });
        } else {
            ItemStack[] contents = new ItemStack[27];
            for (int i = 0; i < 27; i++) {
                contents[i] = guiInventory.getItem(InvSeeGUI.SLOT_INVENTORY_START + i);
            }
            
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                boolean success = dataUtil.saveOfflineEnderChest(gui.getTargetUUID(), contents);
                if (success) {
                    plugin.getLogger().info("已保存离线玩家 " + gui.getTargetName() + " 的末影箱数据");
                }
            });
        }
    }
    
    private void refreshGUI(Player viewer, InvSeeGUI oldGui) {
        Player target = Bukkit.getPlayer(oldGui.getTargetUUID());
        boolean isOnline = target != null;
        
        InvSeeGUI newGui = new InvSeeGUI(
            oldGui.getSettings(),
            oldGui.getDataUtil(),
            oldGui.getTargetUUID(),
            oldGui.getTargetName(),
            isOnline,
            oldGui.canEdit(),
            oldGui.getViewType()
        );
        
        viewer.openInventory(newGui.getInventory());
        registerGUI(viewer, newGui);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Inventory topInventory = event.getView().getTopInventory();
        if (!(topInventory.getHolder() instanceof InvSeeGUI)) {
            return;
        }
        
        InvSeeGUI gui = (InvSeeGUI) topInventory.getHolder();
        
        if (!gui.canEdit()) {
            event.setCancelled(true);
            return;
        }
        
        for (int slot : event.getRawSlots()) {
            if (slot < InvSeeGUI.GUI_SIZE) {
                if (InvSeeGUI.isButtonSlot(slot) || InvSeeGUI.isFillerSlot(slot, gui.getViewType())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        
        Player target = Bukkit.getPlayer(gui.getTargetUUID());
        Bukkit.getScheduler().runTaskLater(plugin, () -> syncToTarget(target, gui), 1L);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        InvSeeGUI gui = openGUIs.remove(player.getUniqueId());
        
        if (gui != null && !gui.isOnline() && gui.canEdit()) {
            saveOfflineInventory(gui);
        }
    }
    
    public void clearAll() {
        openGUIs.clear();
    }
}
