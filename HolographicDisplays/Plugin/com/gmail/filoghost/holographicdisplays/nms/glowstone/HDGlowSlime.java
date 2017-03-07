package com.gmail.filoghost.holographicdisplays.nms.glowstone;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import com.gmail.filoghost.holographicdisplays.nms.interfaces.entity.NMSEntityBase;
import com.gmail.filoghost.holographicdisplays.nms.interfaces.entity.NMSSlime;
import com.gmail.filoghost.holographicdisplays.object.line.CraftHologramLine;
import com.gmail.filoghost.holographicdisplays.object.line.CraftTouchSlimeLine;
import com.gmail.filoghost.holographicdisplays.util.DebugHandler;
import com.gmail.filoghost.holographicdisplays.util.ReflectionUtils;
import com.gmail.filoghost.holographicdisplays.util.VersionUtils;

import net.glowstone.entity.GlowEntity;
import net.glowstone.entity.GlowPlayer;
import net.glowstone.entity.meta.MetadataIndex;
import net.glowstone.entity.meta.MetadataIndex.StatusFlags;
import net.glowstone.entity.monster.GlowSlime;
import net.glowstone.net.message.play.entity.DestroyEntitiesMessage;
import net.glowstone.net.message.play.player.InteractEntityMessage;

public class HDGlowSlime extends GlowSlime implements NMSSlime {

    private CraftTouchSlimeLine parentPiece;

    public HDGlowSlime(Location loc, CraftTouchSlimeLine parentPiece) {
        super(loc);
        super.setInvulnerable(true);
        super.setCollidable(false);
        super.setSize(1);
        metadata.setBit(MetadataIndex.STATUS, StatusFlags.INVISIBLE, true);
        metadata.setBit(MetadataIndex.STATUS, StatusFlags.GLIDING, true);
        this.parentPiece = parentPiece;

        setBoundingBox(0, 0);
    }

    // HD API methods

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

    @Override
    public void setPassengerOfNMS(NMSEntityBase vehicleBase) {
        if (vehicleBase == null || !(vehicleBase instanceof Entity)) {
            return;
        }
        try {
            ReflectionUtils.setPrivateField(GlowEntity.class, vehicleBase, "passenger", this);
            vehicle = (GlowEntity) vehicleBase;
        } catch (Exception e) {
            DebugHandler.handleDebugException(e);
        }
    }

    // GlowSlime methods

    @Override
    public boolean entityInteract(GlowPlayer player, InteractEntityMessage msg) {
        return true;
    }

    @Override
    public void damage(double amount, Entity source, DamageCause cause) {
    }

    @Override
    public void setRawLocation(Location location, boolean fall) {
    }

    @Override
    public void pulse() {
    }

    @Override
    protected void pulsePhysics() {
    }

    @Override
    public boolean shouldSave() {
        return false;
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

    // Methods from Slime
    @Override
    public void setSize(int size) {
    }
}
