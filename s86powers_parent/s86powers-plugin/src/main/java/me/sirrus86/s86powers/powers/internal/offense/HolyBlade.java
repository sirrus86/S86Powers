package me.sirrus86.s86powers.powers.internal.offense;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Holy Blade", type = PowerType.OFFENSE, author = "sirrus86", concept = "onlycoops", icon=Material.GOLDEN_SWORD,
	description = "[wItem][act:item]ing while holding [item][/wItem][wMany] or [/wMany][wSword]attacking with any sword[/wSword] creates a beam of light which shoots in the same direction. This beam deals [dmg] damage[killUndead], and will instantly kill Zombies, Skeletons and PigZombies[/killUndead]. [cooldown] cooldown.")
public class HolyBlade extends Power {

	private double dmg;
	private int range;
	private boolean breakNonSolid, killUndead, swordWear;
	@SuppressWarnings("unused")
	private boolean wMany;
	
	@Override
	protected void options() {
		breakNonSolid = option("break-non-solid-blocks", true, "Whether non-solid blocks are broken by beams.");
		cooldown = option("cooldown", PowerTime.toMillis(200), "Period of time before power can be used again.");
		dmg = option("damage", 5.0D, "Damage done by light beams.");
		item = option("item", new ItemStack(Material.GOLDEN_SWORD), "Item used to create light beams.");
		killUndead = option("instantly-kill-undead", true, "Whether light beams should instantly kill undead enemies.");
		range = option("beam-range", 12, "Range of light beams.");
		swordWear = option("wear-down-sword", true, "Whether power use should wear down the sword used.");
		wItem = option("use-specified-item", true, "Whether power can be used with the specified item.");
		wSword = option("use-any-sword", false, "Whether power can be used with any sword.");
		wMany = wItem && wSword;
		supplies(item);
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true)
	private void onUse(PowerUseEvent event) {
		if (event.getPower() == this) {
			PowerUser user = event.getUser();
			if (user.getCooldown(this) <= 0) {
				if (!swordWear
						|| !PowerTools.isSword(event.getItem())
						|| event.getItem().getDurability() < event.getItem().getType().getMaxDurability()) {
					if (swordWear
							&& PowerTools.isSword(event.getItem())) {
						event.getItem().setDurability((short) (event.getItem().getDurability() + 1));
					}
					new Beam(user, range);
					user.setCooldown(this, cooldown);
				}
			}
		}
	}
	
	private class Beam {
		
		private final Vector dir;
		private final int length;
		private Location selected;
		private int stage = 0;
		private final PowerUser user;
		
		protected Beam(PowerUser user, int length) {
			this.user = user;
			dir = user.getPlayer().getLocation().getDirection();
			this.length = length;
			selected = user.getPlayer().getEyeLocation().clone().add(dir);
			tick();
		}
		
		private void tick() {
			if (stage < length
					&& (selected.getBlock().isEmpty()
							|| (!selected.getBlock().getType().isSolid() && breakNonSolid))) {
				if (!selected.getBlock().isEmpty()) {
					BlockBreakEvent event = new BlockBreakEvent(selected.getBlock(), user.getPlayer());
					callEvent(event);
					if (!event.isCancelled()) selected.getBlock().breakNaturally();
				}
				for (LivingEntity entity : PowerTools.getNearbyEntities(LivingEntity.class, selected, 1.5D, user.getPlayer())) {
					if (killUndead
							&& (entity instanceof Skeleton || entity instanceof Zombie)) {
						user.causeDamage(getInstance(), entity, DamageCause.MAGIC, entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
					}
					else {
						user.causeDamage(getInstance(), entity, DamageCause.MAGIC, dmg);
					}
				}
				runTaskLater(new Runnable() {
					@Override
					public void run() {
						PowerTools.blockUpdate(selected.getBlock());
						selected.getWorld().playEffect(selected, Effect.STEP_SOUND, Material.GLOWSTONE);
						selected.add(dir);
						stage ++;
						tick();
					}
				}, 1L);
			}
		}
		
	}

}
