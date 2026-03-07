package com.oolonghoo.woomni.module.vanish.gui;

import com.oolonghoo.woomni.module.vanish.VanishData;
import com.oolonghoo.woomni.module.vanish.VanishDataManager;
import com.oolonghoo.woomni.module.vanish.VanishHider;
import com.oolonghoo.woomni.module.vanish.VanishBossBar;
import com.oolonghoo.woomni.util.GUILocale;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VanishEditGUI implements InventoryHolder {
    
    private final VanishDataManager dataManager;
    private final VanishHider hider;
    private final VanishBossBar bossBar;
    private final Inventory inventory;
    private final UUID targetUUID;
    private final String targetName;
    
    public VanishEditGUI(VanishDataManager dataManager, VanishHider hider, 
                         VanishBossBar bossBar, Player target) {
        this.dataManager = dataManager;
        this.hider = hider;
        this.bossBar = bossBar;
        this.targetUUID = target.getUniqueId();
        this.targetName = target.getName();
        this.inventory = Bukkit.createInventory(this, 45, 
            GUILocale.getComponent("gui.vanishedit.title", NamedTextColor.DARK_PURPLE, "player", targetName));
        
        setupItems();
    }
    
    private void setupItems() {
        VanishData data = dataManager.getIfPresent(targetUUID);
        if (data == null) {
            data = dataManager.getVanishData(targetUUID);
        }
        
        inventory.setItem(4, createPlayerHead());
        
        inventory.setItem(10, createToggleItem(Material.LIME_DYE, "gui.vanishedit.toggle-vanish", data.isVanished()));
        inventory.setItem(11, createToggleItem(Material.GOLDEN_APPLE, "gui.vanishedit.toggle-night-vision", data.hasNightVision()));
        inventory.setItem(12, createToggleItem(Material.CHEST, "gui.vanishedit.toggle-no-pickup", !data.canPickupItems()));
        inventory.setItem(13, createToggleItem(Material.SHIELD, "gui.vanishedit.toggle-no-damage", !data.canTakeDamage()));
        inventory.setItem(14, createToggleItem(Material.DIAMOND_SWORD, "gui.vanishedit.toggle-no-attack", !data.canDamageOthers()));
        inventory.setItem(15, createToggleItem(Material.ANVIL, "gui.vanishedit.toggle-no-collision", !data.hasPhysicalCollision()));
        inventory.setItem(16, createToggleItem(Material.ENDER_CHEST, "gui.vanishedit.toggle-silent-chest", data.hasSilentChest()));
        
        inventory.setItem(19, createToggleItem(Material.SPAWNER, "gui.vanishedit.toggle-no-mob-spawn", data.shouldPreventMobSpawn()));
        inventory.setItem(20, createToggleItem(Material.PLAYER_HEAD, "gui.vanishedit.toggle-hide-join", !data.shouldShowJoinMessage()));
        inventory.setItem(21, createToggleItem(Material.PLAYER_HEAD, "gui.vanishedit.toggle-hide-quit", !data.shouldShowQuitMessage()));
        inventory.setItem(22, createToggleItem(Material.DRAGON_BREATH, "BOSSBAR提示", data.isBossbarEnabled()));
        inventory.setItem(23, createToggleItem(Material.ENDER_EYE, "Tab列表隐藏", data.shouldHideFromTab()));
        inventory.setItem(24, createToggleItem(Material.ENDER_PEARL, "gui.vanishedit.toggle-auto-vanish", data.isAutoVanishJoin()));
        
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.displayName(GUILocale.getComponent("gui.vanishedit.button-close", NamedTextColor.RED));
        closeItem.setItemMeta(closeMeta);
        inventory.setItem(40, closeItem);
    }
    
    private ItemStack createPlayerHead() {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.displayName(Component.text(targetName, NamedTextColor.GOLD));
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(targetUUID));
        head.setItemMeta(meta);
        return head;
    }
    
    private ItemStack createToggleItem(Material material, String localeKey, boolean enabled) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        String name = GUILocale.get(localeKey);
        String statusKey = enabled ? "gui.vanishedit.status-enabled" : "gui.vanishedit.status-disabled";
        NamedTextColor statusColor = enabled ? NamedTextColor.GREEN : NamedTextColor.RED;
        
        meta.displayName(Component.text(name, statusColor));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(GUILocale.get("gui.vanishedit.status") + ": ", NamedTextColor.GRAY)
            .append(Component.text(GUILocale.get(statusKey), statusColor)));
        meta.lore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    public void handleClick(int slot, Player viewer) {
        VanishData data = dataManager.getIfPresent(targetUUID);
        if (data == null) {
            data = dataManager.getVanishData(targetUUID);
        }
        
        Player target = Bukkit.getPlayer(targetUUID);
        
        switch (slot) {
            case 10:
                toggleVanish(viewer, data, target);
                break;
            case 11:
                toggleNightVision(data, target);
                break;
            case 12:
                data.setPickupItems(!data.canPickupItems());
                break;
            case 13:
                data.setCanTakeDamage(!data.canTakeDamage());
                break;
            case 14:
                data.setCanDamageOthers(!data.canDamageOthers());
                break;
            case 15:
                data.setPhysicalCollision(!data.hasPhysicalCollision());
                break;
            case 16:
                data.setSilentChest(!data.hasSilentChest());
                break;
            case 19:
                toggleMobSpawnMechanism(data, target);
                break;
            case 20:
                data.setShowJoinMessage(!data.shouldShowJoinMessage());
                break;
            case 21:
                data.setShowQuitMessage(!data.shouldShowQuitMessage());
                break;
            case 22:
                toggleBossBar(data, target);
                break;
            case 23:
                toggleTabVisibility(data, target);
                break;
            case 24:
                data.setAutoVanishJoin(!data.isAutoVanishJoin());
                break;
            case 40:
                viewer.closeInventory();
                return;
        }
        
        dataManager.saveVanishData(targetUUID);
        setupItems();
        viewer.openInventory(inventory);
    }
    
    private void toggleVanish(Player viewer, VanishData data, Player target) {
        boolean newState = !data.isVanished();
        data.setVanished(newState);
        
        if (target != null) {
            if (newState) {
                hider.hidePlayer(target);
                if (data.isBossbarEnabled()) {
                    bossBar.showBossBar(target);
                }
                if (data.hasNightVision()) {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
                }
                if (data.shouldPreventMobSpawn()) {
                    target.setAffectsSpawning(false);
                }
            } else {
                hider.showPlayer(target);
                bossBar.removeBossBar(target);
                target.removePotionEffect(PotionEffectType.NIGHT_VISION);
                target.setAffectsSpawning(true);
            }
        }
    }
    
    private void toggleNightVision(VanishData data, Player target) {
        boolean newState = !data.hasNightVision();
        data.setNightVision(newState);
        
        if (target != null && data.isVanished()) {
            if (newState) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
            } else {
                target.removePotionEffect(PotionEffectType.NIGHT_VISION);
            }
        }
    }
    
    private void toggleBossBar(VanishData data, Player target) {
        boolean newState = !data.isBossbarEnabled();
        data.setBossbarEnabled(newState);
        
        if (target != null && data.isVanished()) {
            if (newState) {
                bossBar.showBossBar(target);
            } else {
                bossBar.removeBossBar(target);
            }
        }
    }
    
    private void toggleTabVisibility(VanishData data, Player target) {
        boolean newHideState = !data.shouldHideFromTab();
        data.setHideFromTab(newHideState);
        
        if (target != null) {
            hider.updateTabVisibility(target, newHideState);
        }
    }
    
    private void toggleMobSpawnMechanism(VanishData data, Player target) {
        boolean newState = !data.shouldPreventMobSpawn();
        data.setPreventMobSpawn(newState);
        
        if (target != null) {
            target.setAffectsSpawning(!newState);
        }
    }
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
    
    public UUID getTargetUUID() {
        return targetUUID;
    }
}
