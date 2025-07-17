package bwillows.chunkhopper.core;

import bwillows.chunkhopper.ChunkHopper;
import bwillows.chunkhopper.common.ChunkLocation;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class Data {
    private File dataFolder;
    private File dataFile;

    private FileConfiguration data;

    public Data() {
        createFolderFile();
        load();
    }

    public void load() {
        data = YamlConfiguration.loadConfiguration(dataFile);
        for(String key : data.getKeys(false)) {
            ConfigurationSection chunkHopperSection = data.getConfigurationSection(key);
            if(chunkHopperSection == null) continue;

            bwillows.chunkhopper.model.ChunkHopper chunkHopper = new bwillows.chunkhopper.model.ChunkHopper();
            chunkHopper.owner = UUID.fromString(chunkHopperSection.getString("owner"));

            ConfigurationSection locationSection = chunkHopperSection.getConfigurationSection("location");
            String world = locationSection.getString("world");
            if(Bukkit.getWorld(world) == null) {
                if(ChunkHopper.instance.chunkHopperConfig.config.settings.deleteInvalidChunkHoppers) {
                    Bukkit.getLogger().info("[ChunkHopper] ChunkHopper with invalid world " + world + " in config.yml, deleting on next save due to configuration settings.");
                    continue;
                } else {
                    Bukkit.getLogger().info("[ChunkHopper] ChunkHopper with invalid world " + world + " in config.yml");
                    chunkHopper.backupLocation.world = world;
                    chunkHopper.backupLocation.x = locationSection.getDouble("x");
                    chunkHopper.backupLocation.y = locationSection.getDouble("y");
                    chunkHopper.backupLocation.z = locationSection.getDouble("z");
                }
            } else {
                chunkHopper.location = new org.bukkit.Location(
                        Bukkit.getWorld(world),
                        locationSection.getDouble("x"),
                        locationSection.getDouble("y"),
                        locationSection.getDouble("z"));
            }

            ConfigurationSection settingsSection = chunkHopperSection.getConfigurationSection("settings");
            if(settingsSection != null) {
                chunkHopper.settings.hologramEnabled = settingsSection.getBoolean("hologram-enabled", true);
                chunkHopper.settings.autoSellEnabled = settingsSection.getBoolean("autosell-enabled", true);
                chunkHopper.settings.collectionEnabled = settingsSection.getBoolean("collection-enabled", true);
            }

            ConfigurationSection statsSection = chunkHopperSection.getConfigurationSection("statistics");
            if(statsSection != null) {
                chunkHopper.statistics.lifetimeItemsCollected = statsSection.getLong("lifetime-items-collected", 0L);
                chunkHopper.statistics.lifetimeItemsSold = statsSection.getLong("lifetime-items-sold", 0L);
                chunkHopper.statistics.lifetimeSales = statsSection.getDouble("lifetime-sales", 0.0);
            }

            ConfigurationSection itemsSection = chunkHopperSection.getConfigurationSection("items");
            for(String itemKey : itemsSection.getKeys(false)) {
                String[] parts = itemKey.split(":");
                if(parts.length < 2) continue; // Invalid item key format
                try {
                    org.bukkit.Material material = org.bukkit.Material.valueOf(parts[0]);
                    short damage = Short.parseShort(parts[1]);
                    long amount = itemsSection.getLong(itemKey, 0L);
                    bwillows.chunkhopper.common.ItemType itemType = new bwillows.chunkhopper.common.ItemType(material, damage);
                    chunkHopper.items.put(itemType, amount);
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().warning("[ChunkHopper] Invalid item type in data file: " + itemKey);
                }
            }

            if(chunkHopper.location != null) {
                ChunkHopper.instance.manager.chunkHoppers.put(chunkHopper.location, chunkHopper);
                ChunkLocation chunkLocation = ChunkLocation.fromLocation(chunkHopper.location);
                ChunkHopper.instance.manager.chunkHopperChunks.put(chunkLocation, chunkHopper.location);
                ChunkHopper.instance.manager.chunkHoppersByOwner.computeIfAbsent(chunkHopper.owner, k -> new HashSet<>()).add(chunkHopper.location);
            } else {
                ChunkHopper.instance.manager.invalidChunkHoppers.computeIfAbsent(world, k -> new HashSet<>()).add(chunkHopper);
            }
        }
    }

    public void save() {
        createFolderFile();

        data = new YamlConfiguration();
        int i = 0;
        for(bwillows.chunkhopper.model.ChunkHopper chunkHopper : ChunkHopper.instance.manager.chunkHoppers.values()) {
            String key = Integer.toString(i);
            ConfigurationSection chunkHopperSection = data.createSection(key);
            chunkHopperSection.set("owner", chunkHopper.owner.toString());
            ConfigurationSection locationSection = chunkHopperSection.createSection("location");
            if(chunkHopper.location != null) {
                locationSection.set("world", chunkHopper.location.getWorld().getName());
                locationSection.set("x", chunkHopper.location.getX());
                locationSection.set("y", chunkHopper.location.getY());
                locationSection.set("z", chunkHopper.location.getZ());
            } else {
                if(chunkHopper.backupLocation != null) {
                    locationSection.set("world", chunkHopper.backupLocation.world);
                    locationSection.set("x", chunkHopper.backupLocation.x);
                    locationSection.set("y", chunkHopper.backupLocation.y);
                    locationSection.set("z", chunkHopper.backupLocation.z);
                }
            }
            ConfigurationSection settingsSection = chunkHopperSection.createSection("settings");
            settingsSection.set("hologram-enabled", chunkHopper.settings.hologramEnabled);
            settingsSection.set("autosell-enabled", chunkHopper.settings.autoSellEnabled);
            settingsSection.set("collection-enabled", chunkHopper.settings.collectionEnabled);

            ConfigurationSection statsSection = chunkHopperSection.createSection("statistics");
            statsSection.set("lifetime-items-collected", chunkHopper.statistics.lifetimeItemsCollected);
            statsSection.set("lifetime-items-sold", chunkHopper.statistics.lifetimeItemsSold);
            statsSection.set("lifetime-sales", chunkHopper.statistics.lifetimeSales);

            ConfigurationSection itemsSection = chunkHopperSection.createSection("items");
            for(Map.Entry<bwillows.chunkhopper.common.ItemType, Long> entry : chunkHopper.items.entrySet()) {
                bwillows.chunkhopper.common.ItemType itemType = entry.getKey();
                long amount = entry.getValue();
                String itemKey = itemType.material.name() + ":" + itemType.damage;
                itemsSection.set(itemKey, amount);
            }

            i++;
        }

        try {
            data.save(dataFile);
        } catch (Exception exception) {
            ChunkHopper.instance.getLogger().severe("[ChunkHopper] Failed to save data file: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    private void createFolderFile() {
        dataFolder = new File(ChunkHopper.instance.pluginFolder, "data");
        if(!dataFolder.exists()) {
            dataFolder.mkdir();
        }
        dataFile = new File(dataFolder, "data.yml");
        if(!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (Exception e) {
                ChunkHopper.instance.getLogger().warning("[ChunkHopper] Failed to create data file: " + e.getMessage());
            }
        }
    }
}
