package bwillows.chunkhopper.worth;

import bwillows.chunkhopper.common.ItemType;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import bwillows.chunkhopper.common.ItemType;
import net.ess3.api.IEssentials;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class EssentialsWorthProvider implements WorthProvider {

    private final int weight;
    private final IEssentials essentials;

    public EssentialsWorthProvider(int weight) {
        this.weight = weight;
        this.essentials = (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");
    }

    @Override
    public boolean isEnabled() {
        return essentials != null;
    }

    @Override
    public double getWorth(ItemStack stack) {
        if (stack == null) return 0.0;
        return getWorth(new ItemType(stack.getType(), stack.getDurability()));
    }

    @Override
    public double getWorth(ItemType type) {
        if (!isEnabled()) return 0.0;

        ItemStack stack = new ItemStack(type.material);
        stack.setDurability(type.damage);

        BigDecimal value = essentials.getWorth().getPrice(essentials, stack);
        double result = value != null ? value.doubleValue() : 0.0;

        return result;
    }

    @Override
    public String getId() {
        return "essentials";
    }

    @Override
    public int getWeight() {
        return weight;
    }
}