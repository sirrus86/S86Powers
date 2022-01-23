package me.sirrus86.s86powers.powers.internal.passive;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Ore Detector", type = PowerType.PASSIVE, author = "sirrus86", concept = "sirrus86", icon = Material.COMPASS, usesPackets = true,
	description = "While holding [item] in either hand, nearby ore blocks within [detect-range] blocks will become visible."
			+ "[use-progression] Mining detected blocks will eventually allow you to detect more rare veins.[/use-progression]")
public final class OreDetector extends Power {

	private Set<Material> detectable = EnumSet.noneOf(Material.class);
	private Map<PowerUser, Set<Block>> detectBlocks;
	
	private PowerStat copperMined, goldMined, ironMined, lapisMined, quartzMined;
	private PowerOption<Double> range;
	private PowerOption<Boolean> useProgression;
	private PowerOption<String> coalColor, copperColor, debrisColor, diamondColor, emeraldColor,
			goldColor, ironColor, lapisColor, quartzColor, redstoneColor;
	
	@Override
	protected void onEnable() {
		detectable.clear();
		for (Material material : Material.values()) {
			if (material.name().contains("_ORE")
					|| material.name().contains("ANCIENT_DEBRIS")) {
				detectable.add(material);
			}
		}
		detectBlocks = new HashMap<>();
	}
	
	@Override
	protected void onEnable(PowerUser user) {
		refreshDetect(user);
	}

	@Override
	protected void onDisable(PowerUser user) {
		if (detectBlocks.containsKey(user)) {
			for (Block block : detectBlocks.get(user)) {
				PowerTools.removeSpectralBlock(user.getPlayer(), block);
			}
			detectBlocks.remove(user);
		}
	}

	@Override
	protected void config() {
		coalColor = option("color.coal-ore", ChatColor.DARK_GRAY.toString(), "Color to highlight coal ore blocks when detected.");
		cooldown = option("update-cooldown", PowerTime.toMillis(1, 0), "Minimum time needed before updating detectable blocks when moving.");
		copperColor = option("color.copper-ore", ChatColor.DARK_AQUA.toString(), "Color to highlight copper ore blocks when detected.");
		copperMined = stat("copper-mined", 250, "Copper mined while detecting", "You can now detect gold and iron veins.");
		debrisColor = option("color.ancient-debris", ChatColor.DARK_RED.toString(), "Color to highlight ancient debris blocks when detected.");
		diamondColor = option("color.diamond-ore", ChatColor.AQUA.toString(), "Color to highlight diamond ore blocks when detected.");
		emeraldColor = option("color.emerald-ore", ChatColor.GREEN.toString(), "Color to highlight emerald ore blocks when detected.");
		goldColor = option("color.gold-ore", ChatColor.YELLOW.toString(), "Color to highlight gold ore blocks when detected.");
		goldMined = stat("gold-mined", 150, "Gold mined while detecting", "You can now detect lapis lazuli, quartz, and redstone veins.");
		ironColor = option("color.iron-ore", ChatColor.GOLD.toString(), "Color to highlight iron ore blocks when detected.");
		ironMined = stat("iron-mined", 500, "Iron mined while detecting", "You can now detect diamond veins.");
		item = option("item", new ItemStack(Material.COMPASS), "Item used to detect ores.");
		lapisColor = option("color.lapis-ore", ChatColor.BLUE.toString(), "Color to highlight lapis ore blocks when detected.");
		lapisMined = stat("lapis-mined", 300, "Lapis lazuli mined while detecting", "You can now detect emerald veins.");
		quartzColor = option("color.quartz-ore", ChatColor.WHITE.toString(), "Color to highlight quartz ore blocks when detected.");
		quartzMined = stat("quartz-mined", 500, "Quartz mined while detecting", "You can now detect ancient debris.");
		range = option("detect-range", 10.0D, "Maximum range to detect ores.");
		redstoneColor = option("color.redstone-ore", ChatColor.RED.toString(), "Color to highlight redstone ore blocks when detected.");
		useProgression = option("use-progression", true, "Whether a progression system should require users to earn the ability to detect better ores.");
		supplies(getRequiredItem());
	}
	
	private void refreshDetect(PowerUser user) {
		if (user.allowPower(this)
				&& !detectBlocks.containsKey(user)) {
			detectBlocks.put(user, new HashSet<>());
		}
		Set<Block> blockMap = detectBlocks.get(user);
		if (user.allowPower(this)
				&& user.isHoldingItem(user.getOption(item))) {
			user.setCooldown(this, user.getOption(cooldown));
			Set<Block> blockCheck = PowerTools.getNearbyBlocks(user.getPlayer().getEyeLocation(), user.getOption(range), detectable);
			for (Block block : Set.copyOf(blockMap)) {
				if (!blockCheck.contains(block)
						|| !detectable.contains(block.getType())) {
					PowerTools.removeSpectralBlock(user.getPlayer(), block);
					blockMap.remove(block);
				}
			}
			blockMap.retainAll(blockCheck);
			boolean showBlock = true;
			for (Block block : blockCheck) {
				if (!blockMap.contains(block)) {
					ChatColor color = ChatColor.BLACK;
					if (block.getType().name().contains("ANCIENT_DEBRIS")) {
						showBlock = !user.getOption(useProgression) || user.hasStatMaxed(quartzMined);
						color = ChatColor.getByChar(user.getOption(debrisColor).charAt(1));
					}
					else if (block.getType().name().contains("COAL_ORE")) {
						color = ChatColor.getByChar(user.getOption(coalColor).charAt(1));
					}
					else if (block.getType().name().contains("COPPER_ORE")) {
						color = ChatColor.getByChar(user.getOption(copperColor).charAt(1));
					}
					else if (block.getType().name().contains("DIAMOND_ORE")) {
						showBlock = !user.getOption(useProgression) || user.hasStatMaxed(ironMined);
						color = ChatColor.getByChar(user.getOption(diamondColor).charAt(1));
					}
					else if (block.getType().name().contains("EMERALD_ORE")) {
						showBlock = !user.getOption(useProgression) || user.hasStatMaxed(lapisMined);
						color = ChatColor.getByChar(user.getOption(emeraldColor).charAt(1));
					}
					else if (block.getType().name().contains("GOLD_ORE")) {
						showBlock = !user.getOption(useProgression) || user.hasStatMaxed(copperMined);
						color = ChatColor.getByChar(user.getOption(goldColor).charAt(1));
					}
					else if (block.getType().name().contains("IRON_ORE")) {
						showBlock = !user.getOption(useProgression) || user.hasStatMaxed(copperMined);
						color = ChatColor.getByChar(user.getOption(ironColor).charAt(1));
					}
					else if (block.getType().name().contains("LAPIS_ORE")) {
						showBlock = !user.getOption(useProgression) || user.hasStatMaxed(goldMined);
						color = ChatColor.getByChar(user.getOption(lapisColor).charAt(1));
					}
					else if (block.getType().name().contains("QUARTZ_ORE")) {
						showBlock = !user.getOption(useProgression) || user.hasStatMaxed(goldMined);
						color = ChatColor.getByChar(user.getOption(quartzColor).charAt(1));
					}
					else if (block.getType().name().contains("REDSTONE_ORE")) {
						showBlock = !user.getOption(useProgression) || user.hasStatMaxed(goldMined);
						color = ChatColor.getByChar(user.getOption(redstoneColor).charAt(1));
					}
					if (showBlock) {
						PowerTools.addSpectralBlock(user.getPlayer(), block, color);
						blockMap.add(block);
					}
				}
			}
		}
		else if (blockMap != null) {
			for (Block block : blockMap) {
				PowerTools.removeSpectralBlock(user.getPlayer(), block);
			}
			blockMap.clear();
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onBreak(BlockBreakEvent event) {
		for (PowerUser user : detectBlocks.keySet()) {
			if (detectBlocks.get(user).contains(event.getBlock())) {
				if (user.isOnline()
						&& user.allowPower(this)
						&& user.isHoldingItem(user.getOption(item))
						&& event.getPlayer() == user.getPlayer()
						&& detectable.contains(event.getBlock().getType())
						&& user.getOption(useProgression)) {
					if (event.getBlock().getType().name().contains("COPPER_ORE")) {
						user.increaseStat(copperMined, 1);
					}
					else if (user.hasStatMaxed(copperMined)) {
						if (event.getBlock().getType().name().contains("GOLD_ORE")) {
							user.increaseStat(goldMined, 1);
						}
						else if (event.getBlock().getType().name().contains("IRON_ORE")) {
							user.increaseStat(ironMined, 1);
						}
					}
					else if (user.hasStatMaxed(goldMined)) {
						if (event.getBlock().getType().name().contains("LAPIS_ORE")) {
							user.increaseStat(lapisMined, 1);
						}
						else if (event.getBlock().getType().name().contains("QUARTZ_ORE")) {
							user.increaseStat(quartzMined, 1);
						}
					}
				}
				runTaskLater(new Runnable() {

					@Override
					public void run() {
						refreshDetect(user);
					}
					
				}, 1L);
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onBlockDamage(BlockDamageEvent event) {
		if (event.getInstaBreak()) {
			for (PowerUser user : detectBlocks.keySet()) {
				if (detectBlocks.get(user).contains(event.getBlock())) {
					runTaskLater(new Runnable() {

						@Override
						public void run() {
							refreshDetect(user);
						}
						
					}, 1L);
				}
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onExtend(BlockPistonExtendEvent event) {
		for (PowerUser user : detectBlocks.keySet()) {
			if (!Collections.disjoint(detectBlocks.get(user), event.getBlocks())) {
				runTaskLater(new Runnable() {

					@Override
					public void run() {
						refreshDetect(user);
					}
					
				}, 1L);
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onRetract(BlockPistonRetractEvent event) {
		for (PowerUser user : detectBlocks.keySet()) {
			if (!Collections.disjoint(detectBlocks.get(user), event.getBlocks())) {
				runTaskLater(new Runnable() {

					@Override
					public void run() {
						refreshDetect(user);
					}
					
				}, 1L);
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onItemChange(PlayerItemHeldEvent event) {
		refreshDetect(getUser(event.getPlayer()));
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onMove(PlayerMoveEvent event) {
		if (event.getFrom().getWorld() != event.getTo().getWorld()
				|| event.getFrom().distanceSquared(event.getTo()) > 0.0D) {
			PowerUser user = getUser(event.getPlayer());
			if (user.getCooldown(this) <= 0L) {
				refreshDetect(getUser(event.getPlayer()));
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onItemSwap(PlayerSwapHandItemsEvent event) {
		refreshDetect(getUser(event.getPlayer()));
	}

}
