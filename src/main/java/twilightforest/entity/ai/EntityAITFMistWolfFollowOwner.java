package twilightforest.entity.ai;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import twilightforest.entity.EntityTFMistWolf;

public class EntityAITFMistWolfFollowOwner extends EntityAIBase
{
    private final EntityTFMistWolf mistWolf;
    private EntityLivingBase owner;
    World world;
    private final double followSpeed;
    private final PathNavigate petPathfinder;
    private int timeToRecalcPath;
    float maxDist;
    float minDist;
    private float oldWaterCost;

    public EntityAITFMistWolfFollowOwner(EntityTFMistWolf mistWolfIn, double followSpeedIn, float minDistIn, float maxDistIn)
    {
        this.mistWolf = mistWolfIn;
        this.world = mistWolfIn.world;
        this.followSpeed = followSpeedIn;
        this.petPathfinder = mistWolfIn.getNavigator();
        this.minDist = minDistIn;
        this.maxDist = maxDistIn;
        this.setMutexBits(3);

        if (!(mistWolfIn.getNavigator() instanceof PathNavigateGround) && !(mistWolfIn.getNavigator() instanceof PathNavigateFlying))
        {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        EntityLivingBase entitylivingbase = this.mistWolf.getOwner();
        
		if (this.mistWolf.getPreventGrowthTeleport() && (this.mistWolf.onGround || this.mistWolf.isInWater()))
		{
			this.mistWolf.setPreventGrowthTeleport(false);
		}
		
        if (entitylivingbase == null)
        {
            return false;
        }
        else if (entitylivingbase instanceof EntityPlayer && ((EntityPlayer)entitylivingbase).isSpectator())
        {
            return false;
        }
        else if (this.mistWolf.isSitting())
        {
            return false;
        }
        else if (this.mistWolf.isTamedHostile())
        {
        	return false;
        }
        else if (this.mistWolf.getDistanceSq(entitylivingbase) < (double)(this.minDist * this.minDist))
        {
            return false;
        }
        else
        {    		
            this.owner = entitylivingbase;
            return true;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean shouldContinueExecuting()
    {
        return !this.petPathfinder.noPath() && this.mistWolf.getDistanceSq(this.owner) > (double)(this.maxDist * this.maxDist) && !this.mistWolf.isSitting() && !this.mistWolf.isTamedHostile() && !this.mistWolf.getPreventGrowthTeleport();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.mistWolf.getPathPriority(PathNodeType.WATER);
        this.mistWolf.setPathPriority(PathNodeType.WATER, 0.0F);
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void resetTask()
    {
        this.owner = null;
        this.petPathfinder.clearPath();
        this.mistWolf.setPathPriority(PathNodeType.WATER, this.oldWaterCost);
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void updateTask()
    {
        this.mistWolf.getLookHelper().setLookPositionWithEntity(this.owner, 10.0F, (float)this.mistWolf.getVerticalFaceSpeed());

        if (!this.mistWolf.isSitting() && !this.mistWolf.isTamedHostile() && !this.mistWolf.getPreventGrowthTeleport())
        {
            if (--this.timeToRecalcPath <= 0)
            {
                this.timeToRecalcPath = 10;

                if (!this.petPathfinder.tryMoveToEntityLiving(this.owner, this.followSpeed))
                {
                    if (!this.mistWolf.getLeashed() && !this.mistWolf.isRiding())
                    {
                        if (this.mistWolf.getDistanceSq(this.owner) >= 144.0D)
                        {
                            int i = MathHelper.floor(this.owner.posX) - 2;
                            int j = MathHelper.floor(this.owner.posZ) - 2;
                            int k = MathHelper.floor(this.owner.getEntityBoundingBox().minY);

                            for (int l = 1; l <= 4; ++l)
                            {
                                for (int i1 = 1; i1 <= 4; ++i1)
                                {
                                    if ((l == 1 || i1 == 1 || l == 4 || i1 == 4) && this.isTeleportFriendlyBlock(i, j, k, l, i1))
                                    {
                                    	this.mistWolf.setLocationAndAngles((double)((float)(i + l)), (double)k, (double)((float)(j + i1)), this.mistWolf.rotationYaw, this.mistWolf.rotationPitch);
                                    	this.petPathfinder.clearPath();
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected boolean isTeleportFriendlyBlock(int x, int z, int y, int xOffset, int zOffset)
    {
        BlockPos blockpos1 = new BlockPos(x + xOffset, y - 1, z + zOffset);
        BlockPos blockpos2 = blockpos1.add(-1.0D, 0.0D, 0.0D);
        BlockPos blockpos3 = blockpos1.add(0.0D, 0.0D, -1.0D);
        BlockPos blockpos4 = blockpos1.add(-1.0D, 0.0D, -1.0D);
        
        IBlockState iblockstate1 = this.world.getBlockState(blockpos1);
        IBlockState iblockstate2 = this.world.getBlockState(blockpos2);
        IBlockState iblockstate3 = this.world.getBlockState(blockpos3);
        IBlockState iblockstate4 = this.world.getBlockState(blockpos4);
        
        int solidGround = 0;
        if (iblockstate1.getBlockFaceShape(this.world, blockpos1, EnumFacing.DOWN) == BlockFaceShape.SOLID)
        {
        	solidGround++;
        }
        if (iblockstate2.getBlockFaceShape(this.world, blockpos1, EnumFacing.DOWN) == BlockFaceShape.SOLID)
        {
        	solidGround++;
        }
        if (iblockstate3.getBlockFaceShape(this.world, blockpos1, EnumFacing.DOWN) == BlockFaceShape.SOLID)
        {
        	solidGround++;
        }
        if (iblockstate4.getBlockFaceShape(this.world, blockpos1, EnumFacing.DOWN) == BlockFaceShape.SOLID)
        {
        	solidGround++;
        }    
        if (solidGround < 3)
        {
        	return false;
        }
        
        boolean canSpawn;
        canSpawn = 
    			iblockstate1.canEntitySpawn(this.mistWolf) && iblockstate2.canEntitySpawn(this.mistWolf) &&
    			iblockstate3.canEntitySpawn(this.mistWolf) && iblockstate4.canEntitySpawn(this.mistWolf);
        if (!canSpawn)
        {
        	return false;
        }
        
        boolean isSolidSpace;
        isSolidSpace =
        		this.world.getBlockState(blockpos1.up()).getMaterial().isSolid() || this.world.getBlockState(blockpos1.up(2)).getMaterial().isSolid() ||
        		this.world.getBlockState(blockpos2.up()).getMaterial().isSolid() || this.world.getBlockState(blockpos2.up(2)).getMaterial().isSolid() || 
        		this.world.getBlockState(blockpos3.up()).getMaterial().isSolid() || this.world.getBlockState(blockpos3.up(2)).getMaterial().isSolid() || 
        		this.world.getBlockState(blockpos4.up()).getMaterial().isSolid() || this.world.getBlockState(blockpos4.up(2)).getMaterial().isSolid();
        if (isSolidSpace)
        {
        	return false;
        }
        
        boolean isLiquidSpace;
        isLiquidSpace =
        	this.world.getBlockState(blockpos1.up()).getMaterial().isLiquid() || this.world.getBlockState(blockpos1.up(2)).getMaterial().isLiquid() ||
        	this.world.getBlockState(blockpos2.up()).getMaterial().isLiquid() || this.world.getBlockState(blockpos2.up(2)).getMaterial().isLiquid() || 
        	this.world.getBlockState(blockpos3.up()).getMaterial().isLiquid() || this.world.getBlockState(blockpos3.up(2)).getMaterial().isLiquid() || 
        	this.world.getBlockState(blockpos4.up()).getMaterial().isLiquid() || this.world.getBlockState(blockpos4.up(2)).getMaterial().isLiquid();
        if (isLiquidSpace)
        {
        	return false;
        }
        
        boolean isSafeSpace;
        isSafeSpace =
        	this.world.getBlockState(blockpos1.up()).getMaterial() != Material.FIRE && this.world.getBlockState(blockpos1.up(2)).getMaterial() != Material.FIRE && 
        	this.world.getBlockState(blockpos2.up()).getMaterial() != Material.FIRE && this.world.getBlockState(blockpos2.up(2)).getMaterial() != Material.FIRE && 
        	this.world.getBlockState(blockpos3.up()).getMaterial() != Material.FIRE && this.world.getBlockState(blockpos3.up(2)).getMaterial() != Material.FIRE && 
        	this.world.getBlockState(blockpos4.up()).getMaterial() != Material.FIRE && this.world.getBlockState(blockpos4.up(2)).getMaterial() != Material.FIRE;
        		
        return isSafeSpace;
    }
}