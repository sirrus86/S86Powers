package me.sirrus86.s86powers.tools.nms.v1_17_1;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftFireball;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Spider;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.util.Vector;

import net.minecraft.EnumChatFormat;
import net.minecraft.world.item.Item;
import net.minecraft.core.IRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.ai.goal.PathfinderGoalMeleeAttack;
import net.minecraft.world.entity.ai.goal.PathfinderGoalSelector;
import net.minecraft.world.entity.ai.goal.PathfinderGoalWrapped;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalHurtByTarget;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.projectile.EntityFireball;

public class NMSLibrary extends me.sirrus86.s86powers.tools.nms.NMSLibrary {

	@Override
	public void controlWASD(Player rider, org.bukkit.entity.LivingEntity entity, float forward, float strafe, boolean jump) {
//		LivingEntity nmsEntity = ((CraftLivingEntity)entity).getHandle();
//		nmsEntity.move(MoverType.SELF, nmsEntity.getDeltaMovement());
//		nmsEntity.a(nmsEntity.isOnGround() ? 0.1F : 0.05F, new Vec3(strafe, 0.0D, forward));
//		nmsEntity.lastPitch = nmsEntity.pitch;
//		nmsEntity.pitch = rider.getEyeLocation().getPitch();
//		nmsEntity.lastYaw = nmsEntity.yaw;
//		nmsEntity.yaw = rider.getEyeLocation().getYaw();
//		nmsEntity.setHeadRotation(rider.getEyeLocation().getYaw());
	}
	
	@Override
	public EnumChatFormat convertColor(ChatColor color) {
		return color != ChatColor.MAGIC ? EnumChatFormat.b(color.name()) : EnumChatFormat.q;
	}
	
	@Override
	public EntityItem createItem(Location loc, org.bukkit.inventory.ItemStack item) {
		return new EntityItem(((CraftWorld)loc.getWorld()).getHandle(), loc.getX(), loc.getY(), loc.getZ(), CraftItemStack.asNMSCopy(item));
	}
	
	@Override
	public int generateEntityID() {
		try {
			Field field = Entity.class.getDeclaredField("b");
			field.setAccessible(true);
			AtomicInteger id = (AtomicInteger) field.get(null);
			return id.incrementAndGet();
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	public DataWatcher getDataWatcher(Object instance) {
		if (instance instanceof Entity) {
			return ((Entity) instance).getDataWatcher();
		}
		return null;
	}
	
	@Override
	public int getEntityTypeID(org.bukkit.entity.EntityType type) {
		return IRegistry.Y.getId(getNMSEntityType(type));
	}
	
	@Override
	public EntityTypes<?> getNMSEntityType(org.bukkit.entity.EntityType type) {
		return EntityTypes.a(type.getName().toLowerCase()).get();
//		EntityType<?> types = null;
//		try {
//			@SuppressWarnings("deprecation")
//			Field field = EntityType.class.getDeclaredField(type.getName().toUpperCase());
//			field.setAccessible(true);
//			types = (EntityType<?>) field.get(null);
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//		return types;
	}
	
	@Override
	public Item getNMSItem(org.bukkit.inventory.ItemStack item) {
		return getNMSItemStack(item).getItem();
	}
	
	@Override
	public ItemStack getNMSItemStack(org.bukkit.inventory.ItemStack item) {
		return CraftItemStack.asNMSCopy(item);
	}
	
	@Override
	public void removePathfinding(Creature entity) {
		try {
			entity.setTarget(null);
			EntityCreature handle = ((CraftCreature)entity).getHandle();
			handle.bP = new PathfinderGoalSelector(handle.t.getMethodProfilerSupplier());
			handle.bQ = new PathfinderGoalSelector(handle.t.getMethodProfilerSupplier()); // TODO: Make separate for 1.17.1
//			PathfinderGoalSelector[] goalSelectors = new PathfinderGoalSelector[] { handle.goalSelector, handle.targetSelector };
//			for (int i = 0; i < goalSelectors.length; i ++) {
//				Field c = goalSelectors[i].getClass().getDeclaredField("c"),
//						d = goalSelectors[i].getClass().getDeclaredField("d");
//				c.setAccessible(true);
//				d.setAccessible(true);
//				c.set(goalSelectors[i], new EnumMap<PathfinderGoal.Type, PathfinderGoalWrapped>(PathfinderGoal.Type.class));
//				d.set(goalSelectors[i], new LinkedHashSet<PathfinderGoalWrapped>());
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setDirection(org.bukkit.entity.Fireball entity, Vector vec) {
		double d = Math.sqrt(vec.getX() * vec.getX() + vec.getY() * vec.getY() + vec.getZ() * vec.getZ());
		EntityFireball nmsEntity = ((CraftFireball)entity).getHandle();
		nmsEntity.setDirection((vec.getX() / d) * 0.1D, (vec.getY() / d) * 0.1D, (vec.getZ() / d) * 0.1D);
	}
	
	@Override
	public org.bukkit.inventory.ItemStack setItemGlow(org.bukkit.inventory.ItemStack item) {
		ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
		NBTTagCompound tag = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();
		NBTTagList ench = new NBTTagList();
		tag.set("ench", ench);
		nmsItem.setTag(tag);
		return CraftItemStack.asCraftMirror(nmsItem);
	}
	
	@Override
	public void setRotation(org.bukkit.entity.Entity entity, float yaw, float pitch) {
		Entity nmsEntity = ((CraftEntity)entity).getHandle();
		nmsEntity.setXRot(yaw);
		nmsEntity.setYRot(pitch);
	}

	@Override
	public void setTamed(Creature entity, Player player) {
		try {
			entity.setTarget(null);
			EntityCreature handle = ((CraftCreature)entity).getHandle();
			PathfinderGoalSelector goalSelector = handle.bP,
					targetSelector = handle.bQ;
			Field c = targetSelector.getClass().getDeclaredField("c"),
					d = targetSelector.getClass().getDeclaredField("d");
			c.setAccessible(true);
			d.setAccessible(true);
			c.set(targetSelector, new EnumMap<PathfinderGoal.Type, PathfinderGoalWrapped>(PathfinderGoal.Type.class));
			d.set(targetSelector, new LinkedHashSet<PathfinderGoalWrapped>());
			if (entity instanceof Endermite
					|| entity instanceof Silverfish
					|| entity instanceof WitherSkeleton
					|| entity instanceof Spider
					|| entity instanceof Zombie) {
				goalSelector.a(4, new PathfinderGoalMeleeAttack(handle, 1.2D, true));
			}
			goalSelector.a(5, new PathfinderGoalFollowTamer(handle, ((CraftPlayer)player).getHandle(), 1.2D, 10.0F, 2.0F, false));
			targetSelector.a(1,new PathfinderGoalTamerHurtByTarget(handle, ((CraftPlayer)player).getHandle()));
			targetSelector.a(2, new PathfinderGoalTamerHurtTarget(handle, ((CraftPlayer)player).getHandle()));
			targetSelector.a(3, new PathfinderGoalHurtByTarget(handle, new Class[0]));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void spawnEntity(org.bukkit.entity.Entity entity, Location loc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <O> O getCustomObject(Class<O> clazz, Class<?>[] constructs, Object[] values) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void unTame(Creature entity) {
		try {
			EntityCreature handle = ((CraftCreature)entity).getHandle();
			PathfinderGoalSelector[] goalSelectors = { handle.bP, handle.bQ };
			for (PathfinderGoalSelector goalSelector : goalSelectors) {
				Field c = goalSelector.getClass().getDeclaredField("c"),
						d = goalSelector.getClass().getDeclaredField("d");
				c.setAccessible(true);
				d.setAccessible(true);
				c.set(goalSelector, new EnumMap<PathfinderGoal.Type, PathfinderGoalWrapped>(PathfinderGoal.Type.class));
				d.set(goalSelector, new LinkedHashSet<PathfinderGoalWrapped>());
			}
			Method initPathfinder = handle.getClass().getDeclaredMethod("registerGoals");
			initPathfinder.setAccessible(true);
			initPathfinder.invoke(handle);
		} catch (NoSuchMethodException e) {
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
