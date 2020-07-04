package me.sirrus86.s86powers.powers.internal.defense;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.tools.version.MCVersion;
import me.sirrus86.s86powers.users.PowerUser;

@PowerManifest(name = "Mob Catcher", type = PowerType.DEFENSE, author = "sirrus86", concept = "kamyarm007", version = MCVersion.v1_14, icon=Material.ZOMBIE_SPAWN_EGG, usesPackets = true,
	description = "[act:item]ing while holding [item] throws it. If it hits a capturable entity, it will be stored in the [item] which is returned to you. [act:item]ing a [item] with a stored entity will thorw it, causing it to expel the stored entity on contact.")
public final class MobCatcher extends Power {

	private final Set<EntityType> capturable = EnumSet.of(EntityType.BAT, EntityType.BLAZE, EntityType.CAT, EntityType.CAVE_SPIDER, EntityType.CHICKEN,
			EntityType.COW, EntityType.CREEPER, EntityType.DOLPHIN, EntityType.DROWNED, EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.FOX,
			EntityType.GHAST, EntityType.GUARDIAN, EntityType.HUSK, EntityType.MAGMA_CUBE, EntityType.MUSHROOM_COW, EntityType.OCELOT, EntityType.PANDA,
			EntityType.PARROT, EntityType.PHANTOM, EntityType.PIG, PowerTools.resolveEntityType("PIGZOMBIE"), EntityType.POLAR_BEAR, EntityType.RABBIT, EntityType.SHEEP,
			EntityType.SHULKER, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME, EntityType.SPIDER, EntityType.SQUID, EntityType.STRAY,
			EntityType.TURTLE, EntityType.WITCH, EntityType.WITHER_SKELETON, EntityType.WOLF, EntityType.ZOMBIE);
	
	private final NamespacedKey ageableAge = createNamespacedKey("ageable-age"),
			catType = createNamespacedKey("cat-type"),
			collarColor = createNamespacedKey("collar-color"),
			creeperPowered = createNamespacedKey("creeper-powered"),
			customName = createNamespacedKey("custom-name"),
			entityHealth = createNamespacedKey("entity-health"),
			entityType = createNamespacedKey("entity-type"),
			foxType = createNamespacedKey("fox-type"),
			mooshroomVariant = createNamespacedKey("mooshroom-variant"),
			ownerUUID = createNamespacedKey("owner-uuid"),
			pandaHiddenGene = createNamespacedKey("panda-hidden-gene"),
			pandaMainGene = createNamespacedKey("panda-main-gene"),
			parrotVariant = createNamespacedKey("parrot-variant"),
			pigSaddle = createNamespacedKey("pig-saddle"),
			rabbitType = createNamespacedKey("rabbit-type"),
			sheepColor = createNamespacedKey("sheep-color"),
			sheepSheared = createNamespacedKey("sheep-sheared"),
			slimeSize = createNamespacedKey("slime-size"),
			zombieBaby = createNamespacedKey("zombie-baby");
	
	private Set<EntityType> allowCapture;
	private Map<Snowball, ItemStack> eggs;
	private Map<Snowball, PowerUser> eggOwners;
	private Map<Item, PowerUser> eggsOnGround;
	
	private String cantCapTamed;
	private boolean captureTamed;
	private PowerStat eggsThrown;
	
	@Override
	protected void onEnable() {
		allowCapture = EnumSet.noneOf(EntityType.class);
		eggOwners = new HashMap<>();
		eggs = new HashMap<>();
		eggsOnGround = new HashMap<>();
	}

	@Override
	protected void config() {
		captureTamed = option("capture-tamed", false, "Whether entities tamed by other players (wolves, cats, etc) can be captured.");
		eggsThrown = stat("eggs-thrown", 50, "Entities relocated", "[item] is now refunded after expelling a stored entity.");
		item = option("item", new ItemStack(Material.ENDER_EYE, 1), "Item used to catch and store mobs.");
		for (EntityType eType : capturable) {
			if (option("capturable." + eType.toString().replaceAll("_", "-").toLowerCase(), true, "Whether " + WordUtils.capitalizeFully(eType.toString().replaceAll("_", " ")) + " should be capturable.")) {
				allowCapture.add(eType);
			}
		}
		cantCapTamed = locale("message.cant-capture-tamed", ChatColor.RED + "You can't capture entities tamed by other players.");
		supplies(new ItemStack(item.getType(), item.getMaxStackSize()));
	}
	
	private ItemStack createEgg(LivingEntity entity) {
		ItemStack egg = new ItemStack(item.getType(), 1);
		ItemMeta meta = egg.hasItemMeta() ? item.getItemMeta() : entity.getServer().getItemFactory().getItemMeta(item.getType());
		List<String> stats = new ArrayList<String>();
		meta.getPersistentDataContainer().set(entityType, PersistentDataType.STRING, entity.getType().toString());
		meta.getPersistentDataContainer().set(entityHealth, PersistentDataType.DOUBLE, entity.getHealth());
		stats.add("Health: " + entity.getHealth() + "/" + entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
		if (entity instanceof Ageable) {
			if (!((Ageable)entity).isAdult()) {
				stats.add("Juvenile");
			}
			meta.getPersistentDataContainer().set(ageableAge, PersistentDataType.INTEGER, ((Ageable) entity).getAge());
		}
		if (entity instanceof Cat) {
			stats.add("Type: " + WordUtils.capitalize(((Cat)entity).getCatType().toString().replace("_", " ").toLowerCase()));
			meta.getPersistentDataContainer().set(catType, PersistentDataType.STRING, ((Cat)entity).getCatType().toString());
			meta.getPersistentDataContainer().set(collarColor, PersistentDataType.STRING, ((Cat)entity).getCollarColor().toString());
		}
		if (entity instanceof Creeper) {
			if (((Creeper)entity).isPowered()) {
				stats.add("Powered");
			}
			meta.getPersistentDataContainer().set(creeperPowered, PersistentDataType.BYTE, ((Creeper)entity).isPowered() ? (byte) 1 : (byte) 0);
		}
		if (entity instanceof Fox) {
			stats.add("Type: " + WordUtils.capitalize(((Fox)entity).getFoxType().toString().replace("_", " ").toLowerCase()));
			meta.getPersistentDataContainer().set(foxType, PersistentDataType.STRING, ((Fox)entity).getFoxType().toString());
		}
		if (entity instanceof MushroomCow) {
			stats.add("Color: " + WordUtils.capitalize(((MushroomCow)entity).getVariant().toString().replace("_", " ").toLowerCase()));
			meta.getPersistentDataContainer().set(foxType, PersistentDataType.STRING, ((MushroomCow)entity).getVariant().toString());
		}
		if (entity instanceof Panda) {
			meta.getPersistentDataContainer().set(pandaHiddenGene, PersistentDataType.STRING, ((Panda)entity).getHiddenGene().toString());
			meta.getPersistentDataContainer().set(pandaMainGene, PersistentDataType.STRING, ((Panda)entity).getMainGene().toString());
		}
		if (entity instanceof Parrot) {
			stats.add("Color: " + WordUtils.capitalize(((Parrot)entity).getVariant().toString().replace("_", " ").toLowerCase()));
			meta.getPersistentDataContainer().set(parrotVariant, PersistentDataType.STRING, ((Parrot)entity).getVariant().toString());
		}
		if (entity instanceof Pig) {
			if (((Pig)entity).hasSaddle()) {
				stats.add("Saddled");
			}
			meta.getPersistentDataContainer().set(pigSaddle, PersistentDataType.BYTE, ((Pig)entity).hasSaddle() ? (byte) 1 : (byte) 0);
		}
		if (entity instanceof Rabbit) {
			stats.add("Type: " + WordUtils.capitalize(((Rabbit)entity).getRabbitType().toString().replace("_", " ").toLowerCase()));
			meta.getPersistentDataContainer().set(rabbitType, PersistentDataType.STRING, ((Rabbit)entity).getRabbitType().toString());
		}
		if (entity instanceof Sheep) {
			stats.add("Color: " + WordUtils.capitalize(((Sheep)entity).getColor().toString().replace("_", " ").toLowerCase()));
			meta.getPersistentDataContainer().set(sheepColor, PersistentDataType.STRING, ((Sheep)entity).getColor().toString());
			meta.getPersistentDataContainer().set(sheepSheared, PersistentDataType.BYTE, ((Sheep)entity).isSheared() ? (byte) 1 : (byte) 0);
		}
		if (entity instanceof Slime) {
			stats.add("Size: " + ((Slime)entity).getSize());
			meta.getPersistentDataContainer().set(slimeSize, PersistentDataType.INTEGER, ((Slime)entity).getSize());
		}
		if (entity instanceof Tameable) {
			if (((Tameable)entity).isTamed()) {
				meta.getPersistentDataContainer().set(ownerUUID, PersistentDataType.STRING, ((Tameable)entity).getOwner().getUniqueId().toString());
			}
		}
		if (entity instanceof Wolf) {
			meta.getPersistentDataContainer().set(collarColor, PersistentDataType.STRING, ((Wolf)entity).getCollarColor().toString());
		}
		if (entity instanceof Zombie) {
			if (((Zombie)entity).isBaby()) {
				stats.add("Juvenile");
			}
			meta.getPersistentDataContainer().set(zombieBaby, PersistentDataType.BYTE, ((Zombie)entity).isBaby() ? (byte) 1 : (byte) 0);
		}
		meta.setDisplayName(WordUtils.capitalize(entity.getType().name().replaceAll("_", " ").toLowerCase()));
		if (entity.getCustomName() != null) {
			meta.setDisplayName(entity.getCustomName());
			meta.getPersistentDataContainer().set(customName, PersistentDataType.STRING, entity.getCustomName());
		}
		meta.setLore(stats);
		egg.setItemMeta(meta);
		return egg;
	}
	
	private LivingEntity createEntity(ItemStack egg, Location loc) {
		ItemMeta meta = egg.getItemMeta();
		LivingEntity entity = (LivingEntity) loc.getWorld().spawnEntity(loc, EntityType.valueOf(meta.getPersistentDataContainer().get(entityType, PersistentDataType.STRING)));
		if (entity instanceof Ageable) {
			((Ageable)entity).setAge(meta.getPersistentDataContainer().get(ageableAge, PersistentDataType.INTEGER));
		}
		if (entity instanceof Cat) {
			((Cat)entity).setCatType(Cat.Type.valueOf(meta.getPersistentDataContainer().get(catType, PersistentDataType.STRING)));
			((Cat)entity).setCollarColor(DyeColor.valueOf(meta.getPersistentDataContainer().get(collarColor, PersistentDataType.STRING)));
		}
		if (entity instanceof Creeper) {
			((Creeper)entity).setPowered(meta.getPersistentDataContainer().get(creeperPowered, PersistentDataType.BYTE) == 1);
		}
		if (entity instanceof Fox) {
			((Fox)entity).setFoxType(Fox.Type.valueOf(meta.getPersistentDataContainer().get(foxType, PersistentDataType.STRING)));
		}
		if (entity instanceof MushroomCow) {
			((MushroomCow)entity).setVariant(MushroomCow.Variant.valueOf(meta.getPersistentDataContainer().get(mooshroomVariant, PersistentDataType.STRING)));
		}
		if (entity instanceof Panda) {
			((Panda)entity).setHiddenGene(Panda.Gene.valueOf(meta.getPersistentDataContainer().get(pandaHiddenGene, PersistentDataType.STRING)));
			((Panda)entity).setMainGene(Panda.Gene.valueOf(meta.getPersistentDataContainer().get(pandaMainGene, PersistentDataType.STRING)));
		}
		if (entity instanceof Parrot) {
			((Parrot)entity).setVariant(Parrot.Variant.valueOf(meta.getPersistentDataContainer().get(parrotVariant, PersistentDataType.STRING)));
		}
		if (entity instanceof Pig) {
			((Pig)entity).setSaddle(meta.getPersistentDataContainer().get(pigSaddle, PersistentDataType.BYTE) == 1);
		}
		if (entity instanceof Rabbit) {
			((Rabbit)entity).setRabbitType(Rabbit.Type.valueOf(meta.getPersistentDataContainer().get(rabbitType, PersistentDataType.STRING)));
		}
		if (entity instanceof Sheep) {
			((Sheep)entity).setColor(DyeColor.valueOf(meta.getPersistentDataContainer().get(sheepColor, PersistentDataType.STRING)));
			((Sheep)entity).setSheared(meta.getPersistentDataContainer().get(sheepSheared, PersistentDataType.BYTE) == 1);
		}
		if (entity instanceof Slime) {
			((Slime)entity).setSize(meta.getPersistentDataContainer().get(slimeSize, PersistentDataType.INTEGER));
		}
		if (entity instanceof Tameable) {
			if (meta.getPersistentDataContainer().has(ownerUUID, PersistentDataType.STRING)) {
				((Tameable)entity).setOwner(Bukkit.getServer().getOfflinePlayer(UUID.fromString(meta.getPersistentDataContainer().get(ownerUUID, PersistentDataType.STRING))));
			}
		}
		if (entity instanceof Wolf) {
			((Wolf)entity).setCollarColor(DyeColor.valueOf(meta.getPersistentDataContainer().get(collarColor, PersistentDataType.STRING)));
		}
		if (entity instanceof Zombie) {
			((Zombie)entity).setBaby(meta.getPersistentDataContainer().get(zombieBaby, PersistentDataType.BYTE) == 1);
		}
		entity.setHealth(meta.getPersistentDataContainer().get(entityHealth, PersistentDataType.DOUBLE));
		return entity;
	}
	
	private boolean hasEntityStored(ItemStack item) {
		return item.hasItemMeta()
				&& item.getItemMeta().getPersistentDataContainer().has(entityType, PersistentDataType.STRING);
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onCapture(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Snowball
				&& eggs.containsKey(event.getDamager())) {
			Snowball egg = (Snowball) event.getDamager();
			ItemStack spawnEgg = eggs.get(egg);
			if (egg.getShooter() instanceof Player) {
				PowerUser user = getUser((Player) egg.getShooter());
				if (user.allowPower(this)
						&& capturable.contains(event.getEntity().getType())
						&& !hasEntityStored(spawnEgg)) {
					LivingEntity entity = (LivingEntity) event.getEntity();
					if (captureTamed
							|| !(entity instanceof Tameable)
							|| (entity instanceof Tameable &&
									(((Tameable)entity).getOwner() == user.getPlayer() || ((Tameable)entity).getOwner() == null))) {
						ItemStack newSpawnEgg = createEgg(entity);
						Item droppedEgg = entity.getWorld().dropItemNaturally(entity.getLocation(), newSpawnEgg);
						entity.playEffect(EntityEffect.ENTITY_POOF);
						entity.remove();
						if (!user.getPlayer().getInventory().addItem(newSpawnEgg).containsValue(newSpawnEgg)) {
							PowerTools.fakeCollect(user.getPlayer(), droppedEgg);
							droppedEgg.remove();
						}
						else {
							eggsOnGround.put(droppedEgg, user);
						}
						eggs.remove(egg);
					}
					else if (!captureTamed
							&& entity instanceof Tameable
							&& ((Tameable)entity).getOwner() != null
							&& ((Tameable)entity).getOwner() != user.getPlayer()) {
						user.getPlayer().sendMessage(cantCapTamed);
					}
				}
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onPickup(EntityPickupItemEvent event) {
		if (eggsOnGround.containsKey(event.getItem())) {
			if (event.getEntity() instanceof Player) {
				PowerUser user = getUser((Player) event.getEntity());
				event.setCancelled(eggsOnGround.get(event.getItem()) != user);
			}
			else {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onUse(PowerUseEvent event) {
		if (event.getPower() == this) {
			PowerUser user = event.getUser();
			if (user.allowPower(this)) {
				ItemStack item = event.getItem().clone();
				item.setAmount(1);
				Snowball snowball = user.getPlayer().launchProjectile(Snowball.class);
				PowerTools.addDisguise(snowball, item);
				eggs.put(snowball, item);
				if (user.hasStatMaxed(eggsThrown)) {
					eggOwners.put(snowball, user);
				}
				if (event.getItem().hasItemMeta()
						&& event.getItem().getItemMeta().getPersistentDataContainer().has(entityType, PersistentDataType.STRING)) {
					user.increaseStat(eggsThrown, 1);
				}
				event.consumeItem();
			}
		}
	}
	
	@EventHandler
	private void onEggHit(ProjectileHitEvent event) {
		if (eggs.containsKey(event.getEntity())) {
			Snowball egg = (Snowball) event.getEntity();
			ItemStack spawnEgg = eggs.get(egg);
			if (hasEntityStored(spawnEgg)) {
				createEntity(spawnEgg, egg.getLocation());
				eggs.remove(egg);
				if (eggOwners.containsKey(egg)) {
					Item droppedEgg = egg.getWorld().dropItemNaturally(egg.getLocation(), item);
					PowerUser user = eggOwners.get(egg);
					if (!user.getPlayer().getInventory().addItem(item).containsValue(item)) {
						PowerTools.fakeCollect(user.getPlayer(), droppedEgg);
						droppedEgg.remove();
					}
					else {
						eggsOnGround.put(droppedEgg, user);
					}
				}
			}
			else {
				egg.getWorld().dropItemNaturally(egg.getLocation(), spawnEgg);
			}
		}
	}

}
