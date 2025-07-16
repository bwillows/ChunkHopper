package bwillows.chunkhopper.core;

import bwillows.chunkhopper.Utils;
import bwillows.chunkhopper.common.ChunkLocation;
import bwillows.chunkhopper.model.ChunkHopper;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class Manager {
    public Map<Location, ChunkHopper> chunkHoppers = new HashMap<>();
    public Map<UUID, Set<Location>> chunkHoppersByOwner = new HashMap<>();
    // ChunkLocations with ChunkHoppers to ChunkHopper within
    public Map<ChunkLocation, Location> chunkHopperChunks = new HashMap<>();
    // ChunkHoppers with invalid world names by world name
    public Map<String, Set<ChunkHopper>> invalidChunkHoppers = new HashMap<>();

    public void placeChunkHopper(Location location, UUID owner) {
        location.getBlock().setType(bwillows.chunkhopper.ChunkHopper.instance.chunkHopperConfig.config.block.material);
        if(!bwillows.chunkhopper.ChunkHopper.IS_1_13) {
            try {
                // Inline reflection call to setData(byte)
                location.getBlock().getClass().getMethod("setData", byte.class).invoke(location.getBlock(), (byte) bwillows.chunkhopper.ChunkHopper.instance.chunkHopperConfig.config.block.damage);
            } catch (NoSuchMethodException ignored) {
                // Method doesn't exist on 1.13+ — safely ignored
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ChunkHopper chunkHopper = new ChunkHopper();
        chunkHopper.owner = owner;
        chunkHopper.location = location;

        ChunkLocation chunkLocation = ChunkLocation.fromLocation(location);

        chunkHoppers.put(location, chunkHopper);
        chunkHopperChunks.put(chunkLocation, location);
        chunkHopper.createHologram();

        chunkHoppersByOwner.computeIfAbsent(owner, k -> new HashSet<>()).add(location);
    }

    public void removeChunkHopper(Location location) {
        ChunkHopper chunkHopper = chunkHoppers.get(location);
        chunkHopper.removeHologram();

        ChunkLocation chunkLocation = ChunkLocation.fromLocation(location);

        Set<Location> locations = chunkHoppersByOwner.get(chunkHopper.owner);
        if (locations != null) {
            locations.remove(location);
            if (locations.isEmpty()) {
                chunkHoppersByOwner.remove(chunkHopper.owner);
            }
        }

        chunkHoppers.remove(location);
        chunkHopperChunks.remove(chunkLocation);
    }

    public boolean hasChunkHopper(ChunkLocation location) {
        return chunkHopperChunks.containsKey(location);
    }

    public ChunkHopper getChunkHopperAtChunk(ChunkLocation location) {
        if(!hasChunkHopper(location))
            return null;
        Location hopperLocation = chunkHopperChunks.get(location);
        if(hopperLocation == null)
            return null;
        return chunkHoppers.get(hopperLocation);
    }

    public ItemStack getChunkHopperItem() {
        ItemStack item = new ItemStack(bwillows.chunkhopper.ChunkHopper.instance.chunkHopperConfig.config.item.material);
        item.setDurability(bwillows.chunkhopper.ChunkHopper.instance.chunkHopperConfig.config.item.damage);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', bwillows.chunkhopper.ChunkHopper.instance.chunkHopperConfig.config.item.name));
        List<String> lore = new ArrayList<>(bwillows.chunkhopper.ChunkHopper.instance.chunkHopperConfig.config.item.lore);
        for(int i = 0; i < lore.size(); i++) {
            lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);

        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString("type", "ChunkHopper");
        nbtItem.setString("ID", Utils.generateRandomString(7));

        return nbtItem.getItem();
    }

    public boolean isChunkHopperItem(ItemStack itemStack) {
        if(itemStack == null || itemStack.getType() == null)
            return false;
        NBTItem nbtItem = new NBTItem(itemStack);
        return nbtItem.hasKey("type") && nbtItem.getString("type").equals("ChunkHopper");
    }

    public boolean canCollectItem(ItemStack itemStack) {
        return false;
    }
}
