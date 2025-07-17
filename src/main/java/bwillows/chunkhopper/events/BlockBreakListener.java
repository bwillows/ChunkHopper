package bwillows.chunkhopper.events;

import bwillows.chunkhopper.ChunkHopper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {
    @EventHandler
    public void BlockBreakEvent(BlockBreakEvent event) {
        if(event.isCancelled())
            return;
        if(!ChunkHopper.instance.manager.chunkHoppers.containsKey(event.getBlock().getLocation()))
            return;
        event.setCancelled(true);
        bwillows.chunkhopper.model.ChunkHopper chunkHopper = ChunkHopper.instance.manager.chunkHoppers.get(event.getBlock().getLocation());
        Player player = event.getPlayer();

        if(ChunkHopper.instance.chunkHopperConfig.config.settings.onlyOwnerCanAccess) {
            if(!chunkHopper.owner.equals(player.getUniqueId())) {
                if(!player.hasPermission("chunkhopper.*")) {
                    String message = ChunkHopper.instance.chunkHopperConfig.langYml.getString("no-permission-break");
                    if(message != null && !message.trim().isEmpty()) {
                        message = ChatColor.translateAlternateColorCodes('&', message);
                        player.sendMessage(message);
                    }

                    Bukkit.getScheduler().runTask(ChunkHopper.instance, () -> {
                        Block block = event.getBlock();
                        block.getState().update(true, false);
                    });
                    return;
                }
            }
        }

        chunkHopper.removeHologram();
        ChunkHopper.instance.manager.removeChunkHopper(event.getBlock().getLocation());
        event.getBlock().setType(Material.AIR);

        String message = ChunkHopper.instance.chunkHopperConfig.langYml.getString("on-break");
        if(message != null && !message.trim().isEmpty()) {
            message = ChatColor.translateAlternateColorCodes('&', message);
            player.sendMessage(message);
        }

        if(!player.getGameMode().equals(org.bukkit.GameMode.CREATIVE)) {
            event.getBlock().getLocation().getWorld().dropItemNaturally(event.getBlock().getLocation(), ChunkHopper.instance.manager.getChunkHopperItem());
        }
    }
}
