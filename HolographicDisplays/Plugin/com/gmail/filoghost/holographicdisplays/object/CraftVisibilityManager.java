package com.gmail.filoghost.holographicdisplays.object;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.flowpowered.network.Message;
import com.gmail.filoghost.holographicdisplays.HolographicDisplays;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;
import com.gmail.filoghost.holographicdisplays.nms.glowstone.HDGlowArmorStand;
import com.gmail.filoghost.holographicdisplays.nms.glowstone.HDGlowItem;
import com.gmail.filoghost.holographicdisplays.nms.glowstone.HDGlowSlime;
import com.gmail.filoghost.holographicdisplays.object.line.CraftHologramLine;
import com.gmail.filoghost.holographicdisplays.object.line.CraftItemLine;
import com.gmail.filoghost.holographicdisplays.object.line.CraftTextLine;
import com.gmail.filoghost.holographicdisplays.object.line.CraftTouchSlimeLine;
import com.gmail.filoghost.holographicdisplays.object.line.CraftTouchableLine;
import com.gmail.filoghost.holographicdisplays.util.Utils;
import com.gmail.filoghost.holographicdisplays.util.Validator;
import com.gmail.filoghost.holographicdisplays.util.VersionUtils;

import net.glowstone.entity.GlowPlayer;
import net.glowstone.net.message.play.entity.AttachEntityMessage;
import net.glowstone.net.message.play.entity.DestroyEntitiesMessage;

public class CraftVisibilityManager implements VisibilityManager {

    private final CraftHologram hologram;
    private Map<String, Boolean> playersVisibilityMap;
    private boolean visibleByDefault;

    private static final int VISIBILITY_DISTANCE_SQUARED = 64 * 64;

    public CraftVisibilityManager(CraftHologram hologram) {
        Validator.notNull(hologram, "hologram");
        this.hologram = hologram;
        this.visibleByDefault = true;
    }

    @Override
    public boolean isVisibleByDefault() {
        return visibleByDefault;
    }

    @Override
    public void setVisibleByDefault(boolean visibleByDefault) {
        if (this.visibleByDefault != visibleByDefault) {

            boolean oldVisibleByDefault = this.visibleByDefault;
            this.visibleByDefault = visibleByDefault;

            for (Player player : VersionUtils.getOnlinePlayers()) {

                if (playersVisibilityMap != null && playersVisibilityMap.containsKey(player.getName().toLowerCase())) {
                    // Has a specific value set
                    continue;
                }

                if (oldVisibleByDefault) {
                    // If previously was visible, now is NOT visible by
                    // default,
                    // because the value has changed
                    sendDestroyPacketIfNear(player, hologram);
                } else {
                    // Opposite case
                    sendCreatePacketIfNear(player, hologram);
                }
            }
        }
    }

    @Override
    public void showTo(Player player) {
        Validator.notNull(player, "player");

        boolean wasVisible = isVisibleTo(player);

        if (playersVisibilityMap == null) {
            // Lazy initialization
            playersVisibilityMap = new ConcurrentHashMap<String, Boolean>();
        }

        playersVisibilityMap.put(player.getName().toLowerCase(), true);

        if (!wasVisible) {
            sendCreatePacketIfNear(player, hologram);
        }
    }

    @Override
    public void hideTo(Player player) {
        Validator.notNull(player, "player");

        boolean wasVisible = isVisibleTo(player);

        if (playersVisibilityMap == null) {
            // Lazy initialization
            playersVisibilityMap = new ConcurrentHashMap<String, Boolean>();
        }

        playersVisibilityMap.put(player.getName().toLowerCase(), false);

        if (wasVisible) {
            sendDestroyPacketIfNear(player, hologram);
        }
    }

    @Override
    public boolean isVisibleTo(Player player) {
        Validator.notNull(player, "player");

        if (playersVisibilityMap != null) {

            Boolean value = playersVisibilityMap.get(player.getName().toLowerCase());
            if (value != null) {
                return value;
            }
        }

        return visibleByDefault;
    }

    @Override
    public void resetVisibility(Player player) {
        Validator.notNull(player, "player");

        if (playersVisibilityMap == null) {
            return;
        }

        boolean wasVisible = isVisibleTo(player);

        playersVisibilityMap.remove(player.getName().toLowerCase());

        if (visibleByDefault && !wasVisible) {
            sendCreatePacketIfNear(player, hologram);
        } else if (!visibleByDefault && wasVisible) {
            sendDestroyPacketIfNear(player, hologram);
        }
    }

    @Override
    public void resetVisibilityAll() {
        if (playersVisibilityMap != null) {

            // We need to refresh all the players
            Set<String> playerNames = new HashSet<String>(playersVisibilityMap.keySet());

            for (String playerName : playerNames) {
                Player onlinePlayer = Bukkit.getPlayerExact(playerName);
                if (onlinePlayer != null) {
                    resetVisibility(onlinePlayer);
                }
            }

            playersVisibilityMap.clear();
            playersVisibilityMap = null;
        }
    }

    private static void sendCreatePacketIfNear(Player player, CraftHologram hologram) {
        if (!isNear(player, hologram))
            return;

        // FIXME prevent sending before destroy packets
        new BukkitRunnable() {

            @Override
            public void run() {

                for (CraftHologramLine line : hologram.getLinesUnsafe()) {
                    if (line instanceof CraftItemLine) {
                        HDGlowItem item = (HDGlowItem) ((CraftItemLine) line).getNmsItem();
                        HDGlowArmorStand stand = (HDGlowArmorStand) ((CraftItemLine) line).getNmsVehicle();
                        if (line.isSpawned()) {
                            sendPackets(player, stand.createSpawnMessage());
                            sendPackets(player, item.createSpawnMessage());
                            sendAttachPackets(player, item, stand);
                        }
                    } else if (line instanceof CraftTextLine) {
                        HDGlowArmorStand nameable = (HDGlowArmorStand) ((CraftTextLine) line).getNmsNameble();
                        sendPackets(player, nameable.createSpawnMessage());
                    }

                    if (line instanceof CraftTouchableLine) {
                        CraftTouchableLine touchableLine = (CraftTouchableLine) line;
                        if (touchableLine.isSpawned() && touchableLine.getTouchSlime() != null) {
                            CraftTouchSlimeLine slimeLine = touchableLine.getTouchSlime();
                            if (slimeLine.isSpawned()) {
                                sendPackets(player,
                                        ((HDGlowArmorStand) slimeLine.getNmsVehicle()).createSpawnMessage());
                                sendPackets(player, ((HDGlowSlime) slimeLine.getNmsSlime()).createSpawnMessage());
                                sendAttachPackets(player, slimeLine.getNmsSlime().getBukkitEntityNMS(),
                                        slimeLine.getNmsVehicle().getBukkitEntityNMS());
                            }
                        }
                    }
                }
            }
        }.runTask(HolographicDisplays.getInstance());
    }

    private static void sendAttachPackets(Player player, Entity passenger, Entity stand) {
        AttachEntityMessage packet = new AttachEntityMessage(passenger.getEntityId(), stand.getEntityId());
        sendPackets(player, Arrays.asList(packet));
    }

    private static void sendPackets(Player p, List<Message> packets) {
        GlowPlayer player = (GlowPlayer) p;
        player.getSession().sendAll(packets.toArray(new Message[0]));
    }

    private static void sendDestroyPacketIfNear(Player player, CraftHologram hologram) {
        if (!isNear(player, hologram)) {
            return;
        }
        List<Integer> ids = Utils.newList();
        for (CraftHologramLine line : hologram.getLinesUnsafe()) {
            if (line.isSpawned()) {
                for (int id : line.getEntitiesIDs()) {
                    ids.add(id);
                }
            }
        }

        if (!ids.isEmpty()) {
            // FIXME prevent sending before spawn packets
            new BukkitRunnable() {

                @Override
                public void run() {
                    DestroyEntitiesMessage message = new DestroyEntitiesMessage(ids);
                    sendPackets(player, Arrays.asList(message));
                    // it needs to be reworked
                    // spawn packets are sending every tick, not immediately
                }
            }.runTask(HolographicDisplays.getInstance());
        }
    }

    private static boolean isNear(Player player, CraftHologram hologram) {
        return player.isOnline() && player.getWorld().equals(hologram.getWorld())
                && player.getLocation().distanceSquared(hologram.getLocation()) < VISIBILITY_DISTANCE_SQUARED;
    }

    @Override
    public String toString() {
        return "CraftVisibilityManager [playersMap=" + playersVisibilityMap + ", visibleByDefault=" + visibleByDefault
                + "]";
    }

}
