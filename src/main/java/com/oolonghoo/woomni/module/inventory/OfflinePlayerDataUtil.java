package com.oolonghoo.woomni.module.inventory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 离线玩家数据工具类
 * 使用 NMS 反射读取和保存离线玩家的背包和末影箱数据
 * 支持优雅降级和详细的错误处理
 */
public class OfflinePlayerDataUtil {

    private final JavaPlugin plugin;
    
    // 物品槽位常量
    private static final int INVENTORY_SIZE = 41; // 36个背包槽位 + 4个装备槽位 + 1个副手
    private static final int ENDER_CHEST_SIZE = 27;
    
    // 装备槽位映射 (NBT Slot -> 数组索引)
    private static final int SLOT_BOOTS = 100;
    private static final int SLOT_LEGGINGS = 101;
    private static final int SLOT_CHESTPLATE = 102;
    private static final int SLOT_HELMET = 103;
    private static final int SLOT_OFFHAND = -106;
    
    // NMS 反射缓存
    private static boolean nmsInitialized = false;
    private static boolean nmsAvailable = false;
    private static String serverVersion;
    private static String nmsVersion;
    
    // NMS 类缓存
    private static Class<?> nbtTagCompoundClass;
    private static Class<?> nbtTagListClass;
    private static Class<?> nbtCompressedStreamToolsClass;
    private static Class<?> craftItemStackClass;
    private static Class<?> itemStackClass;
    private static Class<?> nbtBaseClass;
    
    // NMS 方法缓存
    private static Method methodSaveItemStack;
    private static Method methodLoadItemStack;
    private static Method methodNbtSetInt;
    private static Method methodNbtSetByte;
    private static Method methodNbtSetString;
    private static Method methodNbtSetTag;
    private static Method methodNbtGetInt;
    private static Method methodNbtGetByte;
    private static Method methodNbtGetString;
    private static Method methodNbtGetCompound;
    private static Method methodNbtHasKey;
    private static Method methodNbtGetList;
    private static Method methodNbtListSize;
    private static Method methodNbtListGet;
    private static Method methodNbtListAdd;
    private static Method methodNbtCompoundSet;
    private static Method methodNbtCompoundGetByte;
    private static Method methodNbtCompoundGetString;
    private static Method methodNbtCompoundGetCompound;
    private static Method methodNbtCompoundGetList;
    private static Method methodNbtCompoundSetByte;
    private static Method methodNbtCompoundSetString;
    private static Method methodNbtCompoundSetInt;
    private static Method methodNbtCompoundHasKey;
    private static Method methodNbtCompoundSetBoolean;
    private static Method methodNbtCompoundGetBoolean;
    private static Method methodCompressedStreamToolsRead;
    private static Method methodCompressedStreamToolsWrite;
    private static Method methodCraftItemStackAsNMSCopy;
    private static Method methodCraftItemStackAsBukkitCopy;
    
    public OfflinePlayerDataUtil(JavaPlugin plugin) {
        this.plugin = plugin;
        initializeNMS();
    }
    
    /**
     * 初始化 NMS 反射
     * 使用双重检查锁定保证线程安全
     */
    private void initializeNMS() {
        if (nmsInitialized) {
            return;
        }
        
        synchronized (OfflinePlayerDataUtil.class) {
            if (nmsInitialized) {
                return;
            }
            
            nmsInitialized = true;
        
        try {
            // 检测服务器版本
            serverVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            plugin.getLogger().info("检测到服务器版本: " + serverVersion);
            
            // 对于 Paper 1.20.5+ (包括 1.21+)，NMS 类名已更改
            // 不再使用版本号前缀
            boolean useVersionedNMS = serverVersion.contains("v");
            
            if (useVersionedNMS) {
                nmsVersion = "net.minecraft.server." + serverVersion + ".";
            } else {
                // Paper 1.20.5+ 使用统一的类名
                nmsVersion = "net.minecraft.";
            }
            
            plugin.getLogger().info("使用 NMS 路径: " + nmsVersion);
            
            // 加载 NMS 类
            loadNMSClasses(useVersionedNMS);
            
            // 加载 NMS 方法
            loadNMSMethods();
            
            nmsAvailable = true;
            plugin.getLogger().info("NMS 反射初始化成功，离线玩家数据加载功能已启用");
            
        } catch (ClassNotFoundException e) {
            nmsAvailable = false;
            plugin.getLogger().log(Level.WARNING, "NMS 反射初始化失败: 未找到必要的 NMS 类");
            plugin.getLogger().log(Level.WARNING, "服务器版本可能不兼容，离线玩家数据加载将使用降级方案");
            plugin.getLogger().log(Level.WARNING, "错误详情: " + e.getMessage());
            
            if (plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().log(Level.WARNING, "详细错误信息:", e);
            }
            
        } catch (NoSuchMethodException e) {
            nmsAvailable = false;
            plugin.getLogger().log(Level.WARNING, "NMS 反射初始化失败: 未找到必要的 NMS 方法");
            plugin.getLogger().log(Level.WARNING, "服务器版本可能不兼容，离线玩家数据加载将使用降级方案");
            plugin.getLogger().log(Level.WARNING, "错误详情: " + e.getMessage());
            
            if (plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().log(Level.WARNING, "详细错误信息:", e);
            }
            
        } catch (Exception e) {
            nmsAvailable = false;
            plugin.getLogger().log(Level.WARNING, "NMS 反射初始化失败: " + e.getMessage());
            plugin.getLogger().log(Level.WARNING, "离线玩家数据加载将使用降级方案");
            
            if (plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().log(Level.WARNING, "详细错误信息:", e);
            }
        }
        }
    }
    
    /**
     * 加载 NMS 类
     */
    private void loadNMSClasses(boolean useVersionedNMS) throws ClassNotFoundException {
        if (useVersionedNMS) {
            // 旧版本 (1.20.4 及以下)
            nbtTagCompoundClass = Class.forName(nmsVersion + "NBTTagCompound");
            nbtTagListClass = Class.forName(nmsVersion + "NBTTagList");
            nbtCompressedStreamToolsClass = Class.forName(nmsVersion + "NBTCompressedStreamTools");
            itemStackClass = Class.forName(nmsVersion + "ItemStack");
            nbtBaseClass = Class.forName(nmsVersion + "NBTBase");
        } else {
            // 新版本 (Paper 1.20.5+ / 1.21+)
            nbtTagCompoundClass = Class.forName("net.minecraft.nbt.NBTTagCompound");
            nbtTagListClass = Class.forName("net.minecraft.nbt.NBTTagList");
            nbtCompressedStreamToolsClass = Class.forName("net.minecraft.nbt.NBTCompressedStreamTools");
            itemStackClass = Class.forName("net.minecraft.world.item.ItemStack");
            nbtBaseClass = Class.forName("net.minecraft.nbt.NBTBase");
        }
        
        // CraftBukkit 类（始终使用版本号）
        craftItemStackClass = Class.forName("org.bukkit.craftbukkit." + serverVersion + ".inventory.CraftItemStack");
    }
    
    /**
     * 加载 NMS 方法
     */
    private void loadNMSMethods() throws NoSuchMethodException, ClassNotFoundException {
        // ItemStack 保存/加载方法
        // 在 1.20.5+ 中，方法签名已更改
        try {
            methodSaveItemStack = itemStackClass.getMethod("save", nbtTagCompoundClass);
            methodLoadItemStack = itemStackClass.getMethod("a", nbtTagCompoundClass);
        } catch (NoSuchMethodException e) {
            // 尝试其他方法名（不同版本的映射）
            try {
                methodSaveItemStack = itemStackClass.getMethod("b", nbtTagCompoundClass);
                methodLoadItemStack = itemStackClass.getMethod("a", nbtTagCompoundClass);
            } catch (NoSuchMethodException e2) {
                // 再尝试其他可能的名称
                for (Method m : itemStackClass.getMethods()) {
                    if (m.getName().equals("save") && m.getParameterCount() == 1 && 
                        m.getParameterTypes()[0] == nbtTagCompoundClass) {
                        methodSaveItemStack = m;
                    }
                    if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == nbtTagCompoundClass &&
                        m.getReturnType() == itemStackClass) {
                        methodLoadItemStack = m;
                    }
                }
                if (methodSaveItemStack == null || methodLoadItemStack == null) {
                    throw new NoSuchMethodException("无法找到 ItemStack 的 save/load 方法");
                }
            }
        }
        
        // NBT Compound 方法
        methodNbtCompoundSetByte = nbtTagCompoundClass.getMethod("a", String.class, byte.class);
        methodNbtCompoundSetString = nbtTagCompoundClass.getMethod("a", String.class, String.class);
        methodNbtCompoundSetInt = nbtTagCompoundClass.getMethod("a", String.class, int.class);
        methodNbtCompoundSetBoolean = nbtTagCompoundClass.getMethod("a", String.class, boolean.class);
        methodNbtCompoundGetByte = nbtTagCompoundClass.getMethod("f", String.class);
        methodNbtCompoundGetString = nbtTagCompoundClass.getMethod("l", String.class);
        methodNbtCompoundGetCompound = nbtTagCompoundClass.getMethod("p", String.class);
        methodNbtCompoundGetList = nbtTagCompoundClass.getMethod("c", String.class, int.class);
        methodNbtCompoundHasKey = nbtTagCompoundClass.getMethod("e", String.class);
        
        // 尝试其他可能的方法名
        try {
            methodNbtCompoundGetByte = nbtTagCompoundClass.getMethod("getByte", String.class);
        } catch (NoSuchMethodException ignored) {}
        
        try {
            methodNbtCompoundGetString = nbtTagCompoundClass.getMethod("getString", String.class);
        } catch (NoSuchMethodException ignored) {}
        
        try {
            methodNbtCompoundGetCompound = nbtTagCompoundClass.getMethod("getCompound", String.class);
        } catch (NoSuchMethodException ignored) {}
        
        try {
            methodNbtCompoundGetList = nbtTagCompoundClass.getMethod("getList", String.class, int.class);
        } catch (NoSuchMethodException ignored) {}
        
        try {
            methodNbtCompoundHasKey = nbtTagCompoundClass.getMethod("hasKey", String.class);
        } catch (NoSuchMethodException ignored) {}
        
        // NBT List 方法
        methodNbtListSize = nbtTagListClass.getMethod("size");
        methodNbtListGet = nbtTagListClass.getMethod("get", int.class);
        
        // NBT CompressedStreamTools 方法
        methodCompressedStreamToolsRead = nbtCompressedStreamToolsClass.getMethod("a", DataInput.class);
        methodCompressedStreamToolsWrite = nbtCompressedStreamToolsClass.getMethod("a", nbtTagCompoundClass, DataOutput.class);
        
        // CraftItemStack 方法
        methodCraftItemStackAsNMSCopy = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
        methodCraftItemStackAsBukkitCopy = craftItemStackClass.getMethod("asBukkitCopy", itemStackClass);
    }
    
    /**
     * 检查 NMS 是否可用
     */
    public boolean isNMSAvailable() {
        return nmsAvailable;
    }
    
    /**
     * 获取玩家数据文件
     */
    public File getPlayerDataFile(UUID uuid) {
        World mainWorld = Bukkit.getWorlds().get(0);
        if (mainWorld == null) {
            plugin.getLogger().warning("无法获取主世界，离线玩家数据加载失败");
            return null;
        }
        
        File worldFolder = mainWorld.getWorldFolder();
        File playerDataFolder = new File(worldFolder, "playerdata");
        
        if (!playerDataFolder.exists() || !playerDataFolder.isDirectory()) {
            plugin.getLogger().warning("玩家数据文件夹不存在: " + playerDataFolder.getPath());
            return null;
        }
        
        File dataFile = new File(playerDataFolder, uuid.toString() + ".dat");
        if (dataFile.exists()) {
            return dataFile;
        }
        
        return null;
    }
    
    /**
     * 检查离线玩家数据文件是否存在
     */
    public boolean hasPlayerData(UUID uuid) {
        return getPlayerDataFile(uuid) != null;
    }
    
    /**
     * 加载离线玩家背包数据
     * 优先使用 NMS 反射，失败时降级为空背包
     */
    public ItemStack[] loadOfflineInventory(UUID uuid) {
        if (!nmsAvailable) {
            plugin.getLogger().warning("NMS 不可用，无法加载离线玩家 " + uuid + " 的背包数据，返回空背包");
            notifyAdmins("离线玩家数据加载失败", "NMS 反射不可用，显示空背包");
            return new ItemStack[INVENTORY_SIZE];
        }
        
        File dataFile = getPlayerDataFile(uuid);
        if (dataFile == null) {
            plugin.getLogger().warning("玩家 " + uuid + " 的数据文件不存在");
            return new ItemStack[INVENTORY_SIZE];
        }
        
        try {
            return loadInventoryViaNMS(dataFile, uuid);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "无法通过 NMS 加载离线玩家 " + uuid + " 的背包数据: " + e.getMessage());
            
            if (plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().log(Level.WARNING, "详细错误信息:", e);
            }
            
            // 降级方案：返回空背包
            notifyAdmins("离线玩家数据加载失败", 
                "玩家 " + uuid + " 的背包数据加载失败，显示空背包。错误: " + e.getMessage());
            return new ItemStack[INVENTORY_SIZE];
        }
    }
    
    /**
     * 加载离线玩家末影箱数据
     */
    public ItemStack[] loadOfflineEnderChest(UUID uuid) {
        if (!nmsAvailable) {
            plugin.getLogger().warning("NMS 不可用，无法加载离线玩家 " + uuid + " 的末影箱数据，返回空末影箱");
            notifyAdmins("离线玩家数据加载失败", "NMS 反射不可用，显示空末影箱");
            return new ItemStack[ENDER_CHEST_SIZE];
        }
        
        File dataFile = getPlayerDataFile(uuid);
        if (dataFile == null) {
            plugin.getLogger().warning("玩家 " + uuid + " 的数据文件不存在");
            return new ItemStack[ENDER_CHEST_SIZE];
        }
        
        try {
            return loadEnderChestViaNMS(dataFile, uuid);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "无法通过 NMS 加载离线玩家 " + uuid + " 的末影箱数据: " + e.getMessage());
            
            if (plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().log(Level.WARNING, "详细错误信息:", e);
            }
            
            notifyAdmins("离线玩家数据加载失败", 
                "玩家 " + uuid + " 的末影箱数据加载失败，显示空末影箱。错误: " + e.getMessage());
            return new ItemStack[ENDER_CHEST_SIZE];
        }
    }
    
    /**
     * 通过 NMS 反射加载背包数据
     */
    private ItemStack[] loadInventoryViaNMS(File dataFile, UUID uuid) throws Exception {
        ItemStack[] result = new ItemStack[INVENTORY_SIZE];
        
        // 读取 NBT 数据
        Object nbtCompound = readNBTFromFile(dataFile);
        if (nbtCompound == null) {
            plugin.getLogger().warning("无法读取玩家 " + uuid + " 的 NBT 数据");
            return result;
        }
        
        // 获取 Inventory 列表
        Object inventoryList = methodNbtCompoundGetList.invoke(nbtCompound, "Inventory", 10);
        if (inventoryList == null) {
            return result;
        }
        
        int listSize = (int) methodNbtListSize.invoke(inventoryList);
        
        for (int i = 0; i < listSize; i++) {
            try {
                Object itemNBT = methodNbtListGet.invoke(inventoryList, i);
                if (itemNBT == null) continue;
                
                // 获取槽位
                byte slot = (byte) methodNbtCompoundGetByte.invoke(itemNBT, "Slot");
                
                // 加载物品
                ItemStack item = loadItemFromNBT(itemNBT);
                if (item != null && item.getType() != Material.AIR) {
                    int arrayIndex = mapSlotToArrayIndex(slot);
                    if (arrayIndex >= 0 && arrayIndex < result.length) {
                        result[arrayIndex] = item;
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("解析背包物品时出错 (索引 " + i + "): " + e.getMessage());
            }
        }
        
        return result;
    }
    
    /**
     * 通过 NMS 反射加载末影箱数据
     */
    private ItemStack[] loadEnderChestViaNMS(File dataFile, UUID uuid) throws Exception {
        ItemStack[] result = new ItemStack[ENDER_CHEST_SIZE];
        
        Object nbtCompound = readNBTFromFile(dataFile);
        if (nbtCompound == null) {
            plugin.getLogger().warning("无法读取玩家 " + uuid + " 的 NBT 数据");
            return result;
        }
        
        Object enderItemsList = methodNbtCompoundGetList.invoke(nbtCompound, "EnderItems", 10);
        if (enderItemsList == null) {
            return result;
        }
        
        int listSize = (int) methodNbtListSize.invoke(enderItemsList);
        
        for (int i = 0; i < listSize; i++) {
            try {
                Object itemNBT = methodNbtListGet.invoke(enderItemsList, i);
                if (itemNBT == null) continue;
                
                byte slot = (byte) methodNbtCompoundGetByte.invoke(itemNBT, "Slot");
                ItemStack item = loadItemFromNBT(itemNBT);
                
                if (item != null && item.getType() != Material.AIR) {
                    if (slot >= 0 && slot < ENDER_CHEST_SIZE) {
                        result[slot] = item;
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("解析末影箱物品时出错 (索引 " + i + "): " + e.getMessage());
            }
        }
        
        return result;
    }
    
    /**
     * 从 NBT 标签加载物品
     */
    private ItemStack loadItemFromNBT(Object itemNBT) throws Exception {
        // 获取物品 ID
        String id = (String) methodNbtCompoundGetString.invoke(itemNBT, "id");
        if (id == null || id.isEmpty()) {
            return null;
        }
        
        // 解析材质
        Material material = parseMaterial(id);
        if (material == null || material == Material.AIR) {
            return null;
        }
        
        // 获取数量
        byte count = (byte) methodNbtCompoundGetByte.invoke(itemNBT, "Count");
        if (count <= 0) {
            count = 1;
        }
        
        // 创建物品
        ItemStack item = new ItemStack(material, count);
        
        // 尝试使用 NMS 方法加载完整的物品数据（包括附魔、标签等）
        try {
            Object nmsItem = methodLoadItemStack.invoke(null, itemNBT);
            if (nmsItem != null) {
                ItemStack loadedItem = (ItemStack) methodCraftItemStackAsBukkitCopy.invoke(null, nmsItem);
                if (loadedItem != null) {
                    return loadedItem;
                }
            }
        } catch (Exception e) {
            // 如果 NMS 加载失败，使用基本物品
            if (plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().warning("使用基本物品加载方式: " + e.getMessage());
            }
        }
        
        return item;
    }
    
    /**
     * 从文件读取 NBT 数据
     */
    private Object readNBTFromFile(File file) throws Exception {
        try (DataInputStream dis = new DataInputStream(new GZIPInputStream(new FileInputStream(file)))) {
            return methodCompressedStreamToolsRead.invoke(null, dis);
        }
    }
    
    /**
     * 将 NBT 数据写入文件
     */
    private void writeNBTToFile(Object nbtCompound, File file) throws Exception {
        try (DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(file)))) {
            methodCompressedStreamToolsWrite.invoke(null, nbtCompound, dos);
        }
    }
    
    /**
     * 解析材质 ID
     */
    private Material parseMaterial(String id) {
        String materialName = id.replace("minecraft:", "").toUpperCase();
        
        try {
            return Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("未知的材质 ID: " + id);
            return null;
        }
    }
    
    /**
     * 映射 NBT 槽位到数组索引
     */
    private int mapSlotToArrayIndex(byte slot) {
        if (slot >= 0 && slot < 36) {
            return slot;
        } else if (slot == SLOT_BOOTS) {
            return 36;
        } else if (slot == SLOT_LEGGINGS) {
            return 37;
        } else if (slot == SLOT_CHESTPLATE) {
            return 38;
        } else if (slot == SLOT_HELMET) {
            return 39;
        } else if (slot == SLOT_OFFHAND) {
            return 40;
        }
        return -1;
    }
    
    /**
     * 映射数组索引到 NBT 槽位
     */
    private byte mapArrayIndexToSlot(int arrayIndex) {
        if (arrayIndex >= 0 && arrayIndex < 36) {
            return (byte) arrayIndex;
        } else if (arrayIndex == 36) {
            return SLOT_BOOTS;
        } else if (arrayIndex == 37) {
            return SLOT_LEGGINGS;
        } else if (arrayIndex == 38) {
            return SLOT_CHESTPLATE;
        } else if (arrayIndex == 39) {
            return SLOT_HELMET;
        } else if (arrayIndex == 40) {
            return SLOT_OFFHAND;
        }
        return -1;
    }
    
    /**
     * 保存离线玩家背包数据 (异步)
     */
    public CompletableFuture<Boolean> saveOfflineInventoryAsync(UUID uuid, ItemStack[] inventoryContents) {
        return CompletableFuture.supplyAsync(() -> saveOfflineInventory(uuid, inventoryContents));
    }
    
    /**
     * 保存离线玩家末影箱数据 (异步)
     */
    public CompletableFuture<Boolean> saveOfflineEnderChestAsync(UUID uuid, ItemStack[] enderChestContents) {
        return CompletableFuture.supplyAsync(() -> saveOfflineEnderChest(uuid, enderChestContents));
    }
    
    /**
     * 保存离线玩家背包数据
     */
    public boolean saveOfflineInventory(UUID uuid, ItemStack[] inventoryContents) {
        if (!nmsAvailable) {
            plugin.getLogger().warning("NMS 不可用，无法保存离线玩家 " + uuid + " 的背包数据");
            return false;
        }
        
        File dataFile = getPlayerDataFile(uuid);
        if (dataFile == null) {
            plugin.getLogger().warning("玩家 " + uuid + " 的数据文件不存在，无法保存");
            return false;
        }
        
        try {
            return saveInventoryViaNMS(dataFile, uuid, inventoryContents);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "无法通过 NMS 保存离线玩家 " + uuid + " 的背包数据: " + e.getMessage());
            
            if (plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().log(Level.SEVERE, "详细错误信息:", e);
            }
            
            return false;
        }
    }
    
    /**
     * 保存离线玩家末影箱数据
     */
    public boolean saveOfflineEnderChest(UUID uuid, ItemStack[] enderChestContents) {
        if (!nmsAvailable) {
            plugin.getLogger().warning("NMS 不可用，无法保存离线玩家 " + uuid + " 的末影箱数据");
            return false;
        }
        
        File dataFile = getPlayerDataFile(uuid);
        if (dataFile == null) {
            plugin.getLogger().warning("玩家 " + uuid + " 的数据文件不存在，无法保存");
            return false;
        }
        
        try {
            return saveEnderChestViaNMS(dataFile, uuid, enderChestContents);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "无法通过 NMS 保存离线玩家 " + uuid + " 的末影箱数据: " + e.getMessage());
            
            if (plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().log(Level.SEVERE, "详细错误信息:", e);
            }
            
            return false;
        }
    }
    
    /**
     * 通过 NMS 反射保存背包数据
     */
    private boolean saveInventoryViaNMS(File dataFile, UUID uuid, ItemStack[] inventoryContents) throws Exception {
        // 读取现有数据
        Object nbtCompound = readNBTFromFile(dataFile);
        if (nbtCompound == null) {
            plugin.getLogger().warning("无法读取玩家 " + uuid + " 的现有 NBT 数据");
            return false;
        }
        
        // 创建新的 Inventory 列表
        Object inventoryList = nbtTagListClass.getDeclaredConstructor().newInstance();
        
        // 写入背包数据
        if (inventoryContents != null) {
            for (int i = 0; i < inventoryContents.length; i++) {
                ItemStack item = inventoryContents[i];
                if (item != null && item.getType() != Material.AIR) {
                    try {
                        Object itemNBT = saveItemToNBT(item, mapArrayIndexToSlot(i));
                        if (itemNBT != null) {
                            // 添加到列表
                            Method addMethod = nbtTagListClass.getMethod("add", nbtBaseClass);
                            addMethod.invoke(inventoryList, itemNBT);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("保存背包物品时出错 (索引 " + i + "): " + e.getMessage());
                    }
                }
            }
        }
        
        // 设置 Inventory 列表
        Method setMethod = nbtTagCompoundClass.getMethod("set", String.class, nbtBaseClass);
        setMethod.invoke(nbtCompound, "Inventory", inventoryList);
        
        // 保存文件
        writeNBTToFile(nbtCompound, dataFile);
        return true;
    }
    
    /**
     * 通过 NMS 反射保存末影箱数据
     */
    private boolean saveEnderChestViaNMS(File dataFile, UUID uuid, ItemStack[] enderChestContents) throws Exception {
        Object nbtCompound = readNBTFromFile(dataFile);
        if (nbtCompound == null) {
            plugin.getLogger().warning("无法读取玩家 " + uuid + " 的现有 NBT 数据");
            return false;
        }
        
        Object enderItemsList = nbtTagListClass.getDeclaredConstructor().newInstance();
        
        if (enderChestContents != null) {
            for (int i = 0; i < enderChestContents.length; i++) {
                ItemStack item = enderChestContents[i];
                if (item != null && item.getType() != Material.AIR) {
                    try {
                        Object itemNBT = saveItemToNBT(item, (byte) i);
                        if (itemNBT != null) {
                            Method addMethod = nbtTagListClass.getMethod("add", nbtBaseClass);
                            addMethod.invoke(enderItemsList, itemNBT);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("保存末影箱物品时出错 (索引 " + i + "): " + e.getMessage());
                    }
                }
            }
        }
        
        Method setMethod = nbtTagCompoundClass.getMethod("set", String.class, nbtBaseClass);
        setMethod.invoke(nbtCompound, "EnderItems", enderItemsList);
        
        writeNBTToFile(nbtCompound, dataFile);
        return true;
    }
    
    /**
     * 将物品保存为 NBT 标签
     */
    private Object saveItemToNBT(ItemStack item, byte slot) throws Exception {
        // 创建 NBT 复合标签
        Object itemNBT = nbtTagCompoundClass.getDeclaredConstructor().newInstance();
        
        // 设置基本属性
        String materialId = "minecraft:" + item.getType().name().toLowerCase();
        methodNbtCompoundSetString.invoke(itemNBT, "id", materialId);
        methodNbtCompoundSetByte.invoke(itemNBT, "Count", (byte) item.getAmount());
        methodNbtCompoundSetByte.invoke(itemNBT, "Slot", slot);
        
        // 尝试使用 NMS 方法保存完整的物品数据
        try {
            Object nmsItem = methodCraftItemStackAsNMSCopy.invoke(null, item);
            if (nmsItem != null) {
                Object savedNBT = methodSaveItemStack.invoke(nmsItem, itemNBT);
                if (savedNBT != null) {
                    return savedNBT;
                }
            }
        } catch (Exception e) {
            if (plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().warning("使用基本物品保存方式: " + e.getMessage());
            }
        }
        
        return itemNBT;
    }
    
    /**
     * 通知管理员
     */
    private void notifyAdmins(String title, String message) {
        // 记录到日志
        plugin.getLogger().warning("[管理员通知] " + title + ": " + message);
        
        // 如果配置了向在线管理员发送消息，可以在这里添加
        // 但这需要依赖其他模块，这里只记录日志
    }
    
    /**
     * 获取服务器版本信息
     */
    public String getServerVersion() {
        return serverVersion != null ? serverVersion : "未知";
    }
    
    /**
     * 获取 NMS 状态信息
     */
    public String getNMSStatus() {
        if (!nmsInitialized) {
            return "未初始化";
        }
        return nmsAvailable ? "可用" : "不可用";
    }
}
