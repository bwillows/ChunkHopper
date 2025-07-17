package bwillows.chunkhopper.worth;

import bwillows.chunkhopper.ChunkHopper;
import bwillows.chunkhopper.common.ItemType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LocalWorthProvider implements WorthProvider {

    private Map<ItemType, Double> worthMap = new HashMap<>();
    private File worthFile;
    private FileConfiguration config;
    private int weight;
    private boolean enabled = true;

    public LocalWorthProvider(int weight) {
        this.weight = weight;
        this.worthFile = new File(ChunkHopper.instance.pluginFolder, "worth.yml");

        if (!worthFile.exists()) {
            try {
                worthFile.createNewFile();
            } catch (IOException e) {
                enabled = false;
                return;
            }
        }

        this.config = YamlConfiguration.loadConfiguration(worthFile);

        loadWorths();
    }

    public void loadWorths() {
        ConfigurationSection section = config.getConfigurationSection("worth");
        if (section == null) {
            this.enabled = false;
            return;
        }

        for (String key : section.getKeys(false)) {
            Material material;
            try {
                material = Material.valueOf(key);
            } catch (IllegalArgumentException e) {
                continue;
            }

            if (section.isDouble(key)) {
                double value = section.getDouble(key);
                worthMap.put(new ItemType(material, (short) 0), value);
            } else {
                ConfigurationSection sub = section.getConfigurationSection(key);
                if (sub == null) continue;

                for (String damageKey : sub.getKeys(false)) {
                    try {
                        short damage = Short.parseShort(damageKey);
                        double value = sub.getDouble(damageKey);
                        worthMap.put(new ItemType(material, damage), value);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public double getWorth(ItemStack stack) {
        if (stack == null) return 0.0;
        return getWorth(new ItemType(stack.getType(), stack.getDurability()));
    }

    @Override
    public double getWorth(ItemType type) {
        if(worthMap.get(type) == null) return 0.0;
        return worthMap.get(type);
    }

    @Override
    public void setWorth(ItemType type, double value) {
        worthMap.put(type, value);

        String path = "worth." + type.material.name();

        if (type.damage == 0) {
            config.set(path, value);
        } else {
            path += "." + type.damage;
            config.set(path, value);
        }

        try {
            config.save(worthFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        loadWorths();
    }

    @Override
    public String getId() {
        return "local";
    }

    @Override
    public int getWeight() {
        return weight;
    }
}