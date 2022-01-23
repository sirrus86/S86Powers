package me.sirrus86.s86powers.tools.nms.v1_13_R2;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftFireball;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Spider;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.util.Vector;

import com.google.common.collect.Sets;

import net.minecraft.server.v1_13_R2.Item;
import net.minecraft.server.v1_13_R2.EntityTypes;
import net.minecraft.server.v1_13_R2.IRegistry;
import net.minecraft.server.v1_13_R2.EnumMoveType;
import net.minecraft.server.v1_13_R2.ItemStack;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.NBTTagList;
import net.minecraft.server.v1_13_R2.Block;
import net.minecraft.server.v1_13_R2.DataWatcher;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityCreature;
import net.minecraft.server.v1_13_R2.EntityFireball;
import net.minecraft.server.v1_13_R2.EntityItem;
import net.minecraft.server.v1_13_R2.EntityLiving;
import net.minecraft.server.v1_13_R2.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_13_R2.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_13_R2.PathfinderGoalSelector;

public class NMSLibrary extends me.sirrus86.s86powers.tools.nms.NMSLibrary {
	
	@Override
	public void controlWASD(Player rider, LivingEntity entity, float forward, float strafe, boolean jump) {
		EntityLiving nmsEntity = ((CraftLivingEntity)entity).getHandle();
		nmsEntity.move(EnumMoveType.SELF, nmsEntity.motX, nmsEntity.motY, nmsEntity.motZ);
		nmsEntity.a(nmsEntity.onGround ? 0.1F : 0.05F, strafe, 0.0F, forward);
		nmsEntity.lastPitch = nmsEntity.pitch;
		nmsEntity.pitch = rider.getEyeLocation().getPitch();
		nmsEntity.lastYaw = nmsEntity.yaw;
		nmsEntity.yaw = rider.getEyeLocation().getYaw();
		nmsEntity.setHeadRotation(rider.getEyeLocation().getYaw());
	}
	
	@Override
	public EntityItem createItem(Location loc, org.bukkit.inventory.ItemStack item) {
		return new EntityItem(((CraftWorld)loc.getWorld()).getHandle(), loc.getX(), loc.getY(), loc.getZ(), CraftItemStack.asNMSCopy(item));
	}
	
	@Override
	public int generateEntityID() {
		try {
			Field field = Entity.class.getDeclaredField("entityCount");
			field.setAccessible(true);
			int id = field.getInt(null);
			field.set(null, id + 1);
			return id;
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
	public int getEntityTypeID(EntityType type) {
		return IRegistry.ENTITY_TYPE.a(getNMSEntityType(type));
	}

	@Override
	public int getFallingBlockData(org.bukkit.block.Block block) {
		return Block.getCombinedId(((CraftBlock)block).getNMS());
	}
	
	@Override
	public EntityTypes<?> getNMSEntityType(EntityType type) {
		EntityTypes<?> types = null;
		try {
			@SuppressWarnings("deprecation")
			Field field = EntityTypes.class.getDeclaredField(type.getName().toUpperCase());
			field.setAccessible(true);
			types = (EntityTypes<?>) field.get(null);
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
			EntityCreature handle = ((CraftCreature)entity).getHandle();
			PathfinderGoalSelector[] goalSelectors = new PathfinderGoalSelector[] { handle.goalSelector, handle.targetSelector };
			for (int i = 0; i < goalSelectors.length; i ++) {
				Field c = goalSelectors[i].getClass().getDeclaredField("c"),
						d = goalSelectors[i].getClass().getDeclaredField("d");
				c.setAccessible(true);
				d.setAccessible(true);
				c.set(goalSelectors[i], Sets.newLinkedHashSet());
				d.set(goalSelectors[i], Sets.newLinkedHashSet());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setDirection(Fireball entity, Vector vec) {
		double d = Math.sqrt(vec.getX() * vec.getX() + vec.getY() * vec.getY() + vec.getZ() * vec.getZ());
		EntityFireball nmsEntity = ((CraftFireball)entity).getHandle();
		nmsEntity.dirX = (vec.getX() / d) * 0.1D;
		nmsEntity.dirY = (vec.getY() / d) * 0.1D;
		nmsEntity.dirZ = (vec.getZ() / d) * 0.1D;
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
		nmsEntity.lastYaw = nmsEntity.yaw;
		nmsEntity.yaw = yaw;
		nmsEntity.lastPitch = nmsEntity.pitch;
		nmsEntity.pitch = pitch;
	}

	@Override
	public void setTamed(Creature entity, Player player) {
		try {
			entity.setTarget(null);
			EntityCreature handle = ((CraftCreature)entity).getHandle();
			PathfinderGoalSelector goalSelector = handle.goalSelector,
					targetSelector = handle.targetSelector;
			Field b = targetSelector.getClass().getDeclaredField("b"),
					c = targetSelector.getClass().getDeclaredField("c");
			b.setAccessible(true);
			c.setAccessible(true);
			b.set(targetSelector, Sets.newLinkedHashSet());
			c.set(targetSelector, Sets.newLinkedHashSet());
			if (entity instanceof Endermite
					|| entity instanceof Silverfish
					|| entity instanceof WitherSkeleton
					|| entity instanceof Spider
					|| entity instanceof Zombie) {
				goalSelector.a(4, new PathfinderGoalMeleeAttack(handle, 1.2D, true));
			}
			goalSelector.a(5, new PathfinderGoalFollowTamer(handle, ((CraftPlayer)player).getHandle(), 1.2D, 10.0F, 2.0F));
			targetSelector.a(1, new PathfinderGoalTamerHurtByTarget(handle, ((CraftPlayer)player).getHandle()));
			targetSelector.a(2, new PathfinderGoalTamerHurtTarget(handle, ((CraftPlayer)player).getHandle()));
			targetSelector.a(3, new PathfinderGoalHurtByTarget(handle, false, new Class[0]));
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
			PathfinderGoalSelector[] goalSelectors = {handle.goalSelector, handle.targetSelector};
			for (PathfinderGoalSelector goalSelector : goalSelectors) {
				Field c = goalSelector.getClass().getDeclaredField("c"),
						d = goalSelector.getClass().getDeclaredField("d");
				c.setAccessible(true);
				d.setAccessible(true);
				c.set(goalSelector, Sets.newLinkedHashSet());
				d.set(goalSelector, Sets.newLinkedHashSet());
			}
			Method initPathfinder = handle.getClass().getDeclaredMethod("initPathfinder");
			initPathfinder.setAccessible(true);
			initPathfinder.invoke(handle);
		} catch (NoSuchMethodException e) {
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
