package me.sirrus86.s86powers.powers.internal.passive;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Sets;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;

@PowerManifest(name = "Lumberjack", type = PowerType.PASSIVE, author = "sirrus86", concept = "sirrus86", icon = Material.IRON_AXE,
	description = "Breaking log blocks[require-axe] using an axe[/require-axe] will cause all adjacent logs[break-leaves] and leaves[/break-leaves] to also break."
			+ "[auto-replant] Trees cut down are automatically replanted.[/auto-replant]")
public final class Lumberjack extends Power {

	private List<Block> blocks;
	private final Set<Material> plantable = Sets.newHashSet(Material.DIRT, Material.GRASS);
	private boolean doLeaves, doThreshold, replant, reqAxe;
	private int threshold;
	
	@Override
	protected void onEnable() {
		blocks = new ArrayList<>();
	}

	@Override
	protected void config() {
		doLeaves = option("break-leaves", true, "Whether adjacent leaves should also be broken when trees are chopped down.");
		doThreshold = option("apply-threshold", true, "Whether the number of blocks broken at one time should be limited.");
		replant = option("auto-replant", true, "Whether a sapling should be placed where the tree once stood after being chopped.");
		reqAxe = option("require-axe", true, "Whether trees can only be chopped by users when holding an axe.");
		threshold = option("threshold", 30, "Maximum number of blocks that can be broken at one time. 'apply-threshold' must be true for this to apply.");
		supplies(new ItemStack(Material.IRON_AXE));
	}

	private Material getSapling(TreeSpecies species) {
		switch (species) {
			case ACACIA: return Material.ACACIA_SAPLING;
			case BIRCH: return Material.BIRCH_SAPLING;
			case DARK_OAK: return Material.DARK_OAK_SAPLING;
			case GENERIC: return Material.OAK_SAPLING;
			case JUNGLE: return Material.JUNGLE_SAPLING;
			case REDWOOD: return Material.SPRUCE_SAPLING;
		}
		return Material.OAK_SAPLING;
	}
	
	private TreeSpecies getTreeSpecies(Material material) {
		for (TreeSpecies species : TreeSpecies.values()) {
			String speciesStr = species.toString();
			if (speciesStr.equalsIgnoreCase("GENERIC")) {
				speciesStr = "OAK";
			}
			else if (speciesStr.equalsIgnoreCase("REDWOOD")) {
				speciesStr = "SPRUCE";
			}
			if (material.toString().startsWith(speciesStr)) {
				return species;
			}
		}
		return null;
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onChop(BlockBreakEvent event) {
		if (event.getPlayer() != null) {
			PowerUser user = getUser(event.getPlayer());
			if (user.allowPower(this)
					&& (PowerTools.isAxe(user.getPlayer().getInventory().getItemInMainHand()) || !reqAxe)
					&& event.getBlock().getType().toString().contains("LOG")) {
				Block block = event.getBlock();
				blocks.add(block);
				TreeSpecies species = getTreeSpecies(block.getType());
				while(blocks.size() > 0) {
					for (int i = 0; i < blocks.size(); i ++) {
						Block log = blocks.get(i);
						if ((log.getType().toString().contains("LOG") || (log.getType().toString().contains("LEAVES") && doLeaves))
								&& getTreeSpecies(log.getType()) == species) {
							log.breakNaturally();
							for (BlockFace face : BlockFace.values()) {
								if (log.getRelative(face).getType().toString().contains("LOG")
										|| (log.getRelative(face).getType().toString().contains("LEAVES") && doLeaves)) {
									if (!doThreshold || blocks.size() < threshold) {
										blocks.add(log.getRelative(face));
									}
								}
								else if (face == BlockFace.DOWN
										&& plantable.contains(log.getRelative(BlockFace.DOWN).getType())
										&& replant
										&& species != null) {
									log.setType(getSapling(species));
								}
							}
						}
						blocks.remove(log);
					}
				}
			}
		}
	}

}
