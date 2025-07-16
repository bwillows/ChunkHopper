package bwillows.chunkhopper.events;

import bwillows.chunkhopper.ChunkHopper;
import bwillows.chunkhopper.common.ChunkLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.Set;

public class WorldLoadListener implements Listener {
    @EventHandler
    public void WorldLoadListener(WorldLoadEvent event) {
        String worldName = event.getWorld().getName();
        if(!ChunkHopper.instance.manager.invalidChunkHoppers.containsKey(worldName))
            return;

        Bukkit.getLogger().info("[ChunkHopper] World with previously invalid chunk hoppers loaded, initializing");

        Set<bwillows.chunkhopper.model.ChunkHopper> previouslyInvalidChunkHoppers = ChunkHopper.instance.manager.invalidChunkHoppers.get(worldName);
        for(bwillows.chunkhopper.model.ChunkHopper chunkHopper : previouslyInvalidChunkHoppers) {
            if(chunkHopper == null)
                continue;
            chunkHopper.createHologram();
            chunkHopper.location = new Location(event.getWorld(), chunkHopper.backupLocation.x, chunkHopper.backupLocation.y, chunkHopper.backupLocation.z);
            ChunkLocation chunkLocation = ChunkLocation.fromLocation(chunkHopper.location);
            ChunkHopper.instance.manager.chunkHoppers.put(chunkHopper.location, chunkHopper);
            ChunkHopper.instance.manager.chunkHopperChunks.put(chunkLocation, chunkHopper.location);
        }
        ChunkHopper.instance.manager.invalidChunkHoppers.remove(worldName);
    }
}
