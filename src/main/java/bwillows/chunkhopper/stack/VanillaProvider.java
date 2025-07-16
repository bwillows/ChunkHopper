package bwillows.chunkhopper.stack;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;


public class VanillaProvider implements ItemAmountProvider {

    @Override
    public boolean canHandle(ItemStack stack) {
        return stack != null;
    }

    @Override
    public int getAmount(ItemStack stack) {
        return stack.getAmount();
    }

    @Override
    public boolean canHandle(Entity entity) {
        if (entity instanceof Item) {
            ItemStack stack = ((Item) entity).getItemStack();
            return stack != null;
        }
        return false;
    }

    @Override
    public int getAmount(Entity entity) {
        if (entity instanceof Item) {
            return ((Item) entity).getItemStack().getAmount();
        }
        return 1;
    }
}
