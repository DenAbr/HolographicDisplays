package com.gmail.filoghost.holographicdisplays.nms.glowstone;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.flowpowered.network.Message;
import com.gmail.filoghost.holographicdisplays.nms.interfaces.entity.NMSArmorStand;
import com.gmail.filoghost.holographicdisplays.object.line.CraftHologramLine;
import com.gmail.filoghost.holographicdisplays.util.DebugHandler;
import com.gmail.filoghost.holographicdisplays.util.ReflectionUtils;
import com.gmail.filoghost.holographicdisplays.util.VersionUtils;

import net.glowstone.entity.GlowEntity;
import net.glowstone.entity.GlowPlayer;
import net.glowstone.entity.meta.MetadataIndex;
import net.glowstone.entity.meta.MetadataIndex.ArmorStandFlags;
import net.glowstone.entity.meta.MetadataIndex.StatusFlags;
import net.glowstone.entity.meta.MetadataMap;
import net.glowstone.entity.objects.GlowArmorStand;
import net.glowstone.net.message.play.entity.DestroyEntitiesMessage;
import net.glowstone.net.message.play.entity.SetPassengerMessage;
import net.glowstone.net.message.play.player.InteractEntityMessage;

public class HDGlowArmorStand extends GlowArmorStand implements NMSArmorStand, ArmorStand {

    private CraftHologramLine parentPiece;
    private boolean updateCalled = false;

    public HDGlowArmorStand(Location loc, CraftHologramLine parentPiece) {
        super(loc);
        this.parentPiece = parentPiece;

        metadata.setBit(MetadataIndex.ARMORSTAND_FLAGS, ArmorStandFlags.HAS_GRAVITY, false);
        metadata.setBit(MetadataIndex.ARMORSTAND_FLAGS, ArmorStandFlags.IS_SMALL, true);
        metadata.setBit(MetadataIndex.ARMORSTAND_FLAGS, ArmorStandFlags.HAS_ARMS, false);
        metadata.setBit(MetadataIndex.ARMORSTAND_FLAGS, ArmorStandFlags.IS_MARKER, true);
        metadata.setBit(MetadataIndex.ARMORSTAND_FLAGS, ArmorStandFlags.NO_BASE_PLATE, false);
        metadata.setBit(MetadataIndex.STATUS, StatusFlags.INVISIBLE, true);
        metadata.set(MetadataIndex.SHOW_NAME_TAG, false);

        super.setInvulnerable(true);
        super.setBoundingBox(0, 0);
    }

    // HD API methods

    @Override
    public String getCustomNameNMS() {
        return super.getCustomName();
    }

    @Override
    public void setCustomNameNMS(String arg0) {
        super.setCustomName(arg0);
        super.setCustomNameVisible(true);
    }

    @Override
    public Entity getBukkitEntityNMS() {
        return this;
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

    public void setPassengerNMS(HDGlowItem hdGlowItem) {
        try {
            ReflectionUtils.setPrivateField(GlowEntity.class, this, "passenger", hdGlowItem);
            passengerChanged = true;
        } catch (Exception e) {
            DebugHandler.handleDebugException(e);
        }
    }

    // GlowArmorStand methods
    @Override
    public void setRawLocation(Location location, boolean fall) {
    }

    @Override
    public boolean entityInteract(GlowPlayer player, InteractEntityMessage msg) {
        return true;
    }

    public MetadataMap getGlowMetadata() {
        return metadata;
    }

    @Override
    public List<Message> createSpawnMessage() {
        if (super.getPassenger() != null) {
            List<Message> mes = new LinkedList<>(super.createSpawnMessage());
            mes.add(new SetPassengerMessage(id, new int[] { super.getPassenger().getEntityId() }));
            return mes;
        }
        return super.createSpawnMessage();
    }

    @Override
    public List<Message> createUpdateMessage() {
        updateCalled = true;
        return super.createUpdateMessage();
    }

    @Override
    public void reset() {
        boolean pc = passengerChanged;
        super.reset();
        if (passengerChanged != pc) {
            if (pc) {
                if (!updateCalled) {
                    passengerChanged = true; // I really dont understand why
                                             // passenger sets right before
                                             // reset method
                } else {
                    updateCalled = false;
                }
            }
        }
    }

    @Override
    public void pulse() {
    }

    @Override
    protected void pulsePhysics() {
    }

    @Override
    public void damage(double amount, Entity source, DamageCause cause) {
    }

    @Override
    public EntityType getType() {
        return EntityType.ARMOR_STAND;
    }

    @Override
    public boolean shouldSave() {
        return false;
    }

    // Disallow all the bukkit methods.
    // ArmorStand methods
    @Override
    public void setArms(boolean arms) {
    }

    @Override
    public void setBasePlate(boolean basePlate) {
    }

    @Override
    public void setBodyPose(EulerAngle pose) {
    }

    @Override
    public void setBoots(ItemStack item) {
    }

    @Override
    public void setChestplate(ItemStack item) {
    }

    @Override
    public void setHeadPose(EulerAngle pose) {
    }

    @Override
    public void setHelmet(ItemStack item) {
    }

    @Override
    public void setItemInHand(ItemStack item) {
    }

    @Override
    public void setLeftArmPose(EulerAngle pose) {
    }

    @Override
    public void setLeftLegPose(EulerAngle pose) {
    }

    @Override
    public void setLeggings(ItemStack item) {
    }

    @Override
    public void setRightArmPose(EulerAngle pose) {
    }

    @Override
    public void setRightLegPose(EulerAngle pose) {
    }

    @Override
    public void setSmall(boolean small) {
    }

    @Override
    public void setVisible(boolean visible) {
    }

    @Override
    public void setMarker(boolean marker) {
    }

    // Methods from LivingEntity class
    @Override
    public boolean addPotionEffect(PotionEffect effect) {
        return false;
    }

    @Override
    public boolean addPotionEffect(PotionEffect effect, boolean param) {
        return false;
    }

    @Override
    public boolean addPotionEffects(Collection<PotionEffect> effects) {
        return false;
    }

    @Override
    public void setRemoveWhenFarAway(boolean remove) {
    }

    @Override
    public void setAI(boolean ai) {
    }

    @Override
    public void setCanPickupItems(boolean pickup) {
    }

    @Override
    public void setCollidable(boolean collidable) {
    }

    @Override
    public void setGliding(boolean gliding) {
    }

    @Override
    public boolean setLeashHolder(Entity holder) {
        return false;
    }

    // Methods from Entity
    @Override
    public void setVelocity(Vector vel) {
    }

    @Override
    public boolean teleport(Location loc) {
        return false;
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

    @Override
    public void setCustomName(String name) {
    }

    @Override
    public void setCustomNameVisible(boolean flag) {
    }

    @Override
    public boolean hasGravity() {
        return false;
    }

    @Override
    public void remove() {
        // Cannot be removed, this is the most important to override.
    }
}