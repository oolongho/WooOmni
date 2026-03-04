package com.oolonghoo.woomni.listener;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.config.MessageManager;
import com.oolonghoo.woomni.module.inventory.OfflinePlayerDataUtil;
import com.oolonghoo.woomni.module.inventory.gui.EnderSeeGUI;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 末影箱查看GUI监听器
 * 处理GUI点击事件和权限检查
 */
public class EnderSeeListener implements Listener {
    
    private final WooOmni plugin;
    private final MessageManager msg;
    private final OfflinePlayerDataUtil dataUtil;
    
    // 记录打开的GUI
    private final Map<UUID, EnderSeeGUI> openGUIs = new HashMap<>();
    
    public EnderSeeListener(WooOmni plugin, OfflinePlayerDataUtil dataUtil) {
        this.plugin = plugin;
        this.msg = plugin.getMessageManager();
        this.dataUtil = dataUtil;
    }
    
    /**
     * 注册打开的GUI
     */
    public void registerGUI(Player player, EnderSeeGUI gui) {
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
        
        // 检查是否是EnderSeeGUI
        if (!(topInventory.getHolder() instanceof EnderSeeGUI)) {
            return;
        }
        
        EnderSeeGUI gui = (EnderSeeGUI) topInventory.getHolder();
        int rawSlot = event.getRawSlot();
        
        // 点击的是GUI内部
        if (rawSlot >= 0 && rawSlot < 45) {
            handleGUIClick(event, viewer, gui, rawSlot);
            return;
        }
        
        // 点击的是玩家自己的背包（底部库存）
        // 如果没有编辑权限，取消事件
        if (!gui.canEdit()) {
            // 允许玩家在自己的背包内移动物品
            if (clickedInventory != null && clickedInventory.equals(viewer.getInventory())) {
                // 检查是否试图将物品放入GUI
                if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    event.setCancelled(true);
                    viewer.sendMessage(msg.getWithPrefix("ender.no-edit"));
                }
            }
        }
    }
    
    /**
     * 处理GUI内部点击
     */
    private void handleGUIClick(InventoryClickEvent event, Player viewer, EnderSeeGUI gui, int slot) {
        // 功能按钮点击
        if (isButtonSlot(slot)) {
            event.setCancelled(true);
            handleButtonClick(viewer, gui, slot);
            return;
        }
        
        // 末影箱槽位点击
        if (EnderSeeGUI.isEnderChestSlot(slot)) {
            if (!gui.canEdit()) {
                event.setCancelled(true);
                viewer.sendMessage(msg.getWithPrefix("ender.no-edit"));
                return;
            }
            
            // 处理末影箱编辑
            handleEnderChestClick(event, viewer, gui, slot);
        } else {
            // 其他区域（填充物品）不可点击
            event.setCancelled(true);
        }
    }
    
    /**
     * 判断是否为功能按钮槽位
     */
    private boolean isButtonSlot(int slot) {
        return slot >= EnderSeeGUI.SLOT_COPY && slot <= EnderSeeGUI.SLOT_LOG;
    }
    
    /**
     * 处理功能按钮点击
     */
    private void handleButtonClick(Player viewer, EnderSeeGUI gui, int slot) {
        gui.handleClick(slot, viewer);
    }
    
    /**
     * 处理末影箱槽位点击
     */
    private void handleEnderChestClick(InventoryClickEvent event, Player viewer, EnderSeeGUI gui, int slot) {
        Player target = gui.getOnlineTarget();
        
        // 处理不同的点击动作
        switch (event.getAction()) {
            case PICKUP_ALL:
            case PICKUP_SOME:
            case PICKUP_HALF:
            case PICKUP_ONE:
                // 取出物品 - 允许默认行为
                break;
            case PLACE_ALL:
            case PLACE_SOME:
            case PLACE_ONE:
                // 放入物品 - 允许默认行为
                break;
            case SWAP_WITH_CURSOR:
                // 交换物品 - 允许默认行为
                break;
            case MOVE_TO_OTHER_INVENTORY:
                // Shift+点击 - 需要特殊处理
                handleShiftClick(event, viewer, gui, slot);
                return;
            case HOTBAR_SWAP:
            case HOTBAR_MOVE_AND_READD:
                // 快捷栏交换 - 允许默认行为
                break;
            default:
                break;
        }
        
        // 同步到目标玩家
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            gui.syncToTarget();
        }, 1L);
    }
    
    /**
     * 处理Shift+点击
     */
    private void handleShiftClick(InventoryClickEvent event, Player viewer, EnderSeeGUI gui, int slot) {
        event.setCancelled(true);
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        
        // 从末影箱移除物品并添加到查看者背包
        gui.getInventory().setItem(slot, null);
        viewer.getInventory().addItem(clickedItem.clone());
        
        // 同步到目标玩家
        gui.syncToTarget();
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Inventory topInventory = event.getView().getTopInventory();
        if (!(topInventory.getHolder() instanceof EnderSeeGUI)) {
            return;
        }
        
        EnderSeeGUI gui = (EnderSeeGUI) topInventory.getHolder();
        
        // 如果没有编辑权限，取消拖拽
        if (!gui.canEdit()) {
            event.setCancelled(true);
            return;
        }
        
        // 检查是否拖拽到了功能按钮区域或填充区域
        for (int slot : event.getRawSlots()) {
            if (slot < 18 && !EnderSeeGUI.isEnderChestSlot(slot)) {
                event.setCancelled(true);
                return;
            }
        }
        
        // 同步到目标玩家
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            gui.syncToTarget();
        }, 1L);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        EnderSeeGUI gui = openGUIs.remove(player.getUniqueId());
        
        // 如果是离线玩家GUI，在关闭时保存数据
        if (gui != null && !gui.isOnline() && gui.canEdit()) {
            saveOfflineEnderChest(gui);
        }
    }
    
    /**
     * 保存离线玩家末影箱数据
     */
    private void saveOfflineEnderChest(EnderSeeGUI gui) {
        if (!gui.isOnline() && dataUtil != null) {
            Inventory guiInventory = gui.getInventory();
            ItemStack[] contents = new ItemStack[27];
            
            // 收集末影箱物品
            for (int i = 0; i < 27; i++) {
                contents[i] = guiInventory.getItem(EnderSeeGUI.ENDER_CHEST_START + i);
            }
            
            // 异步保存
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                boolean success = gui.saveOfflineData(contents);
                if (success) {
                    plugin.getLogger().info("已保存离线玩家 " + gui.getTargetName() + " 的末影箱数据");
                } else {
                    plugin.getLogger().warning("保存离线玩家 " + gui.getTargetName() + " 的末影箱数据失败");
                }
            });
        }
    }
    
    /**
     * 清理所有记录
     */
    public void clearAll() {
        openGUIs.clear();
    }
}
