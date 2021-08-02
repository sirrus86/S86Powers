package me.sirrus86.s86powers.tools.nms.v1_17;

import java.util.EnumSet;

import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalTarget;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;
import net.minecraft.world.entity.player.EntityHuman;

import org.bukkit.event.entity.EntityTargetEvent;

public class PathfinderGoalTamerHurtTarget  extends PathfinderGoalTarget {
	
	private EntityLiving b;
	private int c;
	private EntityHuman owner;

	public PathfinderGoalTamerHurtTarget(EntityInsentient entitytameableanimal, EntityHuman owner) {
		super(entitytameableanimal, false);
		this.owner = owner;
		a(EnumSet.of(PathfinderGoal.Type.d));
	}

	public boolean a() {
		EntityLiving entityliving = this.owner;
		if (entityliving == null) {
			return false;
		}
		this.b = entityliving.dI();
		int i = entityliving.dJ();
		return (i != this.c && a(this.b, PathfinderTargetCondition.a));
	}

	public void c() {
		this.e.setGoalTarget(this.b, EntityTargetEvent.TargetReason.OWNER_ATTACKED_TARGET, true);
		EntityLiving entityliving = this.owner;
		if (entityliving != null) {
			this.c = entityliving.dJ();
		}
		super.c();
	}
}
