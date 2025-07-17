package bwillows.chunkhopper.worth;

import bwillows.chunkhopper.common.ItemType;
import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.ShopGuiPlugin;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ShopGUIWorthProvider implements WorthProvider {

    private final int weight;
    private final ShopGuiPlugin plugin;

    public ShopGUIWorthProvider(ShopGuiPlugin plugin, int weight) {
        this.plugin = plugin;
        this.weight = weight;
    }

    @Override
    public boolean isEnabled() {
        return plugin != null;
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

        double price = ShopGuiPlusApi.getItemStackPriceSell(stack);
        return price;
    }

    @Override
    public String getId() {
        return "shopgui";
    }

    @Override
    public int getWeight() {
        return weight;
    }
}