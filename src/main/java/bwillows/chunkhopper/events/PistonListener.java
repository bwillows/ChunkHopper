package bwillows.chunkhopper.events;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import java.util.List;

public class PistonListener implements Listener {
    @EventHandler
    public void PistonExtendListener(BlockPistonExtendEvent event) {
        List<Block> blocksList = event.getBlocks();
        for(Block block : blocksList) {
            if(bwillows.chunkhopper.ChunkHopper.instance.manager.chunkHoppers.containsKey(block.getLocation())) {
                // If the block is a chunk hopper, cancel the piston extension
                event.setCancelled(true);
                return; // No need to check further blocks
            }
        }
    }

    @EventHandler
    public void PistonRetractListener(BlockPistonRetractEvent event) {
        List<Block> blocksList = event.getBlocks();
        for(Block block : blocksList) {
            if(bwillows.chunkhopper.ChunkHopper.instance.manager.chunkHoppers.containsKey(block.getLocation())) {
                // If the block is a chunk hopper, cancel the piston retraction
                event.setCancelled(true);
                return; // No need to check further blocks
            }
        }
    }
}
