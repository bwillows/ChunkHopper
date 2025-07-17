package bwillows.chunkhopper;

import bwillows.chunkhopper.commands.ChunkHopperCommand;
import bwillows.chunkhopper.core.Data;
import bwillows.chunkhopper.core.Manager;
import bwillows.chunkhopper.economy.EconomyHandler;
import bwillows.chunkhopper.events.*;
import bwillows.chunkhopper.stack.ItemPickup;
import bwillows.chunkhopper.task.TaskManager;
import bwillows.chunkhopper.worth.WorthHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Properties;

public final class ChunkHopper extends JavaPlugin {
    public static ChunkHopper instance;
    public static String version;

    public static boolean IS_1_13 = Utils.isVersionAtLeast1_13();
    public static boolean IS_1_9 = Utils.isVersionAtLeast1_9();

    public static NumberFormat numberFormat;

    public File pluginFolder;

    public ItemPickup itemPickup;
    public ChunkHopperConfig chunkHopperConfig;
    public Manager manager;
    public Data data;
    public WorthHandler worth;
    public EconomyHandler economy;
    public TaskManager taskManager;
    public GUI gui;

    @Override
    public void onEnable() {
        instance = this;

        pluginFolder = new File(getDataFolder().getParent(), getDescription().getName());
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }

        Properties props = new Properties();
        try (InputStream in = getResource("version.properties")) {
            if (in != null) {
                props.load(in);
                this.version = props.getProperty("version");
            } else {
                Bukkit.getLogger().warning("[ChunkHopper] version.properties not found in plugin jar.");
            }
        } catch (IOException exception) {
            Bukkit.getLogger().warning("[ChunkHopper] Unhandled exception loading version info");
            exception.printStackTrace();
        }

        economy = new EconomyHandler();
        itemPickup = new ItemPickup();
        chunkHopperConfig = new ChunkHopperConfig();
        manager = new Manager();
        data = new Data();
        taskManager = new TaskManager();
        gui = new GUI();


        if (Bukkit.getPluginManager().getPlugin("ShopGUIPlus") != null) {
            Bukkit.getPluginManager().registerEvents(new ShopGUIPostEnableListener(), this);
        }

        Bukkit.getPluginManager().registerEvents(new BlockPlaceListener(), this);
        Bukkit.getPluginManager().registerEvents(new BlockBreakListener(), this);
        Bukkit.getPluginManager().registerEvents(new ChunkLoadListener(), this);
        Bukkit.getPluginManager().registerEvents(new ChunkUnloadListener(), this);
        Bukkit.getPluginManager().registerEvents(new WorldLoadListener(), this);
        Bukkit.getPluginManager().registerEvents(new EntityExplodeListener(), this);
        Bukkit.getPluginManager().registerEvents(new PistonListener(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryPickupItemListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(), this);
        Bukkit.getPluginManager().registerEvents(new ItemSpawnListener(), this);
        Bukkit.getPluginManager().registerEvents(gui, this);

        getCommand("chunkhopper").setExecutor(new ChunkHopperCommand());


    }

    @Override
    public void onDisable() {
        gracefulShutdown();
    }

    public void reload() {
        gracefulShutdown();
        chunkHopperConfig.reload();
        taskManager.createTasks();
    }

    public void gracefulShutdown() {
        taskManager.cancelTasks();
        for(bwillows.chunkhopper.model.ChunkHopper chunkHopper : manager.chunkHoppers.values()) {
            chunkHopper.removeHologram();
        }
        data.save();
    }
}
