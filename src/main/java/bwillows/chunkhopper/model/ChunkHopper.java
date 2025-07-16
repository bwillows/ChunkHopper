package bwillows.chunkhopper.model;

import bwillows.chunkhopper.common.ItemType;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
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
        public Long itemsCollected = 0L;
        public Long itemsSold = 0L;
        public Double sales = 0.0;
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

        return false;
    }
    public boolean attemptCollect(Entity item) {
        if(!(item instanceof Item))
            return false;

        return false;
    }
}
