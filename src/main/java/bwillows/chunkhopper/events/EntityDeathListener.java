package bwillows.chunkhopper.events;

import bwillows.chunkhopper.ChunkHopper;
import bwillows.chunkhopper.common.ChunkLocation;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class EntityDeathListener implements Listener {
    // TODO : Handle entity drops at death

    @EventHandler
    public void EntityDeathListener(EntityDeathEvent event) {
        Location deathLocation = event.getEntity().getLocation();
        ChunkLocation chunkLocation = ChunkLocation.fromLocation(deathLocation);

        if(!ChunkHopper.instance.manager.hasChunkHopper(chunkLocation))
            return;

        bwillows.chunkhopper.model.ChunkHopper chunkHopper = ChunkHopper.instance.manager.getChunkHopperAtChunk(chunkLocation);

        // TODO: first check if entity is stacked, modified, or otherwhise not using vanilla drop list
        List<ItemStack> dropList = event.getDrops();

    }
}
