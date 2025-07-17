package bwillows.chunkhopper;

import bwillows.chunkhopper.common.ItemType;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GUI implements Listener {
    public static class GUIstate {
        public int page;
        public bwillows.chunkhopper.model.ChunkHopper chunkHopper;
    }

    public Map<UUID, GUIstate> openGUIs = new HashMap<>();

    public void OpenGUI(UUID player_UUID, bwillows.chunkhopper.model.ChunkHopper chunkHopper) {
        int inventory_size = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getInt("size");
        String inventory_name = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getString("title");
        if(inventory_name != null) {
            inventory_name = ChatColor.translateAlternateColorCodes('&', inventory_name);
        }
        Inventory inventory = Bukkit.createInventory(null, inventory_size, inventory_name);

        Player player = Bukkit.getPlayer(player_UUID);
        if(player == null) {
            return;
        }

        GUIstate guiState = new GUIstate();
        guiState.chunkHopper = chunkHopper;
        guiState.page = 0;
        openGUIs.put(player_UUID, guiState);

        player.openInventory(inventory);
        openPage(Bukkit.getPlayer(player_UUID), guiState.page);
    }

    public void openPage(Player player, int page) {
        if(player == null)
            return;

        GUIstate guiState = openGUIs.get(player.getUniqueId());
        guiState.page = page;

        Inventory inventory = player.getOpenInventory().getTopInventory();
        inventory.clear();

        bwillows.chunkhopper.model.ChunkHopper chunkHopper = guiState.chunkHopper;

        List<ItemType> itemTypes = new ArrayList<>(chunkHopper.items.keySet());

        int max_page = (int)Math.floor(itemTypes.size() / ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getIntegerList("items.item.slots").size());

        // empty slots
        for(int slot : ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getIntegerList("items.empty.slots")) {
            String material_string = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getString("items.empty.material");
            if(material_string == null) {
                Bukkit.getLogger().severe("[ChunkHopper] Invalid gui empty item in /gui/primary.yml");
                return;
            }
            Material material = Material.matchMaterial(material_string);
            if(material == null) {
                Bukkit.getLogger().severe("[ChunkHopper] Invalid gui empty material in /gui/primary.yml");
                return;
            }
            ItemStack itemStack = new ItemStack(material);
            itemStack.setDurability((short) ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getInt("items.empty.damage"));

            ItemMeta itemMeta = itemStack.getItemMeta();
            String name = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getString("items.empty.name");
            if(name != null) {
                name = ChatColor.translateAlternateColorCodes('&', name);
                itemMeta.setDisplayName(name);
            }
            List<String> lore = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getStringList("items.empty.lore");
            if(lore != null) {
                for(int i = 0; i < lore.size(); i++) {
                    lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
                }
            }
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);

            NBTItem nbtItem = new NBTItem(itemStack);
            nbtItem.setString("type", "EMPTY");

            inventory.clear(slot);
            inventory.setItem(slot, nbtItem.getItem());
        }

        // item contents
        int indexBase = page * ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getIntegerList("items.item.slots").size();
        int iterator = 0;
        for(int slot : ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getIntegerList("items.item.slots")) {
            int index = indexBase + iterator;

            if(itemTypes.size() <= index)
                continue;

            ItemType itemType = itemTypes.get(index);

            Long quantity = chunkHopper.items.get(itemType);

            Double worth_ea = (double)0;

            worth_ea = ChunkHopper.instance.worth.getWorth(itemType);

            Double worth = quantity * worth_ea;

            String quantity_formatted =  ChunkHopper.numberFormat.format(quantity);
            String worth_formatted = ChunkHopper.numberFormat.format(worth);

            ItemStack itemStack = new ItemStack(itemType.material);
            itemStack.setDurability(itemType.damage);
            ItemMeta itemMeta = itemStack.getItemMeta();
            if(!ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getString("items.item.name").equals("%material%")) {
                String itemName = new String(ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getString("items.item.name"));
                itemName = ChatColor.translateAlternateColorCodes('&', itemName);
                itemName.replace("%material%", itemType.material.name());
                itemName.replace("%worth%", worth_formatted);
                itemName.replace("%quantity%", quantity_formatted);
                itemMeta.setDisplayName(itemName);
            }
            if(ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getStringList("items.item.lore") != null) {
                List<String> lore = new ArrayList<>(ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getStringList("items.item.lore"));
                for(int i = 0; i < lore.size(); i++) {
                    lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
                    lore.set(i, lore.get(i).replace("%material%", itemType.material.name()));
                    lore.set(i, lore.get(i).replace("%worth%", worth_formatted));
                    lore.set(i, lore.get(i).replace("%quantity%", quantity_formatted));
                }
                itemMeta.setLore(lore);
            }
            itemStack.setItemMeta(itemMeta);
            NBTItem nbtItem = new NBTItem(itemStack);
            nbtItem.setString("type", "ITEM");
            nbtItem.setString("MATERIAL", itemType.material.name());
            nbtItem.setInteger("DAMAGE", (int)itemType.damage);
            inventory.clear(slot);
            inventory.setItem(slot, nbtItem.getItem());

            iterator++;
        }

        if(page < max_page) {
            String material_string = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getString("items.next-page.material");
            if(material_string == null) {
                Bukkit.getLogger().severe("[ChunkHopper] Invalid next-page item material string in /gui/primary.yml");
                return;
            }
            Material material = Material.matchMaterial(material_string);
            if(material == null) {
                Bukkit.getLogger().severe("[ChunkHopper] Invalid next-page item material in /gui/primary.yml");
                return;
            }
            ItemStack itemStack = new ItemStack(material);

            itemStack.setDurability((short)ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getInt("items.next-page.damage"));
            ItemMeta itemMeta = itemStack.getItemMeta();

            String name = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getString("items.next-page.name");
            if(name != null) {
                name = ChatColor.translateAlternateColorCodes('&', name);
                itemMeta.setDisplayName(name);
            }
            List<String> lore = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getStringList("items.next-page.lore");
            if(lore != null) {
                for(int i = 0; i < lore.size(); i++) {
                    lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
                }
            }
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
            NBTItem nbtItem = new NBTItem(itemStack);
            nbtItem.setString("type", "NEXT_PAGE");

            int next_page_slot = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getInt("items.next-page.slot");
            if(next_page_slot >= 0) {
                inventory.clear(next_page_slot);
                inventory.setItem(next_page_slot, nbtItem.getItem());
            }
        }

        if(page > 0) {
            String material_string = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getString("items.previous-page.material");
            if(material_string == null) {
                Bukkit.getLogger().severe("[ChunkHopper] Invalid previous page item material string in /gui/primary.yml");
                return;
            }
            Material material = Material.matchMaterial(material_string);
            if(material == null) {
                Bukkit.getLogger().severe("[ChunkHopper] Invalid previous page item material in /gui/primary.yml");
                return;
            }
            ItemStack itemStack = new ItemStack(material);
            itemStack.setDurability((short) ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getInt("items.previous-page.damage"));
            ItemMeta itemMeta = itemStack.getItemMeta();

            String name = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getString("items.previous-page.name");
            if(name != null) {
                name = ChatColor.translateAlternateColorCodes('&', name);
                itemMeta.setDisplayName(name);
            }

            List<String> lore = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getStringList("items.previous-page.lore");
            if(lore != null) {
                for(int i = 0; i < lore.size(); i++) {
                    lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
                }
            }
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
            NBTItem nbtItem = new NBTItem(itemStack);
            nbtItem.setString("type", "PREVIOUS_PAGE");

            int previous_page_slot = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getInt("items.previous-page.slot");

            if(previous_page_slot >= 0) {
                inventory.clear(previous_page_slot);
                inventory.setItem(previous_page_slot, nbtItem.getItem());
            }
        }

        if(chunkHopper.settings.hologramEnabled) {
            String material_string = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getString("items.holograms-enabled.material");
            if(material_string == null) {
                Bukkit.getLogger().severe("[ChunkHopper] Invalid holograms enabled item in /gui/primary.yml");
                return;
            }
            Material material = Material.matchMaterial(material_string);
            if(material == null) {
                Bukkit.getLogger().severe("[ChunkHopper] Invalid holograms enabled material in /gui/primary.yml");
                return;
            }

            ItemStack itemStack = new ItemStack(material);
            itemStack.setDurability((short) ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getInt("items.holograms-enabled.damage"));

            ItemMeta itemMeta = itemStack.getItemMeta();
            String name = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getString("items.holograms-enabled.name");
            if(name != null) {
                name = ChatColor.translateAlternateColorCodes('&', name);
                itemMeta.setDisplayName(name);
            }
            List<String> lore = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getStringList("items.holograms-enabled.lore");
            if(lore != null) {
                for(int i = 0; i < lore.size(); i++) {
                    lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
                }
            }
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);

            NBTItem nbtItem = new NBTItem(itemStack);
            nbtItem.setString("type", "DISABLE_HOLOGRAMS");

            int slot = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getInt("items.holograms-enabled.slot");

            if (slot >= 0) {
                inventory.clear(slot);
                inventory.setItem(slot, nbtItem.getItem());
            }
        } else {
            String material_string = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getString("items.holograms-disabled.material");
            if(material_string == null) {
                Bukkit.getLogger().severe("[ChunkHopper] Invalid holograms disabled item in /gui/primary.yml");
                return;
            }
            Material material = Material.matchMaterial(material_string);
            if(material == null) {
                Bukkit.getLogger().severe("[ChunkHopper] Invalid holograms disabled material in /gui/primary.yml");
                return;
            }

            ItemStack itemStack = new ItemStack(material);
            itemStack.setDurability((short) ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getInt("items.holograms-disabled.damage"));

            ItemMeta itemMeta = itemStack.getItemMeta();
            String name = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getString("items.holograms-disabled.name");
            if(name != null) {
                name = ChatColor.translateAlternateColorCodes('&', name);
                itemMeta.setDisplayName(name);
            }
            List<String> lore = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getStringList("items.holograms-disabled.lore");
            if(lore != null) {
                for(int i = 0; i < lore.size(); i++) {
                    lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
                }
            }
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);

            NBTItem nbtItem = new NBTItem(itemStack);
            nbtItem.setString("type", "ENABLE_HOLOGRAMS");

            int slot = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getInt("items.holograms-disabled.slot");
            if(slot >= 0) {
                inventory.clear(slot);
                inventory.setItem(slot, nbtItem.getItem());
            }
        }

        if(chunkHopper.settings.autoSellEnabled) {
            String material_string = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getString("items.autosell-enabled.material");
            if(material_string == null) {
                Bukkit.getLogger().severe("[ChunkHopper] Invalid autosell enabled item in /gui/primary.yml");
                return;
            }
            Material material = Material.matchMaterial(material_string);
            if(material == null) {
                Bukkit.getLogger().severe("[ChunkHopper] Invalid autosell enabled material in /gui/primary.yml");
                return;
            }

            ItemStack itemStack = new ItemStack(material);
            itemStack.setDurability((short) ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getInt("items.autosell-enabled.damage"));

            ItemMeta itemMeta = itemStack.getItemMeta();
            String name = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getString("items.autosell-enabled.name");
            if(name != null) {
                name = ChatColor.translateAlternateColorCodes('&', name);
                itemMeta.setDisplayName(name);
            }
            List<String> lore = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getStringList("items.autosell-enabled.lore");
            if(lore != null) {
                for(int i = 0; i < lore.size(); i++) {
                    lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
                }
            }
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);

            NBTItem nbtItem = new NBTItem(itemStack);
            nbtItem.setString("type", "DISABLE_AUTOSELL");

            int slot = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getInt("items.autosell-enabled.slot");
            if(slot >= 0) {
                inventory.clear(slot);
                inventory.setItem(slot, nbtItem.getItem());
            }
        } else {
            String material_string = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getString("items.autosell-disabled.material");
            if(material_string == null) {
                Bukkit.getLogger().severe("[ChunkHopper] Invalid autosell disabled item in /gui/primary.yml");
                return;
            }
            Material material = Material.matchMaterial(material_string);
            if(material == null) {
                Bukkit.getLogger().severe("[ChunkHopper] Invalid autosell disabled material in /gui/primary.yml");
                return;
            }

            ItemStack itemStack = new ItemStack(material);
            itemStack.setDurability((short) ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getInt("items.autosell-disabled.damage"));

            ItemMeta itemMeta = itemStack.getItemMeta();
            String name = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getString("items.autosell-disabled.name");
            if(name != null) {
                name = ChatColor.translateAlternateColorCodes('&', name);
                itemMeta.setDisplayName(name);
            }
            List<String> lore = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getStringList("items.autosell-disabled.lore");
            if(lore != null) {
                for(int i = 0; i < lore.size(); i++) {
                    lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
                }
            }
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);

            NBTItem nbtItem = new NBTItem(itemStack);
            nbtItem.setString("type", "ENABLE_AUTOSELL");

            int slot = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getInt("items.autosell-disabled.slot");
            if(slot >= 0) {
                inventory.clear(slot);
                inventory.setItem(slot, nbtItem.getItem());
            }
        }
        if(chunkHopper.settings.collectionEnabled) {
            String material_string = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getString("items.collection-enabled.material");
            if(material_string == null) {
                Bukkit.getLogger().severe("[ChunkHopper] Invalid collection enabled item in /gui/primary.yml");
                return;
            }
            Material material = Material.matchMaterial(material_string);
            if(material == null) {
                Bukkit.getLogger().severe("[ChunkHopper] Invalid collection enabled material in /gui/primary.yml");
                return;
            }

            ItemStack itemStack = new ItemStack(material);
            itemStack.setDurability((short) ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getInt("items.collection-enabled.damage"));

            ItemMeta itemMeta = itemStack.getItemMeta();
            String name = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getString("items.collection-enabled.name");
            if(name != null) {
                name = ChatColor.translateAlternateColorCodes('&', name);
                itemMeta.setDisplayName(name);
            }
            List<String> lore = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getStringList("items.collection-enabled.lore");
            if(lore != null) {
                for(int i = 0; i < lore.size(); i++) {
                    lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
                }
            }
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);

            NBTItem nbtItem = new NBTItem(itemStack);
            nbtItem.setString("type", "DISABLE_COLLECTION");

            int slot = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getInt("items.collection-enabled.slot");
            if(slot >= 0) {
                inventory.clear(slot);
                inventory.setItem(slot, nbtItem.getItem());
            }
        } else {
            String material_string = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getString("items.collection-disabled.material");
            if(material_string == null) {
                Bukkit.getLogger().severe("[ChunkHopper] Invalid collection disabled item in /gui/primary.yml");
                return;
            }
            Material material = Material.matchMaterial(material_string);
            if(material == null) {
                Bukkit.getLogger().severe("[ChunkHopper] Invalid collection disabled material in /gui/primary.yml");
                return;
            }

            ItemStack itemStack = new ItemStack(material);
            itemStack.setDurability((short) ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getInt("items.collection-disabled.damage"));

            ItemMeta itemMeta = itemStack.getItemMeta();
            String name = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getString("items.collection-disabled.name");
            if(name != null) {
                name = ChatColor.translateAlternateColorCodes('&', name);
                itemMeta.setDisplayName(name);
            }
            List<String> lore = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getStringList("items.collection-disabled.lore");
            if(lore != null) {
                for(int i = 0; i < lore.size(); i++) {
                    lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
                }
            }
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);

            NBTItem nbtItem = new NBTItem(itemStack);
            nbtItem.setString("type", "ENABLE_COLLECTION");

            int slot = ChunkHopper.instance.chunkHopperConfig.gui_primaryYml.getInt("items.collection-disabled.slot");
            if(slot >= 0) {
                inventory.clear(slot);
                inventory.setItem(slot, nbtItem.getItem());
            }
        }

        player.updateInventory();
    }

    @EventHandler
    public void InventoryClickEvent(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if(!openGUIs.containsKey(player.getUniqueId()))
            return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        NBTItem nbtItem = new NBTItem(clicked);
        GUIstate guiState = openGUIs.get(event.getWhoClicked().getUniqueId());

        if(!nbtItem.hasTag("type"))
            return;

        String type = nbtItem.getString("type");

        if(type.equals("NEXT_PAGE")) {
            openPage(player, guiState.page + 1);
            return;
        }

        if(type.equals("PREVIOUS_PAGE")) {
            openPage(player, guiState.page - 1);
            return;
        }

        if(type.equals("DISABLE_HOLOGRAMS")) {
            guiState.chunkHopper.settings.hologramEnabled = false;
            guiState.chunkHopper.createHologram();
            openPage(player, guiState.page);
            return;
        }

        if(type.equals("ENABLE_HOLOGRAMS")) {
            guiState.chunkHopper.settings.hologramEnabled = true;
            guiState.chunkHopper.createHologram();
            openPage(player, guiState.page);
            return;
        }

        if(type.equals("ENABLE_AUTOSELL")) {
            guiState.chunkHopper.settings.autoSellEnabled = true;
            openPage(player, guiState.page);
            return;
        }

        if(type.equals("DISABLE_AUTOSELL")) {
            guiState.chunkHopper.settings.autoSellEnabled = false;
            openPage(player, guiState.page);
            return;
        }

        if(type.equals("ENABLE_COLLECTION")) {
            guiState.chunkHopper.settings.collectionEnabled = true;
            openPage(player, guiState.page);
            return;
        }

        if(type.equals("DISABLE_COLLECTION")) {
            guiState.chunkHopper.settings.collectionEnabled = false;
            openPage(player, guiState.page);
            return;
        }

        if(type.equals("ITEM")) {
            Material material = Material.matchMaterial(nbtItem.getString("MATERIAL"));
            ItemType itemType = new ItemType();
            itemType.material = material;
            itemType.damage = (short)(int)nbtItem.getInteger("DAMAGE");

            Long quantity = guiState.chunkHopper.items.get(itemType);

            if(event.getClick().isRightClick() && !Utils.isInventoryFull(player)) {
                // ATTEMPT WITHDRAW

                Long withdraw_quantity = quantity > 64 ? 64 : quantity;

                guiState.chunkHopper.items.put(itemType, quantity - withdraw_quantity);
                if(guiState.chunkHopper.items.get(itemType) <= 0) {
                    guiState.chunkHopper.items.remove(itemType);
                }

                ItemStack itemStack = new ItemStack(material);
                itemStack.setDurability(itemType.damage);
                itemStack.setAmount(withdraw_quantity.intValue());
                player.getInventory().addItem(itemStack);
                openPage(player, guiState.page);

                return;
            }
            if(event.getClick().isLeftClick()) {

                ItemStack toSell = new ItemStack(itemType.material);
                toSell.setDurability(itemType.damage);
                toSell.setAmount(quantity.intValue());
                boolean sellResult = guiState.chunkHopper.attemptSell(toSell);

                if(sellResult) {
                    guiState.chunkHopper.items.remove(itemType);

                    Double sell_amount = ChunkHopper.instance.worth.getWorth(itemType) * quantity;
                    String message = ChunkHopper.instance.chunkHopperConfig.langYml.getString("on-sell");
                    if(message != null) {
                        message = ChatColor.translateAlternateColorCodes('&', message);
                        if(message.length() > 0) {
                            if(!message.equals(" ")) {
                                message = message.replace("%worth%", ChunkHopper.numberFormat.format(sell_amount));
                                player.sendMessage(message);
                            }
                        }
                    }

                    openPage(player, guiState.page);
                }
                // ATTEMPT SELL
                return;
            }
        }

    }

    @EventHandler
    public void InventoryCloseListener(InventoryCloseEvent event) {
        openGUIs.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void PlayerQuitListener(PlayerQuitEvent event) {
        openGUIs.remove(event.getPlayer().getUniqueId());
    }
}
