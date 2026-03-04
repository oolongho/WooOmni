package com.oolonghoo.woomni.module.inventory.gui;

import com.oolonghoo.woomni.module.inventory.InventorySettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

/**
 * 背包查看GUI
 * 6行布局，包含功能按钮、装备栏、背包物品
 */
public class InvSeeGUI implements InventoryHolder {
    
    // GUI布局常量
    public static final int GUI_SIZE = 54; // 6行
    
    // 功能按钮位置
    public static final int SLOT_COPY = 0;      // 复制背包
    public static final int SLOT_CLEAR = 1;     // 清空背包
    public static final int SLOT_TOGGLE = 2;    // 切换到末影箱
    public static final int SLOT_INFO = 3;      // 玩家信息
    public static final int SLOT_LOG = 4;       // 数据记录
    
    // 装备栏位置
    public static final int SLOT_HELMET = 6;    // 头盔
    public static final int SLOT_CHESTPLATE = 7; // 胸甲
    public static final int SLOT_LEGGINGS = 15; // 护腿
    public static final int SLOT_BOOTS = 16;    // 靴子
    public static final int SLOT_OFFHAND = 17;  // 副手
    
    // 背包区域起始位置
    public static final int SLOT_INVENTORY_START = 18; // 背包第一行
    
    private final Inventory inventory;
    private final InventorySettings settings;
    private final UUID targetUUID;
    private final String targetName;
    private final boolean isOnline;
    private final boolean canEdit;
    
    // 玩家背包内容的副本（用于离线玩家）
    private ItemStack[] inventoryContents;
    private ItemStack[] armorContents;
    private ItemStack offHandItem;
    
    /**
     * 构造函数
     * @param settings 设置管理器
     * @param targetUUID 目标玩家UUID
     * @param targetName 目标玩家名称
     * @param isOnline 目标是否在线
     * @param canEdit 是否可编辑
     */
    public InvSeeGUI(InventorySettings settings, UUID targetUUID, String targetName, 
                     boolean isOnline, boolean canEdit) {
        this.settings = settings;
        this.targetUUID = targetUUID;
        this.targetName = targetName;
        this.isOnline = isOnline;
        this.canEdit = canEdit;
        
        // 创建库存
        String title = isOnline ? "背包 - " + targetName : "背包 (离线) - " + targetName;
        this.inventory = Bukkit.createInventory(this, GUI_SIZE, Component.text(title));
        
        // 加载玩家背包内容
        loadInventoryContents();
        
        // 设置GUI内容
        setupItems();
    }
    
    /**
     * 加载玩家背包内容
     */
    private void loadInventoryContents() {
        Player onlinePlayer = Bukkit.getPlayer(targetUUID);
        
        if (onlinePlayer != null) {
            // 在线玩家：直接获取背包内容
            inventoryContents = onlinePlayer.getInventory().getContents();
            armorContents = new ItemStack[] {
                onlinePlayer.getInventory().getBoots(),
                onlinePlayer.getInventory().getLeggings(),
                onlinePlayer.getInventory().getChestplate(),
                onlinePlayer.getInventory().getHelmet()
            };
            offHandItem = onlinePlayer.getInventory().getItemInOffHand();
        } else {
            // 离线玩家：从数据文件加载（简化处理，实际需要从NBT文件读取）
            // 这里创建空数组，实际实现需要读取玩家数据文件
            inventoryContents = new ItemStack[41];
            armorContents = new ItemStack[4];
            offHandItem = null;
            
            // TODO: 实现离线玩家数据加载
            // 可以使用 Bukkit.getWorlds().get(0).getWorldFolder() 获取世界文件夹
            // 然后读取 playerdata/<uuid>.dat 文件
        }
    }
    
    /**
     * 设置GUI内容
     */
    private void setupItems() {
        // 填充空白位置
        fillEmptySlots();
        
        // 设置功能按钮
        setupButtons();
        
        // 设置装备栏
        setupArmor();
        
        // 设置背包物品
        setupInventory();
    }
    
    /**
     * 填充空白位置
     */
    private void fillEmptySlots() {
        ItemStack filler = settings.getFillerItem();
        
        // 第一行空白位置
        inventory.setItem(5, filler);
        inventory.setItem(8, filler);
        
        // 第二行空白位置
        for (int i = 9; i < 15; i++) {
            inventory.setItem(i, filler);
        }
    }
    
    /**
     * 设置功能按钮
     */
    private void setupButtons() {
        // 复制按钮
        ItemStack copyButton = settings.getButtonItem("copy");
        if (copyButton == null) {
            copyButton = createDefaultButton(Material.CHEST, "复制背包", NamedTextColor.GREEN);
        }
        inventory.setItem(SLOT_COPY, copyButton);
        
        // 清空按钮
        ItemStack clearButton = settings.getButtonItem("clear");
        if (clearButton == null) {
            clearButton = createDefaultButton(Material.BARRIER, "清空背包", NamedTextColor.RED);
        }
        inventory.setItem(SLOT_CLEAR, clearButton);
        
        // 切换按钮
        ItemStack toggleButton = settings.getButtonItem("toggle");
        if (toggleButton == null) {
            toggleButton = createDefaultButton(Material.ENDER_CHEST, "查看末影箱", NamedTextColor.YELLOW);
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
     * 设置装备栏
     */
    private void setupArmor() {
        // 头盔 (armorContents[3])
        if (armorContents[3] != null) {
            inventory.setItem(SLOT_HELMET, armorContents[3].clone());
        } else {
            inventory.setItem(SLOT_HELMET, createEmptySlot("头盔"));
        }
        
        // 胸甲 (armorContents[2])
        if (armorContents[2] != null) {
            inventory.setItem(SLOT_CHESTPLATE, armorContents[2].clone());
        } else {
            inventory.setItem(SLOT_CHESTPLATE, createEmptySlot("胸甲"));
        }
        
        // 护腿 (armorContents[1])
        if (armorContents[1] != null) {
            inventory.setItem(SLOT_LEGGINGS, armorContents[1].clone());
        } else {
            inventory.setItem(SLOT_LEGGINGS, createEmptySlot("护腿"));
        }
        
        // 靴子 (armorContents[0])
        if (armorContents[0] != null) {
            inventory.setItem(SLOT_BOOTS, armorContents[0].clone());
        } else {
            inventory.setItem(SLOT_BOOTS, createEmptySlot("靴子"));
        }
        
        // 副手
        if (offHandItem != null && offHandItem.getType() != Material.AIR) {
            inventory.setItem(SLOT_OFFHAND, offHandItem.clone());
        } else {
            inventory.setItem(SLOT_OFFHAND, createEmptySlot("副手"));
        }
    }
    
    /**
     * 设置背包物品
     * 玩家背包结构：
     * - 0-8: 快捷栏
     * - 9-35: 背包物品（27格）
     * - 36-39: 盔甲
     * - 40: 副手
     * 
     * GUI布局：
     * - 第3行 (18-26): 背包第一行 (9-17)
     * - 第4行 (27-35): 背包第二行 (18-26)
     * - 第5行 (36-44): 背包第三行 (27-35)
     * - 第6行 (45-53): 快捷栏 (0-8)
     */
    private void setupInventory() {
        // 背包第一行 (玩家背包 9-17 -> GUI 18-26)
        for (int i = 0; i < 9; i++) {
            int playerSlot = 9 + i;
            int guiSlot = SLOT_INVENTORY_START + i;
            if (inventoryContents[playerSlot] != null) {
                inventory.setItem(guiSlot, inventoryContents[playerSlot].clone());
            }
        }
        
        // 背包第二行 (玩家背包 18-26 -> GUI 27-35)
        for (int i = 0; i < 9; i++) {
            int playerSlot = 18 + i;
            int guiSlot = 27 + i;
            if (inventoryContents[playerSlot] != null) {
                inventory.setItem(guiSlot, inventoryContents[playerSlot].clone());
            }
        }
        
        // 背包第三行 (玩家背包 27-35 -> GUI 36-44)
        for (int i = 0; i < 9; i++) {
            int playerSlot = 27 + i;
            int guiSlot = 36 + i;
            if (inventoryContents[playerSlot] != null) {
                inventory.setItem(guiSlot, inventoryContents[playerSlot].clone());
            }
        }
        
        // 快捷栏 (玩家背包 0-8 -> GUI 45-53)
        for (int i = 0; i < 9; i++) {
            int guiSlot = 45 + i;
            if (inventoryContents[i] != null) {
                inventory.setItem(guiSlot, inventoryContents[i].clone());
            }
        }
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
     * 创建空槽位提示物品
     */
    private ItemStack createEmptySlot(String name) {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, NamedTextColor.GRAY));
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * 判断是否为功能按钮槽位
     */
    public static boolean isButtonSlot(int slot) {
        return slot >= SLOT_COPY && slot <= SLOT_LOG;
    }
    
    /**
     * 判断是否为装备槽位
     */
    public static boolean isArmorSlot(int slot) {
        return slot == SLOT_HELMET || slot == SLOT_CHESTPLATE || 
               slot == SLOT_LEGGINGS || slot == SLOT_BOOTS || slot == SLOT_OFFHAND;
    }
    
    /**
     * 判断是否为背包槽位
     */
    public static boolean isInventorySlot(int slot) {
        return slot >= SLOT_INVENTORY_START;
    }
    
    /**
     * 将GUI槽位转换为玩家背包槽位
     */
    public static int guiSlotToPlayerSlot(int guiSlot) {
        if (guiSlot >= 45 && guiSlot <= 53) {
            // 快捷栏
            return guiSlot - 45;
        } else if (guiSlot >= 36 && guiSlot <= 44) {
            // 背包第三行
            return guiSlot - 36 + 27;
        } else if (guiSlot >= 27 && guiSlot <= 35) {
            // 背包第二行
            return guiSlot - 27 + 18;
        } else if (guiSlot >= 18 && guiSlot <= 26) {
            // 背包第一行
            return guiSlot - 18 + 9;
        }
        return -1;
    }
    
    /**
     * 将装备槽位转换为玩家背包槽位
     */
    public static int armorSlotToPlayerSlot(int guiSlot) {
        switch (guiSlot) {
            case SLOT_HELMET:
                return 39;
            case SLOT_CHESTPLATE:
                return 38;
            case SLOT_LEGGINGS:
                return 37;
            case SLOT_BOOTS:
                return 36;
            case SLOT_OFFHAND:
                return 40;
            default:
                return -1;
        }
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
    
    public boolean isOnline() {
        return isOnline;
    }
    
    public boolean canEdit() {
        return canEdit;
    }
    
    public InventorySettings getSettings() {
        return settings;
    }
}
