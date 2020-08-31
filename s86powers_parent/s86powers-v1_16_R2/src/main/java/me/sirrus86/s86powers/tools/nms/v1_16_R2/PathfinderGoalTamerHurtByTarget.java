package me.sirrus86.s86powers.tools.nms.v1_16_R2;

import java.util.EnumSet;

import org.bukkit.event.entity.EntityTargetEvent;

import net.minecraft.server.v1_16_R2.EntityCreature;
import net.minecraft.server.v1_16_R2.EntityHuman;
import net.minecraft.server.v1_16_R2.EntityLiving;
import net.minecraft.server.v1_16_R2.PathfinderGoalTarget;
import net.minecraft.server.v1_16_R2.PathfinderTargetCondition;

public class PathfinderGoalTamerHurtByTarget extends PathfinderGoalTarget {

	private EntityLiving b;
	private int c;
	private EntityHuman owner;

	public PathfinderGoalTamerHurtByTarget(EntityCreature entitytameableanimal, final EntityHuman owner) {
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
	    final int i = entityliving.cZ();
	    return i != this.c && this.a(this.b, PathfinderTargetCondition.a);
	}

	@Override
	public void c() {
        this.e.setGoalTarget(this.b, EntityTargetEvent.TargetReason.TARGET_ATTACKED_OWNER, true);
        final EntityLiving entityliving = this.owner;
        if (entityliving != null) {
        	this.c = entityliving.cZ();
        }
        super.c();
    }

}