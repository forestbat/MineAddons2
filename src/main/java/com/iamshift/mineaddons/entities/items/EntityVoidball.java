package com.iamshift.mineaddons.entities.items;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityVoidball extends Entity
{
	public EntityLivingBase shootingEntity;
    private int ticksAlive;
    private int ticksInAir;
    public double accelerationX;
    public double accelerationY;
    public double accelerationZ;
    
	public EntityVoidball(World worldIn)
	{
		super(worldIn);
		this.setSize(0.3125F, 0.3125F);
	}
	
	public EntityVoidball(World worldIn, double x, double y, double z, double accelX, double accelY, double accelZ)
    {
        super(worldIn);
        this.setSize(0.3125F, 0.3125F);
        this.setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch);
        this.setPosition(x, y, z);
        double d0 = (double)MathHelper.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ);
        this.accelerationX = accelX / d0 * 0.1D;
        this.accelerationY = accelY / d0 * 0.1D;
        this.accelerationZ = accelZ / d0 * 0.1D;
    }

	public EntityVoidball(World worldIn, EntityLivingBase shooter, double accelX, double accelY, double accelZ)
    {
        super(worldIn);
        this.shootingEntity = shooter;
        this.setSize(0.3125F, 0.3125F);
        this.setLocationAndAngles(shooter.posX, shooter.posY, shooter.posZ, shooter.rotationYaw, shooter.rotationPitch);
        this.setPosition(this.posX, this.posY, this.posZ);
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        accelX = accelX + this.rand.nextGaussian() * 0.4D;
        accelY = accelY + this.rand.nextGaussian() * 0.4D;
        accelZ = accelZ + this.rand.nextGaussian() * 0.4D;
        double d0 = (double)MathHelper.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ);
        this.accelerationX = accelX / d0 * 0.1D;
        this.accelerationY = accelY / d0 * 0.1D;
        this.accelerationZ = accelZ / d0 * 0.1D;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance)
	{
		double d0 = this.getEntityBoundingBox().getAverageEdgeLength() * 4.0D;

        if (Double.isNaN(d0))
        {
            d0 = 4.0D;
        }

        d0 = d0 * 64.0D;
        return distance < d0 * d0;
	}
	
	@Override
	protected void entityInit() {}
	
	@Override
	public boolean isBurning()
	{
		return false;
	}
	
	@Override
	public boolean canBeCollidedWith()
	{
		return true;
	}
	
	@Override
	public float getCollisionBorderSize()
	{
		return 1.0F;
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount)
	{
		return false;
	}
	
	@Override
	public void onUpdate()
	{
		if (this.world.isRemote || (this.shootingEntity == null || !this.shootingEntity.isDead) && this.world.isBlockLoaded(new BlockPos(this)))
        {
            super.onUpdate();

            ++this.ticksInAir;
            RayTraceResult raytraceresult = ProjectileHelper.forwardsRaycast(this, true, this.ticksInAir >= 25, this.shootingEntity);

            if (raytraceresult != null && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult))
            {
                this.onImpact(raytraceresult);
            }

            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;
            ProjectileHelper.rotateTowardsMovement(this, 0.2F);
            float f = 0.95F;

            if (this.isInWater())
            {
                for (int i = 0; i < 4; ++i)
                {
                    float f1 = 0.25F;
                    this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * 0.25D, this.posY - this.motionY * 0.25D, this.posZ - this.motionZ * 0.25D, this.motionX, this.motionY, this.motionZ);
                }

                f = 0.8F;
            }

            this.motionX += this.accelerationX;
            this.motionY += this.accelerationY;
            this.motionZ += this.accelerationZ;
            this.motionX *= (double)f;
            this.motionY *= (double)f;
            this.motionZ *= (double)f;
            this.world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
            this.setPosition(this.posX, this.posY, this.posZ);
        }
        else
        {
            this.setDead();
        }
	}
	
	protected void onImpact(RayTraceResult result)
	{
		if (!this.world.isRemote)
		{
			if (result.entityHit != null)
			{
				if (this.shootingEntity != null)
				{
					if (result.entityHit.attackEntityFrom(DamageSource.causeMobDamage(this.shootingEntity), 8.0F))
					{
						if (result.entityHit.isEntityAlive())
						{
							this.applyEnchantments(this.shootingEntity, result.entityHit);
						}
						else
						{
							this.shootingEntity.heal(5.0F);
						}
					}
				}
				else
				{
					result.entityHit.attackEntityFrom(DamageSource.MAGIC, 5.0F);
				}

				if (result.entityHit instanceof EntityLivingBase)
				{
					int i = 1;

					if (this.world.getDifficulty() == EnumDifficulty.NORMAL)
					{
						i = 10;
					}
					else if (this.world.getDifficulty() == EnumDifficulty.HARD)
					{
						i = 40;
					}

					((EntityLivingBase)result.entityHit).addPotionEffect(new PotionEffect(MobEffects.WITHER, 40 * i, 1));
				}
			}

			this.setDead();
		}
	}
	
	@Override
	protected void writeEntityToNBT(NBTTagCompound compound)
	{
		compound.setTag("direction", this.newDoubleNBTList(new double[] {this.motionX, this.motionY, this.motionZ}));
        compound.setTag("power", this.newDoubleNBTList(new double[] {this.accelerationX, this.accelerationY, this.accelerationZ}));
        compound.setInteger("life", this.ticksAlive);
	}
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound compound)
	{
		if (compound.hasKey("power", 9))
        {
            NBTTagList nbttaglist = compound.getTagList("power", 6);

            if (nbttaglist.tagCount() == 3)
            {
                this.accelerationX = nbttaglist.getDoubleAt(0);
                this.accelerationY = nbttaglist.getDoubleAt(1);
                this.accelerationZ = nbttaglist.getDoubleAt(2);
            }
        }

        this.ticksAlive = compound.getInteger("life");

        if (compound.hasKey("direction", 9) && compound.getTagList("direction", 6).tagCount() == 3)
        {
            NBTTagList nbttaglist1 = compound.getTagList("direction", 6);
            this.motionX = nbttaglist1.getDoubleAt(0);
            this.motionY = nbttaglist1.getDoubleAt(1);
            this.motionZ = nbttaglist1.getDoubleAt(2);
        }
        else
        {
            this.setDead();
        }
	}
	
	@Override
	public float getBrightness()
	{
		return 1.0F;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender()
	{
		return 15728880;
	}
}
