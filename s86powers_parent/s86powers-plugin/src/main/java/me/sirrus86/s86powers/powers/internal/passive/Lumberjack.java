package me.sirrus86.s86powers.powers.internal.passive;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;

@PowerManifest(name = "Lumberjack", type = PowerType.PASSIVE, author = "sirrus86", concept = "sirrus86", icon = Material.IRON_AXE,
	description = "Breaking log blocks[require-axe] using an axe[/require-axe] will cause all adjacent logs[break-leaves] and leaves[/break-leaves] to also break."
			+ "[auto-replant] Trees cut down are automatically replanted.[/auto-replant]")
public final class Lumberjack extends Power {

	private List<Block> blocks;
	private final Set<Material> plantable = Set.of(Material.DIRT, Material.GRASS);
	private PowerOption<Boolean> doLeaves, doThreshold, replant, reqAxe;
	private PowerOption<Integer> threshold;
	
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
	
	@EventHandler(ignoreCancelled = true)
	private void onChop(BlockBreakEvent event) {
		PowerUser user = getUser(event.getPlayer());
		if (user.allowPower(this)
				&& (PowerTools.isAxe(user.getPlayer().getInventory().getItemInMainHand()) || !user.getOption(reqAxe))
				&& event.getBlock().getType().toString().contains("LOG")) {
			Block block = event.getBlock();
			blocks.add(block);
			TreeSpecies species = TreeSpecies.getByMaterial(block.getType());
			while(blocks.size() > 0) {
				for (int i = 0; i < blocks.size(); i ++) {
					Block log = blocks.get(i);
					if ((log.getType().toString().contains("LOG") || (log.getType().toString().contains("LEAVES") && user.getOption(doLeaves)))
							&& TreeSpecies.getByMaterial(log.getType()) == species) {
						log.breakNaturally();
						for (BlockFace face : BlockFace.values()) {
							if (log.getRelative(face).getType().toString().contains("LOG")
									|| (log.getRelative(face).getType().toString().contains("LEAVES") && user.getOption(doLeaves))) {
								if (!user.getOption(doThreshold) || blocks.size() < user.getOption(threshold)) {
									blocks.add(log.getRelative(face));
								}
							}
							else if (face == BlockFace.DOWN
									&& plantable.contains(log.getRelative(BlockFace.DOWN).getType())
									&& user.getOption(replant)
									&& species != null) {
								runTask(() -> log.setType(species.getSapling()));
							}
						}
					}
					blocks.remove(log);
				}
			}
		}
	}

	private enum TreeSpecies {
		ACACIA(Material.ACACIA_LOG, Material.ACACIA_LEAVES, Material.ACACIA_SAPLING),
		BIRCH(Material.BIRCH_LOG, Material.BIRCH_LEAVES, Material.BIRCH_SAPLING),
		DARK_OAK(Material.DARK_OAK_LOG, Material.DARK_OAK_LEAVES, Material.DARK_OAK_SAPLING),
		OAK(Material.OAK_LOG, Material.OAK_LEAVES, Material.OAK_SAPLING),
		JUNGLE(Material.JUNGLE_LOG, Material.JUNGLE_LEAVES, Material.JUNGLE_SAPLING),
		MANGROVE(Material.MANGROVE_LOG, Material.MANGROVE_LEAVES, Material.MANGROVE_PROPAGULE),
		SPRUCE(Material.SPRUCE_LOG, Material.SPRUCE_LEAVES, Material.SPRUCE_SAPLING);

		private final Material leaves;
		private final Material log;
		private final Material sapling;

		TreeSpecies(Material log, Material leaves, Material sapling) {
			this.leaves = leaves;
			this.log = log;
			this.sapling = sapling;
		}

		public static TreeSpecies getByMaterial(Material material) {
			for (TreeSpecies species : values()) {
				if (material == species.getLog()
						|| material == species.getLeaves()) {
					return species;
				}
			}
			return null;
		}

		public Material getLeaves() {
			return leaves;
		}

		public Material getLog() {
			return log;
		}

		public Material getSapling() {
			return sapling;
		}
	}

}
