package com.gmail.filoghost.holographicdisplays.nms.glowstone;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import com.gmail.filoghost.holographicdisplays.nms.interfaces.FancyMessage;
import com.gmail.filoghost.holographicdisplays.nms.interfaces.NMSManager;
import com.gmail.filoghost.holographicdisplays.nms.interfaces.entity.NMSArmorStand;
import com.gmail.filoghost.holographicdisplays.nms.interfaces.entity.NMSEntityBase;
import com.gmail.filoghost.holographicdisplays.nms.interfaces.entity.NMSHorse;
import com.gmail.filoghost.holographicdisplays.nms.interfaces.entity.NMSItem;
import com.gmail.filoghost.holographicdisplays.nms.interfaces.entity.NMSWitherSkull;
import com.gmail.filoghost.holographicdisplays.object.line.CraftHologramLine;
import com.gmail.filoghost.holographicdisplays.object.line.CraftItemLine;
import com.gmail.filoghost.holographicdisplays.object.line.CraftTouchSlimeLine;

public class GlowManagerImpl implements NMSManager {

    @Override
    public void setup() throws Exception {

    }

    @Override
    public NMSHorse spawnNMSHorse(org.bukkit.World world, double x, double y, double z, CraftHologramLine parentPiece) {
        throw new NotImplementedException("Method can only be used on 1.7 or lower");
    }

    @Override
    public NMSWitherSkull spawnNMSWitherSkull(org.bukkit.World bukkitWorld, double x, double y, double z,
            CraftHologramLine parentPiece) {
        throw new NotImplementedException("Method can only be used on 1.7 or lower");
    }

    @Override
    public NMSItem spawnNMSItem(org.bukkit.World bukkitWorld, double x, double y, double z, CraftItemLine parentPiece,
            ItemStack stack) {
        Location loc = new Location(bukkitWorld, x, y, z);
        HDGlowItem customItem = new HDGlowItem(loc, parentPiece, stack);
        return customItem;
    }

    @Override
    public HDGlowSlime spawnNMSSlime(org.bukkit.World bukkitWorld, double x, double y, double z,
            CraftTouchSlimeLine parentPiece) {
        Location loc = new Location(bukkitWorld, x, y, z);
        HDGlowSlime touchSlime = new HDGlowSlime(loc, parentPiece);
        return touchSlime;
    }

    @Override
    public NMSArmorStand spawnNMSArmorStand(org.bukkit.World bukkitWorld, double x, double y, double z,
            CraftHologramLine parentPiece) {
        Location loc = new Location(bukkitWorld, x, y, z);
        HDGlowArmorStand invisibleArmorStand = new HDGlowArmorStand(loc, parentPiece);
        return invisibleArmorStand;
    }

    @Override
    public boolean isNMSEntityBase(org.bukkit.entity.Entity bukkitEntity) {
        return bukkitEntity instanceof NMSEntityBase;
    }

    @Override
    public NMSEntityBase getNMSEntityBase(org.bukkit.entity.Entity bukkitEntity) {
        if (bukkitEntity instanceof NMSEntityBase) {
            return ((NMSEntityBase) bukkitEntity);
        }
        return null;
    }

    @Override
    public FancyMessage newFancyMessage(String text) {
        return new FancyMessageImpl(text);
    }

    @Override
    public boolean isUnloadUnsure(Chunk bukkitChunk) {
        return bukkitChunk.getWorld().isChunkInUse(bukkitChunk.getX(), bukkitChunk.getZ());
    }

}
