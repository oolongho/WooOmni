package com.oolonghoo.woomni.module.inventory.gui;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.module.inventory.InventorySettings;
import com.oolonghoo.woomni.module.inventory.OfflinePlayerDataUtil;
import com.oolonghoo.woomni.util.GUILocale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 末影箱查看GUI
 * 5行布局：
 * - 第1行(0-8): 功能按钮区域
 * - 第2行(9-17): 填充物品
 * - 第3-5行(18-44): 末影箱物品（27格）
 */
public class EnderSeeGUI implements InventoryHolder {
    
    private final InventorySettings settings;
    private final OfflinePlayerDataUtil dataUtil;
    private final Inventory inventory;
    private final UUID targetUUID;
    private final String targetName;
    private final Player onlineTarget;
    private final boolean canEdit;
    private final boolean isOnline;
    
    // 末影箱内容（用于离线玩家）
    private ItemStack[] enderChestContents;
    
    // 按钮槽位常量
    public static final int SLOT_COPY = 0;
    public static final int SLOT_CLEAR = 1;
    public static final int SLOT_TOGGLE = 2;
    public static final int SLOT_INFO = 3;
    public static final int SLOT_LOG = 4;
    
    // 末影箱物品起始槽位
    public static final int ENDER_CHEST_START = 18;
    public static final int ENDER_CHEST_END = 44;
    
    public EnderSeeGUI(InventorySettings settings, OfflinePlayerDataUtil dataUtil, UUID targetUUID, String targetName, Player onlineTarget, boolean canEdit) {
        this.settings = settings;
        this.dataUtil = dataUtil;
        this.targetUUID = targetUUID;
        this.targetName = targetName;
        this.onlineTarget = onlineTarget;
        this.canEdit = canEdit;
        this.isOnline = onlineTarget != null;
        
        String titleKey = isOnline ? "gui.endersee.title" : "gui.endersee.title-offline";
        String title = GUILocale.get(titleKey, "player", targetName);
        if (!canEdit) {
            title += " (仅查看)";
        }
        this.inventory = Bukkit.createInventory(this, 45, Component.text(title));
        
        setupItems();
    }
    
    /**
     * 设置GUI物品
     */
    private void setupItems() {
        // 设置功能按钮
        setupButtons();
        
        // 设置填充物品
        setupFiller();
        
        // 设置末影箱物品
        setupEnderChestItems();
    }
    
    /**
     * 设置功能按钮
     */
    private void setupButtons() {
        ItemStack copyButton = settings.getButtonItem("copy");
        if (copyButton == null) {
            copyButton = createDefaultButton(Material.CHEST, "gui.endersee.button-copy", NamedTextColor.GREEN);
        }
        inventory.setItem(SLOT_COPY, copyButton);
        
        ItemStack clearButton = settings.getButtonItem("clear");
        if (clearButton == null) {
            clearButton = createDefaultButton(Material.BARRIER, "gui.endersee.button-clear", NamedTextColor.RED);
        }
        inventory.setItem(SLOT_CLEAR, clearButton);
        
        ItemStack toggleButton = settings.getButtonItem("toggle");
        if (toggleButton == null) {
            toggleButton = createDefaultButton(Material.LEVER, "gui.endersee.button-view-inv", NamedTextColor.YELLOW);
        }
        inventory.setItem(SLOT_TOGGLE, toggleButton);
        
        ItemStack infoButton = settings.getButtonItem("info");
        if (infoButton == null) {
            infoButton = createDefaultButton(Material.BOOK, "gui.endersee.button-info", NamedTextColor.AQUA);
        }
        inventory.setItem(SLOT_INFO, infoButton);
        
        ItemStack logButton = settings.getButtonItem("log");
        if (logButton == null) {
            logButton = createDefaultButton(Material.WRITABLE_BOOK, "gui.endersee.button-log", NamedTextColor.LIGHT_PURPLE);
        }
        inventory.setItem(SLOT_LOG, logButton);
    }
    
    /**
     * 设置填充物品
     */
    private void setupFiller() {
        ItemStack filler = settings.getFillerItem();
        
        // 第1行剩余位置（5-8）
        for (int i = 5; i <= 8; i++) {
            inventory.setItem(i, filler);
        }
        
        // 第2行（9-17）
        for (int i = 9; i <= 17; i++) {
            inventory.setItem(i, filler);
        }
    }
    
    /**
     * 设置末影箱物品
     */
    private void setupEnderChestItems() {
        if (onlineTarget != null) {
            // 在线玩家 - 直接获取末影箱内容
            ItemStack[] enderContents = onlineTarget.getEnderChest().getContents();
            enderChestContents = enderContents;
            for (int i = 0; i < 27; i++) {
                if (enderContents[i] != null) {
                    inventory.setItem(ENDER_CHEST_START + i, enderContents[i].clone());
                }
            }
        } else {
            // 离线玩家 - 从数据文件加载
            loadOfflineEnderChest();
        }
    }
    
    /**
     * 加载离线玩家的末影箱数据
     */
    private void loadOfflineEnderChest() {
        if (dataUtil != null && dataUtil.hasPlayerData(targetUUID)) {
            // 使用NMS加载离线玩家末影箱数据
            enderChestContents = dataUtil.loadOfflineEnderChest(targetUUID);
            
            if (enderChestContents != null) {
                for (int i = 0; i < 27; i++) {
                    if (i < enderChestContents.length && enderChestContents[i] != null) {
                        inventory.setItem(ENDER_CHEST_START + i, enderChestContents[i].clone());
                    }
                }
            }
        } else {
            // 没有数据文件，使用空数组
            enderChestContents = new ItemStack[27];
        }
    }
    
    /**
     * 创建默认按钮
     */
    private ItemStack createDefaultButton(Material material, String localeKey, NamedTextColor color) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(GUILocale.get(localeKey), color));
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * 处理按钮点击
     * @param slot 点击的槽位
     * @param viewer 查看者
     * @return 是否处理成功
     */
    public boolean handleClick(int slot, Player viewer) {
        switch (slot) {
            case SLOT_COPY:
                handleCopy(viewer);
                return true;
            case SLOT_CLEAR:
                handleClear(viewer);
                return true;
            case SLOT_TOGGLE:
                handleToggle(viewer);
                return true;
            case SLOT_INFO:
                handleInfo(viewer);
                return true;
            case SLOT_LOG:
                handleLog(viewer);
                return true;
            default:
                // 检查是否是末影箱区域
                if (slot >= ENDER_CHEST_START && slot <= ENDER_CHEST_END) {
                    // 末影箱物品点击由 InventoryListener 处理
                    return false;
                }
                return false;
        }
    }
    
    /**
     * 处理复制按钮
     */
    private void handleCopy(Player viewer) {
        if (!canEdit) {
            viewer.sendMessage(Component.text(GUILocale.get("gui.endersee.no-permission"), NamedTextColor.RED));
            return;
        }
        
        if (onlineTarget != null) {
            ItemStack[] enderContents = onlineTarget.getEnderChest().getContents();
            viewer.getEnderChest().setContents(enderContents.clone());
            viewer.sendMessage(Component.text(GUILocale.get("gui.endersee.copied", "player", targetName), NamedTextColor.GREEN));
        } else {
            viewer.sendMessage(Component.text(GUILocale.get("gui.endersee.cannot-copy-offline"), NamedTextColor.RED));
        }
    }
    
    private void handleClear(Player viewer) {
        if (!canEdit) {
            viewer.sendMessage(Component.text(GUILocale.get("gui.endersee.no-permission"), NamedTextColor.RED));
            return;
        }
        
        if (onlineTarget != null) {
            onlineTarget.getEnderChest().clear();
            for (int i = ENDER_CHEST_START; i <= ENDER_CHEST_END; i++) {
                inventory.setItem(i, null);
            }
            viewer.sendMessage(Component.text(GUILocale.get("gui.endersee.cleared", "player", targetName), NamedTextColor.GREEN));
        } else {
            viewer.sendMessage(Component.text(GUILocale.get("gui.endersee.cannot-clear-offline"), NamedTextColor.RED));
        }
    }
    
    /**
     * 处理切换按钮（切换到背包视图）
     */
    private void handleToggle(Player viewer) {
        viewer.closeInventory();
        viewer.performCommand("inv " + targetName);
    }
    
    /**
     * 处理信息按钮
     */
    private void handleInfo(Player viewer) {
        if (onlineTarget != null) {
            double health = onlineTarget.getHealth();
            double maxHealth = onlineTarget.getMaxHealth();
            float exp = onlineTarget.getExp();
            int level = onlineTarget.getLevel();
            int foodLevel = onlineTarget.getFoodLevel();
            float saturation = onlineTarget.getSaturation();
            
            List<Component> info = new ArrayList<>();
            info.add(Component.text(GUILocale.get("gui.endersee.info-header"), NamedTextColor.GOLD));
            info.add(Component.text(GUILocale.get("gui.endersee.player") + ": ", NamedTextColor.GRAY)
                    .append(Component.text(targetName, NamedTextColor.GREEN)));
            info.add(Component.text(GUILocale.get("gui.endersee.health") + ": ", NamedTextColor.GRAY)
                    .append(Component.text(String.format("%.1f/%.1f", health, maxHealth), NamedTextColor.RED)));
            info.add(Component.text(GUILocale.get("gui.endersee.hunger") + ": ", NamedTextColor.GRAY)
                    .append(Component.text(String.valueOf(foodLevel) + "/20", NamedTextColor.YELLOW)));
            info.add(Component.text(GUILocale.get("gui.endersee.saturation") + ": ", NamedTextColor.GRAY)
                    .append(Component.text(String.format("%.1f", saturation), NamedTextColor.YELLOW)));
            info.add(Component.text(GUILocale.get("gui.endersee.level") + ": ", NamedTextColor.GRAY)
                    .append(Component.text(String.valueOf(level), NamedTextColor.GREEN)));
            info.add(Component.text(GUILocale.get("gui.endersee.exp") + ": ", NamedTextColor.GRAY)
                    .append(Component.text(String.format("%.1f%%", exp * 100), NamedTextColor.GREEN)));
            info.add(Component.text(GUILocale.get("gui.endersee.gamemode") + ": ", NamedTextColor.GRAY)
                    .append(Component.text(onlineTarget.getGameMode().name(), NamedTextColor.AQUA)));
            info.add(Component.text(GUILocale.get("gui.endersee.location") + ": ", NamedTextColor.GRAY)
                    .append(Component.text(String.format("%s (%.1f, %.1f, %.1f)", 
                            onlineTarget.getWorld().getName(), 
                            onlineTarget.getLocation().getX(), 
                            onlineTarget.getLocation().getY(), 
                            onlineTarget.getLocation().getZ()), NamedTextColor.WHITE)));
            info.add(Component.text("==============================", NamedTextColor.GOLD));
            
            for (Component line : info) {
                viewer.sendMessage(line);
            }
        } else {
            viewer.sendMessage(Component.text(GUILocale.get("gui.endersee.offline-info", "player", targetName), NamedTextColor.YELLOW));
        }
    }
    
    private void handleLog(Player viewer) {
        viewer.sendMessage(Component.text(GUILocale.get("gui.endersee.feature-not-implemented"), NamedTextColor.YELLOW));
    }
    
    /**
     * 同步末影箱内容到目标玩家
     * 在编辑操作后调用
     */
    public void syncToTarget() {
        ItemStack[] contents = new ItemStack[27];
        for (int i = 0; i < 27; i++) {
            contents[i] = inventory.getItem(ENDER_CHEST_START + i);
        }
        
        if (onlineTarget != null) {
            // 在线玩家：直接设置
            onlineTarget.getEnderChest().setContents(contents);
        } else {
            // 离线玩家：保存到文件
            saveOfflineData(contents);
        }
    }
    
    /**
     * 保存离线玩家数据
     */
    public boolean saveOfflineData(ItemStack[] contents) {
        if (dataUtil != null) {
            return dataUtil.saveOfflineEnderChest(targetUUID, contents);
        }
        return false;
    }
    
    /**
     * 刷新GUI内容
     */
    public void refresh() {
        // 清空末影箱区域
        for (int i = ENDER_CHEST_START; i <= ENDER_CHEST_END; i++) {
            inventory.setItem(i, null);
        }
        
        // 重新加载末影箱物品
        setupEnderChestItems();
    }
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
    
    public UUID getTargetUUID() {
        return targetUUID;
    }
    
    public String getTargetName() {
        return targetName;
    }
    
    public Player getOnlineTarget() {
        return onlineTarget;
    }
    
    public boolean canEdit() {
        return canEdit;
    }
    
    public boolean isOnline() {
        return isOnline;
    }
    
    public OfflinePlayerDataUtil getDataUtil() {
        return dataUtil;
    }
    
    /**
     * 检查槽位是否在末影箱区域
     */
    public static boolean isEnderChestSlot(int slot) {
        return slot >= ENDER_CHEST_START && slot <= ENDER_CHEST_END;
    }
    
    /**
     * 将GUI槽位转换为末影箱索引
     */
    public static int toEnderChestIndex(int slot) {
        return slot - ENDER_CHEST_START;
    }
    
    /**
     * 将末影箱索引转换为GUI槽位
     */
    public static int toGUISlot(int enderIndex) {
        return enderIndex + ENDER_CHEST_START;
    }
}
