package me.sirrus86.s86powers.powers.internal.offense;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Holy Blade", type = PowerType.OFFENSE, author = "sirrus86", concept = "onlycoops", icon = Material.GOLDEN_SWORD,
	description = "[use-specified-item][act:item]ing while holding [item][/use-specified-item][wMany] or [/wMany][use-any-sword]attacking with any sword[/use-any-sword]"
			+ " creates a beam of light which shoots in the same direction. This beam deals [damage] damage[instantly-kill-undead], and will instantly kill Zombies,"
			+ " Skeletons and PigZombies[/instantly-kill-undead]. [cooldown] cooldown.")
public final class HolyBlade extends Power {

	private PowerOption<String> beamBlock;
	private PowerOption<Double> dmg;
	private PowerOption<Integer> range;
	private PowerOption<Boolean> breakNonSolid, killUndead, swordWear;

	@SuppressWarnings({"unused", "FieldCanBeLocal"})
	private boolean wMany;
	
	@Override
	protected void config() {
		beamBlock = option("beam-block-type", "GLOWSTONE", "Block type to be used for beam effects.");
		breakNonSolid = option("break-non-solid-blocks", true, "Whether non-solid blocks are broken by beams.");
		cooldown = option("cooldown", PowerTime.toMillis(200), "Period of time before power can be used again.");
		dmg = option("damage", 5.0D, "Damage done by light beams.");
		item = option("item", new ItemStack(Material.GOLDEN_SWORD), "Item used to create light beams.");
		killUndead = option("instantly-kill-undead", true, "Whether light beams should instantly kill undead enemies.");
		range = option("beam-range", 12, "Range of light beams.");
		swordWear = option("wear-down-sword", true, "Whether power use should wear down the sword used.");
		wItem = option("use-specified-item", true, "Whether power can be used with the specified item.");
		wSword = option("use-any-sword", false, "Whether power can be used with any sword.");
		wMany = getOption(wItem) && getOption(wSword);
		supplies(getRequiredItem());
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	private void onUse(PlayerInteractEvent event) {
		PowerUser user = getUser(event.getPlayer());
		if (user.allowPower(this)
				&& event.getAction().name().startsWith("LEFT_CLICK")
				&& ((user.getOption(wSword) && event.getItem() != null && PowerTools.isSword(event.getItem()))
						|| (event.getItem() != null && event.getItem().getType() == getRequiredItem().getType()))) {
			if (user.getCooldown(this) <= 0L) {
				if ((event.getItem().getItemMeta() == null
						|| !event.getItem().getItemMeta().isUnbreakable())
							&& user.getOption(swordWear)
							&& PowerTools.hasDurability(event.getItem())
							&& event.getItem().getDurability() < event.getItem().getType().getMaxDurability()) {
					event.getItem().setDurability((short) (event.getItem().getDurability() + 1));
				}
				new Beam(user, user.getOption(range));
				user.setCooldown(this, user.getOption(cooldown));
			}
			else {
				user.showCooldown(this);
			}
		}
	}
	
	private class Beam {
		
		private final Vector dir;
		private final int length;
		private final Location selected;
		private int stage = 0;
		private final PowerUser user;
		
		protected Beam(PowerUser user, int length) {
			this.user = user;
			dir = user.getPlayer().getEyeLocation().getDirection();
			this.length = length;
			selected = user.getPlayer().getEyeLocation().clone().add(dir);
			tick();
		}
		
		private void tick() {
			if (stage < length
					&& selected.getWorld() != null
					&& (selected.getBlock().isEmpty()
							|| (!selected.getBlock().getType().isSolid() && user.getOption(breakNonSolid)))) {
				if (!selected.getBlock().isEmpty()) {
					BlockBreakEvent event = new BlockBreakEvent(selected.getBlock(), user.getPlayer());
					callEvent(event);
					if (!event.isCancelled()) {
						selected.getBlock().breakNaturally();
					}
				}
				for (LivingEntity entity : PowerTools.getNearbyEntities(LivingEntity.class, selected, 1.5D, user.getPlayer())) {
					AttributeInstance health = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
					if (user.getOption(killUndead)
							&& (entity instanceof Skeleton || entity instanceof Zombie)
							&& health != null) {
						user.causeDamage(getInstance(), entity, DamageCause.MAGIC, health.getValue() * 2.0D);
					}
					else {
						user.causeDamage(getInstance(), entity, DamageCause.MAGIC, user.getOption(dmg));
					}
				}
				runTaskLater(() -> {
					selected.getWorld().playEffect(selected, Effect.STEP_SOUND, Material.getMaterial(user.getOption(beamBlock)));
					selected.add(dir);
					stage ++;
					tick();
				}, 1L);
			}
		}
		
	}

}
