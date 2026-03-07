package com.oolonghoo.woomni.module.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 离线玩家数据工具类
 * 使用 Paper API 读取和保存离线玩家的背包和末影箱数据
 * Paper 1.21+ 使用 serializeAsBytes/deserializeBytes API
 */
public class OfflinePlayerDataUtil {

    private final JavaPlugin plugin;
    
    private static final int INVENTORY_SIZE = 41;
    private static final int ENDER_CHEST_SIZE = 27;
    
    private static final int SLOT_BOOTS = 100;
    private static final int SLOT_LEGGINGS = 101;
    private static final int SLOT_CHESTPLATE = 102;
    private static final int SLOT_HELMET = 103;
    private static final int SLOT_OFFHAND = -106;
    
    private static final ConcurrentHashMap<UUID, Object> playerLocks = new ConcurrentHashMap<>();
    
    private static volatile boolean initialized = false;
    private static volatile boolean nmsAvailable = false;
    
    private static Class<?> nbtTagCompoundClass;
    private static Class<?> nbtTagListClass;
    private static Class<?> nbtCompressedStreamToolsClass;
    
    private static Method methodCompressedStreamToolsRead;
    private static Method methodCompressedStreamToolsWrite;
    private static Method methodNbtCompoundGetList;
    private static Method methodNbtCompoundGetByte;
    private static Method methodNbtCompoundGetString;
    private static Method methodNbtCompoundGetByteArray;
    private static Method methodNbtListSize;
    private static Method methodNbtListGet;
    private static Method methodNbtCompoundSetString;
    private static Method methodNbtCompoundSetByte;
    private static Method methodNbtCompoundSetByteArray;
    
    public OfflinePlayerDataUtil(JavaPlugin plugin) {
        this.plugin = plugin;
        initialize();
    }
    
    private void initialize() {
        if (initialized) return;
        
        synchronized (OfflinePlayerDataUtil.class) {
            if (initialized) return;
            initialized = true;
            
            initNMS();
        }
    }
    
    private void initNMS() {
        try {
            nbtTagCompoundClass = Class.forName("net.minecraft.nbt.NBTTagCompound");
            nbtTagListClass = Class.forName("net.minecraft.nbt.NBTTagList");
            nbtCompressedStreamToolsClass = Class.forName("net.minecraft.nbt.NBTCompressedStreamTools");
            
            methodNbtCompoundGetList = findMethod(nbtTagCompoundClass, new String[]{"c", "getList"}, String.class, int.class);
            methodNbtCompoundGetByte = findMethod(nbtTagCompoundClass, new String[]{"f", "getByte"}, String.class);
            methodNbtCompoundGetString = findMethod(nbtTagCompoundClass, new String[]{"l", "getString"}, String.class);
            methodNbtCompoundGetByteArray = findMethod(nbtTagCompoundClass, new String[]{"m", "getByteArray"}, String.class);
            
            methodNbtCompoundSetString = findMethod(nbtTagCompoundClass, new String[]{"a", "setString"}, String.class, String.class);
            methodNbtCompoundSetByte = findMethod(nbtTagCompoundClass, new String[]{"a", "setByte"}, String.class, byte.class);
            methodNbtCompoundSetByteArray = findMethod(nbtTagCompoundClass, new String[]{"a", "setByteArray"}, String.class, byte[].class);
            
            methodNbtListSize = nbtTagListClass.getMethod("size");
            methodNbtListGet = nbtTagListClass.getMethod("get", int.class);
            
            methodCompressedStreamToolsRead = findMethod(nbtCompressedStreamToolsClass, 
                new String[]{"a", "readNBT", "loadNBT"}, DataInput.class);
            methodCompressedStreamToolsWrite = findMethod(nbtCompressedStreamToolsClass, 
                new String[]{"a", "writeNBT", "saveNBT"}, nbtTagCompoundClass, DataOutput.class);
            
            if (methodCompressedStreamToolsRead == null) {
                for (Method m : nbtCompressedStreamToolsClass.getMethods()) {
                    if (m.getParameterCount() == 1 && 
                        DataInput.class.isAssignableFrom(m.getParameterTypes()[0]) &&
                        m.getReturnType() == nbtTagCompoundClass) {
                        methodCompressedStreamToolsRead = m;
                        break;
                    }
                }
            }
            
            if (methodCompressedStreamToolsWrite == null) {
                for (Method m : nbtCompressedStreamToolsClass.getMethods()) {
                    if (m.getParameterCount() == 2 && 
                        m.getParameterTypes()[0] == nbtTagCompoundClass &&
                        DataOutput.class.isAssignableFrom(m.getParameterTypes()[1])) {
                        methodCompressedStreamToolsWrite = m;
                        break;
                    }
                }
            }
            
            nmsAvailable = true;
            
        } catch (Exception e) {
            nmsAvailable = false;
            plugin.getLogger().log(Level.WARNING, "[inventory] 离线玩家数据加载不可用: " + e.getMessage());
        }
    }
    
    private Method findMethod(Class<?> clazz, String[] names, Class<?>... paramTypes) {
        for (String name : names) {
            try {
                return clazz.getMethod(name, paramTypes);
            } catch (NoSuchMethodException ignored) {
            }
        }
        return null;
    }
    
    public boolean isAvailable() {
        return nmsAvailable;
    }
    
    public File getPlayerDataFile(UUID uuid) {
        World mainWorld = Bukkit.getWorlds().get(0);
        if (mainWorld == null) return null;
        
        File worldFolder = mainWorld.getWorldFolder();
        File playerDataFolder = new File(worldFolder, "playerdata");
        
        if (!playerDataFolder.exists() || !playerDataFolder.isDirectory()) return null;
        
        File dataFile = new File(playerDataFolder, uuid.toString() + ".dat");
        return dataFile.exists() ? dataFile : null;
    }
    
    public boolean hasPlayerData(UUID uuid) {
        return getPlayerDataFile(uuid) != null;
    }
    
    public ItemStack[] loadOfflineInventory(UUID uuid) {
        if (!isAvailable()) {
            return new ItemStack[INVENTORY_SIZE];
        }
        
        File dataFile = getPlayerDataFile(uuid);
        if (dataFile == null) {
            return new ItemStack[INVENTORY_SIZE];
        }
        
        try {
            return loadInventory(dataFile, uuid);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "加载离线玩家 " + uuid + " 背包失败: " + e.getMessage());
            return new ItemStack[INVENTORY_SIZE];
        }
    }
    
    public ItemStack[] loadOfflineEnderChest(UUID uuid) {
        if (!isAvailable()) {
            return new ItemStack[ENDER_CHEST_SIZE];
        }
        
        File dataFile = getPlayerDataFile(uuid);
        if (dataFile == null) {
            return new ItemStack[ENDER_CHEST_SIZE];
        }
        
        try {
            return loadEnderChest(dataFile, uuid);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "加载离线玩家 " + uuid + " 末影箱失败: " + e.getMessage());
            return new ItemStack[ENDER_CHEST_SIZE];
        }
    }
    
    private ItemStack[] loadInventory(File dataFile, UUID uuid) throws Exception {
        ItemStack[] result = new ItemStack[INVENTORY_SIZE];
        
        Object nbtCompound = readNBTFromFile(dataFile);
        if (nbtCompound == null) return result;
        
        Object inventoryList = methodNbtCompoundGetList.invoke(nbtCompound, "Inventory", 10);
        if (inventoryList == null) return result;
        
        int listSize = (int) methodNbtListSize.invoke(inventoryList);
        
        for (int i = 0; i < listSize; i++) {
            try {
                Object itemNBT = methodNbtListGet.invoke(inventoryList, i);
                if (itemNBT == null) continue;
                
                byte slot = (byte) methodNbtCompoundGetByte.invoke(itemNBT, "Slot");
                ItemStack item = loadItemFromNBT(itemNBT);
                
                if (item != null && item.getType() != Material.AIR) {
                    int arrayIndex = mapSlotToArrayIndex(slot);
                    if (arrayIndex >= 0 && arrayIndex < result.length) {
                        result[arrayIndex] = item;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        
        return result;
    }
    
    private ItemStack[] loadEnderChest(File dataFile, UUID uuid) throws Exception {
        ItemStack[] result = new ItemStack[ENDER_CHEST_SIZE];
        
        Object nbtCompound = readNBTFromFile(dataFile);
        if (nbtCompound == null) return result;
        
        Object enderItemsList = methodNbtCompoundGetList.invoke(nbtCompound, "EnderItems", 10);
        if (enderItemsList == null) return result;
        
        int listSize = (int) methodNbtListSize.invoke(enderItemsList);
        
        for (int i = 0; i < listSize; i++) {
            try {
                Object itemNBT = methodNbtListGet.invoke(enderItemsList, i);
                if (itemNBT == null) continue;
                
                byte slot = (byte) methodNbtCompoundGetByte.invoke(itemNBT, "Slot");
                ItemStack item = loadItemFromNBT(itemNBT);
                
                if (item != null && item.getType() != Material.AIR && slot >= 0 && slot < ENDER_CHEST_SIZE) {
                    result[slot] = item;
                }
            } catch (Exception ignored) {
            }
        }
        
        return result;
    }
    
    private ItemStack loadItemFromNBT(Object itemNBT) throws Exception {
        byte[] bytes = (byte[]) methodNbtCompoundGetByteArray.invoke(itemNBT, "bytes");
        if (bytes != null && bytes.length > 0) {
            try {
                return ItemStack.deserializeBytes(bytes);
            } catch (Exception ignored) {
            }
        }
        
        String id = (String) methodNbtCompoundGetString.invoke(itemNBT, "id");
        if (id == null || id.isEmpty()) return null;
        
        Material material = parseMaterial(id);
        if (material == null || material == Material.AIR) return null;
        
        byte count = (byte) methodNbtCompoundGetByte.invoke(itemNBT, "Count");
        if (count <= 0) count = 1;
        
        return new ItemStack(material, count);
    }
    
    private Object readNBTFromFile(File file) throws Exception {
        try (DataInputStream dis = new DataInputStream(new GZIPInputStream(new FileInputStream(file)))) {
            return methodCompressedStreamToolsRead.invoke(null, dis);
        }
    }
    
    private Material parseMaterial(String id) {
        String materialName = id.replace("minecraft:", "").toUpperCase();
        try {
            return Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    private int mapSlotToArrayIndex(byte slot) {
        if (slot >= 0 && slot < 36) return slot;
        if (slot == SLOT_BOOTS) return 36;
        if (slot == SLOT_LEGGINGS) return 37;
        if (slot == SLOT_CHESTPLATE) return 38;
        if (slot == SLOT_HELMET) return 39;
        if (slot == SLOT_OFFHAND) return 40;
        return -1;
    }
    
    public CompletableFuture<Boolean> saveOfflineInventoryAsync(UUID uuid, ItemStack[] inventoryContents) {
        return CompletableFuture.supplyAsync(() -> saveOfflineInventory(uuid, inventoryContents));
    }
    
    public CompletableFuture<Boolean> saveOfflineEnderChestAsync(UUID uuid, ItemStack[] enderChestContents) {
        return CompletableFuture.supplyAsync(() -> saveOfflineEnderChest(uuid, enderChestContents));
    }
    
    public boolean saveOfflineInventory(UUID uuid, ItemStack[] inventoryContents) {
        org.bukkit.entity.Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null && onlinePlayer.isOnline()) {
            return false;
        }
        
        if (!isAvailable()) {
            plugin.getLogger().warning("无法保存离线玩家 " + uuid + " 的背包数据");
            return false;
        }
        
        File dataFile = getPlayerDataFile(uuid);
        if (dataFile == null) return false;
        
        Object lock = playerLocks.computeIfAbsent(uuid, k -> new Object());
        
        synchronized (lock) {
            try {
                return saveInventoryViaNMS(dataFile, uuid, inventoryContents);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "保存离线玩家 " + uuid + " 背包失败: " + e.getMessage());
                return false;
            } finally {
                playerLocks.remove(uuid, lock);
            }
        }
    }
    
    public boolean saveOfflineEnderChest(UUID uuid, ItemStack[] enderChestContents) {
        org.bukkit.entity.Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null && onlinePlayer.isOnline()) {
            return false;
        }
        
        if (!isAvailable()) {
            plugin.getLogger().warning("无法保存离线玩家 " + uuid + " 的末影箱数据");
            return false;
        }
        
        File dataFile = getPlayerDataFile(uuid);
        if (dataFile == null) return false;
        
        Object lock = playerLocks.computeIfAbsent(uuid, k -> new Object());
        
        synchronized (lock) {
            try {
                return saveEnderChestViaNMS(dataFile, uuid, enderChestContents);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "保存离线玩家 " + uuid + " 末影箱失败: " + e.getMessage());
                return false;
            } finally {
                playerLocks.remove(uuid, lock);
            }
        }
    }
    
    private boolean saveInventoryViaNMS(File dataFile, UUID uuid, ItemStack[] inventoryContents) throws Exception {
        Object nbtCompound = readNBTFromFile(dataFile);
        if (nbtCompound == null) return false;
        
        Object inventoryList = nbtTagListClass.getDeclaredConstructor().newInstance();
        
        if (inventoryContents != null) {
            for (int i = 0; i < inventoryContents.length; i++) {
                ItemStack item = inventoryContents[i];
                if (item != null && item.getType() != Material.AIR) {
                    try {
                        Object itemNBT = saveItemToNBT(item, mapArrayIndexToSlot(i));
                        if (itemNBT != null) {
                            Method addMethod = nbtTagListClass.getMethod("add", nbtTagCompoundClass);
                            addMethod.invoke(inventoryList, itemNBT);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        
        Method setMethod = nbtTagCompoundClass.getMethod("set", String.class, nbtTagListClass.getInterfaces()[0]);
        setMethod.invoke(nbtCompound, "Inventory", inventoryList);
        
        writeNBTToFile(nbtCompound, dataFile);
        return true;
    }
    
    private boolean saveEnderChestViaNMS(File dataFile, UUID uuid, ItemStack[] enderChestContents) throws Exception {
        Object nbtCompound = readNBTFromFile(dataFile);
        if (nbtCompound == null) return false;
        
        Object enderItemsList = nbtTagListClass.getDeclaredConstructor().newInstance();
        
        if (enderChestContents != null) {
            for (int i = 0; i < enderChestContents.length; i++) {
                ItemStack item = enderChestContents[i];
                if (item != null && item.getType() != Material.AIR) {
                    try {
                        Object itemNBT = saveItemToNBT(item, (byte) i);
                        if (itemNBT != null) {
                            Method addMethod = nbtTagListClass.getMethod("add", nbtTagCompoundClass);
                            addMethod.invoke(enderItemsList, itemNBT);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        
        Method setMethod = nbtTagCompoundClass.getMethod("set", String.class, nbtTagListClass.getInterfaces()[0]);
        setMethod.invoke(nbtCompound, "EnderItems", enderItemsList);
        
        writeNBTToFile(nbtCompound, dataFile);
        return true;
    }
    
    private Object saveItemToNBT(ItemStack item, byte slot) throws Exception {
        Object itemNBT = nbtTagCompoundClass.getDeclaredConstructor().newInstance();
        
        try {
            byte[] bytes = item.serializeAsBytes();
            methodNbtCompoundSetByteArray.invoke(itemNBT, "bytes", bytes);
        } catch (Exception ignored) {
            String materialId = "minecraft:" + item.getType().name().toLowerCase();
            methodNbtCompoundSetString.invoke(itemNBT, "id", materialId);
            methodNbtCompoundSetByte.invoke(itemNBT, "Count", (byte) item.getAmount());
        }
        
        methodNbtCompoundSetByte.invoke(itemNBT, "Slot", slot);
        
        return itemNBT;
    }
    
    private void writeNBTToFile(Object nbtCompound, File file) throws Exception {
        try (DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(file)))) {
            methodCompressedStreamToolsWrite.invoke(null, nbtCompound, dos);
        }
    }
    
    private byte mapArrayIndexToSlot(int arrayIndex) {
        if (arrayIndex >= 0 && arrayIndex < 36) return (byte) arrayIndex;
        if (arrayIndex == 36) return SLOT_BOOTS;
        if (arrayIndex == 37) return SLOT_LEGGINGS;
        if (arrayIndex == 38) return SLOT_CHESTPLATE;
        if (arrayIndex == 39) return SLOT_HELMET;
        if (arrayIndex == 40) return SLOT_OFFHAND;
        return -1;
    }
    
    public String getStatus() {
        return nmsAvailable ? "Paper API" : "不可用";
    }
}
