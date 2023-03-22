package me.sirrus86.tools.nms.v1_19_4;

import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalTarget;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;
import net.minecraft.world.entity.player.EntityHuman;

import org.bukkit.event.entity.EntityTargetEvent;

import java.util.EnumSet;

public class PathfinderGoalTamerHurtByTarget extends PathfinderGoalTarget {

	private EntityLiving b;
	private int c;
	private EntityHuman owner;

	public PathfinderGoalTamerHurtByTarget(EntityInsentient entitytameableanimal, EntityHuman owner) {
		super(entitytameableanimal, false);
		this.owner = owner;
		a(EnumSet.of(Type.d));
	}

	public boolean a() {
		EntityLiving entityliving = this.owner;
		if (entityliving == null) {
			return false;
		}
		this.b = entityliving.ea();
		int i = entityliving.eb();
		return (i != this.c && a(this.b, PathfinderTargetCondition.a));
	}

	public void c() {
		this.e.setTarget(this.b, EntityTargetEvent.TargetReason.TARGET_ATTACKED_OWNER, true);
		EntityLiving entityliving = this.owner;
		if (entityliving != null)
			this.c = entityliving.dX(); 
		super.c();
	}
}
