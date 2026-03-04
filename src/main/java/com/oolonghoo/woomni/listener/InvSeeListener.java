package com.oolonghoo.woomni.listener;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.config.MessageManager;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 背包查看GUI监听器
 * 处理GUI点击事件和权限检查
 */
public class InvSeeListener implements Listener {
    
    private final WooOmni plugin;
    private final MessageManager msg;
    
    // 记录打开的GUI
    private final Map<UUID, InvSeeGUI> openGUIs = new HashMap<>();
    
    public InvSeeListener(WooOmni plugin) {
        this.plugin = plugin;
        this.msg = plugin.getMessageManager();
    }
    
    /**
     * 注册打开的GUI
     */
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
        
        // 检查是否是InvSeeGUI
        if (!(topInventory.getHolder() instanceof InvSeeGUI)) {
            return;
        }
        
        InvSeeGUI gui = (InvSeeGUI) topInventory.getHolder();
        int rawSlot = event.getRawSlot();
        
        // 点击的是GUI内部
        if (rawSlot >= 0 && rawSlot < InvSeeGUI.GUI_SIZE) {
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
                    viewer.sendMessage(msg.getWithPrefix("inv.no-edit"));
                }
            }
        }
    }
    
    /**
     * 处理GUI内部点击
     */
    private void handleGUIClick(InventoryClickEvent event, Player viewer, InvSeeGUI gui, int slot) {
        // 功能按钮点击
        if (InvSeeGUI.isButtonSlot(slot)) {
            event.setCancelled(true);
            handleButtonClick(viewer, gui, slot);
            return;
        }
        
        // 装备槽位点击
        if (InvSeeGUI.isArmorSlot(slot)) {
            if (!gui.canEdit()) {
                event.setCancelled(true);
                viewer.sendMessage(msg.getWithPrefix("inv.no-edit"));
                return;
            }
            
            // 处理装备编辑
            handleArmorClick(event, viewer, gui, slot);
            return;
        }
        
        // 背包槽位点击
        if (InvSeeGUI.isInventorySlot(slot)) {
            if (!gui.canEdit()) {
                event.setCancelled(true);
                viewer.sendMessage(msg.getWithPrefix("inv.no-edit"));
                return;
            }
            
            // 处理背包编辑
            handleInventoryClick(event, viewer, gui, slot);
        }
    }
    
    /**
     * 处理功能按钮点击
     */
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
                handleInfo(viewer, gui);
                break;
            case InvSeeGUI.SLOT_LOG:
                handleLog(viewer, gui);
                break;
        }
    }
    
    /**
     * 处理复制背包
     */
    private void handleCopy(Player viewer, InvSeeGUI gui) {
        Player target = Bukkit.getPlayer(gui.getTargetUUID());
        if (target == null) {
            viewer.sendMessage(msg.getWithPrefix("inv.player-offline", "player", gui.getTargetName()));
            return;
        }
        
        // 复制目标玩家的背包内容到查看者的背包
        viewer.getInventory().setContents(target.getInventory().getContents());
        viewer.sendMessage(msg.getWithPrefix("inv.copied", "player", gui.getTargetName()));
    }
    
    /**
     * 处理清空背包
     */
    private void handleClear(Player viewer, InvSeeGUI gui) {
        if (!gui.canEdit()) {
            viewer.sendMessage(msg.getWithPrefix("inv.no-edit"));
            return;
        }
        
        Player target = Bukkit.getPlayer(gui.getTargetUUID());
        if (target == null) {
            viewer.sendMessage(msg.getWithPrefix("inv.player-offline", "player", gui.getTargetName()));
            return;
        }
        
        // 清空目标玩家的背包（包括装备栏）
        target.getInventory().clear();
        // 清空盔甲栏
        target.getInventory().setHelmet(null);
        target.getInventory().setChestplate(null);
        target.getInventory().setLeggings(null);
        target.getInventory().setBoots(null);
        // 清空副手
        target.getInventory().setItemInOffHand(null);
        
        viewer.sendMessage(msg.getWithPrefix("inv.cleared", "player", gui.getTargetName()));
        
        // 刷新GUI
        refreshGUI(viewer, gui);
    }
    
    /**
     * 处理切换到末影箱视图
     */
    private void handleToggle(Player viewer, InvSeeGUI gui) {
        // 关闭当前GUI并打开末影箱GUI
        viewer.closeInventory();
        viewer.performCommand("ender " + gui.getTargetName());
    }
    
    /**
     * 处理显示玩家信息
     */
    private void handleInfo(Player viewer, InvSeeGUI gui) {
        Player target = Bukkit.getPlayer(gui.getTargetUUID());
        if (target == null) {
            viewer.sendMessage(msg.getWithPrefix("inv.player-offline", "player", gui.getTargetName()));
            return;
        }
        
        // 显示玩家实时状态
        viewer.sendMessage(Component.text("========== 玩家信息 ==========", NamedTextColor.GOLD));
        viewer.sendMessage(Component.text("玩家: ", NamedTextColor.GRAY)
                .append(Component.text(target.getName(), NamedTextColor.WHITE)));
        viewer.sendMessage(Component.text("生命值: ", NamedTextColor.GRAY)
                .append(Component.text(String.format("%.1f/%.1f", target.getHealth(), target.getMaxHealth()), NamedTextColor.RED)));
        viewer.sendMessage(Component.text("饥饿值: ", NamedTextColor.GRAY)
                .append(Component.text(String.format("%d/20", target.getFoodLevel()), NamedTextColor.YELLOW)));
        viewer.sendMessage(Component.text("饱和度: ", NamedTextColor.GRAY)
                .append(Component.text(String.format("%.1f", target.getSaturation()), NamedTextColor.YELLOW)));
        viewer.sendMessage(Component.text("经验等级: ", NamedTextColor.GRAY)
                .append(Component.text(String.valueOf(target.getLevel()), NamedTextColor.GREEN)));
        viewer.sendMessage(Component.text("经验值: ", NamedTextColor.GRAY)
                .append(Component.text(String.format("%.1f%%", target.getExp() * 100), NamedTextColor.GREEN)));
        viewer.sendMessage(Component.text("游戏模式: ", NamedTextColor.GRAY)
                .append(Component.text(target.getGameMode().name(), NamedTextColor.AQUA)));
        viewer.sendMessage(Component.text("位置: ", NamedTextColor.GRAY)
                .append(Component.text(String.format("%s (%.1f, %.1f, %.1f)", 
                        target.getWorld().getName(), 
                        target.getLocation().getX(), 
                        target.getLocation().getY(), 
                        target.getLocation().getZ()), NamedTextColor.WHITE)));
    }
    
    /**
     * 处理显示数据记录
     */
    private void handleLog(Player viewer, InvSeeGUI gui) {
        // 数据记录功能暂未实现
        viewer.sendMessage(msg.getWithPrefix("inv.feature-not-implemented"));
    }
    
    /**
     * 处理装备槽位点击
     */
    private void handleArmorClick(InventoryClickEvent event, Player viewer, InvSeeGUI gui, int slot) {
        Player target = Bukkit.getPlayer(gui.getTargetUUID());
        if (target == null) {
            event.setCancelled(true);
            viewer.sendMessage(msg.getWithPrefix("inv.player-offline", "player", gui.getTargetName()));
            return;
        }
        
        int playerSlot = InvSeeGUI.armorSlotToPlayerSlot(slot);
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();
        
        // 处理不同的点击动作
        switch (event.getAction()) {
            case PICKUP_ALL:
            case PICKUP_SOME:
            case PICKUP_HALF:
            case PICKUP_ONE:
                // 取出物品
                target.getInventory().setItem(playerSlot, null);
                break;
            case PLACE_ALL:
            case PLACE_SOME:
            case PLACE_ONE:
                // 放入物品
                target.getInventory().setItem(playerSlot, cursorItem.clone());
                event.setCancelled(true);
                break;
            case SWAP_WITH_CURSOR:
                // 交换物品
                target.getInventory().setItem(playerSlot, cursorItem.clone());
                break;
            case MOVE_TO_OTHER_INVENTORY:
                // Shift+点击
                event.setCancelled(true);
                return;
            default:
                break;
        }
        
        // 延迟刷新GUI
        Bukkit.getScheduler().runTaskLater(plugin, () -> refreshGUI(viewer, gui), 1L);
    }
    
    /**
     * 处理背包槽位点击
     */
    private void handleInventoryClick(InventoryClickEvent event, Player viewer, InvSeeGUI gui, int slot) {
        Player target = Bukkit.getPlayer(gui.getTargetUUID());
        if (target == null) {
            event.setCancelled(true);
            viewer.sendMessage(msg.getWithPrefix("inv.player-offline", "player", gui.getTargetName()));
            return;
        }
        
        int playerSlot = InvSeeGUI.guiSlotToPlayerSlot(slot);
        if (playerSlot < 0) {
            event.setCancelled(true);
            return;
        }
        
        ItemStack cursorItem = event.getCursor();
        
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
                handleShiftClick(event, viewer, target, playerSlot);
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
            syncToTarget(target, gui);
            refreshGUI(viewer, gui);
        }, 1L);
    }
    
    /**
     * 处理Shift+点击
     */
    private void handleShiftClick(InventoryClickEvent event, Player viewer, Player target, int playerSlot) {
        event.setCancelled(true);
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        
        // 从目标玩家背包移除物品并添加到查看者背包
        target.getInventory().setItem(playerSlot, null);
        viewer.getInventory().addItem(clickedItem.clone());
        
        // 刷新GUI
        InvSeeGUI gui = (InvSeeGUI) event.getView().getTopInventory().getHolder();
        Bukkit.getScheduler().runTaskLater(plugin, () -> refreshGUI(viewer, gui), 1L);
    }
    
    /**
     * 同步GUI内容到目标玩家
     */
    private void syncToTarget(Player target, InvSeeGUI gui) {
        Inventory guiInventory = gui.getInventory();
        
        // 同步背包物品
        for (int guiSlot = InvSeeGUI.SLOT_INVENTORY_START; guiSlot < InvSeeGUI.GUI_SIZE; guiSlot++) {
            int playerSlot = InvSeeGUI.guiSlotToPlayerSlot(guiSlot);
            if (playerSlot >= 0) {
                ItemStack item = guiInventory.getItem(guiSlot);
                target.getInventory().setItem(playerSlot, item);
            }
        }
        
        // 同步装备
        int[] armorSlots = {InvSeeGUI.SLOT_HELMET, InvSeeGUI.SLOT_CHESTPLATE, 
                           InvSeeGUI.SLOT_LEGGINGS, InvSeeGUI.SLOT_BOOTS, InvSeeGUI.SLOT_OFFHAND};
        for (int slot : armorSlots) {
            int playerSlot = InvSeeGUI.armorSlotToPlayerSlot(slot);
            if (playerSlot >= 0) {
                ItemStack item = guiInventory.getItem(slot);
                target.getInventory().setItem(playerSlot, item);
            }
        }
    }
    
    /**
     * 刷新GUI
     */
    private void refreshGUI(Player viewer, InvSeeGUI oldGui) {
        Player target = Bukkit.getPlayer(oldGui.getTargetUUID());
        if (target == null) {
            return;
        }
        
        // 创建新的GUI实例
        InvSeeGUI newGui = new InvSeeGUI(
            oldGui.getSettings(),
            oldGui.getTargetUUID(),
            oldGui.getTargetName(),
            true,
            oldGui.canEdit()
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
        
        // 如果没有编辑权限，取消拖拽
        if (!gui.canEdit()) {
            event.setCancelled(true);
            return;
        }
        
        // 检查是否拖拽到了功能按钮区域
        for (int slot : event.getRawSlots()) {
            if (slot < InvSeeGUI.GUI_SIZE && InvSeeGUI.isButtonSlot(slot)) {
                event.setCancelled(true);
                return;
            }
        }
        
        // 同步到目标玩家
        Player target = Bukkit.getPlayer(gui.getTargetUUID());
        if (target != null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> syncToTarget(target, gui), 1L);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        openGUIs.remove(player.getUniqueId());
    }
    
    /**
     * 清理所有记录
     */
    public void clearAll() {
        openGUIs.clear();
    }
}
