package me.sirrus86.s86powers.tools.nms.v1_14_R1;

import net.minecraft.server.v1_14_R1.EntityCreature;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.PathfinderGoalTarget;
import net.minecraft.server.v1_14_R1.PathfinderTargetCondition;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;

import org.bukkit.event.entity.EntityTargetEvent;

import me.sirrus86.s86powers.version.MCVersion;

public class PathfinderGoalTamerHurtByTarget extends PathfinderGoalTarget {

	private EntityLiving b;
	private int c;
	private EntityHuman owner;
	
	private final Method m1 = resolveMethod("ct", "cs");

	public PathfinderGoalTamerHurtByTarget(final EntityCreature entitytameableanimal, final EntityHuman owner) {
		super(entitytameableanimal, false);
		this.owner = owner;
		this.a(EnumSet.of(Type.TARGET));
	}

	@Override
	public boolean a() {
		final EntityLiving entityliving = this.owner;
        if (entityliving == null) {
            return false;
        }
        this.b = entityliving.getLastDamager();
        final int i = (int) invokeMethod(entityliving, m1);
//	    final int i = entityliving.cs();
	    return i != this.c && this.a(this.b, PathfinderTargetCondition.a);
	}

	@Override
	public void c() {
        this.e.setGoalTarget(this.b, EntityTargetEvent.TargetReason.TARGET_ATTACKED_OWNER, true);
        final EntityLiving entityliving = this.owner;
        if (entityliving != null) {
        	this.c = (int) invokeMethod(entityliving, m1);
//        	this.c = entityliving.cs();
        }
        super.c();
    }
	
	private Object invokeMethod(Object object, Method method) {
		try {
			return method.invoke(object);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private final Method resolveMethod(String string1, String string2) {
		try {
			return MCVersion.isVersion(MCVersion.v1_14_3, MCVersion.v1_14_4) ? EntityLiving.class.getMethod(string1) : EntityLiving.class.getMethod(string2);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

}
