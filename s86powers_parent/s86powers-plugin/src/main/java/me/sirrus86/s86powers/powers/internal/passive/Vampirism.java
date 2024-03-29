package me.sirrus86.s86powers.powers.internal.passive;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.sirrus86.s86powers.events.UserMaxedStatEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.tools.version.MCMetadata;
import me.sirrus86.s86powers.tools.version.MCMetadata.EntityMeta;
import me.sirrus86.s86powers.users.PowerUser;

@PowerManifest(name = "Vampirism", type = PowerType.PASSIVE, author = "sirrus86", concept = "TheClownOfCrime", icon = Material.FERMENTED_SPIDER_EYE, usesPackets = true,
	description = "You rapidly lose hunger while in direct sunlight, eventually igniting you[helmet-prevents-ignition] unless you are wearing a helmet[/helmet-prevents-ignition]."
			+ " While sprinting, running speed and jump height increase. Immune to fall damage. Wooden tools do [wood-damage-multiplier]x more damage to you.")
public final class Vampirism extends Power {

	private Set<PowerUser> sprinting, transformed;
	
	private final MCMetadata flyingMeta = new MCMetadata();
	
	private PowerOption<Boolean> helmProt, infect;
	private PowerOption<Integer> food, jmp, spd;
	private PowerOption<Double> infectChance, wMult;
	private PowerStat kills;
	private String infected, turnToBat, turnToHuman;
	
	@Override
	protected void onEnable() {
		flyingMeta.setEntry(EntityMeta.BAT_IS_HANGING, (byte) 0x00);
		sprinting = new HashSet<>();
		transformed = new HashSet<>();
		runTaskTimer(manage, 0L, 5L);
	}
	
	@Override
	protected void onEnable(PowerUser user) {
		if (user.hasStatMaxed(kills)) {
			user.setAllowFlight(true);
		}
	}

	@Override
	protected void onDisable(PowerUser user) {
		user.setAllowFlight(false);
		if (sprinting.contains(user)) {
			user.removePotionEffect(PotionEffectType.JUMP);
			user.removePotionEffect(PotionEffectType.SPEED);
			sprinting.remove(user);
		}
		if (transformed.contains(user)) {
			transform(user, false);
		}
	}

	@Override
	protected void config() {
		food = option("food-regen", 7, "Amount of food regeneration granted by killing other entities.");
		helmProt = option("helmet-prevents-ignition", false, "Whether wearing a helmet will prevent vampires from igniting in sunlight.");
		infect = option("infect-other-players", false, "Whether attacks from vampires should infect players without the power.");
		infectChance = option("infect-chance", 15.0D, "Percent chance that other players will be infected.");
		jmp = option("jump-degree", 1, "Jump effect to apply to user while sprinting.");
		kills = stat("vampire-kills", 100, "Kills as a vampire", "Able to transform into a bat and fly. Exposure to sunlight will end the transformation.");
		spd = option("speed-degree", 3, "Speed effect to apply to user while sprinting.");
		wMult = option("wood-damage-multiplier", 3.0D, "Amount to multiply damage by when caused by wooden items.");
		infected = locale("message.been-infected", ChatColor.RED + "You've been infected with [power]!");
		turnToHuman = locale("message.turn-to-human", ChatColor.YELLOW + "You return to human form.");
		turnToBat = locale("message.turn-to-bat", ChatColor.GREEN + "You transform into a bat.");
	}
	
	private final BukkitRunnable manage = new BukkitRunnable() {
		@Override
		public void run() {
			for (PowerUser user : getInstance().getUsers()) {
				if (user.isOnline()
						&& user.allowPower(getInstance())) {
					if (PowerTools.inSunlight(user.getPlayer().getEyeLocation())) {
						if (transformed.contains(user)) {
							transform(user, false);
						}
						if (user.getPlayer().getFoodLevel() > 0) {
							user.getPlayer().setFoodLevel(user.getPlayer().getFoodLevel() - 1);
							user.getPlayer().getWorld().playEffect(user.getPlayer().getEyeLocation(), Effect.SMOKE, BlockFace.UP);
						} else if (!user.getOption(helmProt)
								|| user.getPlayer().getInventory().getHelmet() == null
								|| user.getPlayer().getInventory().getHelmet().getType() == Material.AIR) {
							user.getPlayer().setFireTicks(20);
						}
					} else if (user.hasStatMaxed(kills)
							&& user.getCooldown(getInstance()) < 0L) {
						user.setAllowFlight(true);
					}
				}
			}
		}
	};
	
	private void transform(PowerUser user, boolean transform) {
		user.getPlayer().playEffect(EntityEffect.ENTITY_POOF);
		if (transform) {
			PowerTools.addDisguise(user.getPlayer(), EntityType.BAT, flyingMeta);
			transformed.add(user);
			user.sendMessage(turnToBat);
		}
		else {
			PowerTools.removeDisguise(user.getPlayer());
			transformed.remove(user);
			user.getPlayer().setFlying(false);
			user.sendMessage(turnToHuman);
		}
	}
	
	@EventHandler
	private void noInteract(PlayerInteractEvent event) {
		PowerUser user = getUser(event.getPlayer());
		if (user.allowPower(this)
				&& transformed.contains(user)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onDmg(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			PowerUser user = getUser((Player) event.getEntity());
			if (user.allowPower(this)
					&& event.getCause() == DamageCause.FALL) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onDmg(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {
			PowerUser user = getUser((Player) event.getDamager());
			if (transformed.contains(user)) {
				event.setCancelled(true);
			}
		}
		if (event.getEntity() instanceof Player
				&& event.getDamager() instanceof LivingEntity attacker) {
			PowerUser user = getUser((Player) event.getEntity());
			if (user.allowPower(this)) {
				ItemStack used = null;
				if (attacker.getEquipment() != null) {
					used = attacker.getEquipment().getItemInMainHand();
				}
				if (used != null
						&& used.getType().toString().startsWith("WOOD")) {
					event.setDamage(event.getDamage() * user.getOption(wMult));
				}
			}
			else if (attacker instanceof Player
					&& user.getOption(infect)) {
				PowerUser pAttacker = getUser((Player) attacker);
				if (pAttacker.allowPower(this)
						&& !user.hasPower(this)
						&& !user.hasPower("Lycanthropy")
						&& random.nextDouble() < (user.getOption(infectChance) / 100.0D)) {
					user.sendMessage(infected.replace("[power]", this.getName()));
					user.addPower(this, true);
				}
			}
		}
	}
	
	@EventHandler
	private void onDeath(EntityDeathEvent event) {
		LivingEntity entity = event.getEntity();
		if (entity.getKiller() != null) {
			PowerUser user = getUser(entity.getKiller());
			if (user.allowPower(this)) {
				user.increaseStat(kills, 1);
				user.regenHunger(user.getOption(food));
			}
		}
		if (entity instanceof Player) {
			PowerUser user = getUser((Player) entity);
			if (user.allowPower(this)) {
				onDisable(user);
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onConsume(PlayerItemConsumeEvent event) {
		PowerUser user = getUser(event.getPlayer());
		if (user.allowPower(this)
				&& event.getItem().getType().isEdible()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onFly(PlayerToggleFlightEvent event) {
		PowerUser user = getUser(event.getPlayer());
		if (user.allowPower(this)
				&& user.hasStatMaxed(kills)) {
			transform(user, !transformed.contains(user));
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onSprint(PlayerToggleSprintEvent event) {
		PowerUser user = getUser(event.getPlayer());
		if (user.allowPower(this)) {
			if (event.isSprinting()) {
				sprinting.add(user);
				user.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, user.getOption(jmp)));
				user.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, user.getOption(spd)));
			}
			else if (sprinting.contains(user)) {
				sprinting.remove(user);
				user.removePotionEffect(PotionEffectType.JUMP);
				user.removePotionEffect(PotionEffectType.SPEED);
			}
		}
	}
	
	@EventHandler
	private void onStatMax(UserMaxedStatEvent event) {
		PowerUser user = event.getUser();
		if (event.getStat() == kills) {
			user.setAllowFlight(true);
		}
	}

}
