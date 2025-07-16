package bwillows.chunkhopper.events;

import bwillows.chunkhopper.ChunkHopper;
import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.event.ShopGUIPlusPostEnableEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ShopGUIPostEnableListener implements Listener {
    @EventHandler
    public void ShopGUIPlusPostEnableListener(ShopGUIPlusPostEnableEvent event) {
        Bukkit.getLogger().info("[ChunkHopper] Initializing ShopGUIPlus hook");
        if(ChunkHopper.instance.worth == null) {
            ChunkHopper.instance.worth.tryRegisterShopGUIWorthProvider(ShopGuiPlusApi.getPlugin());
        } else {
            Bukkit.getLogger().severe("[ChunkHopper] Worth handler null during ShopGUIPlus initialization, worth provider may not be registered correctly.");
        }
    }
}
