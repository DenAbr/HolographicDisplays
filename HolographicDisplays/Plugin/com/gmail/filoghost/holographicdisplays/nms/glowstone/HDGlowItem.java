package com.gmail.filoghost.holographicdisplays.nms.glowstone;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.flowpowered.network.Message;
import com.gmail.filoghost.holographicdisplays.listener.MainListener;
import com.gmail.filoghost.holographicdisplays.nms.interfaces.entity.NMSEntityBase;
import com.gmail.filoghost.holographicdisplays.nms.interfaces.entity.NMSItem;
import com.gmail.filoghost.holographicdisplays.object.line.CraftHologramLine;
import com.gmail.filoghost.holographicdisplays.object.line.CraftItemLine;
import com.gmail.filoghost.holographicdisplays.util.DebugHandler;
import com.gmail.filoghost.holographicdisplays.util.VersionUtils;

import net.glowstone.entity.GlowEntity;
import net.glowstone.entity.GlowPlayer;
import net.glowstone.entity.meta.MetadataIndex;
import net.glowstone.entity.meta.MetadataIndex.StatusFlags;
import net.glowstone.entity.meta.MetadataMap;
import net.glowstone.net.message.play.entity.DestroyEntitiesMessage;
import net.glowstone.net.message.play.entity.EntityMetadataMessage;
import net.glowstone.net.message.play.entity.EntityTeleportMessage;
import net.glowstone.net.message.play.entity.EntityVelocityMessage;
import net.glowstone.net.message.play.entity.SpawnObjectMessage;
import net.glowstone.net.message.play.player.InteractEntityMessage;
import net.glowstone.util.Position;

public class HDGlowItem extends GlowEntity implements NMSItem, Item {

    private CraftItemLine parentPiece;
    private int pickupDelay;
    private Map<Player, Integer> pickupingPlayers;

    public HDGlowItem(Location loc, CraftItemLine piece, ItemStack stack) {
        super(loc);
        super.setInvulnerable(true);
        setItemStackNMS(stack);
        metadata.setBit(MetadataIndex.STATUS, StatusFlags.GLIDING, true);
        this.parentPiece = piece;
        this.pickupingPlayers = new ConcurrentHashMap<>();
    }

    // HD API methods

    @Override
    public Entity getBukkitEntityNMS() {
        return this;
    }

    public MetadataMap getGlowMetadata() {
        return metadata;
    }

    @Override
    public CraftHologramLine getHologramLine() {
        return parentPiece;
    }

    @Override
    public int getIdNMS() {
        return id;
    }

    @Override
    public boolean isDeadNMS() {
        return super.isDead();
    }

    @Override
    public void killEntityNMS() {
        super.remove();
        DestroyEntitiesMessage mes = new DestroyEntitiesMessage(Arrays.asList(id));
        for (Player p : VersionUtils.getOnlinePlayers()) {
            ((GlowPlayer) p).getSession().send(mes);
        }
    }

    @Override
    public void setLocationNMS(double arg0, double arg1, double arg2) {
        Location location = getLocation().clone();
        location.setX(arg0);
        location.setY(arg1);
        location.setZ(arg2);
        super.setRawLocation(location, false);
    }

    @Override
    public void setLockTick(boolean arg0) {
    }

    @Override
    public void setPassengerOfNMS(NMSEntityBase vehicleBase) {
        try {
            HDGlowArmorStand stand = (HDGlowArmorStand) vehicleBase;
            vehicle = (GlowEntity) vehicleBase;
            stand.setPassengerNMS(this);
        } catch (Exception e) {
            DebugHandler.handleDebugException(e);
        }
    }

    @Override
    public void setItemStackNMS(ItemStack is) {
        is.setAmount(1);
        metadata.set(MetadataIndex.ITEM_ITEM, is.clone());
    }

    // GlowEntity methods
    @Override
    public void setRawLocation(Location location, boolean fall) {
    }

    @Override
    public ItemStack getItemStack() {
        return (ItemStack) metadata.get(MetadataIndex.ITEM_ITEM);
    }

    @Override
    public int getPickupDelay() {
        return pickupDelay;
    }

    @Override
    public void allowPickup(boolean pickup) {
    }

    @Override
    public Object getRawItemStack() {
        return getItemStack();
    }

    @Override
    public List<Message> createSpawnMessage() {
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        int yaw = Position.getIntYaw(location);
        int pitch = Position.getIntPitch(location);

        return Arrays.asList(new SpawnObjectMessage(id, getUniqueId(), SpawnObjectMessage.ITEM, x, y, z, pitch, yaw),
                new EntityMetadataMessage(id, metadata.getEntryList()),
                new EntityTeleportMessage(id, x, y, z, yaw, pitch), new EntityVelocityMessage(id, new Vector(0, 0, 0)));
    }

    @Override
    public void pulse() {
        // handle every near player at the same time because no one can pickup
        // the
        // item
        if (this.parentPiece.getPickupHandler() != null) {
            Collection<Player> gone = new HashSet<>(pickupingPlayers.keySet());
            getNearbyEntities(1, 0.5, 1).stream().filter(entity -> entity.getType() == EntityType.PLAYER)
                    .filter(entity -> !entity.isDead()).map(entity -> (Player) entity).forEach(player -> {
                        if (pickupingPlayers.containsKey(player)) {
                            pickupingPlayers.put(player, pickupingPlayers.get(player) + 1);
                            if (pickupingPlayers.get(player) == 15)
                                MainListener.handleItemLinePickup(player, this.parentPiece.getPickupHandler(),
                                        this.parentPiece.getParent());
                            else
                                gone.remove(player);

                        } else
                            pickupingPlayers.put(player, 0);
                    });
            gone.forEach(pickupingPlayers::remove);
        }
    }

    @Override
    protected void pulsePhysics() {
    }

    @Override
    public EntityType getType() {
        return EntityType.DROPPED_ITEM;
    }

    @Override
    public boolean shouldSave() {
        return false;
    }

    // Methods from Entity
    @Override
    public void remove() {
    }

    @Override
    public void setVelocity(Vector vel) {
    }

    @Override
    public String getCustomName() {
        return getItemStack().getItemMeta().getDisplayName();
    }

    @Override
    public boolean teleport(Location loc) {
        return false;
    }

    @Override
    public boolean entityInteract(GlowPlayer player, InteractEntityMessage msg) {
        return true;
    }

    @Override
    public boolean teleport(Entity entity) {
        return false;
    }

    @Override
    public boolean teleport(Location loc, TeleportCause cause) {
        return false;
    }

    @Override
    public boolean teleport(Entity entity, TeleportCause cause) {
        return false;
    }

    @Override
    public void setFireTicks(int ticks) {
    }

    @Override
    public boolean setPassenger(Entity entity) {
        return false;
    }

    @Override
    public boolean eject() {
        return false;
    }

    @Override
    public boolean leaveVehicle() {
        return false;
    }

    @Override
    public void playEffect(EntityEffect effect) {
    }

    @Override
    public void setCustomName(String name) {
    }

    @Override
    public void setCustomNameVisible(boolean flag) {
    }

    @Override
    public void setGlowing(boolean flag) {
    }

    @Override
    public void setGravity(boolean gravity) {
    }

    @Override
    public void setInvulnerable(boolean flag) {
    }

    @Override
    public void setSilent(boolean flag) {
    }

    @Override
    public void setTicksLived(int value) {
    }

    // Methods from Item
    @Override
    public void setItemStack(ItemStack stack) {
    }

    @Override
    public void setPickupDelay(int delay) {
    }
}
