package bwillows.chunkhopper.events;

import bwillows.chunkhopper.ChunkHopper;
import bwillows.chunkhopper.Utils;
import bwillows.chunkhopper.common.ChunkLocation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class BlockPlaceListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void BlockPlaceListener(BlockPlaceEvent event) {
        if(!ChunkHopper.instance.manager.isChunkHopperItem(event.getItemInHand()))
            return;

        ChunkLocation chunkLocation = ChunkLocation.fromLocation(event.getBlock().getLocation());
        Player player = event.getPlayer();

        if(ChunkHopper.instance.manager.hasChunkHopper(chunkLocation)) {
            event.setCancelled(true);
            String message = ChunkHopper.instance.chunkHopperConfig.langYml.getString("chunk-contains");
            if(message != null && !message.trim().isEmpty()) {
                message = ChatColor.translateAlternateColorCodes('&', message);
                player.sendMessage(message);
            }

            Bukkit.getScheduler().runTask(ChunkHopper.instance, () -> {
                Block block = event.getBlockPlaced();
                block.getState().update(true, false);
            });
            return;
        } else {
            if(ChunkHopper.instance.chunkHopperConfig.config.settings.maxPlacedPermission) {
                int maxAmount = Utils.getMaxChunkHopperAmount(player);
                Set<Location> playerChunkHoppers = ChunkHopper.instance.manager.chunkHoppersByOwner.get(player.getUniqueId());

                if(playerChunkHoppers != null) {
                    if(playerChunkHoppers.size() >= maxAmount) {
                        event.setCancelled(true);
                        String message = ChunkHopper.instance.chunkHopperConfig.langYml.getString("max-placed");
                        if(message != null && !message.trim().isEmpty()) {
                            message = ChatColor.translateAlternateColorCodes('&', message);
                            player.sendMessage(message);
                        }

                        Bukkit.getScheduler().runTask(ChunkHopper.instance, () -> {
                            Block block = event.getBlockPlaced();
                            block.getState().update(true, false);
                        });

                        return;
                    }
                }
            }

            ChunkHopper.instance.manager.placeChunkHopper(event.getBlock().getLocation(), player.getUniqueId());
            String message = ChunkHopper.instance.chunkHopperConfig.langYml.getString("on-place");
            if(message != null && !message.trim().isEmpty()) {
                message = ChatColor.translateAlternateColorCodes('&', message);
                player.sendMessage(message);
            }
        }
    }
}
