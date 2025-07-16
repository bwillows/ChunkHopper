package bwillows.chunkhopper.stack;

public interface ItemAmountProvider {

    /**
     * Determines whether this provider can handle the given entity.
     * @param entity the entity to check
     * @return true if this provider supports extracting amount from the entity
     */
    boolean canHandle(org.bukkit.entity.Entity entity);

    /**
     * Gets the custom stack amount from the entity.
     * @param entity the entity to inspect
     * @return the stack amount, or 1 if unknown or not supported
     */
    int getAmount(org.bukkit.entity.Entity entity);

    /**
     * Determines whether this provider can handle the given item stack.
     * @param stack the item stack to check
     * @return true if this provider supports extracting amount from the stack
     */
    boolean canHandle(org.bukkit.inventory.ItemStack stack);

    /**
     * Gets the custom stack amount from the item stack.
     * @param stack the item stack to inspect
     * @return the stack amount, or stack.getAmount() if unknown or not supported
     */
    int getAmount(org.bukkit.inventory.ItemStack stack);
}
