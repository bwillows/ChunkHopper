package bwillows.chunkhopper.worth;

import bwillows.chunkhopper.common.ItemType;
import org.bukkit.inventory.ItemStack;

public interface WorthProvider {
    boolean isEnabled();
    double getWorth(ItemStack item);
    double getWorth(ItemType itemType);
    String getId();
    int getWeight();

    default void setWorth(ItemType type, double value) {
        throw new UnsupportedOperationException("This provider does not support setting worth.");
    }
}
