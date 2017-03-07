package com.gmail.filoghost.holographicdisplays.commands.main.subs;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gmail.filoghost.holographicdisplays.api.handler.PickupHandler;
import com.gmail.filoghost.holographicdisplays.api.handler.TouchHandler;
import com.gmail.filoghost.holographicdisplays.api.line.ItemLine;
import com.gmail.filoghost.holographicdisplays.commands.Strings;
import com.gmail.filoghost.holographicdisplays.commands.main.HologramSubCommand;
import com.gmail.filoghost.holographicdisplays.exception.CommandException;
import com.gmail.filoghost.holographicdisplays.object.NamedHologram;
import com.gmail.filoghost.holographicdisplays.object.NamedHologramManager;

import net.glowstone.entity.GlowPlayer;

public class GlowTest extends HologramSubCommand {

    public GlowTest() {
        super("gtest");
        setPermission(Strings.BASE_PERM + "glowstonetest");
    }

    @Override
    public String getPossibleArguments() {
        return "";
    }

    @Override
    public int getMinimumArguments() {
        return 0;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) throws CommandException {
        GlowPlayer player = (GlowPlayer) sender;
        NamedHologram hologram = new NamedHologram(player.getEyeLocation(), "gtest1");
        NamedHologramManager.addHologram(hologram);
        hologram.appendTextLine("Hello World");
        ItemLine line = hologram.appendItemLine(new ItemStack(Material.DIAMOND));
        line.setTouchHandler(new TouchHandler() {

            @Override
            public void onTouch(Player player) {
                player.sendMessage("Item touch works");
            }
        });
        line.setPickupHandler(new PickupHandler() {

            @Override
            public void onPickup(Player player) {
                player.sendMessage("Item pickup handler works");
            }
        });
        hologram.appendTextLine("This hologram is");
        hologram.appendTextLine(ChatColor.BOLD + "on Glowstone server").setTouchHandler(new TouchHandler() {

            @Override
            public void onTouch(Player player) {
                player.sendMessage("Text touch works");
            }
        });

        NamedHologram visibilityTest = new NamedHologram(player.getEyeLocation().clone().add(0, 3, 0), "gtest2");
        NamedHologramManager.addHologram(visibilityTest);
        visibilityTest.appendTextLine("This hologram shouble be visible for");
        visibilityTest.appendTextLine(player.getName());
        visibilityTest.appendTextLine("but visibility works bad");

        visibilityTest.getVisibilityManager().setVisibleByDefault(false);
        visibilityTest.getVisibilityManager().showTo(player);
    }

    @Override
    public List<String> getTutorial() {
        return Arrays.asList("Run some tests");
    }

    @Override
    public SubCommandType getType() {
        return SubCommandType.GENERIC;
    }

}
