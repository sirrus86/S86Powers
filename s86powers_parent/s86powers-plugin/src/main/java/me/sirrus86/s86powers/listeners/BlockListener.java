package me.sirrus86.s86powers.listeners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.config.ConfigOption;
import me.sirrus86.s86powers.events.PowerDamageEvent;
import me.sirrus86.s86powers.events.PowerIgniteEvent;
import me.sirrus86.s86powers.powers.PowerFire;
import me.sirrus86.s86powers.tools.PowerTools;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Fire;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class BlockListener implements Listener {

	private Map<Block, PowerFire> bList = new HashMap<>();
	private Map<Entity, PowerFire> burnList = new HashMap<>();
	private Set<Explosive> eList = new HashSet<>();
	
	private final Fire fireData = (Fire) Material.FIRE.createBlockData();
	
	private final S86Powers plugin;
	
	public BlockListener(S86Powers plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		plugin.getServer().getScheduler().runTaskTimer(plugin, manage, 0L, 5L);
	}
	
	private Runnable manage = new BukkitRunnable() {
		@Override
		public void run() {
			for (Entity entity : burnList.keySet()) {
				if (entity.getFireTicks() <= 0) {
					burnList.remove(entity);
				}
			}
		}
	};
	
	public void addExplosive(Explosive exp) {
		eList.add(exp);
	}
	
	public void addIgnite(Entity entity, PowerFire fire) {
		burnList.put(entity, fire);
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onBreak(BlockBreakEvent event) {
		if (PowerTools.hasDisguise(event.getBlock())) {
			PowerTools.blockUpdate(event.getBlock());
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onBurn(BlockBurnEvent event) {
		for (BlockFace face : BlockFace.values()) {
			if (bList.containsKey(event.getBlock().getRelative(face))
					&& ConfigOption.Powers.PREVENT_GRIEFING) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onFade(BlockFadeEvent event) {
		if (bList.containsKey(event.getBlock())
				&& event.getNewState().getType() != Material.FIRE) {
			bList.remove(event.getBlock());
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onIgnite(BlockIgniteEvent event) {
		try {
			if (bList.containsKey(event.getIgnitingBlock())
					&& ConfigOption.Powers.PREVENT_GRIEFING) {
				event.setCancelled(true);
			}
		} catch (Exception e) {
			for (BlockFace face : BlockFace.values()) {
				if (bList.containsKey(event.getBlock().getRelative(face))
						&& ConfigOption.Powers.PREVENT_GRIEFING) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onSpread(BlockSpreadEvent event) {
		if (bList.containsKey(event.getBlock())
				&& ConfigOption.Powers.PREVENT_GRIEFING) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onCombust(EntityCombustEvent event) {
		for (Block block : PowerTools.getNearbyBlocks(event.getEntity().getLocation(), 1.5D)) {
			if (block.getType() == Material.FIRE
					&& bList.containsKey(block)) {
				burnList.put(event.getEntity(), bList.get(block));
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onDamage(EntityDamageEvent event) {
		if (burnList.containsKey(event.getEntity())
				&& event.getCause().name().startsWith("FIRE")) {
			PowerFire pFire = burnList.get(event.getEntity());
			PowerDamageEvent pEvent = new PowerDamageEvent(pFire.getPower(), pFire.getUser(), event, Double.MAX_VALUE);
			plugin.getServer().getPluginManager().callEvent(pEvent);
		}
	}
	
	@EventHandler
	private void onDeath(EntityDeathEvent event) {
		if (burnList.containsKey(event.getEntity())) {
			burnList.remove(event.getEntity());
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onExplode(EntityExplodeEvent event) {
		if (eList.contains(event.getEntity())) {
			if (ConfigOption.Powers.PREVENT_GRIEFING) {
				event.blockList().clear();
			}
			eList.remove(event.getEntity());
		}
	}
	
	@EventHandler
	private void onIgnite(PowerIgniteEvent event) {
		if (event.getBlock() != null
				&& event.getBlock().getType() == Material.AIR) {
			Block block = event.getBlock();
			BlockPlaceEvent bEvent = new BlockPlaceEvent(block, block.getState(),
					event.getBlockFace() != null ? block.getRelative(event.getBlockFace().getOppositeFace()) : block,
					new ItemStack(Material.FIRE), event.getUser().getPlayer(), true, EquipmentSlot.HAND);
			plugin.getServer().getPluginManager().callEvent(bEvent);
			if (!bEvent.isCancelled()) {
				block.setType(Material.FIRE);
				for (BlockFace face : fireData.getAllowedFaces()) {
					fireData.setFace(face, false);
				}
				if (fireData.getAllowedFaces().contains(event.getBlockFace().getOppositeFace())) {
					fireData.setFace(event.getBlockFace().getOppositeFace(), true);
				}
				if (!fireData.getFaces().isEmpty()) {
					block.setBlockData(fireData);
				}
				bList.put(block, new PowerFire(event.getPower(), event.getUser()));
			}
		}
		else if (event.getEntity() != null) {
			burnList.put(event.getEntity(), new PowerFire(event.getPower(), event.getUser()));
		}
	}
	
}
