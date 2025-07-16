package bwillows.chunkhopper.events;

import bwillows.chunkhopper.ChunkHopper;
import org.bukkit.Location;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class InventoryPickupItemListener implements Listener {
    @EventHandler
    public void InventoryPickupItemListener(InventoryPickupItemEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof Hopper)) return;

        Hopper hopper = (Hopper) holder;
        Location location = hopper.getBlock().getLocation();
        if(!ChunkHopper.instance.manager.chunkHoppers.keySet().contains(location))
            return;

        event.setCancelled(true);

        ItemStack itemStack = event.getItem().getItemStack();
        if(itemStack == null)
            return;

        if(!ChunkHopper.instance.manager.canCollectItem(itemStack))
            return;

        bwillows.chunkhopper.model.ChunkHopper chunkHopper = ChunkHopper.instance.manager.chunkHoppers.get(location);
        boolean pickupResult = chunkHopper.attemptCollect(event.getItem());
        if(pickupResult) {
            event.getItem().remove();
        }
    }
}
