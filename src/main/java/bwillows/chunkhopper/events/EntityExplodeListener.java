package bwillows.chunkhopper.events;

import bwillows.chunkhopper.ChunkHopper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ArrayList;
import java.util.List;

public class EntityExplodeListener implements Listener {

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        List<Block> toRemove = new ArrayList<>();

        for (Block block : event.blockList()) {
            if (!ChunkHopper.instance.manager.chunkHoppers.containsKey(block.getLocation()))
                continue;

            toRemove.add(block);

            if (!ChunkHopper.instance.chunkHopperConfig.config.settings.explosionBreak) {
                continue;
            }

            // Manually handle to ensure drop is handled correctly
            block.setType(Material.AIR);

            bwillows.chunkhopper.model.ChunkHopper chunkHopper = ChunkHopper.instance.manager.chunkHoppers.get(block.getLocation());
            if (chunkHopper == null)
                continue;

            ChunkHopper.instance.manager.removeChunkHopper(block.getLocation());

            if (ChunkHopper.instance.chunkHopperConfig.config.settings.explosionDrop) {
                block.getLocation().getWorld().dropItemNaturally(block.getLocation(), ChunkHopper.instance.manager.getChunkHopperItem());
            }
        }

        event.blockList().removeAll(toRemove); // Safe removal after iteration
    }
}