package me.sirrus86.s86powers.powers.internal.offense;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Shuriken", type = PowerType.OFFENSE, author = "sirrus86", concept = "n33dy1", icon = Material.FLINT, usesPackets = true,
	description = "[act:item]ing while holding [item] throws it like a shuriken. Shuriken that hit enemies will deal [damage] damage to them."
			+ "[pEither] If the victim is[penetrate-armor] wearing armor[/penetrate-armor][pBoth] or[/pBoth][penetrate-blocking] blocking[/penetrate-blocking] it will harmlessly bounce off.[/pEither]")
public final class Shuriken extends Power {

	private Map<Snowball, PowerUser> shurikens;
	
	private PowerOption<Double> damage;
	private PowerOption<Boolean> drop, pArmor, pBlock;

	@SuppressWarnings({"unused", "FieldCanBeLocal"})
	private boolean pBoth, pEither;
	
	@Override
	protected void onEnable() {
		shurikens = new HashMap<>();
	}

	@Override
	protected void config() {
		cooldown = option("cooldown", PowerTime.toMillis(5), "Amount of time before power can be used again.");
		damage = option("damage", 3.0D, "Amount of damage caused by shurikens.");
		drop = option("drop-after-impact", true, "Whether thrown shuriken should become an item drop after hitting something.");
		item = option("item", new ItemStack(Material.FLINT), "Item to be thrown as a shuriken.");
		pArmor = option("penetrate-armor", false, "Whether shurikens should penetrate armor.");
		pBlock = option("penetrate-blocking", false, "Whether shurikens should penetrate blocking.");
		pBoth = getOption(pArmor) && getOption(pBlock);
		pEither = getOption(pArmor) || getOption(pBlock);
		supplies(new ItemStack(getRequiredItem().getType(), getRequiredItem().getMaxStackSize()));
	}
	
	private boolean hasArmor(LivingEntity entity) {
		EntityEquipment inv = entity.getEquipment();
		if (inv != null) {
			for (ItemStack item : inv.getArmorContents()) {
				if (item != null
						&& item.getType() != Material.AIR) {
					return true;
				}
			}
		}
		return false;
	}
	
	@EventHandler
	private void onDmg(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Snowball shuriken
				&& shurikens.containsKey(shuriken)
				&& event.getEntity() instanceof LivingEntity target) {
			PowerUser user = shurikens.get(shuriken);
			if ((!user.getOption(pArmor) && hasArmor(target))
					|| (!user.getOption(pBlock) && target instanceof Player && ((Player) target).isBlocking())) {
				target.getWorld().playSound(shuriken.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0F, 1.0F);
				if (user.getOption(drop)) {
					shuriken.getWorld().dropItemNaturally(shuriken.getLocation(), getRequiredItem());
				}
				event.setCancelled(true);
			}
			else {
				event.setDamage(user.getOption(damage));
			}
			shurikens.remove(shuriken);
			shuriken.remove();
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onUse(PowerUseEvent event) {
		if (event.getPower() == this) {
			PowerUser user = event.getUser();
			if (user.allowPower(this)
					&& user.getCooldown(this) <= 0) {
				event.consumeItem();
				Snowball shuriken = user.getPlayer().launchProjectile(Snowball.class);
				shuriken.setVelocity(user.getPlayer().getEyeLocation().getDirection().clone().normalize().multiply(3));
				shurikens.put(shuriken, user);
				PowerTools.addDisguise(shuriken, user.getOption(item));
			}
		}
	}
	
	@EventHandler
	private void onHit(ProjectileHitEvent event) {
		if (event.getEntity() instanceof Snowball shuriken
				&& shurikens.containsKey(shuriken)) {
			if (event.getHitEntity() == null
					&& shurikens.get(shuriken).getOption(drop)) {
				shuriken.getWorld().dropItemNaturally(shuriken.getLocation(), shurikens.get(shuriken).getOption(item));
				shurikens.remove(shuriken);
				shuriken.remove();
			}
		}
	}

}
