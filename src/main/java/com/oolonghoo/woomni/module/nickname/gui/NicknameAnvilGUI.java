package com.oolonghoo.woomni.module.nickname.gui;

import com.oolonghoo.woomni.Perms;
import com.oolonghoo.woomni.module.nickname.NicknameModule;
import com.oolonghoo.woomni.module.nickname.NicknameSettings;
import com.oolonghoo.woomni.util.GUILocale;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class NicknameAnvilGUI implements Listener {
    
    private static final Map<UUID, NicknameAnvilGUI> openGUIs = new HashMap<>();
    
    private final JavaPlugin plugin;
    private final NicknameModule module;
    private final Player player;
    private final String currentNickname;
    
    private Inventory inventory;
    private AnvilInventory anvilInventory;
    private boolean processed = false;
    
    public NicknameAnvilGUI(NicknameModule module, Player player, String currentNickname) {
        this.plugin = (JavaPlugin) module.getPlugin();
        this.module = module;
        this.player = player;
        this.currentNickname = currentNickname;
    }
    
    public void open() {
        InventoryView view = player.openAnvil(null, true);
        if (view == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                GUILocale.get("gui.nickname-anvil.open-failed")));
            return;
        }
        
        this.inventory = view.getTopInventory();
        if (!(this.inventory instanceof AnvilInventory)) {
            player.closeInventory();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                GUILocale.get("gui.nickname-anvil.open-failed")));
            return;
        }
        
        this.anvilInventory = (AnvilInventory) this.inventory;
        
        ItemStack inputItem = new ItemStack(Material.PAPER);
        ItemMeta meta = inputItem.getItemMeta();
        if (currentNickname != null && !currentNickname.isEmpty()) {
            meta.setDisplayName(currentNickname);
        } else {
            meta.setDisplayName(ChatColor.GRAY + GUILocale.get("gui.nickname-anvil.hint-text"));
        }
        inputItem.setItemMeta(meta);
        anvilInventory.setItem(0, inputItem);
        
        openGUIs.put(player.getUniqueId(), this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        
        if (!clicker.equals(player)) return;
        
        if (event.getInventory() != anvilInventory) return;
        
        int slot = event.getRawSlot();
        
        if (slot == 2) {
            event.setCancelled(true);
            
            ItemStack resultItem = anvilInventory.getItem(2);
            if (resultItem == null || resultItem.getType() == Material.AIR) {
                return;
            }
            
            String newNickname = resultItem.hasItemMeta() && resultItem.getItemMeta().hasDisplayName() 
                ? resultItem.getItemMeta().getDisplayName() 
                : "";
            
            processResult(newNickname);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player closer = (Player) event.getPlayer();
        
        if (!closer.equals(player)) return;
        
        if (event.getInventory() != anvilInventory) return;
        
        cleanup();
    }
    
    private void processResult(String newNickname) {
        if (processed) return;
        processed = true;
        
        if (newNickname.isEmpty() || newNickname.equals(ChatColor.GRAY + GUILocale.get("gui.nickname-anvil.hint-text"))) {
            module.clearNickname(player, player);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                GUILocale.get("gui.nickname-anvil.cleared")));
        } else {
            String validationResult = validateNickname(newNickname);
            if (validationResult != null) {
                player.sendMessage(validationResult);
            } else {
                if (module.isEconomyEnabled()) {
                    int cost = module.getSetCost(player);
                    if (cost > 0) {
                        if (!module.canAfford(player)) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                                GUILocale.get("gui.nickname-anvil.not-enough-money", "cost", module.formatCost(cost))));
                            player.closeInventory();
                            return;
                        }
                        if (!module.chargePlayer(player)) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                                GUILocale.get("gui.nickname-anvil.payment-failed")));
                            player.closeInventory();
                            return;
                        }
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                            GUILocale.get("gui.nickname-anvil.charged", "cost", module.formatCost(cost))));
                    }
                }
                
                String processedNick = processNickname(newNickname);
                module.setNickname(player, processedNick, player);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    GUILocale.get("gui.nickname-anvil.set-success", "nickname", processedNick)));
            }
        }
        
        player.closeInventory();
    }
    
    private String validateNickname(String nickname) {
        NicknameSettings settings = module.getSettings();
        
        String stripped = ChatColor.stripColor(nickname.replace("&", "§"));
        
        if (!player.hasPermission(Perms.Nickname.BYPASS_LENGTH)) {
            if (stripped.length() < settings.getMinLength() || stripped.length() > settings.getMaxLength()) {
                return ChatColor.translateAlternateColorCodes('&', 
                    GUILocale.get("gui.nickname-anvil.invalid-length", 
                        "min", String.valueOf(settings.getMinLength()), 
                        "max", String.valueOf(settings.getMaxLength())));
            }
        }
        
        if (!player.hasPermission(Perms.Nickname.BYPASS_REGEX)) {
            Pattern regex = settings.getNickRegex();
            if (!regex.matcher(stripped).matches()) {
                return ChatColor.translateAlternateColorCodes('&', 
                    GUILocale.get("gui.nickname-anvil.invalid-chars"));
            }
        }
        
        if (!player.hasPermission(Perms.Nickname.BYPASS_BLACKLIST)) {
            if (settings.isBlacklisted(nickname)) {
                return ChatColor.translateAlternateColorCodes('&', 
                    GUILocale.get("gui.nickname-anvil.blacklisted"));
            }
        }
        
        String lowerNick = stripped.toLowerCase();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player)) continue;
            
            String onlineNick = module.getNickname(online.getUniqueId());
            if (onlineNick != null) {
                String strippedOnline = ChatColor.stripColor(onlineNick.replace("&", "§"));
                if (lowerNick.equals(strippedOnline.toLowerCase())) {
                    return ChatColor.translateAlternateColorCodes('&', 
                        GUILocale.get("gui.nickname-anvil.already-used"));
                }
            }
            
            if (lowerNick.equals(online.getName().toLowerCase())) {
                return ChatColor.translateAlternateColorCodes('&', 
                    GUILocale.get("gui.nickname-anvil.already-used"));
            }
        }
        
        return null;
    }
    
    private String processNickname(String nickname) {
        NicknameSettings settings = module.getSettings();
        
        if (settings.isAllowColors()) {
            if (settings.isColorPermission() && !player.hasPermission(Perms.Nickname.COLORS)) {
                return ChatColor.stripColor(nickname.replace("&", "§"));
            }
            return nickname.replace("&", "§");
        }
        return ChatColor.stripColor(nickname.replace("&", "§"));
    }
    
    private void cleanup() {
        HandlerList.unregisterAll(this);
        openGUIs.remove(player.getUniqueId());
    }
    
    public static NicknameAnvilGUI getOpenGUI(UUID uuid) {
        return openGUIs.get(uuid);
    }
    
    public static boolean hasOpenGUI(UUID uuid) {
        return openGUIs.containsKey(uuid);
    }
}
