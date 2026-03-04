package com.oolonghoo.woomni.module.inventory.gui;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.module.inventory.InventorySettings;
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
    
    private final WooOmni plugin;
    private final InventorySettings settings;
    private final Inventory inventory;
    private final UUID targetUUID;
    private final String targetName;
    private final Player onlineTarget;
    private final boolean canEdit;
    
    // 按钮槽位常量
    public static final int SLOT_COPY = 0;
    public static final int SLOT_CLEAR = 1;
    public static final int SLOT_TOGGLE = 2;
    public static final int SLOT_INFO = 3;
    public static final int SLOT_LOG = 4;
    
    // 末影箱物品起始槽位
    public static final int ENDER_CHEST_START = 18;
    public static final int ENDER_CHEST_END = 44;
    
    public EnderSeeGUI(WooOmni plugin, InventorySettings settings, UUID targetUUID, 
                       String targetName, Player onlineTarget, boolean canEdit) {
        this.plugin = plugin;
        this.settings = settings;
        this.targetUUID = targetUUID;
        this.targetName = targetName;
        this.onlineTarget = onlineTarget;
        this.canEdit = canEdit;
        
        // 创建5行GUI
        String title = "末影箱 - " + targetName;
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
        // 复制按钮
        ItemStack copyButton = settings.getButtonItem("copy");
        if (copyButton == null) {
            copyButton = createDefaultButton(Material.CHEST, "复制末影箱", NamedTextColor.GREEN);
        }
        inventory.setItem(SLOT_COPY, copyButton);
        
        // 清空按钮
        ItemStack clearButton = settings.getButtonItem("clear");
        if (clearButton == null) {
            clearButton = createDefaultButton(Material.BARRIER, "清空末影箱", NamedTextColor.RED);
        }
        inventory.setItem(SLOT_CLEAR, clearButton);
        
        // 切换按钮（切换到背包视图）
        ItemStack toggleButton = settings.getButtonItem("toggle");
        if (toggleButton == null) {
            toggleButton = createDefaultButton(Material.LEVER, "查看背包", NamedTextColor.YELLOW);
        }
        inventory.setItem(SLOT_TOGGLE, toggleButton);
        
        // 信息按钮
        ItemStack infoButton = settings.getButtonItem("info");
        if (infoButton == null) {
            infoButton = createDefaultButton(Material.BOOK, "玩家信息", NamedTextColor.AQUA);
        }
        inventory.setItem(SLOT_INFO, infoButton);
        
        // 记录按钮
        ItemStack logButton = settings.getButtonItem("log");
        if (logButton == null) {
            logButton = createDefaultButton(Material.WRITABLE_BOOK, "数据记录", NamedTextColor.LIGHT_PURPLE);
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
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetUUID);
        if (offlinePlayer.getPlayer() != null) {
            // 玩家实际上在线（可能刚刚登录）
            ItemStack[] enderContents = offlinePlayer.getPlayer().getEnderChest().getContents();
            for (int i = 0; i < 27; i++) {
                if (enderContents[i] != null) {
                    inventory.setItem(ENDER_CHEST_START + i, enderContents[i].clone());
                }
            }
        }
        // 注意：完整的离线玩家数据加载需要读取世界数据文件
        // 这里暂时只处理在线玩家，离线玩家数据加载可以在后续版本中实现
    }
    
    /**
     * 创建默认按钮
     */
    private ItemStack createDefaultButton(Material material, String name, NamedTextColor color) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, color));
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
            viewer.sendMessage(Component.text("你没有权限执行此操作。", NamedTextColor.RED));
            return;
        }
        
        // 复制目标玩家的末影箱到查看者的末影箱
        if (onlineTarget != null) {
            ItemStack[] enderContents = onlineTarget.getEnderChest().getContents();
            viewer.getEnderChest().setContents(enderContents.clone());
            viewer.sendMessage(Component.text("已复制 " + targetName + " 的末影箱内容到你的末影箱。", NamedTextColor.GREEN));
        } else {
            viewer.sendMessage(Component.text("无法复制离线玩家的末影箱。", NamedTextColor.RED));
        }
    }
    
    /**
     * 处理清空按钮
     */
    private void handleClear(Player viewer) {
        if (!canEdit) {
            viewer.sendMessage(Component.text("你没有权限执行此操作。", NamedTextColor.RED));
            return;
        }
        
        // 清空目标玩家的末影箱
        if (onlineTarget != null) {
            onlineTarget.getEnderChest().clear();
            // 更新GUI
            for (int i = ENDER_CHEST_START; i <= ENDER_CHEST_END; i++) {
                inventory.setItem(i, null);
            }
            viewer.sendMessage(Component.text("已清空 " + targetName + " 的末影箱。", NamedTextColor.GREEN));
        } else {
            viewer.sendMessage(Component.text("无法清空离线玩家的末影箱。", NamedTextColor.RED));
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
            // 显示在线玩家的实时状态
            double health = onlineTarget.getHealth();
            double maxHealth = onlineTarget.getMaxHealth();
            float exp = onlineTarget.getExp();
            int level = onlineTarget.getLevel();
            int foodLevel = onlineTarget.getFoodLevel();
            float saturation = onlineTarget.getSaturation();
            
            List<Component> info = new ArrayList<>();
            info.add(Component.text("========== 玩家信息 ==========", NamedTextColor.GOLD));
            info.add(Component.text("玩家: ", NamedTextColor.GRAY)
                    .append(Component.text(targetName, NamedTextColor.GREEN)));
            info.add(Component.text("生命值: ", NamedTextColor.GRAY)
                    .append(Component.text(String.format("%.1f/%.1f", health, maxHealth), NamedTextColor.RED)));
            info.add(Component.text("饥饿值: ", NamedTextColor.GRAY)
                    .append(Component.text(String.valueOf(foodLevel) + "/20", NamedTextColor.YELLOW)));
            info.add(Component.text("饱和度: ", NamedTextColor.GRAY)
                    .append(Component.text(String.format("%.1f", saturation), NamedTextColor.YELLOW)));
            info.add(Component.text("经验等级: ", NamedTextColor.GRAY)
                    .append(Component.text(String.valueOf(level), NamedTextColor.GREEN)));
            info.add(Component.text("经验值: ", NamedTextColor.GRAY)
                    .append(Component.text(String.format("%.1f%%", exp * 100), NamedTextColor.GREEN)));
            info.add(Component.text("游戏模式: ", NamedTextColor.GRAY)
                    .append(Component.text(onlineTarget.getGameMode().name(), NamedTextColor.AQUA)));
            info.add(Component.text("位置: ", NamedTextColor.GRAY)
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
            viewer.sendMessage(Component.text("玩家 " + targetName + " 当前离线，无法获取实时信息。", NamedTextColor.YELLOW));
        }
    }
    
    /**
     * 处理记录按钮
     */
    private void handleLog(Player viewer) {
        // 数据记录功能暂未实现
        viewer.sendMessage(Component.text("此功能暂未实现", NamedTextColor.YELLOW));
    }
    
    /**
     * 同步末影箱内容到目标玩家
     * 在编辑操作后调用
     */
    public void syncToTarget() {
        if (onlineTarget == null) {
            return;
        }
        
        ItemStack[] contents = new ItemStack[27];
        for (int i = 0; i < 27; i++) {
            contents[i] = inventory.getItem(ENDER_CHEST_START + i);
        }
        
        onlineTarget.getEnderChest().setContents(contents);
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
