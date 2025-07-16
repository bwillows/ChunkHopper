package bwillows.chunkhopper.events;

import bwillows.chunkhopper.ChunkHopper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class EntityExplodeListener implements Listener {
    @EventHandler
    public void EntityExplodeListener(EntityExplodeEvent event) {
        List<Block> blockList = event.blockList();
        for(Block block : blockList) {
            if(!ChunkHopper.instance.manager.chunkHoppers.containsKey(block.getLocation()))
                continue;
            event.blockList().remove(block);
            if(!ChunkHopper.instance.chunkHopperConfig.config.settings.explosionBreak) {
                return;
            }
            // Manually handle to ensure drop is handled correctly
            block.setType(Material.AIR);
            if(ChunkHopper.instance.chunkHopperConfig.config.settings.explosionDrop) {
                block.getLocation().getWorld().dropItemNaturally(block.getLocation(), ChunkHopper.instance.manager.getChunkHopperItem());
            }
        }
    }
}
