package bwillows.chunkhopper.events;

import bwillows.chunkhopper.ChunkHopper;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractListener implements Listener {
    @EventHandler
    public void PlayerInteractListener(PlayerInteractEvent event) {
        if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
            return;
        Block clickedBlock = event.getClickedBlock();
        if(clickedBlock == null)
            return;
        if(!ChunkHopper.instance.manager.chunkHoppers.containsKey(clickedBlock.getLocation()))
            return;

        bwillows.chunkhopper.model.ChunkHopper chunkHopper = ChunkHopper.instance.manager.chunkHoppers.get(clickedBlock.getLocation());

        if(chunkHopper == null)
            return;

        event.setCancelled(true);

        if(ChunkHopper.instance.chunkHopperConfig.config.settings.onlyOwnerCanAccess) {
            if(!chunkHopper.owner.equals(event.getPlayer().getUniqueId()) && !event.getPlayer().hasPermission("chunkhopper.*")) {
                if(!event.getPlayer().hasPermission("chunkhopper.*")) {
                    String message = ChunkHopper.instance.chunkHopperConfig.langYml.getString("no-permission-access");
                    if(message != null && !message.trim().isEmpty()) {
                        message = ChatColor.translateAlternateColorCodes('&', message);
                        event.getPlayer().sendMessage(message);
                    }
                    return;
                }
            }
        }

        // handle player action
        ChunkHopper.instance.gui.OpenGUI(event.getPlayer().getUniqueId(), chunkHopper);
    }
}
