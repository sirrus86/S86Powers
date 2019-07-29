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
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Shuriken", type = PowerType.OFFENSE, author = "sirrus86", concept = "n33dy1", icon=Material.FLINT,
	description = "[act:item]ing while holding [item] throws it like a shuriken. Shuriken that hit enemies will deal [damage] damage to them.[pEither] If the victim is[pArmor] wearing armor[/pArmor][pBoth] or[/pBoth][pBlock] blocking[/pBlock] it will harmlessly bounce off.[/pEither]")
public class Shuriken extends Power {

	private Map<Snowball, PowerUser> shurikens;
	
	private double damage;
	@SuppressWarnings("unused")
	private boolean pArmor, pBlock, pBoth, pEither;
	
	@Override
	protected void onEnable() {
		shurikens = new HashMap<>();
	}

	@Override
	protected void options() {
		cooldown = option("cooldown", PowerTime.toMillis(5), "Amount of time before power can be used again.");
		damage = option("damage", 3.0D, "Amount of damage caused by shurikens.");
		item = option("item", new ItemStack(Material.FLINT), "Item to be thrown as a shuriken.");
		pArmor = option("penetrate-armor", false, "Whether shurikens should penetrate armor.");
		pBlock = option("penetrate-blocking", false, "Whether shurikens should penetrate blocking.");
		pBoth = pArmor && pBlock;
		pEither = pArmor || pBlock;
		supplies(new ItemStack(item.getType(), item.getMaxStackSize()));
	}
	
	private boolean hasArmor(LivingEntity entity) {
		EntityEquipment inv = entity.getEquipment();
		for (ItemStack item : inv.getArmorContents()) {
			if (item != null
					&& item.getType() != Material.AIR) {
				return true;
			}
		}
		return false;
	}
	
	@EventHandler
	private void onDmg(EntityDamageByEntityEvent event) {
		if (shurikens.containsKey(event.getDamager())
				&& event.getEntity() instanceof LivingEntity) {
			Snowball shuriken = (Snowball) event.getDamager();
			LivingEntity target = (LivingEntity) event.getEntity();
			if ((!pArmor && hasArmor(target))
					|| (!pBlock && target instanceof Player && ((Player) target).isBlocking())) {
				target.getWorld().playSound(shuriken.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0F, 1.0F);
				target.getWorld().dropItemNaturally(shuriken.getLocation(), item);
				event.setCancelled(true);
			}
			else {
				event.setDamage(damage);
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
				PowerTools.addDisguise(shuriken, item);
			}
		}
	}
	
	@EventHandler
	private void onHit(ProjectileHitEvent event) {
		if (shurikens.containsKey(event.getEntity())) {
			Snowball shuriken = (Snowball) event.getEntity();
			shuriken.getWorld().dropItemNaturally(shuriken.getLocation(), item);
			shurikens.remove(shuriken);
			shuriken.remove();
		}
	}

}
