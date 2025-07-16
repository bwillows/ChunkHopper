package bwillows.chunkhopper.stack;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;


public class WildStackerProvider implements ItemAmountProvider {

    @Override
    public boolean canHandle(ItemStack stack) {
        return false; // WildStacker does NOT support pure ItemStack queries
    }

    @Override
    public int getAmount(ItemStack stack) {
        return stack != null ? stack.getAmount() : 0; // fallback; not handled by WildStacker
    }

    @Override
    public boolean canHandle(Entity entity) {
        return entity instanceof Item && WildStackerAPI.getStackedItem((Item) entity) != null;
    }

    @Override
    public int getAmount(Entity entity) {
        return WildStackerAPI.getItemAmount((Item) entity);
    }
}