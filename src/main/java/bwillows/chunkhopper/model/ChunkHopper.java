package bwillows.chunkhopper.model;

import bwillows.chunkhopper.common.ItemType;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ChunkHopper {
    public UUID owner;
    public Location location;

    // Used in case of invalid world name for ChunkHopper(s) within data
    public class BackupLocation {
        public String world;
        public double x, y, z;
    }
    public BackupLocation backupLocation;

    public class Statistics {
        public Long lifetimeItemsCollected = 0L;
        public Long lifetimeItemsSold = 0L;
        public Double lifetimeSales = 0.0;

        public Long periodItemsCollected = 0L;
        public Long periodItemsSold = 0L;
        public Double periodSales = 0.0;
    }
    public Statistics statistics = new Statistics();

    public class Settings {
        public boolean hologramEnabled = true;
        public boolean autoSellEnabled = true;
        public boolean collectionEnabled = true;
    }
    public Settings settings = new Settings();

    public Map<ItemType, Long> items = new HashMap<>();

    public Set<ArmorStand> holograms = new HashSet<>();

    public void createHologram() {
        removeHologram();
        if(!bwillows.chunkhopper.ChunkHopper.instance.chunkHopperConfig.config.hologram.enabled || !settings.hologramEnabled)
            return;
        // Prevents unnecessarily loading chunks
        if(!location.getChunk().isLoaded())
            return;

        createHologram(bwillows.chunkhopper.ChunkHopper.instance.chunkHopperConfig.config.hologram.lines);
    }

    public void removeHologram() {
        for (ArmorStand hologram : holograms) {
            hologram.remove();
        }
        holograms.clear();
    }

    public boolean collectItem(Entity entity) {
        if(!(entity instanceof Item))
            return false;


        return false;
    }


    // Attempts to collect an item and returns true if successful
    public boolean attemptCollect(ItemStack itemStack) {
        if(!settings.collectionEnabled)
            return false;

        statistics.lifetimeItemsCollected += itemStack.getAmount();
        statistics.periodItemsCollected += itemStack.getAmount();

        boolean autoSellResult = attemptAutoSell(itemStack);
        if(autoSellResult) {
            return true;
        }

        ItemType itemType = new ItemType(itemStack.getType(), itemStack.getDurability());
        long existing = items.containsKey(itemType) ? items.get(itemType) : 0L;
        items.put(itemType, existing + itemStack.getAmount());
        return true;
    }

    public boolean attemptCollect(Entity item) {
        if(!(item instanceof Item))
            return false;

        ItemStack itemStack = ((Item) item).getItemStack();
        int amount = bwillows.chunkhopper.ChunkHopper.instance.itemPickup.getAmount(item);
        if(amount != 0)
            itemStack.setAmount(amount);

        return attemptCollect(itemStack);
    }

    public boolean attemptAutoSell(ItemStack itemStack) {
        if(!bwillows.chunkhopper.ChunkHopper.instance.chunkHopperConfig.config.settings.autoSell || !settings.autoSellEnabled)
            return false;
        return attemptSell(itemStack);
    }

    public boolean attemptSell(ItemStack itemStack) {
        ItemType itemType = new ItemType(itemStack.getType(), itemStack.getDurability());

        if(!bwillows.chunkhopper.ChunkHopper.instance.economy.isHooked())
            return false;

        Double worth = bwillows.chunkhopper.ChunkHopper.instance.worth.getWorth(itemStack);
        if(worth == null || worth <= 0.0)
            return false;

        OfflinePlayer owner = Bukkit.getOfflinePlayer(this.owner);
        if(owner == null || !owner.hasPlayedBefore()) {
            Bukkit.getLogger().warning("[ChunkHopper] Attempted to sell items for an invalid owner: " + this.owner);
            return false;
        }

        double sellAmount = itemStack.getAmount() * worth;

        statistics.lifetimeItemsSold += itemStack.getAmount();
        statistics.lifetimeSales += sellAmount;
        statistics.periodItemsSold += itemStack.getAmount();
        statistics.periodSales += sellAmount;

        bwillows.chunkhopper.ChunkHopper.instance.economy.getProvider().deposit(owner, sellAmount);

        return true;
    }

    public void createHologram(List<String> hologramLinesRaw) {
        removeHologram();

        List<String> hologramLines = new ArrayList<>(hologramLinesRaw);

        for(int i = 0; i < hologramLines.size(); i++) {
            hologramLines.set(i, ChatColor.translateAlternateColorCodes('&', hologramLines.get(i)));
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner);
            String playerName = new String("");
            if(offlinePlayer != null) {
                playerName = offlinePlayer.getName();
            }
            hologramLines.set(i, hologramLines.get(i)
                    .replace("%player%", playerName)
                    .replace("%lifetime_items_collected%", bwillows.chunkhopper.ChunkHopper.numberFormat.format(statistics.lifetimeItemsCollected))
                    .replace("%lifetime_items_sold%", bwillows.chunkhopper.ChunkHopper.numberFormat.format(statistics.lifetimeItemsSold))
                    .replace("%lifetime_sales%", bwillows.chunkhopper.ChunkHopper.numberFormat.format(statistics.lifetimeSales))
                    .replace("%period_items_collected%", bwillows.chunkhopper.ChunkHopper.numberFormat.format(statistics.periodItemsCollected))
                    .replace("%period_items_sold%", bwillows.chunkhopper.ChunkHopper.numberFormat.format(statistics.periodItemsSold))
                    .replace("%period_sales%", bwillows.chunkhopper.ChunkHopper.numberFormat.format(statistics.periodSales))
            );
        }

        Location topLocation = new Location(location.getWorld(), location.getX() + 0.5, location.getY() - 0.2 + (0.25 * hologramLines.size()), location.getZ() + 0.5);
        createHologram(topLocation, hologramLines);
    }

    public void createHologram(Location top, List<String> lines) {
        double lineHeight = 0.25;
        for (int i = 0; i < lines.size(); i++) {
            // each subsequent line goes a bit lower
            Location lineLoc = top.clone().subtract(0, i * lineHeight, 0);
            ArmorStand armorStand = spawnHologramLine(lineLoc, lines.get(i));
            holograms.add(armorStand);
        }
    }

    public ArmorStand spawnHologramLine(Location loc, String text) {
        World world = loc.getWorld();
        ArmorStand hologram = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
        // make the stand invisible and immovable
        hologram.setVisible(false);
        hologram.setGravity(false);
        hologram.setBasePlate(false);
        hologram.setSmall(true);            // shrinks the hitbox a bit
        // name settings
        hologram.setCustomName(text);
        hologram.setCustomNameVisible(true);
        // for 1.9+ you can also do hologram.setMarker(true) to remove even the tiny hitbox entirely
        return hologram;
    }
}
