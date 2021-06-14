package me.sirrus86.s86powers.tools.nms.v1_17_R1;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.concurrent.atomic.AtomicInteger;

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

import net.minecraft.world.item.Item;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

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
	public ItemEntity createItem(Location loc, org.bukkit.inventory.ItemStack item) {
		return new ItemEntity(((CraftWorld)loc.getWorld()).getHandle(), loc.getX(), loc.getY(), loc.getZ(), CraftItemStack.asNMSCopy(item));
	}
	
	@Override
	public int generateEntityID() {
		try {
			Field field = Entity.class.getDeclaredField("entityCount");
			field.setAccessible(true);
			AtomicInteger id = (AtomicInteger) field.get(null);
			return id.incrementAndGet();
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	public SynchedEntityData getDataWatcher(Object instance) {
		if (instance instanceof Entity) {
			return ((Entity) instance).getEntityData();
		}
		return null;
	}
	
	@Override
	public int getEntityTypeID(org.bukkit.entity.EntityType type) {
		return Registry.ENTITY_TYPE.getId(getNMSEntityType(type));
	}
	
	@Override
	public EntityType<?> getNMSEntityType(org.bukkit.entity.EntityType type) {
		EntityType<?> types = null;
		try {
			@SuppressWarnings("deprecation")
			Field field = EntityType.class.getDeclaredField(type.getName().toUpperCase());
			field.setAccessible(true);
			types = (EntityType<?>) field.get(null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return types;
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
			PathfinderMob handle = ((CraftCreature)entity).getHandle();
			handle.goalSelector = new GoalSelector(handle.level.getProfilerSupplier());
			handle.targetSelector = new GoalSelector(handle.level.getProfilerSupplier());
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
		AbstractHurtingProjectile nmsEntity = ((CraftFireball)entity).getHandle();
		nmsEntity.setDirection((vec.getX() / d) * 0.1D, (vec.getY() / d) * 0.1D, (vec.getZ() / d) * 0.1D);
	}
	
	@Override
	public org.bukkit.inventory.ItemStack setItemGlow(org.bukkit.inventory.ItemStack item) {
		ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
		CompoundTag tag = nmsItem.hasTag() ? nmsItem.getTag() : new CompoundTag();
		ListTag ench = new ListTag();
		tag.put("ench", ench);
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
			PathfinderMob handle = ((CraftCreature)entity).getHandle();
			GoalSelector goalSelector = handle.goalSelector,
					targetSelector = handle.targetSelector;
			Field c = targetSelector.getClass().getDeclaredField("c"),
					d = targetSelector.getClass().getDeclaredField("d");
			c.setAccessible(true);
			d.setAccessible(true);
			c.set(targetSelector, new EnumMap<Goal.Flag, WrappedGoal>(Goal.Flag.class));
			d.set(targetSelector, new LinkedHashSet<WrappedGoal>());
			if (entity instanceof Endermite
					|| entity instanceof Silverfish
					|| entity instanceof WitherSkeleton
					|| entity instanceof Spider
					|| entity instanceof Zombie) {
				goalSelector.addGoal(4, new MeleeAttackGoal(handle, 1.2D, true));
			}
			goalSelector.addGoal(5, new FollowTamerGoal(handle, ((CraftPlayer)player).getHandle(), 1.2D, 10.0F, 2.0F, false));
			targetSelector.addGoal(1,new TamerHurtByTargetGoal(handle, ((CraftPlayer)player).getHandle()));
			targetSelector.addGoal(2, new TamerHurtTargetGoal(handle, ((CraftPlayer)player).getHandle()));
			targetSelector.addGoal(3, new HurtByTargetGoal(handle, new Class[0]));
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
			PathfinderMob handle = ((CraftCreature)entity).getHandle();
			GoalSelector[] goalSelectors = { handle.goalSelector, handle.targetSelector };
			for (GoalSelector goalSelector : goalSelectors) {
				Field c = goalSelector.getClass().getDeclaredField("c"),
						d = goalSelector.getClass().getDeclaredField("d");
				c.setAccessible(true);
				d.setAccessible(true);
				c.set(goalSelector, new EnumMap<Goal.Flag, WrappedGoal>(Goal.Flag.class));
				d.set(goalSelector, new LinkedHashSet<WrappedGoal>());
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
