package com.oolonghoo.woomni.module.inventory;

import com.oolonghoo.woomni.WooOmni;
import com.oolonghoo.woomni.config.ConfigLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Inventory模块设置管理器
 * 负责加载和管理inventory.yml配置文件
 */
public class InventorySettings extends ConfigLoader {
    
    // 填充物品
    private ItemStack fillerItem;
    
    // 按钮物品缓存
    private final Map<String, ItemStack> buttonItems = new HashMap<>();
    
    public InventorySettings(WooOmni plugin) {
        super(plugin, "settings/inventory.yml");
    }
    
    @Override
    protected void loadValues() {
        cache.clear();
        buttonItems.clear();
        
        // 加载填充物品
        loadFillerItem();
        
        // 加载按钮物品
        loadButtonItems();
    }
    
    /**
     * 加载填充物品配置
     */
    private void loadFillerItem() {
        ConfigurationSection fillerSection = config.getConfigurationSection("filler");
        if (fillerSection == null) {
            // 使用默认填充物品
            fillerItem = createDefaultFillerItem();
            return;
        }
        
        String materialName = fillerSection.getString("material", "LIME_STAINED_GLASS_PANE");
        String displayName = fillerSection.getString("name", " ");
        
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("[Inventory] 无效的填充物品材质: " + materialName + ", 使用默认值 LIME_STAINED_GLASS_PANE");
            material = Material.LIME_STAINED_GLASS_PANE;
        }
        
        fillerItem = new ItemStack(material);
        ItemMeta meta = fillerItem.getItemMeta();
        meta.displayName(Component.text(displayName.replace("&", "\u00A7")));
        fillerItem.setItemMeta(meta);
    }
    
    /**
     * 创建默认填充物品
     */
    private ItemStack createDefaultFillerItem() {
        ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(" "));
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * 加载按钮物品配置
     */
    private void loadButtonItems() {
        ConfigurationSection buttonsSection = config.getConfigurationSection("buttons");
        if (buttonsSection == null) {
            // 使用默认按钮物品
            loadDefaultButtonItems();
            return;
        }
        
        for (String buttonKey : buttonsSection.getKeys(false)) {
            ConfigurationSection buttonSection = buttonsSection.getConfigurationSection(buttonKey);
            if (buttonSection == null) continue;
            
            ItemStack buttonItem = loadButtonItem(buttonSection);
            if (buttonItem != null) {
                buttonItems.put(buttonKey.toLowerCase(), buttonItem);
            }
        }
    }
    
    /**
     * 加载单个按钮物品
     */
    private ItemStack loadButtonItem(ConfigurationSection section) {
        String materialName = section.getString("material", "STONE");
        String displayName = section.getString("name", "按钮");
        List<String> loreStrings = section.getStringList("lore");
        
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("[Inventory] 无效的按钮物品材质: " + materialName + ", 使用默认值 STONE");
            material = Material.STONE;
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(displayName.replace("&", "\u00A7")));
        
        if (!loreStrings.isEmpty()) {
            List<Component> lore = new ArrayList<>();
            for (String line : loreStrings) {
                lore.add(Component.text(line.replace("&", "\u00A7")));
            }
            meta.lore(lore);
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * 加载默认按钮物品
     */
    private void loadDefaultButtonItems() {
        // 复制按钮
        ItemStack copyButton = new ItemStack(Material.CHEST);
        ItemMeta copyMeta = copyButton.getItemMeta();
        copyMeta.displayName(Component.text("复制", NamedTextColor.GREEN));
        copyButton.setItemMeta(copyMeta);
        buttonItems.put("copy", copyButton);
        
        // 清空按钮
        ItemStack clearButton = new ItemStack(Material.BARRIER);
        ItemMeta clearMeta = clearButton.getItemMeta();
        clearMeta.displayName(Component.text("清空", NamedTextColor.RED));
        clearButton.setItemMeta(clearMeta);
        buttonItems.put("clear", clearButton);
        
        // 切换按钮
        ItemStack toggleButton = new ItemStack(Material.LEVER);
        ItemMeta toggleMeta = toggleButton.getItemMeta();
        toggleMeta.displayName(Component.text("切换", NamedTextColor.YELLOW));
        toggleButton.setItemMeta(toggleMeta);
        buttonItems.put("toggle", toggleButton);
        
        // 信息按钮
        ItemStack infoButton = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoButton.getItemMeta();
        infoMeta.displayName(Component.text("信息", NamedTextColor.AQUA));
        infoButton.setItemMeta(infoMeta);
        buttonItems.put("info", infoButton);
        
        // 记录按钮
        ItemStack logButton = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta logMeta = logButton.getItemMeta();
        logMeta.displayName(Component.text("记录", NamedTextColor.LIGHT_PURPLE));
        logButton.setItemMeta(logMeta);
        buttonItems.put("log", logButton);
    }
    
    /**
     * 获取填充物品
     * @return 填充物品
     */
    public ItemStack getFillerItem() {
        return fillerItem.clone();
    }
    
    /**
     * 获取按钮物品
     * @param buttonKey 按钮键名 (copy, clear, toggle, info, log)
     * @return 按钮物品，如果不存在则返回null
     */
    public ItemStack getButtonItem(String buttonKey) {
        ItemStack item = buttonItems.get(buttonKey.toLowerCase());
        return item != null ? item.clone() : null;
    }
    
    /**
     * 检查按钮是否存在
     * @param buttonKey 按钮键名
     * @return 是否存在
     */
    public boolean hasButton(String buttonKey) {
        return buttonItems.containsKey(buttonKey.toLowerCase());
    }
    
    /**
     * 获取所有按钮键名
     * @return 按钮键名集合
     */
    public java.util.Set<String> getButtonKeys() {
        return buttonItems.keySet();
    }
}
