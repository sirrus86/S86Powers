package me.sirrus86.s86powers.tools.nms.reflect;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.sirrus86.s86powers.tools.version.MCVersion;

public class NMSLibrary extends me.sirrus86.s86powers.tools.nms.NMSLibrary {

	private Class<?> entityItem = resolveNMSClass("world.entity.item", "EntityItem"),
			nmsEntity = resolveNMSClass("world.entity", "Entity");
	private Class<?> craftLivingEntity = resolveOCBClass("entity", "CraftLivingEntity"),
			craftWorld = resolveOCBClass("", "CraftWorld");

	@Override
	public void controlWASD(Player rider, LivingEntity entity, float forward, float strafe, boolean jump) {
		try {
			Object nmsEntity = getHandle(entity);
			// TODO
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Object createItem(Location loc, ItemStack item) {
		// return new EntityItem(((CraftWorld)loc.getWorld()).getHandle(), loc.getX(), loc.getY(), loc.getZ(), CraftItemStack.asNMSCopy(item));
		return null; // TODO
	}

	@Override
	public int generateEntityID() {
		try {
			Field field = nmsEntity.getDeclaredField(MCVersion.isLessThan(MCVersion.v1_17) ? "entityCount" : "b");
			field.setAccessible(true);
			if (MCVersion.isLessThan(MCVersion.v1_14)) {
				int id = field.getInt(null);
				field.set(null, id + 1);
				return id;
			}
			else {
				AtomicInteger id = (AtomicInteger) field.get(null);
				return id.incrementAndGet();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	public <O> O getCustomObject(Class<O> clazz, Class<?>[] constructs, Object[] values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getDataWatcher(Object instance) {
		try {
			if (nmsEntity.isInstance(instance)) {
				return nmsEntity.getMethod("getDataWatcher").invoke(instance);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getEntityTypeID(EntityType type) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getFallingBlockData(Block block) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private Object getHandle(Object instance) {
		try {
			return instance.getClass().getMethod("getHandle").invoke(instance);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Object getNMSEntityType(EntityType type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getNMSItem(ItemStack item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getNMSItemStack(ItemStack item) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private Class<?> resolveNMSClass(String path, String className) {
		try {
			if (MCVersion.isLessThan(MCVersion.v1_17)) {
				return Class.forName("net.minecraft.server." + MCVersion.CURRENT_VERSION.getPath() + "." + className);
			}
			else {
				return Class.forName("net.minecraft." + path + (path != "" ? "." : "") + className);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private Class<?> resolveOCBClass(String path, String className) {
		try {
			return Class.forName("org.bukkit.craftbukkit." + MCVersion.CURRENT_VERSION.getPath() + "." + path +  (path != "" ? "." : "") + className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void removePathfinding(Creature creature) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDirection(Fireball entity, Vector vec) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ItemStack setItemGlow(ItemStack item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRotation(Entity entity, float yaw, float pitch) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTamed(Creature entity, Player player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void spawnEntity(Entity entity, Location loc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unTame(Creature entity) {
		// TODO Auto-generated method stub
		
	}

}
