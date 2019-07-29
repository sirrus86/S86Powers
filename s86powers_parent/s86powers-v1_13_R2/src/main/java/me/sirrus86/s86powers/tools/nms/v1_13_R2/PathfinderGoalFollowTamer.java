package me.sirrus86.s86powers.tools.nms.v1_13_R2;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.event.entity.EntityTeleportEvent;

import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityInsentient;
import net.minecraft.server.v1_13_R2.EntityLiving;
import net.minecraft.server.v1_13_R2.EnumBlockFaceShape;
import net.minecraft.server.v1_13_R2.EnumDirection;
import net.minecraft.server.v1_13_R2.IBlockData;
import net.minecraft.server.v1_13_R2.IWorldReader;
import net.minecraft.server.v1_13_R2.MathHelper;
import net.minecraft.server.v1_13_R2.Navigation;
import net.minecraft.server.v1_13_R2.NavigationAbstract;
import net.minecraft.server.v1_13_R2.NavigationFlying;
import net.minecraft.server.v1_13_R2.PathType;
import net.minecraft.server.v1_13_R2.PathfinderGoal;

public class PathfinderGoalFollowTamer extends PathfinderGoal {

	private final EntityInsentient b;
	private EntityLiving c;
	protected final IWorldReader a;
	private final double d;
	private final NavigationAbstract e;
	private int f;
	private final float g;
	private final float h;
	private float i;
	private EntityHuman owner;

	public PathfinderGoalFollowTamer(final EntityInsentient entitytameableanimal, final EntityHuman owner, final double d0, final float f, final float f1) {
		this.b = entitytameableanimal;
		this.a = entitytameableanimal.world;
		this.d = d0;
		this.e = entitytameableanimal.getNavigation();
		this.h = f;
		this.g = f1;
		this.a(3);
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
		if (this.b.h(entityliving) < this.h * this.h) {
			return false;
		}
		this.c = entityliving;
		return true;
	}

	@Override
	public boolean b() {
		return !this.e.p() && this.b.h(this.c) > this.g * this.g;
	}

	@Override
	public void c() {
		this.f = 0;
		this.i = this.b.a(PathType.WATER);
		this.b.a(PathType.WATER, 0.0F);
	}

	@Override
	public void d() {
		this.c = null;
		this.e.q();
		this.b.a(PathType.WATER, this.i);
	}

	@Override
	public void e() {
		this.b.getControllerLook().a(this.c, 10.0F, this.b.K());
		if (--this.f <= 0) {
			this.f = 10;
			if (!this.e.a(this.c, this.d) && !this.b.isLeashed() && !this.b.isPassenger() && this.b.h(this.c) >= 144.0) {
				final int i = MathHelper.floor(this.c.locX) - 2;
                final int j = MathHelper.floor(this.c.locZ) - 2;
                final int k = MathHelper.floor(this.c.getBoundingBox().minY);
                for (int l = 0; l <= 4; ++l) {
                	int i2 = 0;
                	while (i2 <= 4) {
                        if ((l < 1 || i2 < 1 || l > 3 || i2 > 3) && this.a(i, j, k, l, i2)) {
                            final CraftEntity entity = this.b.getBukkitEntity();
                            Location to = new Location(entity.getWorld(), i + l + 0.5F, k, j + i2 + 0.5F, this.b.yaw, this.b.pitch);
                            final EntityTeleportEvent event = new EntityTeleportEvent(entity, entity.getLocation(), to);
                            this.b.world.getServer().getPluginManager().callEvent(event);
                            if (event.isCancelled()) {
                                return;
                            }
                            to = event.getTo();
                            this.b.setPositionRotation(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
                            this.e.q();
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
	
	protected boolean a(final int i, final int j, final int k, final int l, final int i1) {
		final BlockPosition blockposition = new BlockPosition(i + l, k - 1, j + i1);
        final IBlockData iblockdata = this.a.getType(blockposition);
        return iblockdata.c(this.a, blockposition, EnumDirection.DOWN) == EnumBlockFaceShape.SOLID && iblockdata.a(this.b) && this.a.isEmpty(blockposition.up()) && this.a.isEmpty(blockposition.up(2));
    }

}
