package me.sirrus86.s86powers.tools.nms;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public abstract class NMSLibrary {
	
	public abstract void controlWASD(Player rider, LivingEntity entity, float forward, float strafe, boolean jump);
	
	public abstract Enum<?> convertColor(ChatColor color);
	
	public abstract Object createItem(Location loc, ItemStack item);
		
	public abstract int generateEntityID();

	public abstract <O> O getCustomObject(Class<O> clazz, Class<?>[] constructs, Object[] values);

	public abstract Object getDataWatcher(Object instance);
	
	public abstract int getEntityTypeID(EntityType type);
	
	public abstract Object getNMSEntityType(EntityType type);
	
	public abstract Object getNMSItem(ItemStack item);

	public abstract Object getNMSItemStack(ItemStack item);
	
	public abstract void removePathfinding(Creature creature);

	public abstract void setDirection(Fireball entity, Vector vec);
	
	public abstract ItemStack setItemGlow(ItemStack item);
	
	public abstract void setRotation(Entity entity, float yaw, float pitch);

	public abstract void setTamed(Creature entity, Player player);

	public abstract void spawnEntity(Entity entity, Location loc);
	
	public abstract void unTame(Creature entity);
	
}
