package bwillows.chunkhopper.events;

import bwillows.chunkhopper.ChunkHopper;
import bwillows.chunkhopper.common.ChunkLocation;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class ChunkLoadListener implements Listener {
    @EventHandler
    public void ChunkLoadListener(ChunkLoadEvent event) {
        ChunkLocation chunkLocation = ChunkLocation.fromChunk(event.getChunk());
        if(!ChunkHopper.instance.manager.hasChunkHopper(chunkLocation))
            return;
        bwillows.chunkhopper.model.ChunkHopper chunkHopper = ChunkHopper.instance.manager.getChunkHopperAtChunk(chunkLocation);
        if(chunkHopper == null)
            return;
        chunkHopper.createHologram();
    }
}
