package bwillows.chunkhopper.task;

import bwillows.chunkhopper.ChunkHopper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class TaskManager {
    public TaskManager() {
        reload();
    }

    public BukkitTask autoSaveTask;
    public BukkitTask hologramUpdateTask;
    public BukkitTask chunkEntityRescanTask;
    public BukkitTask sellUpdateTask;

    public void createTasks() {
        autoSaveTask = Bukkit.getScheduler().runTaskTimer(ChunkHopper.instance, new Runnable() {
            @Override
            public void run() {
                autoSave();
            }
        }, 0L, ChunkHopper.instance.chunkHopperConfig.config.settings.autoSaveInterval * 20L);

        if(ChunkHopper.instance.chunkHopperConfig.config.hologram.enabled) {
            hologramUpdateTask = Bukkit.getScheduler().runTaskTimer(ChunkHopper.instance, new Runnable() {
                @Override
                public void run() {
                    hologramUpdate();
                }
            }, 0L, ChunkHopper.instance.chunkHopperConfig.config.hologram.updateInterval * 20L);
        }
        if(ChunkHopper.instance.chunkHopperConfig.config.settings.chunkEntityScan.enabled) {
            chunkEntityRescanTask = Bukkit.getScheduler().runTaskTimer(ChunkHopper.instance, new Runnable() {
                @Override
                public void run() {
                    chunkEntityRescan();
                }
            }, 0L, ChunkHopper.instance.chunkHopperConfig.config.settings.chunkEntityScan.interval * 20L);
        }
        if(ChunkHopper.instance.chunkHopperConfig.config.sellUpdates.enabled) {
            sellUpdateTask = Bukkit.getScheduler().runTaskTimer(ChunkHopper.instance, new Runnable() {
                @Override
                public void run() {
                    sellUpdate();
                }
            }, 0L, ChunkHopper.instance.chunkHopperConfig.config.sellUpdates.interval * 20L);
        }
    }

    public void cancelTasks() {
        if(autoSaveTask != null) {
            autoSaveTask.cancel();
            autoSaveTask = null;
        }
        if(hologramUpdateTask != null) {
            hologramUpdateTask.cancel();
            hologramUpdateTask = null;
        }
        if(chunkEntityRescanTask != null) {
            chunkEntityRescanTask.cancel();
            chunkEntityRescanTask = null;
        }
        if(sellUpdateTask != null) {
            sellUpdateTask.cancel();
            sellUpdateTask = null;
        }
    }

    public void reload() {
        cancelTasks();
        createTasks();
    }

    public void autoSave() {
        ChunkHopper.instance.data.save();
    }

    public void hologramUpdate() {
        for(bwillows.chunkhopper.model.ChunkHopper chunkHopper : ChunkHopper.instance.manager.chunkHoppers.values()) {
            if(!chunkHopper.location.getChunk().isLoaded())
                continue;

            chunkHopper.createHologram();
        }
    }

    public void chunkEntityRescan() {

        for(bwillows.chunkhopper.model.ChunkHopper chunkHopper : ChunkHopper.instance.manager.chunkHoppers.values()) {
            if(chunkHopper == null)
                continue;
            if(chunkHopper.location == null)
                continue;
            if(chunkHopper.location.getChunk() == null)
                continue;
            if(!chunkHopper.location.getChunk().isLoaded())
                continue;
            if(!chunkHopper.settings.collectionEnabled)
                continue;

            Entity[] entityArray = chunkHopper.location.getChunk().getEntities();
            if(entityArray == null || entityArray.length == 0)
                continue;
            List<Entity> entities = Arrays.asList(entityArray);
            if(entities == null || entities.isEmpty())
                continue;

            for(Entity entity : chunkHopper.location.getChunk().getEntities()) {
                if(entity == null)
                    continue;
                if(!(entity instanceof Item))
                    continue;
                Item item = (Item) entity;

                if(!ChunkHopper.instance.manager.canCollectItem(item.getItemStack()))
                    continue;
                ItemStack itemStack = item.getItemStack();
                int amount = bwillows.chunkhopper.ChunkHopper.instance.itemPickup.getAmount(entity);
                if(amount != 0)
                    itemStack.setAmount(amount);

                if(!chunkHopper.attemptCollect(itemStack))
                    continue;
                entity.remove();
            }
        }
    }

    public void sellUpdate() {
        if(!ChunkHopper.instance.chunkHopperConfig.config.sellUpdates.enabled)
            return;
        for(Player player : Bukkit.getOnlinePlayers()) {
            Set<Location> playerChunkHopperLocations = ChunkHopper.instance.manager.chunkHoppersByOwner.get(player.getUniqueId());
            if(playerChunkHopperLocations == null)
                continue;

            Long allChunkHoppers_lifetimeItemsCollected = 0L;
            Long allChunkHoppers_lifetimeItemsSold = 0L;
            Double allChunkHoppers_lifetimeSales = 0.0;

            Long allChunkHoppers_periodItemsCollected = 0L;
            Long allChunkHoppers_periodItemsSold = 0L;
            Double allChunkHoppers_periodSales = 0.0;

            for(Location chunkHopperLocation : playerChunkHopperLocations) {
                bwillows.chunkhopper.model.ChunkHopper chunkHopper = ChunkHopper.instance.manager.chunkHoppers.get(chunkHopperLocation);
                if(chunkHopper == null)
                    continue;
                allChunkHoppers_lifetimeItemsCollected += chunkHopper.statistics.lifetimeItemsCollected;
                allChunkHoppers_lifetimeItemsSold += chunkHopper.statistics.lifetimeItemsSold;
                allChunkHoppers_lifetimeSales += chunkHopper.statistics.lifetimeSales;

                allChunkHoppers_periodItemsCollected += chunkHopper.statistics.periodItemsCollected;
                allChunkHoppers_periodItemsSold += chunkHopper.statistics.periodItemsSold;
                allChunkHoppers_periodSales += chunkHopper.statistics.periodSales;

                chunkHopper.statistics.periodItemsCollected = 0L;
                chunkHopper.statistics.periodItemsSold = 0L;
                chunkHopper.statistics.periodSales = 0.0;
            }

            List<String> text = new ArrayList<>(ChunkHopper.instance.chunkHopperConfig.config.sellUpdates.lines);
            if(text != null) {
                for(int i = 0; i < text.size(); i++) {
                    text.set(i, ChatColor.translateAlternateColorCodes('&', text.get(i)));

                    text.set(i, text.get(i)
                            .replace("%lifetime_items_collected%", ChunkHopper.numberFormat.format(allChunkHoppers_lifetimeItemsCollected))
                            .replace("%lifetime_items_sold%", ChunkHopper.numberFormat.format(allChunkHoppers_lifetimeItemsSold))
                            .replace("%lifetime_sales%", ChunkHopper.numberFormat.format(allChunkHoppers_lifetimeSales))
                            .replace("%period_items_collected%", ChunkHopper.numberFormat.format(allChunkHoppers_periodItemsCollected))
                            .replace("%period_items_sold%", ChunkHopper.numberFormat.format(allChunkHoppers_periodItemsSold))
                            .replace("%period_sales%", ChunkHopper.numberFormat.format(allChunkHoppers_periodSales))
                    );

                    player.sendMessage(text.get(i));
                }
            }
        }
    }
}
