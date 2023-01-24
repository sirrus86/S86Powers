package me.sirrus86.s86powers.powers.internal.defense;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Grappling Hook", type = PowerType.DEFENSE, author = "sirrus86", concept = "sirrus86", icon = Material.FISHING_ROD,
	description = "When used on land, the fishing rod works as a grappling hook. Use once to establish a destination, reel in to pull yourself to that location."
			+ " User has [fall-resist] of fall protection after use.[pull-hooked-entity] Can also be used to reel in entities.[/pull-hooked-entity]")
public final class GrapplingHook extends Power {

	private Map<Hook, Integer> tasks;
	
	private PowerOption<List<String>> canPull, canRide, pulledTo;
	private PowerOption<Double> hookVel, minDist, reelVel;
	private PowerOption<Boolean> pullHooked;
	private PowerOption<Long> pullTime;
	private PowerStat travels;
	
	@Override
	protected void onEnable() {
		tasks = new HashMap<>();
	}

	@Override
	protected void config() {
		canPull = option("can-pull", List.of("AXOLOTL", "BAT", "BEE", "BLAZE", "CAT", "CAVE_SPIDER", "CHICKEN", "COW", "CREEPER", "DROWNED",
				"ENDERMAN", "ENDERMITE", "EVOKER", "FOX", "GOAT", "GUARDIAN", "HUSK", "ILLUSIONER", "MAGMA_CUBE", "MUSHROOM_COW", "OCELOT",	"PARROT",
				"PHANTOM", "PIG", "PIGLIN", "PIGLIN_BRUTE", "PIGZOMBIE", "PILLAGER", "RABBIT", "SHEEP", "SILVERFISH", "SKELETON", "SLIME", "SNOWMAN",
				"SPIDER", "STRAY", "TURTLE", "VEX", "VILLAGER", "VINDICATOR", "WANDERING_TRADER", "WITCH", "WITHER_SKELETON", "WOLF", "ZOMBIE",
				"ZOMBIE_VILLAGER", "ZOMBIFIED_PIGLIN"), "Entities that the user can pull to themselves."
				+ " Refer to https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html for a list of applicable values.");
		canRide = option("can-ride", List.of("BOAT", "DONKEY", "ENDER_DRAGON", "GHAST", "HOGLIN", "HORSE", "LLAMA", "MINECART", "MULE", "PANDA",
				"POLAR_BEAR", "RAVAGER", "SKELETON_HORSE", "STRIDER", "TRADER_LLAMA", "WITHER", "ZOGLIN", "ZOMBIE_HORSE"), "Entities that the user can ride. Entity must also be on the 'pulled-to' list."
				+ " Refer to https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html for a list of applicable values.");
		cooldown = option("fall-resist", PowerTime.toMillis(3, 0), "How long after using power that the user is immune to fall damage.");
		hookVel = option("hook-velocity", 2.0D, "Velocity at which hooks are cast by the user.");
		minDist = option("minimum-distance-from-target", 2.0D, "Minimum distance entity can be from target location before hook disengages.");
		pulledTo = option("pulled-to", List.of("BOAT", "DONKEY", "ELDER_GUARDIAN", "ENDER_DRAGON", "GHAST", "GIANT", "HOGLIN", "HORSE", "IRON_GOLEM",
				"LLAMA", "MINECART", "MULE", "PANDA", "POLAR_BEAR", "RAVAGER", "SHULKER", "SKELETON_HORSE", "STRIDER", "TRADER_LLAMA", "WITHER", "ZOGLIN",
				"ZOMBIE_HORSE"), "Entities that the user will be pulled to when hooked."
				+ " Refer to https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html for a list of applicable values.");
		pullHooked = option("pull-hooked-entity", true, "Whether hooked entities should be pulled to the user.");
		pullTime = option("maximum-pull-duration", PowerTime.toMillis(3, 0), "Maximum amount of time hook should pull entity before letting go.");
		reelVel = option("reel-velocity", 1.0D, "Speed at which an entity is reeled to their destination.");
		travels = stat("times-traveled", 50, "Times traveled via grappling hook", "Can now mount certain entities that are hooked.");
		supplies(new ItemStack(Material.FISHING_ROD));
	}
	
	private Runnable doReel(final Hook hook, final Entity entity, final Location loc, final Entity mount) {
		return new BukkitRunnable() {
			int i = 0;
			@Override
			public void run() {
				if (entity.isValid()
						&& !entity.isDead()
						&& entity.getLocation().getWorld() == loc.getWorld()
						&& entity.getLocation().distanceSquared(loc) > getOption(minDist) * getOption(minDist)
						&& i < PowerTime.toTicks(getOption(pullTime))) {
					double dist = entity.getLocation().distance(loc);
					Vector incr = loc.clone().subtract(entity.getLocation()).toVector().multiply(getOption(reelVel) / dist);
					entity.setVelocity(incr);
					i ++;
				}
				else {
					if (mount != null) {
						mount.addPassenger(entity);
					}
					cancelTask(tasks.get(hook));
				}
			}
		};
	}
	
	@EventHandler
	private void onDmg(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player
				&& event.getCause() == DamageCause.FALL) {
			PowerUser user = getUser((Player) event.getEntity());
			if (user.allowPower(this)
					&& user.getCooldown(this) > 0) {
				event.setCancelled(true);
				user.setCooldown(this, 0L);
			}
		}
	}
	
	@EventHandler
	private void onFish(PlayerFishEvent event) {
		PowerUser user = getUser(event.getPlayer());
		if (user.allowPower(this)) {
			Hook hook = null;
			if (event.getState() == State.CAUGHT_ENTITY
					&& event.getCaught() != null
					&& event.getCaught() != event.getPlayer()
					&& user.getOption(pullHooked)) {
				hook = new Hook(event.getCaught());
			}
			else if (event.getState() == State.IN_GROUND) {
				hook = new Hook(event.getHook().getLocation());
				user.setCooldown(this, user.getOption(cooldown));
			}
			if (hook != null) {
				user.increaseStat(travels, 1);
				hook.reel(user);
			}
		}
	}
	
	@EventHandler
	private void onLaunch(ProjectileLaunchEvent event) {
		if (event.getEntity() instanceof FishHook hook
				&& hook.getShooter() instanceof Player) {
			PowerUser user = getUser((Player) hook.getShooter());
			if (user.allowPower(this)) {
				hook.setVelocity(hook.getVelocity().multiply(user.getOption(hookVel)));
				hook.setBounce(false);
			}
		}
	}
	
	private class Hook {
		
		private final Entity entity;
		private final Location loc;
		
		public Hook(Location loc) {
			this.entity = null;
			this.loc = loc;
		}
		
		public Hook(Entity entity) {
			this.entity = entity;
			this.loc = null;
		}
		
		public void reel(PowerUser user) {
			if (entity != null) {
				if (user.getOption(canRide).contains(entity.getType().name())
						|| user.getOption(pulledTo).contains(entity.getType().name())) {
					tasks.put(this, runTaskTimer(doReel(this, user.getPlayer(), entity.getLocation(), user.hasStatMaxed(travels) && user.getOption(canRide).contains(entity.getType().name()) ? entity
							: null), 0L, 1L).getTaskId());
				}
				else if (user.getOption(canPull).contains(entity.getType().name())) {
					tasks.put(this, runTaskTimer(doReel(this, entity, user.getPlayer().getEyeLocation(), null), 0L, 1L).getTaskId());
				}
			}
			else if (loc != null) {
				tasks.put(this, runTaskTimer(doReel(this, user.getPlayer(), loc, null), 0L, 1L).getTaskId());
			}
		}
		
	}

}
