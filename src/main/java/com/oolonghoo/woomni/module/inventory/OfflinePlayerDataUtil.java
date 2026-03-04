package com.oolonghoo.woomni.module.inventory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Level;

/**
 * 离线玩家数据工具类
 * 用于读取和保存离线玩家的背包和末影箱数据
 */
public class OfflinePlayerDataUtil {

    private final JavaPlugin plugin;

    public OfflinePlayerDataUtil(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 获取玩家数据文件
     * @param uuid 玩家UUID
     * @return 数据文件，如果不存在则返回null
     */
    public File getPlayerDataFile(UUID uuid) {
        // 获取主世界
        World mainWorld = Bukkit.getWorlds().get(0);
        if (mainWorld == null) {
            return null;
        }

        File worldFolder = mainWorld.getWorldFolder();
        File playerDataFolder = new File(worldFolder, "playerdata");
        
        if (!playerDataFolder.exists() || !playerDataFolder.isDirectory()) {
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
     * @param uuid 玩家UUID
     * @return 是否存在数据文件
     */
    public boolean hasPlayerData(UUID uuid) {
        return getPlayerDataFile(uuid) != null;
    }

    /**
     * 使用NMS加载离线玩家背包数据
     * 注意：此方法使用反射访问NMS代码，可能在不同版本间不兼容
     * 
     * @param uuid 玩家UUID
     * @return 背包物品数组（包含装备和副手），如果加载失败返回空数组
     */
    public ItemStack[] loadOfflineInventory(UUID uuid) {
        try {
            // 使用Paper API尝试通过反射加载
            return loadInventoryViaNMS(uuid);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "无法加载离线玩家 " + uuid + " 的背包数据: " + e.getMessage());
            // 返回空数组
            return new ItemStack[41]; // 36个背包槽位 + 4个装备槽位 + 1个副手
        }
    }

    /**
     * 使用NMS加载离线玩家末影箱数据
     * 
     * @param uuid 玩家UUID
     * @return 末影箱物品数组，如果加载失败返回空数组
     */
    public ItemStack[] loadOfflineEnderChest(UUID uuid) {
        try {
            return loadEnderChestViaNMS(uuid);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "无法加载离线玩家 " + uuid + " 的末影箱数据: " + e.getMessage());
            return new ItemStack[27];
        }
    }

    /**
     * 通过NMS反射加载背包数据
     */
    private ItemStack[] loadInventoryViaNMS(UUID uuid) throws Exception {
        // 获取CraftServer
        Object craftServer = Bukkit.getServer();
        
        // 获取MinecraftServer
        Method getServerMethod = craftServer.getClass().getMethod("getServer");
        Object minecraftServer = getServerMethod.invoke(craftServer);
        
        // 获取PlayerList
        Method getPlayerListMethod = minecraftServer.getClass().getMethod("getPlayerList");
        Object playerList = getPlayerListMethod.invoke(minecraftServer);
        
        // 获取PlayerDataStorage (在旧版本中叫WorldNBTStorage)
        Method getPlayerIoMethod = playerList.getClass().getMethod("getPlayerIo");
        Object playerDataStorage = getPlayerIoMethod.invoke(playerList);
        
        // 创建GameProfile
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        String playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : uuid.toString();
        
        // 使用反射创建GameProfile
        Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
        Object gameProfile = gameProfileClass.getConstructor(UUID.class, String.class).newInstance(uuid, playerName);
        
        // 获取主世界
        World mainWorld = Bukkit.getWorlds().get(0);
        Method getHandleMethod = mainWorld.getClass().getMethod("getHandle");
        Object worldServer = getHandleMethod.invoke(mainWorld);
        
        // 创建临时的EntityPlayer来加载数据
        Class<?> entityPlayerClass = Class.forName("net.minecraft.server.level.EntityPlayer");
        Class<?> minecraftServerClass = Class.forName("net.minecraft.server.MinecraftServer");
        Class<?> worldServerClass = Class.forName("net.minecraft.server.level.WorldServer");
        Class<?> gameProfileClass2 = Class.forName("com.mojang.authlib.GameProfile");
        Class<?> clientInformationClass = Class.forName("net.minecraft.server.level.ClientInformation");
        
        // 获取默认ClientInformation
        Method createDefaultMethod = clientInformationClass.getMethod("createDefault");
        Object clientInformation = createDefaultMethod.invoke(null);
        
        // 创建EntityPlayer实例
        Object entityPlayer = entityPlayerClass.getConstructor(
            minecraftServerClass,
            worldServerClass,
            gameProfileClass2,
            clientInformationClass
        ).newInstance(minecraftServer, worldServer, gameProfile, clientInformation);
        
        // 获取CraftPlayer/BukkitEntity
        Method getBukkitEntityMethod = entityPlayer.getClass().getMethod("getBukkitEntity");
        Object craftPlayer = getBukkitEntityMethod.invoke(entityPlayer);
        
        // 调用loadData方法
        Method loadDataMethod = craftPlayer.getClass().getMethod("loadData");
        loadDataMethod.invoke(craftPlayer);
        
        // 获取背包内容
        Method getInventoryMethod = craftPlayer.getClass().getMethod("getInventory");
        Object playerInventory = getInventoryMethod.invoke(craftPlayer);
        
        Method getContentsMethod = playerInventory.getClass().getMethod("getContents");
        ItemStack[] contents = (ItemStack[]) getContentsMethod.invoke(playerInventory);
        
        // 获取装备
        Method getHelmetMethod = playerInventory.getClass().getMethod("getHelmet");
        Method getChestplateMethod = playerInventory.getClass().getMethod("getChestplate");
        Method getLeggingsMethod = playerInventory.getClass().getMethod("getLeggings");
        Method getBootsMethod = playerInventory.getClass().getMethod("getBoots");
        Method getItemInOffHandMethod = playerInventory.getClass().getMethod("getItemInOffHand");
        
        ItemStack[] result = new ItemStack[41];
        
        // 复制背包内容 (0-35)
        if (contents != null) {
            System.arraycopy(contents, 0, result, 0, Math.min(contents.length, 36));
        }
        
        // 装备 (36-39)
        result[36] = (ItemStack) getBootsMethod.invoke(playerInventory);
        result[37] = (ItemStack) getLeggingsMethod.invoke(playerInventory);
        result[38] = (ItemStack) getChestplateMethod.invoke(playerInventory);
        result[39] = (ItemStack) getHelmetMethod.invoke(playerInventory);
        
        // 副手 (40)
        result[40] = (ItemStack) getItemInOffHandMethod.invoke(playerInventory);
        
        return result;
    }

    /**
     * 通过NMS反射加载末影箱数据
     */
    private ItemStack[] loadEnderChestViaNMS(UUID uuid) throws Exception {
        // 获取CraftServer
        Object craftServer = Bukkit.getServer();
        
        // 获取MinecraftServer
        Method getServerMethod = craftServer.getClass().getMethod("getServer");
        Object minecraftServer = getServerMethod.invoke(craftServer);
        
        // 获取PlayerList
        Method getPlayerListMethod = minecraftServer.getClass().getMethod("getPlayerList");
        Object playerList = getPlayerListMethod.invoke(minecraftServer);
        
        // 获取PlayerDataStorage
        Method getPlayerIoMethod = playerList.getClass().getMethod("getPlayerIo");
        Object playerDataStorage = getPlayerIoMethod.invoke(playerList);
        
        // 创建GameProfile
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        String playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : uuid.toString();
        
        Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
        Object gameProfile = gameProfileClass.getConstructor(UUID.class, String.class).newInstance(uuid, playerName);
        
        // 获取主世界
        World mainWorld = Bukkit.getWorlds().get(0);
        Method getHandleMethod = mainWorld.getClass().getMethod("getHandle");
        Object worldServer = getHandleMethod.invoke(mainWorld);
        
        // 创建临时的EntityPlayer
        Class<?> entityPlayerClass = Class.forName("net.minecraft.server.level.EntityPlayer");
        Class<?> minecraftServerClass = Class.forName("net.minecraft.server.MinecraftServer");
        Class<?> worldServerClass = Class.forName("net.minecraft.server.level.WorldServer");
        Class<?> gameProfileClass2 = Class.forName("com.mojang.authlib.GameProfile");
        Class<?> clientInformationClass = Class.forName("net.minecraft.server.level.ClientInformation");
        
        Method createDefaultMethod = clientInformationClass.getMethod("createDefault");
        Object clientInformation = createDefaultMethod.invoke(null);
        
        Object entityPlayer = entityPlayerClass.getConstructor(
            minecraftServerClass,
            worldServerClass,
            gameProfileClass2,
            clientInformationClass
        ).newInstance(minecraftServer, worldServer, gameProfile, clientInformation);
        
        // 获取CraftPlayer
        Method getBukkitEntityMethod = entityPlayer.getClass().getMethod("getBukkitEntity");
        Object craftPlayer = getBukkitEntityMethod.invoke(entityPlayer);
        
        // 加载数据
        Method loadDataMethod = craftPlayer.getClass().getMethod("loadData");
        loadDataMethod.invoke(craftPlayer);
        
        // 获取末影箱
        Method getEnderChestMethod = craftPlayer.getClass().getMethod("getEnderChest");
        Object enderChest = getEnderChestMethod.invoke(craftPlayer);
        
        Method getContentsMethod = enderChest.getClass().getMethod("getContents");
        ItemStack[] contents = (ItemStack[]) getContentsMethod.invoke(enderChest);
        
        return contents != null ? contents : new ItemStack[27];
    }

    /**
     * 保存离线玩家背包数据
     * 
     * @param uuid 玩家UUID
     * @param inventoryContents 背包内容（包含装备和副手）
     * @return 是否保存成功
     */
    public boolean saveOfflineInventory(UUID uuid, ItemStack[] inventoryContents) {
        try {
            return saveInventoryViaNMS(uuid, inventoryContents);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "无法保存离线玩家 " + uuid + " 的背包数据: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 保存离线玩家末影箱数据
     * 
     * @param uuid 玩家UUID
     * @param enderChestContents 末影箱内容
     * @return 是否保存成功
     */
    public boolean saveOfflineEnderChest(UUID uuid, ItemStack[] enderChestContents) {
        try {
            return saveEnderChestViaNMS(uuid, enderChestContents);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "无法保存离线玩家 " + uuid + " 的末影箱数据: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 通过NMS反射保存背包数据
     */
    private boolean saveInventoryViaNMS(UUID uuid, ItemStack[] inventoryContents) throws Exception {
        // 获取CraftServer
        Object craftServer = Bukkit.getServer();
        
        // 获取MinecraftServer
        Method getServerMethod = craftServer.getClass().getMethod("getServer");
        Object minecraftServer = getServerMethod.invoke(craftServer);
        
        // 获取PlayerList
        Method getPlayerListMethod = minecraftServer.getClass().getMethod("getPlayerList");
        Object playerList = getPlayerListMethod.invoke(minecraftServer);
        
        // 创建GameProfile
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        String playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : uuid.toString();
        
        Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
        Object gameProfile = gameProfileClass.getConstructor(UUID.class, String.class).newInstance(uuid, playerName);
        
        // 获取主世界
        World mainWorld = Bukkit.getWorlds().get(0);
        Method getHandleMethod = mainWorld.getClass().getMethod("getHandle");
        Object worldServer = getHandleMethod.invoke(mainWorld);
        
        // 创建临时的EntityPlayer
        Class<?> entityPlayerClass = Class.forName("net.minecraft.server.level.EntityPlayer");
        Class<?> minecraftServerClass = Class.forName("net.minecraft.server.MinecraftServer");
        Class<?> worldServerClass = Class.forName("net.minecraft.server.level.WorldServer");
        Class<?> gameProfileClass2 = Class.forName("com.mojang.authlib.GameProfile");
        Class<?> clientInformationClass = Class.forName("net.minecraft.server.level.ClientInformation");
        
        Method createDefaultMethod = clientInformationClass.getMethod("createDefault");
        Object clientInformation = createDefaultMethod.invoke(null);
        
        Object entityPlayer = entityPlayerClass.getConstructor(
            minecraftServerClass,
            worldServerClass,
            gameProfileClass2,
            clientInformationClass
        ).newInstance(minecraftServer, worldServer, gameProfile, clientInformation);
        
        // 获取CraftPlayer
        Method getBukkitEntityMethod = entityPlayer.getClass().getMethod("getBukkitEntity");
        Object craftPlayer = getBukkitEntityMethod.invoke(entityPlayer);
        
        // 先加载现有数据
        Method loadDataMethod = craftPlayer.getClass().getMethod("loadData");
        loadDataMethod.invoke(craftPlayer);
        
        // 获取背包
        Method getInventoryMethod = craftPlayer.getClass().getMethod("getInventory");
        Object playerInventory = getInventoryMethod.invoke(craftPlayer);
        
        // 设置背包内容 (0-35)
        ItemStack[] mainContents = new ItemStack[36];
        if (inventoryContents != null) {
            System.arraycopy(inventoryContents, 0, mainContents, 0, Math.min(inventoryContents.length, 36));
        }
        
        Method setContentsMethod = playerInventory.getClass().getMethod("setContents", ItemStack[].class);
        setContentsMethod.invoke(playerInventory, (Object) mainContents);
        
        // 设置装备 (36-39)
        if (inventoryContents != null && inventoryContents.length >= 40) {
            Method setBootsMethod = playerInventory.getClass().getMethod("setBoots", ItemStack.class);
            Method setLeggingsMethod = playerInventory.getClass().getMethod("setLeggings", ItemStack.class);
            Method setChestplateMethod = playerInventory.getClass().getMethod("setChestplate", ItemStack.class);
            Method setHelmetMethod = playerInventory.getClass().getMethod("setHelmet", ItemStack.class);
            
            setBootsMethod.invoke(playerInventory, inventoryContents[36]);
            setLeggingsMethod.invoke(playerInventory, inventoryContents[37]);
            setChestplateMethod.invoke(playerInventory, inventoryContents[38]);
            setHelmetMethod.invoke(playerInventory, inventoryContents[39]);
        }
        
        // 设置副手 (40)
        if (inventoryContents != null && inventoryContents.length >= 41) {
            Method setItemInOffHandMethod = playerInventory.getClass().getMethod("setItemInOffHand", ItemStack.class);
            setItemInOffHandMethod.invoke(playerInventory, inventoryContents[40]);
        }
        
        // 保存数据
        Method saveDataMethod = craftPlayer.getClass().getMethod("saveData");
        saveDataMethod.invoke(craftPlayer);
        
        return true;
    }

    /**
     * 通过NMS反射保存末影箱数据
     */
    private boolean saveEnderChestViaNMS(UUID uuid, ItemStack[] enderChestContents) throws Exception {
        // 获取CraftServer
        Object craftServer = Bukkit.getServer();
        
        // 获取MinecraftServer
        Method getServerMethod = craftServer.getClass().getMethod("getServer");
        Object minecraftServer = getServerMethod.invoke(craftServer);
        
        // 获取PlayerList
        Method getPlayerListMethod = minecraftServer.getClass().getMethod("getPlayerList");
        Object playerList = getPlayerListMethod.invoke(minecraftServer);
        
        // 创建GameProfile
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        String playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : uuid.toString();
        
        Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
        Object gameProfile = gameProfileClass.getConstructor(UUID.class, String.class).newInstance(uuid, playerName);
        
        // 获取主世界
        World mainWorld = Bukkit.getWorlds().get(0);
        Method getHandleMethod = mainWorld.getClass().getMethod("getHandle");
        Object worldServer = getHandleMethod.invoke(mainWorld);
        
        // 创建临时的EntityPlayer
        Class<?> entityPlayerClass = Class.forName("net.minecraft.server.level.EntityPlayer");
        Class<?> minecraftServerClass = Class.forName("net.minecraft.server.MinecraftServer");
        Class<?> worldServerClass = Class.forName("net.minecraft.server.level.WorldServer");
        Class<?> gameProfileClass2 = Class.forName("com.mojang.authlib.GameProfile");
        Class<?> clientInformationClass = Class.forName("net.minecraft.server.level.ClientInformation");
        
        Method createDefaultMethod = clientInformationClass.getMethod("createDefault");
        Object clientInformation = createDefaultMethod.invoke(null);
        
        Object entityPlayer = entityPlayerClass.getConstructor(
            minecraftServerClass,
            worldServerClass,
            gameProfileClass2,
            clientInformationClass
        ).newInstance(minecraftServer, worldServer, gameProfile, clientInformation);
        
        // 获取CraftPlayer
        Method getBukkitEntityMethod = entityPlayer.getClass().getMethod("getBukkitEntity");
        Object craftPlayer = getBukkitEntityMethod.invoke(entityPlayer);
        
        // 先加载现有数据
        Method loadDataMethod = craftPlayer.getClass().getMethod("loadData");
        loadDataMethod.invoke(craftPlayer);
        
        // 获取末影箱
        Method getEnderChestMethod = craftPlayer.getClass().getMethod("getEnderChest");
        Object enderChest = getEnderChestMethod.invoke(craftPlayer);
        
        // 设置末影箱内容
        Method setContentsMethod = enderChest.getClass().getMethod("setContents", ItemStack[].class);
        setContentsMethod.invoke(enderChest, (Object) (enderChestContents != null ? enderChestContents : new ItemStack[27]));
        
        // 保存数据
        Method saveDataMethod = craftPlayer.getClass().getMethod("saveData");
        saveDataMethod.invoke(craftPlayer);
        
        return true;
    }
}
