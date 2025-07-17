package bwillows.chunkhopper.stack;

import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.stack.StackedEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
public class RoseStackerProvider implements ItemAmountProvider {

    private final RoseStackerAPI rsAPI;

    public RoseStackerProvider() {
        this.rsAPI = RoseStackerAPI.getInstance();
    }

    @Override
    public boolean canHandle(ItemStack stack) {
        // RoseStacker doesn't modify ItemStack behavior directly
        return false;
    }

    @Override
    public int getAmount(ItemStack stack) {
        return 0; // Fallback not supported for ItemStack
    }

    @Override
    public boolean canHandle(Entity entity) {
        if (!(entity instanceof LivingEntity)) return false;

        StackedEntity stacked = rsAPI.getStackedEntity((LivingEntity) entity);
        return stacked != null;
    }

    @Override
    public int getAmount(Entity entity) {
        if (!(entity instanceof LivingEntity)) return 1;

        StackedEntity stacked = rsAPI.getStackedEntity((LivingEntity) entity);
        return (stacked != null) ? stacked.getStackSize() : 1;
    }
}