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
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Colorable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.tools.version.MCVersion;
import me.sirrus86.s86powers.users.PowerUser;

@PowerManifest(name = "Mob Catcher", type = PowerType.DEFENSE, author = "sirrus86", concept = "kamyarm007", version = MCVersion.v1_14, icon = Material.ZOMBIE_SPAWN_EGG, usesPackets = true,
	description = "[act:item]ing while holding [item] throws it. If it hits a capturable entity, it will be stored in the [item] which is returned to you."
			+ " [act:item]ing a [item] with a stored entity will thorw it, causing it to expel the stored entity on contact.")
public final class MobCatcher extends Power {

	private final Set<EntityType> capturable = EnumSet.of(EntityType.BAT, EntityType.BLAZE, EntityType.CAT, EntityType.CAVE_SPIDER, EntityType.CHICKEN,
			EntityType.COW, EntityType.CREEPER, EntityType.DOLPHIN, EntityType.DROWNED, EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.FOX,
			EntityType.GHAST, EntityType.GUARDIAN, EntityType.HUSK, EntityType.MAGMA_CUBE, EntityType.MUSHROOM_COW, EntityType.OCELOT, EntityType.PANDA,
			EntityType.PARROT, EntityType.PHANTOM, EntityType.PIG, PowerTools.resolveEntityType("PIGZOMBIE"), EntityType.POLAR_BEAR, EntityType.RABBIT, EntityType.SHEEP,
			EntityType.SHULKER, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME, EntityType.SPIDER, EntityType.SQUID, EntityType.STRAY,
			EntityType.TURTLE, EntityType.WITCH, EntityType.WITHER_SKELETON, EntityType.WOLF, EntityType.ZOMBIE);
	
	private final NamespacedKey ageableAge = createNamespacedKey("ageable-age"),
			axolotlVariant = createNamespacedKey("axolotl-variant"),
			beeHasNectar = createNamespacedKey("bee-has-nectar"),
			beeHasStung = createNamespacedKey("bee-has-stung"),
			catType = createNamespacedKey("cat-type"),
			collarColor = createNamespacedKey("collar-color"),
			colorableColor = createNamespacedKey("colorable-color"),
			creeperPowered = createNamespacedKey("creeper-powered"),
			customName = createNamespacedKey("custom-name"),
			entityHealth = createNamespacedKey("entity-health"),
			entityType = createNamespacedKey("entity-type"),
			foxType = createNamespacedKey("fox-type"),
			goatScreaming = createNamespacedKey("goat-screaming"),
			mooshroomVariant = createNamespacedKey("mooshroom-variant"),
			ownerUUID = createNamespacedKey("owner-uuid"),
			pandaHiddenGene = createNamespacedKey("panda-hidden-gene"),
			pandaMainGene = createNamespacedKey("panda-main-gene"),
			parrotVariant = createNamespacedKey("parrot-variant"),
			phantomSize = createNamespacedKey("phantom-size"),
			rabbitType = createNamespacedKey("rabbit-type"),
			sheepSheared = createNamespacedKey("sheep-sheared"),
			slimeSize = createNamespacedKey("slime-size"),
			steerableSaddle = createNamespacedKey("steerable-saddle");
	
	private Set<EntityType> allowCapture;
	private Map<Snowball, ItemStack> eggs;
	private Map<Snowball, PowerUser> eggOwners;
	
	private String cantCapTamed;
	private PowerOption<Boolean> captureTamed;
	private PowerStat eggsThrown;
	
	@Override
	protected void onEnable() {
		allowCapture = EnumSet.noneOf(EntityType.class);
		eggOwners = new HashMap<>();
		eggs = new HashMap<>();
	}

	@Override
	protected void config() {
		captureTamed = option("capture-tamed", false, "Whether entities tamed by other players (wolves, cats, etc) can be captured.");
		eggsThrown = stat("eggs-thrown", 50, "Entities relocated", "[item] is now refunded after expelling a stored entity.");
		item = option("item", new ItemStack(Material.ENDER_EYE, 1), "Item used to catch and store mobs.");
		for (EntityType eType : capturable) {
			PowerOption<Boolean> capturable = option("capturable." + eType.toString().replaceAll("_", "-").toLowerCase(), true, "Whether " + WordUtils.capitalizeFully(eType.toString().replaceAll("_", " ")) + " should be capturable.");
			if (getOption(capturable)) {
				allowCapture.add(eType);
			}
		}
		cantCapTamed = locale("message.cant-capture-tamed", ChatColor.RED + "You can't capture entities tamed by other players.");
		supplies(new ItemStack(getRequiredItem().getType(), getRequiredItem().getMaxStackSize()));
	}
	
	private ItemStack createEgg(LivingEntity entity) {
		ItemStack egg = new ItemStack(getRequiredItem().getType(), 1);
		ItemMeta meta = egg.hasItemMeta() ? getRequiredItem().getItemMeta() : entity.getServer().getItemFactory().getItemMeta(getRequiredItem().getType());
		if (meta != null) {
			PersistentDataContainer iData = meta.getPersistentDataContainer();
			List<String> stats = new ArrayList<>();
			iData.set(entityType, PersistentDataType.STRING, entity.getType().toString());
			iData.set(entityHealth, PersistentDataType.DOUBLE, entity.getHealth());
			AttributeInstance health = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
			if (health != null) {
				stats.add("Health: " + entity.getHealth() + "/" + health.getValue());
			}
			if (entity instanceof Ageable ageable) {
				if (!ageable.isAdult()) {
					stats.add("Juvenile");
				}
				iData.set(ageableAge, PersistentDataType.INTEGER, ageable.getAge());
			}
			if (entity instanceof Colorable colorable) {
				DyeColor color = colorable.getColor() != null ? colorable.getColor() : DyeColor.WHITE;
				stats.add("Color: " + WordUtils.capitalize(color.toString().replace("_", " ").toLowerCase()));
				iData.set(colorableColor, PersistentDataType.STRING, color.toString());
			}
			if (entity instanceof Steerable steerable) {
				if (steerable.hasSaddle()) {
					stats.add("Saddled");
				}
				iData.set(steerableSaddle, PersistentDataType.BYTE, steerable.hasSaddle() ? (byte) 1 : (byte) 0);
			}
			if (entity instanceof Tameable tameable) {
				if (tameable.isTamed()
						&& tameable.getOwner() != null) {
					iData.set(ownerUUID, PersistentDataType.STRING, tameable.getOwner().getUniqueId().toString());
				}
			}
			switch (entity.getType().name()) {
				case "AXOLOTL" -> {
					if (entity instanceof Axolotl axolotl) {
						stats.add("Type: " + WordUtils.capitalize(axolotl.getVariant().toString().replace("_", " ").toLowerCase()));
						iData.set(axolotlVariant, PersistentDataType.INTEGER, axolotl.getVariant().ordinal());
					}
				}
				case "BEE" -> {
					if (entity instanceof Bee bee) {
						iData.set(beeHasNectar, PersistentDataType.BYTE, bee.hasNectar() ? (byte) 1 : (byte) 0);
						iData.set(beeHasStung, PersistentDataType.BYTE, bee.hasStung() ? (byte) 1 : (byte) 0);
					}
				}
				case "CAT" -> {
					if (entity instanceof Cat cat) {
						stats.add("Type: " + WordUtils.capitalize(cat.getCatType().toString().replace("_", " ").toLowerCase()));
						iData.set(catType, PersistentDataType.STRING, cat.getCatType().toString());
						iData.set(collarColor, PersistentDataType.STRING, cat.getCollarColor().toString());
					}
				}
				case "CREEPER" -> {
					if (entity instanceof Creeper creeper) {
						if (creeper.isPowered()) {
							stats.add("Powered");
						}
						iData.set(creeperPowered, PersistentDataType.BYTE, creeper.isPowered() ? (byte) 1 : (byte) 0);
					}
				}
				case "FOX" -> {
					if (entity instanceof Fox fox) {
						stats.add("Type: " + WordUtils.capitalize(fox.getFoxType().toString().replace("_", " ").toLowerCase()));
						iData.set(foxType, PersistentDataType.STRING, fox.getFoxType().toString());
					}
				}
				case "GOAT" -> {
					if (entity instanceof Goat goat) {
						if (goat.isScreaming()) {
							stats.add("Screaming");
						}
						iData.set(goatScreaming, PersistentDataType.BYTE, goat.isScreaming() ? (byte) 1 : (byte) 0);
					}
				}
				case "MAGMA_CUBE", "SLIME" -> {
					if (entity instanceof Slime slime) {
						stats.add("Size: " + slime.getSize());
						iData.set(slimeSize, PersistentDataType.INTEGER, slime.getSize());
					}
				}
				case "MUSHROOM_COW" -> {
					if (entity instanceof MushroomCow mushroomCow) {
						stats.add("Color: " + WordUtils.capitalize(mushroomCow.getVariant().toString().replace("_", " ").toLowerCase()));
						iData.set(foxType, PersistentDataType.STRING, mushroomCow.getVariant().toString());
					}
				}
				case "PANDA" -> {
					if (entity instanceof Panda panda) {
						iData.set(pandaHiddenGene, PersistentDataType.STRING, panda.getHiddenGene().toString());
						iData.set(pandaMainGene, PersistentDataType.STRING, panda.getMainGene().toString());
					}
				}
				case "PARROT" -> {
					if (entity instanceof Parrot parrot) {
						stats.add("Color: " + WordUtils.capitalize(parrot.getVariant().toString().replace("_", " ").toLowerCase()));
						iData.set(parrotVariant, PersistentDataType.STRING, parrot.getVariant().toString());
					}
				}
				case "PHANTOM" -> {
					if (entity instanceof Phantom phantom) {
						iData.set(phantomSize, PersistentDataType.INTEGER, phantom.getSize());
					}
				}
				case "RABBIT" -> {
					if (entity instanceof Rabbit rabbit) {
						stats.add("Type: " + WordUtils.capitalize(rabbit.getRabbitType().toString().replace("_", " ").toLowerCase()));
						iData.set(rabbitType, PersistentDataType.STRING, rabbit.getRabbitType().toString());
					}
				}
				case "SHEEP" -> {
					if (entity instanceof Sheep sheep) {
						iData.set(sheepSheared, PersistentDataType.BYTE, sheep.isSheared() ? (byte) 1 : (byte) 0);
					}
				}
				case "WOLF" -> {
					if (entity instanceof Wolf wolf) {
						iData.set(collarColor, PersistentDataType.STRING, wolf.getCollarColor().toString());
					}
				}
			}
			meta.setDisplayName(WordUtils.capitalize(entity.getType().name().replaceAll("_", " ").toLowerCase()));
			if (entity.getCustomName() != null) {
				meta.setDisplayName(entity.getCustomName());
				iData.set(customName, PersistentDataType.STRING, entity.getCustomName());
			}
			meta.setLore(stats);
			egg.setItemMeta(meta);
		}
		return egg;
	}

	@SuppressWarnings("DataFlowIssue")
	private void createEntity(ItemStack egg, Location loc) {
		LivingEntity entity = null;
		ItemMeta meta = egg.getItemMeta();
		if (meta != null) {
			PersistentDataContainer iData = meta.getPersistentDataContainer();
			if (loc.getWorld() != null) {
				entity = (LivingEntity) loc.getWorld().spawnEntity(loc, EntityType.valueOf(meta.getPersistentDataContainer().get(entityType, PersistentDataType.STRING)));
			}
			if (entity instanceof Ageable ageable) {
				ageable.setAge(iData.get(ageableAge, PersistentDataType.INTEGER));
			}
			if (entity instanceof Cat cat) {
				cat.setCatType(Cat.Type.valueOf(iData.get(catType, PersistentDataType.STRING)));
				cat.setCollarColor(DyeColor.valueOf(iData.get(collarColor, PersistentDataType.STRING)));
			}
			if (entity instanceof Colorable colorable) {
				colorable.setColor(DyeColor.valueOf(iData.get(colorableColor, PersistentDataType.STRING)));
			}
			if (entity instanceof Creeper creeper) {
				creeper.setPowered(iData.get(creeperPowered, PersistentDataType.BYTE) == 1);
			}
			if (entity instanceof Fox fox) {
				fox.setFoxType(Fox.Type.valueOf(iData.get(foxType, PersistentDataType.STRING)));
			}
			if (entity instanceof Goat goat) {
				goat.setScreaming(iData.get(goatScreaming, PersistentDataType.BYTE) == 1);
			}
			if (entity instanceof MushroomCow mushroomCow) {
				mushroomCow.setVariant(MushroomCow.Variant.valueOf(iData.get(mooshroomVariant, PersistentDataType.STRING)));
			}
			if (entity instanceof Panda panda) {
				panda.setHiddenGene(Panda.Gene.valueOf(iData.get(pandaHiddenGene, PersistentDataType.STRING)));
				panda.setMainGene(Panda.Gene.valueOf(iData.get(pandaMainGene, PersistentDataType.STRING)));
			}
			if (entity instanceof Parrot parrot) {
				parrot.setVariant(Parrot.Variant.valueOf(iData.get(parrotVariant, PersistentDataType.STRING)));
			}
			if (entity instanceof Phantom phantom) {
				phantom.setSize(iData.get(phantomSize, PersistentDataType.INTEGER));
			}
			if (entity instanceof Rabbit rabbit) {
				rabbit.setRabbitType(Rabbit.Type.valueOf(iData.get(rabbitType, PersistentDataType.STRING)));
			}
			if (entity instanceof Sheep sheep) {
				sheep.setSheared(iData.get(sheepSheared, PersistentDataType.BYTE) == 1);
			}
			if (entity instanceof Slime slime) {
				slime.setSize(iData.get(slimeSize, PersistentDataType.INTEGER));
			}
			if (entity instanceof Steerable steerable) {
				steerable.setSaddle(iData.get(steerableSaddle, PersistentDataType.BYTE) == 1);
			}
			if (entity instanceof Tameable tameable) {
				if (iData.has(ownerUUID, PersistentDataType.STRING)) {
					tameable.setOwner(Bukkit.getServer().getOfflinePlayer(UUID.fromString(iData.get(ownerUUID, PersistentDataType.STRING))));
				}
			}
			if (entity instanceof Wolf wolf) {
				wolf.setCollarColor(DyeColor.valueOf(iData.get(collarColor, PersistentDataType.STRING)));
			}
			if (entity != null) {
				entity.setHealth(meta.getPersistentDataContainer().get(entityHealth, PersistentDataType.DOUBLE));
			}
		}
	}
	
	private boolean hasEntityStored(ItemStack item) {
		return item.getItemMeta() != null
				&& item.getItemMeta().getPersistentDataContainer().has(entityType, PersistentDataType.STRING);
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onCapture(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Snowball egg
				&& eggs.containsKey(egg)
				&& allowCapture.contains(event.getEntity().getType())) {
			ItemStack spawnEgg = eggs.get(egg);
			if (egg.getShooter() instanceof Player) {
				PowerUser user = getUser((Player) egg.getShooter());
				if (user.allowPower(this)
						&& capturable.contains(event.getEntity().getType())
						&& !hasEntityStored(spawnEgg)) {
					LivingEntity entity = (LivingEntity) event.getEntity();
					if (user.getOption(captureTamed)
							|| !(entity instanceof Tameable)
							|| (((Tameable)entity).getOwner() == user.getPlayer() || ((Tameable)entity).getOwner() == null)) {
						ItemStack newSpawnEgg = createEgg(entity);
						Item droppedEgg = entity.getWorld().dropItemNaturally(entity.getLocation(), newSpawnEgg);
						droppedEgg.setOwner(user.getUUID());
						entity.playEffect(EntityEffect.ENTITY_POOF);
						entity.remove();
						if (!user.getPlayer().getInventory().addItem(newSpawnEgg).containsValue(newSpawnEgg)) {
							runTask(() -> {
								PowerTools.fakeCollect(user.getPlayer(), droppedEgg);
								droppedEgg.remove();
							});
						}
						eggs.remove(egg);
					}
					else if (!user.getOption(captureTamed)
							&& ((Tameable) entity).getOwner() != null
							&& ((Tameable) entity).getOwner() != user.getPlayer()) {
						user.getPlayer().sendMessage(cantCapTamed);
					}
				}
			}
			event.setCancelled(true);
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
				if (event.getItem().getItemMeta() != null
						&& event.getItem().getItemMeta().getPersistentDataContainer().has(entityType, PersistentDataType.STRING)) {
					user.increaseStat(eggsThrown, 1);
				}
				event.consumeItem();
			}
		}
	}
	
	@EventHandler
	private void onEggHit(ProjectileHitEvent event) {
		if (event.getEntity() instanceof Snowball egg
				&& eggs.containsKey(egg)) {
			ItemStack spawnEgg = eggs.get(egg);
			if (hasEntityStored(spawnEgg)) {
				createEntity(spawnEgg, egg.getLocation());
				eggs.remove(egg);
				if (eggOwners.containsKey(egg)) {
					Item droppedEgg = egg.getWorld().dropItemNaturally(egg.getLocation(), getRequiredItem());
					PowerUser user = eggOwners.get(egg);
					droppedEgg.setOwner(user.getUUID());
					if (!user.getPlayer().getInventory().addItem(getRequiredItem()).containsValue(getRequiredItem())) {
						runTask(() -> {
							PowerTools.fakeCollect(user.getPlayer(), droppedEgg);
							droppedEgg.remove();
						});
					}
				}
			}
			else if (event.getHitEntity() == null
					|| !capturable.contains(event.getHitEntity().getType())) {
				egg.getWorld().dropItemNaturally(egg.getLocation(), spawnEgg);
			}
		}
	}

}
