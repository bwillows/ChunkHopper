package bwillows.chunkhopper.stack;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemPickup {
    private final List<ItemAmountProvider> providers = new ArrayList<>();

    public ItemPickup() {
        if (Bukkit.getPluginManager().isPluginEnabled("WildStacker")) {
            providers.add(new WildStackerProvider());
        }

        if (Bukkit.getPluginManager().isPluginEnabled("RoseStacker")) {
            providers.add(new RoseStackerProvider());
        }

        providers.add(new VanillaProvider()); // fallback LAST
    }

    public int getAmount(ItemStack stack) {
        for (ItemAmountProvider provider : providers) {
            if (provider.canHandle(stack)) {
                return provider.getAmount(stack);
            }
        }
        return stack.getAmount();
    }
}
