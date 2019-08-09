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

import com.google.common.collect.Sets;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Ore Detector", type = PowerType.PASSIVE, author = "sirrus86", concept = "sirrus86", icon=Material.COMPASS,
	description = "While holding [item] in either hand, nearby ore blocks within [range] blocks will become visible.")
public class OreDetector extends Power {

	private Set<Material> detectable = EnumSet.of(Material.COAL_ORE, Material.DIAMOND_ORE, Material.EMERALD_ORE, Material.GOLD_ORE,
			Material.IRON_ORE, Material.LAPIS_ORE, Material.NETHER_QUARTZ_ORE, Material.REDSTONE_ORE);
	private Map<PowerUser, Set<Block>> detectBlocks;
	
	private double range;
	
	@Override
	protected void onEnable() {
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
	protected void options() {
		cooldown = option("update-cooldown", PowerTime.toMillis(1, 0), "Minimum time needed before updating detectable blocks when moving.");
		item = option("item", new ItemStack(Material.COMPASS), "Item used to detect ores.");
		range = option("detect-range", 10.0D, "Maximum range to detect ores.");
		supplies(item);
	}
	
	private void refreshDetect(PowerUser user) {
		if (user.allowPower(this)
				&& !detectBlocks.containsKey(user)) {
			detectBlocks.put(user, new HashSet<>());
		}
		Set<Block> blockMap = detectBlocks.get(user);
		if (user.allowPower(this)
				&& user.isHoldingItem(item)) {
			Set<Block> blockCheck = PowerTools.getNearbyBlocks(user.getPlayer().getEyeLocation(), range, detectable);
			for (Block block : Sets.newConcurrentHashSet(blockMap)) {
				if (!blockCheck.contains(block)
						|| !detectable.contains(block.getType())) {
					PowerTools.removeSpectralBlock(user.getPlayer(), block);
					blockMap.remove(block);
				}
			}
			blockMap.retainAll(blockCheck);
			for (Block block : blockCheck) {
				if (!blockMap.contains(block)) {
					ChatColor color = ChatColor.BLACK;
					switch (block.getType()) {
						case COAL_ORE: color = ChatColor.DARK_GRAY; break;
						case DIAMOND_ORE: color = ChatColor.AQUA; break;
						case EMERALD_ORE: color = ChatColor.GREEN; break;
						case GOLD_ORE: color = ChatColor.YELLOW; break;
						case IRON_ORE: color = ChatColor.GOLD; break;
						case LAPIS_ORE: color = ChatColor.BLUE; break;
						case NETHER_QUARTZ_ORE: color = ChatColor.WHITE; break;
						case REDSTONE_ORE: color = ChatColor.RED; break;
						default: break;
					}
					PowerTools.addSpectralBlock(user.getPlayer(), block, color);
					blockMap.add(block);
				}
			}
		}
		else {
			for (Block block : blockMap) {
				PowerTools.removeSpectralBlock(user.getPlayer(), block);
			}
			blockMap.clear();
		}
		user.setCooldown(this, cooldown);
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onBreak(BlockBreakEvent event) {
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
		PowerUser user = getUser(event.getPlayer());
		if (user.getCooldown(this) <= 0L) {
			refreshDetect(getUser(event.getPlayer()));
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onItemSwap(PlayerSwapHandItemsEvent event) {
		refreshDetect(getUser(event.getPlayer()));
	}

}
