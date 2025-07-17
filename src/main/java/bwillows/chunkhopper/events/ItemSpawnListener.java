package bwillows.chunkhopper.events;

import bwillows.chunkhopper.common.ChunkLocation;
import bwillows.chunkhopper.model.ChunkHopper;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

public class ItemSpawnListener implements Listener {
    @EventHandler
    public void ItemSpawnListener(ItemSpawnEvent event) {
        ChunkLocation chunkLocation = ChunkLocation.fromLocation(event.getEntity().getLocation());
        if(!bwillows.chunkhopper.ChunkHopper.instance.manager.hasChunkHopper(chunkLocation))
            return;

        ChunkHopper chunkHopper = bwillows.chunkhopper.ChunkHopper.instance.manager.getChunkHopperAtChunk(chunkLocation);
        if(chunkHopper == null)
            return;

        ItemStack itemStack = event.getEntity().getItemStack();
        if(!bwillows.chunkhopper.ChunkHopper.instance.manager.canCollectItem(itemStack))
            return;
        int amount = bwillows.chunkhopper.ChunkHopper.instance.itemPickup.getAmount(event.getEntity());
        if(amount <= 0)
            amount = itemStack.getAmount();

        boolean pickupResult = chunkHopper.attemptCollect(itemStack);

        if(pickupResult) {
            event.getEntity().remove();
        }

    }
}
