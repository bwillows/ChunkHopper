package bwillows.chunkhopper.stack;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public class RoseStackerProvider implements ItemAmountProvider {

    @Override
    public boolean canHandle(ItemStack stack) {
        // RoseStacker does not support ItemStack directly
        return false;
    }

    @Override
    public int getAmount(ItemStack stack) {
        // Fallback to default ItemStack amount
        return 0;
    }

    @Override
    public boolean canHandle(Entity entity) {
        return false;
    }

    @Override
    public int getAmount(Entity entity) {
        return 0;
    }
}
