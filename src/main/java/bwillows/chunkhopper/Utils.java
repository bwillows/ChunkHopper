package bwillows.chunkhopper;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.Random;

public class Utils {
    public static String generateRandomString(int length) {
        // Define the characters to choose from (letters + digits)
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        // Create a Random object
        Random random = new Random();

        // StringBuilder to hold the generated string
        StringBuilder stringBuilder = new StringBuilder(length);

        // Generate the random string
        for (int i = 0; i < length; i++) {
            // Pick a random index from the characters string
            int randomIndex = random.nextInt(characters.length());
            // Append the character at the random index to the result
            stringBuilder.append(characters.charAt(randomIndex));
        }

        // Return the generated string
        return stringBuilder.toString();
    }
    public static boolean isInventoryFull(Player player) {
        Inventory inventory = player.getInventory();

        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                return false; // Found an empty slot
            }
        }

        return true; // No empty slots found
    }
    /**
     * Checks if the server is running on a version >= 1.13.
     *
     * @return true if the server version is 1.13 or higher, false otherwise
     */
    public static boolean isVersionAtLeast1_13() {
        // Get the full version string from Bukkit
        String version = Bukkit.getVersion();  // e.g. "git-PaperSpigot-445 (MC: 1.13.2)"

        // Extract the part after "MC: " and split on dots
        String[] versionParts = version.split("MC: ")[1].split("\\."); // ["1","13","2)"]

        try {
            int majorVersion = Integer.parseInt(versionParts[0]);  // e.g. 1
            int minorVersion = Integer.parseInt(versionParts[1]);  // e.g. 13

            // Return true if version is greater than 1, or exactly 1.13+
            return majorVersion > 1 || (majorVersion == 1 && minorVersion >= 13);
        } catch (Exception e) {
            // Fallback on parse error
            Bukkit.getLogger().warning("Failed to parse Minecraft version string: " + version);
            return false;
        }
    }
    /**
     * Checks if the server is running on a version >= 1.9.
     *
     * @return true if the server version is 1.9 or higher, false otherwise
     */
    public static boolean isVersionAtLeast1_9() {
        // Get the full version string from Bukkit
        String version = Bukkit.getVersion();  // e.g. "git-PaperSpigot-445 (MC: 1.13.2)"

        // Extract the part after "MC: " and split on dots
        String[] versionParts = version.split("MC: ")[1].split("\\."); // ["1","13","2)"]

        try {
            int majorVersion = Integer.parseInt(versionParts[0].replaceAll("[^0-9]", ""));
            int minorVersion = Integer.parseInt(versionParts[1].replaceAll("[^0-9]", ""));

            // Return true if version is greater than 1, or exactly 1.9+
            return majorVersion > 1 || (majorVersion == 1 && minorVersion >= 9);
        } catch (Exception e) {
            // Fallback on parse error
            Bukkit.getLogger().warning("Failed to parse Minecraft version string: " + version);
            return false;
        }
    }
    public static int getMaxChunkHopperAmount(Player player) {
        int max = 0;

        for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            String perm = info.getPermission().toLowerCase();

            if (perm.equals("chunkhopper.amount.*")) {
                return Integer.MAX_VALUE; // unlimited placement
            }

            if (perm.startsWith("chunkhopper.amount.")) {
                String value = perm.substring("chunkhopper.amount.".length());
                try {
                    int parsed = Integer.parseInt(value);
                    if (parsed > max) {
                        max = parsed;
                    }
                } catch (NumberFormatException ignored) {
                    // Ignore invalid non-numeric suffixes
                }
            }
        }

        return max;
    }
}
