package bwillows.chunkhopper.task;

import bwillows.chunkhopper.ChunkHopper;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class TaskManager {
    public TaskManager() {
        reload();
    }

    public BukkitTask autoSaveTask;
    public BukkitTask hologramUpdateTask;
    public BukkitTask chunkEntityRescanTask;
    public BukkitTask sellUpdateTask;

    public void reload() {
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

    public void autoSave() {
        ChunkHopper.instance.data.save();
    }

    public void hologramUpdate() {
        for(bwillows.chunkhopper.model.ChunkHopper chunkHopper : ChunkHopper.instance.manager.chunkHoppers.values()) {
            if(!chunkHopper.location.getChunk().isLoaded())
                return;
            chunkHopper.createHologram();
        }
    }

    public void chunkEntityRescan() {
        for(bwillows.chunkhopper.model.ChunkHopper chunkHopper : ChunkHopper.instance.manager.chunkHoppers.values()) {
            if(!chunkHopper.location.getChunk().isLoaded())
                continue;
            if(!chunkHopper.settings.collectionEnabled)
                continue;
            for(Entity entity : chunkHopper.location.getChunk().getEntities()) {
                if(!(entity instanceof Item))
                    continue;
                Item item = (Item) entity;
                if(!ChunkHopper.instance.manager.canCollectItem(item.getItemStack()))
                    continue;
                if(chunkHopper.collectItem(entity))
                    entity.remove();
            }
        }
    }

    public void sellUpdate() {

    }
}
