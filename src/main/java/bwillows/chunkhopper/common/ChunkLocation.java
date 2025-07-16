package bwillows.chunkhopper.common;

import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.Objects;

public class ChunkLocation {
    public String world;
    public int X;
    public int Z;

    public ChunkLocation(String world, int X, int Z) {
        this.world = world;
        this.X = X;
        this.Z = Z;
    }

    @Override
    public String toString() {
        return "WORLD:" + world + ",X:" + Integer.toString(X) + ",Z:" + Integer.toString(Z);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            ChunkLocation that = (ChunkLocation) obj;
            return (that.world.equals(this.world) && that.X == this.X && that.Z == this.Z);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.toString()});
    }

    public static ChunkLocation fromLocation(Location location) {
        ChunkLocation chunkLocation = new ChunkLocation(location.getWorld().getName(), location.getChunk().getX(), location.getChunk().getZ());
        return chunkLocation;
    }

    public static ChunkLocation fromChunk(Chunk chunk) {
        return new ChunkLocation(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }
}


