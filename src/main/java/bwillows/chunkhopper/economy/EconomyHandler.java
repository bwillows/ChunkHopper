package bwillows.chunkhopper.economy;

import org.bukkit.Bukkit;

public class EconomyHandler {

    private EconomyProvider provider;

    public EconomyHandler() {
        // Handle economy provider(s) in order
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            provider = new VaultEconomyProvider();
        } else {
            provider = null;
        }

        // Warn if no provider
        if(provider == null) {
            Bukkit.getLogger().warning("[ChunkHopper] No economy provider found! Some features may not work.");
        }
    }

    public EconomyProvider getProvider() {
        return provider;
    }

    public boolean isHooked() {
        return provider != null;
    }
}
