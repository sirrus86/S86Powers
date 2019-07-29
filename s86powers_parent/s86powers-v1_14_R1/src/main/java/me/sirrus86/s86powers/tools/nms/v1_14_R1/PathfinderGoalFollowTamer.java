package me.sirrus86.s86powers.tools.nms.v1_14_R1;

import java.util.EnumSet;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity;
import org.bukkit.event.entity.EntityTeleportEvent;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityInsentient;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.IBlockData;
import net.minecraft.server.v1_14_R1.IWorldReader;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.Navigation;
import net.minecraft.server.v1_14_R1.NavigationAbstract;
import net.minecraft.server.v1_14_R1.NavigationFlying;
import net.minecraft.server.v1_14_R1.PathType;
import net.minecraft.server.v1_14_R1.PathfinderGoal;

public class PathfinderGoalFollowTamer extends PathfinderGoal {

	private final EntityInsentient a;
	private EntityLiving c;
	protected final IWorldReader b;
	private final double d;
	private final NavigationAbstract e;
	private int f;
	private final float g;
	private final float h;
	private float i;
	private EntityHuman owner;

	public PathfinderGoalFollowTamer(final EntityInsentient entitytameableanimal, final EntityHuman owner, final double d0, final float f, final float f1) {
		this.a = entitytameableanimal;
		this.b = entitytameableanimal.world;
		this.d = d0;
		this.e = entitytameableanimal.getNavigation();
		this.h = f;
		this.g = f1;
		this.a(EnumSet.of(Type.MOVE, Type.LOOK));
		this.owner = owner;
		if (!(entitytameableanimal.getNavigation() instanceof Navigation) && !(entitytameableanimal.getNavigation() instanceof NavigationFlying))
			throw new IllegalArgumentException("Unsupported mob type for FollowTamerGoal");
	}

	@Override
	public boolean a() {
		EntityLiving entityliving = this.owner;
		if (entityliving == null) {
			return false;
		}
		if (entityliving instanceof EntityHuman && ((EntityHuman)entityliving).isSpectator()) {
			return false;
		}
		if (this.a.h(entityliving) < this.h * this.h) {
			return false;
		}
		this.c = entityliving;
		return true;
	}

	@Override
	public boolean b() {
		return !this.e.n() && this.a.h(this.c) > this.g * this.g;
	}

	@Override
	public void c() {
		this.f = 0;
		this.i = this.a.a(PathType.WATER);
		this.a.a(PathType.WATER, 0.0F);
	}

	@Override
	public void d() {
		this.c = null;
		this.e.o();
		this.a.a(PathType.WATER, this.i);
	}

	@Override
	public void e() {
		this.a.getControllerLook().a(this.c, 10.0F, this.a.M());
		if (--this.f <= 0) {
			this.f = 10;
			if (!this.e.a(this.c, this.d) && !this.a.isLeashed() && !this.a.isPassenger() && this.a.h(this.c) >= 144.0) {
				final int i = MathHelper.floor(this.c.locX) - 2;
                final int j = MathHelper.floor(this.c.locZ) - 2;
                final int k = MathHelper.floor(this.c.getBoundingBox().minY);
                for (int l = 0; l <= 4; ++l) {
                	int i2 = 0;
                	while (i2 <= 4) {
                        if ((l < 1 || i2 < 1 || l > 3 || i2 > 3) && this.a(new BlockPosition(i + l, k - l, j + i2))) {
                            final CraftEntity entity = this.a.getBukkitEntity();
                            Location to = new Location(entity.getWorld(), i + l + 0.5F, k, j + i2 + 0.5F, this.a.yaw, this.a.pitch);
                            final EntityTeleportEvent event = new EntityTeleportEvent(entity, entity.getLocation(), to);
                            this.a.world.getServer().getPluginManager().callEvent(event);
                            if (event.isCancelled()) {
                                return;
                            }
                            to = event.getTo();
                            this.a.setPositionRotation(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
                            this.e.o();
                            return;
                        }
                        else {
                        	++i2;
                        }
                    }
				}
			}
		}
	}
	
	protected boolean a(final BlockPosition blockposition) {
        final IBlockData iblockdata = this.b.getType(blockposition);
        return iblockdata.a(this.b, blockposition, this.a.getEntityType()) && this.b.isEmpty(blockposition.up()) && this.b.isEmpty(blockposition.up(2));
    }

}
