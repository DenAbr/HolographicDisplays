package com.gmail.filoghost.holographicdisplays;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.filoghost.holographicdisplays.bridge.bungeecord.BungeeServerTracker;
import com.gmail.filoghost.holographicdisplays.commands.main.HologramsCommandHandler;
import com.gmail.filoghost.holographicdisplays.disk.Configuration;
import com.gmail.filoghost.holographicdisplays.disk.HologramDatabase;
import com.gmail.filoghost.holographicdisplays.disk.UnicodeSymbols;
import com.gmail.filoghost.holographicdisplays.exception.HologramNotFoundException;
import com.gmail.filoghost.holographicdisplays.exception.InvalidFormatException;
import com.gmail.filoghost.holographicdisplays.exception.WorldNotFoundException;
import com.gmail.filoghost.holographicdisplays.listener.MainListener;
import com.gmail.filoghost.holographicdisplays.metrics.MetricsLite;
import com.gmail.filoghost.holographicdisplays.nms.glowstone.GlowManagerImpl;
import com.gmail.filoghost.holographicdisplays.nms.glowstone.HDGlowArmorStand;
import com.gmail.filoghost.holographicdisplays.nms.glowstone.HDGlowItem;
import com.gmail.filoghost.holographicdisplays.nms.glowstone.HDGlowSlime;
import com.gmail.filoghost.holographicdisplays.nms.interfaces.NMSManager;
import com.gmail.filoghost.holographicdisplays.object.NamedHologram;
import com.gmail.filoghost.holographicdisplays.object.NamedHologramManager;
import com.gmail.filoghost.holographicdisplays.object.PluginHologram;
import com.gmail.filoghost.holographicdisplays.object.PluginHologramManager;
import com.gmail.filoghost.holographicdisplays.placeholder.AnimationsRegister;
import com.gmail.filoghost.holographicdisplays.placeholder.PlaceholdersManager;
import com.gmail.filoghost.holographicdisplays.task.BungeeCleanupTask;
import com.gmail.filoghost.holographicdisplays.task.StartupLoadHologramsTask;
import com.gmail.filoghost.holographicdisplays.task.WorldPlayerCounterTask;
import com.gmail.filoghost.holographicdisplays.util.MinecraftVersion;

import net.glowstone.entity.CustomEntityDescriptor;
import net.glowstone.entity.EntityRegistry;

public class HolographicDisplays extends JavaPlugin {

    // The main instance of the plugin.
    private static HolographicDisplays instance;

    // The manager for net.minecraft.server access.
    private static NMSManager nmsManager;

    // The command handler, just in case a plugin wants to register more
    // commands.
    private HologramsCommandHandler commandHandler;

    // The new version found by the updater, null if there is no new version.
    private static String newVersion;

    @Override
    public void onLoad() {
        EntityRegistry
                .registerCustomEntity(new CustomEntityDescriptor<>(HDGlowArmorStand.class, this, "hdarmorstand", null));
        EntityRegistry.registerCustomEntity(new CustomEntityDescriptor<>(HDGlowItem.class, this, "hditem", null));
        EntityRegistry.registerCustomEntity(new CustomEntityDescriptor<>(HDGlowSlime.class, this, "hdslime", null));
    }

    @Override
    public void onEnable() {

        // Warn about plugin reloader and the /reload command.
        if (instance != null || System.getProperty("HolographicDisplaysLoaded") != null) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED
                    + "[HolographicDisplays] Please do not use /reload or plugin reloaders. Use the command \"/holograms reload\" instead. You will receive no support for doing this operation.");
        }

        System.setProperty("HolographicDisplaysLoaded", "true");
        instance = this;

        // Load placeholders.yml.
        UnicodeSymbols.load(this);

        // Load the configuration.
        Configuration.load(this);

        MinecraftVersion.set(MinecraftVersion.v1_11);
        nmsManager = new GlowManagerImpl();

        // Load animation files and the placeholder manager.
        PlaceholdersManager.load(this);
        try {
            AnimationsRegister.loadAnimations(this);
        } catch (Exception ex) {
            ex.printStackTrace();
            getLogger().warning("Failed to load animation files!");
        }

        // Initalize other static classes.
        HologramDatabase.loadYamlFile(this);
        BungeeServerTracker.startTask(Configuration.bungeeRefreshSeconds);

        // Start repeating tasks.
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new BungeeCleanupTask(), 5 * 60 * 20, 5 * 60 * 20);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new WorldPlayerCounterTask(), 0L, 3 * 20);

        Set<String> savedHologramsNames = HologramDatabase.getHolograms();
        if (savedHologramsNames != null && savedHologramsNames.size() > 0) {
            for (String singleHologramName : savedHologramsNames) {
                try {
                    NamedHologram singleHologram = HologramDatabase.loadHologram(singleHologramName);
                    NamedHologramManager.addHologram(singleHologram);
                } catch (HologramNotFoundException e) {
                    getLogger().warning("Hologram '" + singleHologramName + "' not found, skipping it.");
                } catch (InvalidFormatException e) {
                    getLogger().warning("Hologram '" + singleHologramName + "' has an invalid location format.");
                } catch (WorldNotFoundException e) {
                    getLogger().warning("Hologram '" + singleHologramName + "' was in the world '" + e.getMessage()
                            + "' but it wasn't loaded.");
                } catch (Exception e) {
                    e.printStackTrace();
                    getLogger().warning("Unhandled exception while loading the hologram '" + singleHologramName
                            + "'. Please contact the developer.");
                }
            }
        }

        if (getCommand("holograms") == null) {
            printWarnAndDisable("******************************************************",
                    "     HolographicDisplays was unable to register", "     the command \"holograms\". Do not modify",
                    "     plugin.yml removing commands, if this is", "     the case.",
                    "******************************************************");
            return;
        }

        getCommand("holograms").setExecutor(commandHandler = new HologramsCommandHandler());
        Bukkit.getPluginManager().registerEvents(new MainListener(nmsManager), this);

        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch (Exception ignore) {
        }

        // The entities are loaded when the server is ready.
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new StartupLoadHologramsTask(), 10L);
    }

    @Override
    public void onDisable() {
        for (NamedHologram hologram : NamedHologramManager.getHolograms()) {
            hologram.despawnEntities();
        }
        for (PluginHologram hologram : PluginHologramManager.getHolograms()) {
            hologram.despawnEntities();
        }
    }

    public static NMSManager getNMSManager() {
        return nmsManager;
    }

    public HologramsCommandHandler getCommandHandler() {
        return commandHandler;
    }

    private static void printWarnAndDisable(String... messages) {
        StringBuffer buffer = new StringBuffer("\n ");
        for (String message : messages) {
            buffer.append('\n');
            buffer.append(message);
        }
        buffer.append('\n');
        System.out.println(buffer.toString());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
        }
        instance.setEnabled(false);
    }

    public static HolographicDisplays getInstance() {
        return instance;
    }

    public static String getNewVersion() {
        return newVersion;
    }

}
