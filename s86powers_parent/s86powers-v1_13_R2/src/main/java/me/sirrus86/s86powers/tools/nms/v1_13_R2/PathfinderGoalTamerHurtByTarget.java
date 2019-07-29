package me.sirrus86.s86powers.tools.nms.v1_13_R2;

import org.bukkit.event.entity.EntityTargetEvent;

import net.minecraft.server.v1_13_R2.EntityCreature;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityLiving;
import net.minecraft.server.v1_13_R2.PathfinderGoalTarget;

public class PathfinderGoalTamerHurtByTarget extends PathfinderGoalTarget {

	private EntityLiving b;
	private int c;
	private EntityHuman owner;

	public PathfinderGoalTamerHurtByTarget(final EntityCreature entitytameableanimal, final EntityHuman owner) {
		super(entitytameableanimal, false);
		this.owner = owner;
		this.a(1);
	}

	@Override
	public boolean a() {
		final EntityLiving entityliving = this.owner;
        if (entityliving == null) {
            return false;
        }
        this.b = entityliving.getLastDamager();
        final int i = entityliving.cg();
        return i != this.c && this.a(this.b, false);
	}

	@Override
	public void c() {
        this.e.setGoalTarget(this.b, EntityTargetEvent.TargetReason.TARGET_ATTACKED_OWNER, true);
        final EntityLiving entityliving = this.owner;
        if (entityliving != null) {
            this.c = entityliving.cg();
        }
        super.c();
    }

}
