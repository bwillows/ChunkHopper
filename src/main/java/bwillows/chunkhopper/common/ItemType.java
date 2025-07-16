package bwillows.chunkhopper.common;

import org.bukkit.Material;

import java.util.Objects;

public class ItemType {
    public Material material;
    public short damage;

    public ItemType() {
        this.material = Material.AIR;
        this.damage = 0;
    }

    public ItemType(Material material, short damage) {
        this.material = material;
        this.damage = damage;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            ItemType that = (ItemType) obj;
            return (that.material.equals(this.material) && that.damage == this.damage);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.material.toString() + damage});
    }

    @Override
    public String toString() {
        return "MATERIAL:" + material.toString() + ",DAMAGE:" + Integer.toString(damage);
    }
}
