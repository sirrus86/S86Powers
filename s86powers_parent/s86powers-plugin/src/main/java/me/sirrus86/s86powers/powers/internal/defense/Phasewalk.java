package me.sirrus86.s86powers.powers.internal.defense;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Phasewalk", type = PowerType.DEFENSE, author = "sirrus86", concept = "FSCarver", icon = Material.PHANTOM_MEMBRANE, usesPackets = true,
	description = "[act:item]ing while holding [item] phases you into another reality, becoming invisible to all nearby enemies[consume-item],"
			+ " consuming the [item] in the process[/consume-item]. While phased you're immune to damage, your speed increases,"
			+ " and you can move through walls one block deep. [act:item]ing [item] again unphases you, mildly damaging nearby enemies."
			+ "[destabilize.enabled] Your Phasewalk will begin destabilizing if you don't unphase within [destabilize.duration],"
			+ " consuming held [item] every [destabilize.frequency] thereafter.[/destabilize.enabled] [cooldown] cooldown.")
public final class Phasewalk extends Power {

	private Set<PowerUser> destabilizing;
	private Map<PowerUser, Integer> tasks;
	
	private String beginDestab, phaseBack, phaseOut;
	private boolean consume, destabilize;
	private long destabFreq, destabTimer;
	private int speedDegree;
	
	@Override
	protected void onEnable() {
		destabilizing = new HashSet<>();
		tasks = new ConcurrentHashMap<>();
	}
	
	@Override
	protected void onDisable(PowerUser user) {
		if (tasks.containsKey(user)) {
			unphase(user);
		}
	}

	@Override
	protected void config() {
		consume = option("consume-item", true, "Whether item should be consumed when triggering power.");
		cooldown = option("cooldown", PowerTime.toMillis(10, 0), "Amount of time before power can be used again.");
		destabFreq = option("destabilize.frequency", PowerTime.toMillis(1, 0), "How often destabilization should consume an item.");
		destabilize = option("destabilize.enabled", true, "Whether power should destabilize over time.");
		destabTimer = option("destabilize.duration", PowerTime.toMillis(30, 0), "How long before destabilization should occur after power is first used.");
		item = option("item", new ItemStack(Material.PHANTOM_MEMBRANE, 1), "Item used to trigger phasewalking, as well as the item consumed if it destabilizes.");
		speedDegree = option("run-speed", 1, "Level of speed increase while power is active.");
		beginDestab = locale("message.begin-destabilizing", ChatColor.RED + "Phasewalk begins destabilizing.");
		phaseBack = locale("message.phase-back", ChatColor.RED + "You phase back into reality.");
		phaseOut = locale("message.phase-out", ChatColor.GREEN + "You phase out of reality...");
		supplies(new ItemStack(item.getType(), item.getMaxStackSize()));
	}
	
	private Runnable phasewalk(PowerUser user) {
		return new BukkitRunnable() {

			@Override
			public void run() {
				if (!destabilizing.contains(user)) {
					user.sendMessage(beginDestab);
					destabilizing.add(user);
				}
				if (user.getPlayer().getInventory().containsAtLeast(item, 1)) {
					user.getPlayer().getInventory().removeItem(new ItemStack[] {item});
					user.getPlayer().getWorld().spawnParticle(Particle.ITEM_CRACK, user.getPlayer().getEyeLocation().add(user.getPlayer().getLocation().getDirection()), 1, item);
					tasks.put(user, getInstance().runTaskLater(phasewalk(user), PowerTime.toTicks(destabFreq)).getTaskId());
				}
				else {
					unphase(user);
				}
			}
		
		};
	}
	
	private void unphase(PowerUser user) {
		user.sendMessage(phaseBack);
		user.removePotionEffect(PotionEffectType.NIGHT_VISION);
		user.removePotionEffect(PotionEffectType.SPEED);
		destabilizing.remove(user);
		PowerTools.removeGhost(user.getPlayer());
		if (tasks.containsKey(user)) {
			cancelTask(tasks.get(user));
		}
		tasks.remove(user);
		user.setCooldown(this, cooldown);
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onMove(PlayerMoveEvent event) {
		PowerUser user = getUser(event.getPlayer());
		if (user.allowPower(this)
				&& tasks.containsKey(user)
				&& event.getFrom().distanceSquared(event.getTo()) > 0.0D) {
			Location oldLoc = user.getPlayer().getLocation().clone();
			Vector vec = event.getTo().toVector().subtract(event.getFrom().toVector()).multiply(3);
			Block[] blocks = {
					oldLoc.getBlock().getRelative((int) vec.getX(), (int) vec.getY(), (int) vec.getZ()),
					oldLoc.getBlock().getRelative((int) vec.getX() * 2, (int) vec.getY() * 2, (int) vec.getZ() * 2)
				};
			for (int i = 0; i < blocks.length; i ++) {
				if (i == 0) {
					if (!blocks[i].getType().isSolid() && !blocks[i].getRelative(BlockFace.UP).getType().isSolid()) {
						return;
					}
				}
				else if (blocks[i].getType().isSolid() || blocks[i].getRelative(BlockFace.UP).getType().isSolid()) {
					return;
				}
			}
			Location newLoc = new Location(oldLoc.getWorld(), oldLoc.getX() + vec.getX() * 2, oldLoc.getY() + vec.getY() * 2, oldLoc.getZ() + vec.getZ() * 2, oldLoc.getYaw(), oldLoc.getPitch());
			newLoc.getWorld().spawnParticle(Particle.CRIT_MAGIC, newLoc, 5);
			user.getPlayer().playSound(newLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
			user.getPlayer().teleport(newLoc);
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onUse(PowerUseEvent event) {
		if (event.getPower() == this) {
			PowerUser user = event.getUser();
			if (user.getCooldown(this) <= 0) {
				if (!tasks.containsKey(user)) {
					user.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));
					user.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, speedDegree));
					PowerTools.addGhost(user.getPlayer());
					user.sendMessage(phaseOut);
					if (consume) {
						event.consumeItem();
					}
					if (destabilize) {
						tasks.put(user, runTaskLater(phasewalk(user), PowerTime.toTicks(destabTimer)).getTaskId());
					}
				}
				else {
					unphase(user);
				}
			}
			else {
				user.showCooldown(this);
			}
		}
	}

}
