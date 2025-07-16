package bwillows.chunkhopper.worth;
import bwillows.chunkhopper.common.ItemType;
import bwillows.chunkhopper.stack.WildStackerProvider;
import net.brcdev.shopgui.ShopGuiPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WorthHandler {

    private final List<WorthProvider> providers = new ArrayList<>();
    private ConfigurationSection worthSection;
    private ConfigurationSection shopguiSection;

    private LocalWorthProvider localProvider;

    public WorthHandler(FileConfiguration config) {
        this.worthSection = config.getConfigurationSection("worth");
        if (this.worthSection == null) return;

        // Essentials

        if (Bukkit.getPluginManager().isPluginEnabled("Esesentials")) {
            ConfigurationSection essentialsSection = worthSection.getConfigurationSection("essentials");
            if (essentialsSection != null && essentialsSection.getBoolean("enabled", false)) {
                providers.add(new EssentialsWorthProvider(
                        essentialsSection.getInt("weight", 1)
                ));
            }
        }

        // Store ShopGUI config for post-enable injection
        this.shopguiSection = worthSection.getConfigurationSection("shopgui");
        if (shopguiSection != null && shopguiSection.getBoolean("enabled", false)) {
            // Defer actual provider creation until post-enable
        }

        // Local
        ConfigurationSection localSection = worthSection.getConfigurationSection("local");
        if (localSection != null && localSection.getBoolean("enabled", false)) {
            this.localProvider = new LocalWorthProvider(localSection.getInt("weight", 1));
            providers.add(this.localProvider);
        }

        providers.sort(Comparator.comparingInt(WorthProvider::getWeight).reversed());
    }

    public void tryRegisterShopGUIWorthProvider(ShopGuiPlugin plugin) {
        if (shopguiSection == null || !shopguiSection.getBoolean("enabled", false)) return;

        int weight = shopguiSection.getInt("weight", 1);
        WorthProvider provider = new ShopGUIWorthProvider(plugin, weight);

        providers.add(provider);
        providers.sort(Comparator.comparingInt(WorthProvider::getWeight).reversed());
    }

    public double getWorth(ItemStack itemStack) {
        if (itemStack == null) return 0.0;

        for (WorthProvider provider : providers) {
            if (!provider.isEnabled()) continue;

            double worth = provider.getWorth(itemStack);
            if (worth > 0) return worth;
        }
        return 0.0;
    }

    public double getWorth(ItemType itemType) {
        if (itemType == null) return 0.0;

        for (WorthProvider provider : providers) {
            if (!provider.isEnabled()) continue;

            double worth = provider.getWorth(itemType);
            if (worth > 0) return worth;
        }
        return 0.0;
    }

    public List<WorthProvider> getProviders() {
        return providers;
    }

    public LocalWorthProvider getLocalProvider() {
        return localProvider;
    }


}