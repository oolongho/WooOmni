package com.oolonghoo.woomni.module.inventory.gui;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.module.inventory.InventorySettings;
import com.oolonghoo.woomni.module.inventory.OfflinePlayerDataUtil;
import com.oolonghoo.woomni.util.EconomyUtil;
import com.oolonghoo.woomni.util.GUILocale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 背包查看GUI
 * 6行布局，包含功能按钮、装备栏、背包物品
 */
public class InvSeeGUI implements InventoryHolder {
    
    public enum ViewType {
        INVENTORY,
        ENDER_CHEST
    }
    
    public static final int GUI_SIZE = 54;
    
    public static final int SLOT_COPY = 0;
    public static final int SLOT_CLEAR = 1;
    public static final int SLOT_TOGGLE = 2;
    public static final int SLOT_INFO = 3;
    public static final int SLOT_DATA = 4;
    
    public static final int SLOT_HELMET = 6;
    public static final int SLOT_CHESTPLATE = 7;
    public static final int SLOT_LEGGINGS = 15;
    public static final int SLOT_BOOTS = 16;
    public static final int SLOT_OFFHAND = 17;
    
    public static final int SLOT_INVENTORY_START = 18;
    
    private final Inventory inventory;
    private final InventorySettings settings;
    private final OfflinePlayerDataUtil dataUtil;
    private final UUID targetUUID;
    private final String targetName;
    private final boolean isOnline;
    private final boolean canEdit;
    private final ViewType viewType;
    
    private ItemStack[] inventoryContents;
    private ItemStack[] armorContents;
    private ItemStack offHandItem;
    private ItemStack[] enderChestContents;
    
    public InvSeeGUI(InventorySettings settings, OfflinePlayerDataUtil dataUtil, UUID targetUUID, String targetName, 
                     boolean isOnline, boolean canEdit) {
        this(settings, dataUtil, targetUUID, targetName, isOnline, canEdit, ViewType.INVENTORY);
    }
    
    public InvSeeGUI(InventorySettings settings, OfflinePlayerDataUtil dataUtil, UUID targetUUID, String targetName, 
                     boolean isOnline, boolean canEdit, ViewType viewType) {
        this.settings = settings;
        this.dataUtil = dataUtil;
        this.targetUUID = targetUUID;
        this.targetName = targetName;
        this.isOnline = isOnline;
        this.canEdit = canEdit;
        this.viewType = viewType;
        
        String title;
        if (viewType == ViewType.ENDER_CHEST) {
            String key = isOnline ? "gui.invsee.title-ender" : "gui.invsee.title-ender-offline";
            title = GUILocale.get(key, "player", targetName);
        } else {
            String key = isOnline ? "gui.invsee.title-inventory" : "gui.invsee.title-inventory-offline";
            title = GUILocale.get(key, "player", targetName);
        }
        this.inventory = Bukkit.createInventory(this, GUI_SIZE, Component.text(title));
        
        if (viewType == ViewType.INVENTORY) {
            loadInventoryContents();
        } else {
            loadEnderChestContents();
        }
        
        setupItems();
    }
    
    private void loadInventoryContents() {
        Player onlinePlayer = Bukkit.getPlayer(targetUUID);
        
        if (onlinePlayer != null) {
            inventoryContents = onlinePlayer.getInventory().getContents();
            armorContents = new ItemStack[] {
                onlinePlayer.getInventory().getBoots(),
                onlinePlayer.getInventory().getLeggings(),
                onlinePlayer.getInventory().getChestplate(),
                onlinePlayer.getInventory().getHelmet()
            };
            offHandItem = onlinePlayer.getInventory().getItemInOffHand();
        } else {
            if (dataUtil != null && dataUtil.hasPlayerData(targetUUID)) {
                ItemStack[] loadedContents = dataUtil.loadOfflineInventory(targetUUID);
                
                inventoryContents = new ItemStack[41];
                armorContents = new ItemStack[4];
                
                if (loadedContents != null && loadedContents.length >= 41) {
                    System.arraycopy(loadedContents, 0, inventoryContents, 0, 36);
                    armorContents[0] = loadedContents[36];
                    armorContents[1] = loadedContents[37];
                    armorContents[2] = loadedContents[38];
                    armorContents[3] = loadedContents[39];
                    offHandItem = loadedContents[40];
                } else {
                    inventoryContents = new ItemStack[41];
                    armorContents = new ItemStack[4];
                    offHandItem = null;
                }
            } else {
                inventoryContents = new ItemStack[41];
                armorContents = new ItemStack[4];
                offHandItem = null;
            }
        }
    }
    
    private void loadEnderChestContents() {
        Player onlinePlayer = Bukkit.getPlayer(targetUUID);
        
        if (onlinePlayer != null) {
            enderChestContents = onlinePlayer.getEnderChest().getContents();
        } else {
            if (dataUtil != null && dataUtil.hasPlayerData(targetUUID)) {
                enderChestContents = dataUtil.loadOfflineEnderChest(targetUUID);
            } else {
                enderChestContents = new ItemStack[27];
            }
        }
    }
    
    private void setupItems() {
        fillEmptySlots();
        setupButtons();
        
        if (viewType == ViewType.INVENTORY) {
            setupArmor();
            setupInventory();
        } else {
            setupEnderChest();
        }
    }
    
    private void fillEmptySlots() {
        ItemStack filler = settings.getFillerItem();
        
        if (viewType == ViewType.INVENTORY) {
            inventory.setItem(5, filler);
            inventory.setItem(8, filler);
            for (int i = 9; i < 15; i++) {
                inventory.setItem(i, filler);
            }
        } else {
            for (int i = 5; i < 18; i++) {
                inventory.setItem(i, filler);
            }
        }
    }
    
    private void setupButtons() {
        ItemStack copyButton;
        ItemStack clearButton;
        ItemStack toggleButton;
        
        if (viewType == ViewType.INVENTORY) {
            copyButton = createButton(Material.CHEST, 
                GUILocale.get("gui.invsee.button-copy-inv"), 
                NamedTextColor.GREEN, 
                GUILocale.get("gui.invsee.button-copy-inv-desc"));
            clearButton = createButton(Material.BARRIER, 
                GUILocale.get("gui.invsee.button-clear-inv"), 
                NamedTextColor.RED,
                GUILocale.get("gui.invsee.button-clear-inv-desc"));
            toggleButton = createButton(Material.ENDER_CHEST, 
                GUILocale.get("gui.invsee.button-view-ender"), 
                NamedTextColor.LIGHT_PURPLE,
                GUILocale.get("gui.invsee.button-view-ender-desc"));
        } else {
            copyButton = createButton(Material.ENDER_CHEST, 
                GUILocale.get("gui.invsee.button-copy-ender"), 
                NamedTextColor.GREEN,
                GUILocale.get("gui.invsee.button-copy-ender-desc"));
            clearButton = createButton(Material.BARRIER, 
                GUILocale.get("gui.invsee.button-clear-ender"), 
                NamedTextColor.RED,
                GUILocale.get("gui.invsee.button-clear-ender-desc"));
            toggleButton = createButton(Material.CHEST, 
                GUILocale.get("gui.invsee.button-view-inv"), 
                NamedTextColor.YELLOW,
                GUILocale.get("gui.invsee.button-view-inv-desc"));
        }
        
        inventory.setItem(SLOT_COPY, copyButton);
        inventory.setItem(SLOT_CLEAR, clearButton);
        inventory.setItem(SLOT_TOGGLE, toggleButton);
        
        setupInfoButton();
        setupDataButton();
    }
    
    private void setupInfoButton() {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetUUID);
        Player onlinePlayer = offlinePlayer.getPlayer();
        
        ItemStack infoButton = new ItemStack(Material.BOOK);
        ItemMeta meta = infoButton.getItemMeta();
        meta.displayName(GUILocale.getComponent("gui.invsee.info-title", NamedTextColor.AQUA));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text(GUILocale.get("gui.invsee.player") + ": ", NamedTextColor.GRAY)
            .append(Component.text(targetName, NamedTextColor.WHITE)));
        lore.add(Component.text(GUILocale.get("gui.invsee.uuid") + ": ", NamedTextColor.GRAY)
            .append(Component.text(targetUUID.toString(), NamedTextColor.DARK_GRAY)));
        lore.add(Component.text(GUILocale.get("gui.invsee.status") + ": ", NamedTextColor.GRAY)
            .append(Component.text(isOnline ? GUILocale.get("gui.invsee.online") : GUILocale.get("gui.invsee.offline"), isOnline ? NamedTextColor.GREEN : NamedTextColor.RED)));
        
        if (onlinePlayer != null) {
            lore.add(Component.text(""));
            lore.add(Component.text(GUILocale.get("gui.invsee.health") + ": ", NamedTextColor.GRAY)
                .append(Component.text(String.format("%.1f/%.1f", onlinePlayer.getHealth(), onlinePlayer.getMaxHealth()), NamedTextColor.RED)));
            lore.add(Component.text(GUILocale.get("gui.invsee.hunger") + ": ", NamedTextColor.GRAY)
                .append(Component.text(String.format("%d/20", onlinePlayer.getFoodLevel()), NamedTextColor.YELLOW)));
            lore.add(Component.text(GUILocale.get("gui.invsee.level") + ": ", NamedTextColor.GRAY)
                .append(Component.text(String.valueOf(onlinePlayer.getLevel()), NamedTextColor.GREEN)));
            lore.add(Component.text(GUILocale.get("gui.invsee.gamemode") + ": ", NamedTextColor.GRAY)
                .append(Component.text(onlinePlayer.getGameMode().name(), NamedTextColor.AQUA)));
            lore.add(Component.text(GUILocale.get("gui.invsee.location") + ": ", NamedTextColor.GRAY)
                .append(Component.text(String.format("%s (%.0f, %.0f, %.0f)", 
                    onlinePlayer.getWorld().getName(),
                    onlinePlayer.getLocation().getX(),
                    onlinePlayer.getLocation().getY(),
                    onlinePlayer.getLocation().getZ()), NamedTextColor.WHITE)));
        }
        
        meta.lore(lore);
        infoButton.setItemMeta(meta);
        inventory.setItem(SLOT_INFO, infoButton);
    }
    
    private void setupDataButton() {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetUUID);
        
        ItemStack dataButton = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta meta = dataButton.getItemMeta();
        meta.displayName(GUILocale.getComponent("gui.invsee.data-title", NamedTextColor.LIGHT_PURPLE));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        
        Player onlinePlayer = offlinePlayer.getPlayer();
        if (onlinePlayer != null) {
            String ip = onlinePlayer.getAddress() != null ? 
                onlinePlayer.getAddress().getAddress().getHostAddress() : GUILocale.get("gui.invsee.unknown");
            lore.add(Component.text(GUILocale.get("gui.invsee.ip-address") + ": ", NamedTextColor.GRAY)
                .append(Component.text(ip, NamedTextColor.WHITE)));
        } else {
            lore.add(Component.text(GUILocale.get("gui.invsee.ip-address") + ": ", NamedTextColor.GRAY)
                .append(Component.text(GUILocale.get("gui.invsee.ip-offline"), NamedTextColor.DARK_GRAY)));
        }
        
        long firstPlayed = offlinePlayer.getFirstPlayed();
        if (firstPlayed > 0) {
            String firstJoin = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date(firstPlayed));
            lore.add(Component.text(GUILocale.get("gui.invsee.first-join") + ": ", NamedTextColor.GRAY)
                .append(Component.text(firstJoin, NamedTextColor.WHITE)));
        }
        
        long lastPlayed = offlinePlayer.getLastPlayed();
        if (lastPlayed > 0) {
            String lastSeen = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date(lastPlayed));
            lore.add(Component.text(GUILocale.get("gui.invsee.last-seen") + ": ", NamedTextColor.GRAY)
                .append(Component.text(lastSeen, NamedTextColor.WHITE)));
        }
        
        int playTimeTicks = offlinePlayer.getStatistic(Statistic.PLAY_ONE_MINUTE);
        int playTimeHours = playTimeTicks / (20 * 3600);
        int playTimeMinutes = (playTimeTicks % (20 * 3600)) / (20 * 60);
        String playtimeStr = GUILocale.get("gui.invsee.hours-minutes", "hours", String.valueOf(playTimeHours), "minutes", String.valueOf(playTimeMinutes));
        lore.add(Component.text(GUILocale.get("gui.invsee.playtime") + ": ", NamedTextColor.GRAY)
            .append(Component.text(playtimeStr, NamedTextColor.GREEN)));
        
        int deaths = offlinePlayer.getStatistic(Statistic.DEATHS);
        lore.add(Component.text(GUILocale.get("gui.invsee.deaths") + ": ", NamedTextColor.GRAY)
            .append(Component.text(String.valueOf(deaths), NamedTextColor.RED)));
        
        if (EconomyUtil.hasVault()) {
            String vaultBalance = EconomyUtil.getVaultBalanceFormatted(targetUUID);
            if (vaultBalance != null) {
                lore.add(Component.text(GUILocale.get("gui.invsee.money") + ": ", NamedTextColor.GRAY)
                    .append(Component.text(vaultBalance, NamedTextColor.GOLD)));
            }
        }
        
        if (EconomyUtil.hasPlayerPoints()) {
            String points = EconomyUtil.getPlayerPointsFormatted(targetUUID);
            if (points != null) {
                lore.add(Component.text(GUILocale.get("gui.invsee.points") + ": ", NamedTextColor.GRAY)
                    .append(Component.text(points, NamedTextColor.LIGHT_PURPLE)));
            }
        }
        
        meta.lore(lore);
        dataButton.setItemMeta(meta);
        inventory.setItem(SLOT_DATA, dataButton);
    }
    
    private ItemStack createButton(Material material, String name, NamedTextColor color, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, color));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(description, NamedTextColor.GRAY));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private void setupArmor() {
        if (armorContents[3] != null) {
            inventory.setItem(SLOT_HELMET, armorContents[3].clone());
        }
        
        if (armorContents[2] != null) {
            inventory.setItem(SLOT_CHESTPLATE, armorContents[2].clone());
        }
        
        if (armorContents[1] != null) {
            inventory.setItem(SLOT_LEGGINGS, armorContents[1].clone());
        }
        
        if (armorContents[0] != null) {
            inventory.setItem(SLOT_BOOTS, armorContents[0].clone());
        }
        
        if (offHandItem != null && offHandItem.getType() != Material.AIR) {
            inventory.setItem(SLOT_OFFHAND, offHandItem.clone());
        }
    }
    
    private void setupInventory() {
        for (int i = 0; i < 9; i++) {
            int playerSlot = 9 + i;
            int guiSlot = SLOT_INVENTORY_START + i;
            if (inventoryContents[playerSlot] != null) {
                inventory.setItem(guiSlot, inventoryContents[playerSlot].clone());
            }
        }
        
        for (int i = 0; i < 9; i++) {
            int playerSlot = 18 + i;
            int guiSlot = 27 + i;
            if (inventoryContents[playerSlot] != null) {
                inventory.setItem(guiSlot, inventoryContents[playerSlot].clone());
            }
        }
        
        for (int i = 0; i < 9; i++) {
            int playerSlot = 27 + i;
            int guiSlot = 36 + i;
            if (inventoryContents[playerSlot] != null) {
                inventory.setItem(guiSlot, inventoryContents[playerSlot].clone());
            }
        }
        
        for (int i = 0; i < 9; i++) {
            int guiSlot = 45 + i;
            if (inventoryContents[i] != null) {
                inventory.setItem(guiSlot, inventoryContents[i].clone());
            }
        }
    }
    
    private void setupEnderChest() {
        for (int i = 0; i < 27; i++) {
            int guiSlot = SLOT_INVENTORY_START + i;
            if (enderChestContents != null && enderChestContents[i] != null) {
                inventory.setItem(guiSlot, enderChestContents[i].clone());
            }
        }
    }
    
    public static boolean isButtonSlot(int slot) {
        return slot >= SLOT_COPY && slot <= SLOT_DATA;
    }
    
    public static boolean isArmorSlot(int slot) {
        return slot == SLOT_HELMET || slot == SLOT_CHESTPLATE || 
               slot == SLOT_LEGGINGS || slot == SLOT_BOOTS || slot == SLOT_OFFHAND;
    }
    
    public static boolean isInventorySlot(int slot) {
        return slot >= SLOT_INVENTORY_START;
    }
    
    public static boolean isFillerSlot(int slot, ViewType viewType) {
        if (viewType == ViewType.INVENTORY) {
            return slot == 5 || slot == 8 || (slot >= 9 && slot <= 14);
        } else {
            return slot >= 5 && slot <= 17;
        }
    }
    
    public static int guiSlotToPlayerSlot(int guiSlot) {
        if (guiSlot >= 45 && guiSlot <= 53) {
            return guiSlot - 45;
        } else if (guiSlot >= 36 && guiSlot <= 44) {
            return guiSlot - 36 + 27;
        } else if (guiSlot >= 27 && guiSlot <= 35) {
            return guiSlot - 27 + 18;
        } else if (guiSlot >= 18 && guiSlot <= 26) {
            return guiSlot - 18 + 9;
        }
        return -1;
    }
    
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
    
    public ViewType getViewType() {
        return viewType;
    }
    
    public InventorySettings getSettings() {
        return settings;
    }
    
    public OfflinePlayerDataUtil getDataUtil() {
        return dataUtil;
    }
}
