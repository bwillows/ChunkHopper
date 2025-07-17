package bwillows.chunkhopper;

import bwillows.chunkhopper.worth.WorthHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ChunkHopperConfig {
    public ChunkHopperConfig() {
        reload();
    }

    public File configFile;
    public File langFile;

    private File guiFolder;

    public File gui_primaryFile;

    public FileConfiguration configYml;
    public FileConfiguration langYml;
    public FileConfiguration gui_primaryYml;

    public class Config {
        public class Item {
            public Material material;
            public short damage;
            public String name;
            public List<String> lore;
        }
        public Item item = new Item();
        public class Block {
            public Material material;
            public short damage;
        }
        public Block block = new Block();
        public class Hologram {
            public boolean enabled;
            public int updateInterval;
            public List<String> lines;
        }
        public Hologram hologram = new Hologram();
        public class SellUpdates {
            public boolean enabled;
            public int interval;
            public List<String> lines;
        }
        public SellUpdates sellUpdates = new SellUpdates();
        public class Settings {
            public int autoSaveInterval;
            public boolean deleteInvalidChunkHoppers;
            public boolean dropGiveOnGroundInventoryFull;
            public boolean onlyOwnerCanAccess;
            public boolean explosionBreak;
            public boolean explosionDrop;
            public boolean autoSell;
            public boolean ignoreRenamedEnchantedItems;
            public boolean maxPlacedPermission;

            public boolean whitelistedAll;
            public boolean blacklistedAll;

            public Set<Material> whitelistedItems = new HashSet<>();
            public Set<Material> blacklistedItems = new HashSet<>();

            public class ChunkEntityScan {
                public boolean enabled;
                public int interval;
            }
            public ChunkEntityScan chunkEntityScan = new ChunkEntityScan();
        }
        public Settings settings = new Settings();
    }
    public Config config = new Config();

    public void reload() {
        configFile = new File(ChunkHopper.instance.pluginFolder, "config.yml");
        if (!configFile.exists()) {
            ChunkHopper.instance.saveResource("config.yml", true);
        }
        configYml = YamlConfiguration.loadConfiguration(configFile);

        langFile = new File(ChunkHopper.instance.pluginFolder, "lang.yml");
        if (!langFile.exists()) {
            ChunkHopper.instance.saveResource("lang.yml", true);
        }
        langYml = YamlConfiguration.loadConfiguration(langFile);

        guiFolder = new File(ChunkHopper.instance.pluginFolder, "gui");
        if (!guiFolder.exists()) {
            guiFolder.mkdir();
        }

        gui_primaryFile = new File(guiFolder, "primary.yml");
        if (!gui_primaryFile.exists()) {
            ChunkHopper.instance.saveResource("gui/primary.yml", true);
        }
        gui_primaryYml = YamlConfiguration.loadConfiguration(gui_primaryFile);

        // Load frequently used settings

        String localeString = configYml.getString("locale", "en_US");
        Locale locale;

        try {
            locale = Locale.forLanguageTag(localeString.replace('_', '-'));

            // Defensive: check if the locale has a language (invalid tags return a default blank locale)
            if (locale.getLanguage().isEmpty()) {
                throw new IllegalArgumentException("Invalid locale: " + localeString);
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("[ChunkHopper] Invalid locale '" + localeString + "', falling back to en_US.");
            locale = Locale.US;
        }

        ChunkHopper.numberFormat = NumberFormat.getNumberInstance(locale);
        ChunkHopper.numberFormat.setMinimumFractionDigits(0);
        ChunkHopper.numberFormat.setMaximumFractionDigits(2);

        config.item.material = Material.matchMaterial(configYml.getString("item.material"));
        if(config.item.material == null) {
            ChunkHopper.instance.getLogger().severe("[ChunkHopper] Invalid item material in config.yml");
        }
        config.item.damage = Short.parseShort(configYml.getString("item.damage"));
        config.item.name = configYml.getString("item.name");
        config.item.lore = configYml.getStringList("item.lore");
        config.block.material = Material.matchMaterial(configYml.getString("block.material"));
        if(config.block.material == null) {
            ChunkHopper.instance.getLogger().severe("[ChunkHopper] Invalid block material in config.yml");
        }
        config.block.damage = Short.parseShort(configYml.getString("block.damage"));
        config.hologram.enabled = configYml.getBoolean("hologram.enabled");
        config.hologram.updateInterval = configYml.getInt("hologram.update-interval");
        config.hologram.lines = configYml.getStringList("hologram.text");
        config.sellUpdates.enabled = configYml.getBoolean("sell-updates.enabled");
        config.sellUpdates.interval = configYml.getInt("sell-updates.interval");
        config.sellUpdates.lines = configYml.getStringList("sell-updates.text");
        config.settings.autoSaveInterval = configYml.getInt("settings.auto-save-interval");
        config.settings.deleteInvalidChunkHoppers = configYml.getBoolean("settings.delete-invalid-chunk-hoppers");
        config.settings.dropGiveOnGroundInventoryFull = configYml.getBoolean("settings.drop-give-on-ground-inventory-full");
        config.settings.onlyOwnerCanAccess = configYml.getBoolean("settings.only-owner-can-access");
        config.settings.explosionBreak = configYml.getBoolean("settings.explosion-break");
        config.settings.explosionDrop = configYml.getBoolean("settings.explosion-drop");
        config.settings.autoSell = configYml.getBoolean("settings.auto-sell");
        config.settings.ignoreRenamedEnchantedItems = configYml.getBoolean("settings.ignore-renamed-enchanted-items");
        config.settings.maxPlacedPermission = configYml.getBoolean("settings.max-placed-permission");

        List<String> whitelistedItems = configYml.getStringList("settings.pickup-whitelist");
        for(String key : whitelistedItems) {
            if(key.equalsIgnoreCase("ALL")) {
                config.settings.whitelistedAll = true;
                break;
            } else {
                Material material = Material.matchMaterial(key);
                if(material == null) {
                    ChunkHopper.instance.getLogger().warning("[ChunkHopper] Invalid material in config.yml whitelist: " + key);
                    continue;
                }
                config.settings.whitelistedItems.add(material);
            }
        }
        List<String> blacklistedItems = configYml.getStringList("settings.pickup-blacklist");
        for(String key : blacklistedItems) {
            if(key.equalsIgnoreCase("ALL")) {
                config.settings.blacklistedAll = true;
                break;
            } else {
                Material material = Material.matchMaterial(key);
                if(material == null) {
                    ChunkHopper.instance.getLogger().warning("[ChunkHopper] Invalid material in config.yml blacklist: " + key);
                    continue;
                }
                config.settings.blacklistedItems.add(material);
            }
        }
        config.settings.chunkEntityScan.enabled = configYml.getBoolean("settings.chunk-entity-scan.enabled");
        config.settings.chunkEntityScan.interval = configYml.getInt("settings.chunk-entity-scan.interval");

        if(ChunkHopper.instance.economy.isHooked()) {
            // Initialize the worth handler ( located here due to dependency on config )
            ChunkHopper.instance.worth = new WorthHandler(configYml);
        }
    }
}
