package twilightforest.entity.ai;

import java.util.List;
import java.util.Random;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import twilightforest.entity.EntityTFMistWolf;

public class EntityAITFMistWolfMate extends EntityAIBase
{
    private final EntityTFMistWolf mistWolf;
    
    private final Class <? extends EntityAnimal > mateClass;
    World world;
    private EntityTFMistWolf targetMistWolf;

    /**
     * Delay preventing a baby from spawning immediately when two mate-able animals find each other.
     */
    int spawnBabyDelay;

    /** The speed the creature moves at during mating behavior. */
    double moveSpeed;

    public EntityAITFMistWolfMate(EntityTFMistWolf mistWolf, double speedIn)
    {
        this(mistWolf, speedIn, mistWolf.getClass());
    }

    public EntityAITFMistWolfMate(EntityTFMistWolf p_i47306_1_, double p_i47306_2_, Class <? extends EntityAnimal > p_i47306_4_)
    {
        this.mistWolf = p_i47306_1_;
        this.world = p_i47306_1_.world;
        this.mateClass = p_i47306_4_;
        this.moveSpeed = p_i47306_2_;
        this.setMutexBits(3);
        
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if (!this.mistWolf.isInLove(true))
        {
            return false;
        }
        else if (!this.mistWolf.isTamed())
        {
        	return false;
        }
        else
        {
            this.targetMistWolf = (EntityTFMistWolf)this.getNearbyMate();
            return this.targetMistWolf != null;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean shouldContinueExecuting()
    {
        return this.targetMistWolf.isEntityAlive() && this.targetMistWolf.isInLove(true) && this.spawnBabyDelay < 60;
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void resetTask()
    {
        this.targetMistWolf = null;
        this.spawnBabyDelay = 0;
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void updateTask()
    {
        this.mistWolf.getLookHelper().setLookPositionWithEntity(this.targetMistWolf, 10.0F, (float)this.mistWolf.getVerticalFaceSpeed());
        this.mistWolf.getNavigator().tryMoveToEntityLiving(this.targetMistWolf, this.moveSpeed);
        ++this.spawnBabyDelay;

        if (this.spawnBabyDelay >= 60 && this.mistWolf.getDistanceSq(this.targetMistWolf) < 9.0D)
        {
            this.spawnBaby();
        }
    }

    /**
     * Loops through nearby animals and finds another animal of the same type that can be mated with. Returns the first
     * valid mate found.
     */
    private EntityAnimal getNearbyMate()
    {
        List<EntityAnimal> list = this.world.<EntityAnimal>getEntitiesWithinAABB(this.mateClass, this.mistWolf.getEntityBoundingBox().grow(8.0D));
        double d0 = Double.MAX_VALUE;
        EntityAnimal entityanimal = null;

        for (EntityAnimal entityanimal1 : list)
        {
            if (this.mistWolf.canMateWith(entityanimal1) && this.mistWolf.getDistanceSq(entityanimal1) < d0)
            {
                entityanimal = entityanimal1;
                d0 = this.mistWolf.getDistanceSq(entityanimal1);
            }
        }

        return entityanimal;
    }

    /**
     * Spawns a baby animal of the same type.
     */
    private void spawnBaby()
    {
        EntityAgeable entityageable = this.mistWolf.createChild(this.targetMistWolf);

        if (entityageable != null)
        {
            EntityPlayerMP entityplayermp = this.mistWolf.getLoveCause();

            if (entityplayermp == null && this.targetMistWolf.getLoveCause() != null)
            {
                entityplayermp = this.targetMistWolf.getLoveCause();
            }

            if (entityplayermp != null)
            {
                entityplayermp.addStat(StatList.ANIMALS_BRED);
                CriteriaTriggers.BRED_ANIMALS.trigger(entityplayermp, this.mistWolf, this.targetMistWolf, entityageable);
            }

            this.mistWolf.setGrowingAge(6000);
            this.targetMistWolf.setGrowingAge(6000);
            this.mistWolf.resetInLove();
            this.targetMistWolf.resetInLove();
            entityageable.setGrowingAge(-24000);
            entityageable.setLocationAndAngles(this.mistWolf.posX, this.mistWolf.posY, this.mistWolf.posZ, 0.0F, 0.0F);
            this.world.spawnEntity(entityageable);
            Random random = this.mistWolf.getRNG();

            for (int i = 0; i < 7; ++i)
            {
                double d0 = random.nextGaussian() * 0.02D;
                double d1 = random.nextGaussian() * 0.02D;
                double d2 = random.nextGaussian() * 0.02D;
                double d3 = random.nextDouble() * (double)this.mistWolf.width * 2.0D - (double)this.mistWolf.width;
                double d4 = 0.5D + random.nextDouble() * (double)this.mistWolf.height;
                double d5 = random.nextDouble() * (double)this.mistWolf.width * 2.0D - (double)this.mistWolf.width;
                this.world.spawnParticle(EnumParticleTypes.HEART, this.mistWolf.posX + d3, this.mistWolf.posY + d4, this.mistWolf.posZ + d5, d0, d1, d2);
            }

            if (this.world.getGameRules().getBoolean("doMobLoot"))
            {
                this.world.spawnEntity(new EntityXPOrb(this.world, this.mistWolf.posX, this.mistWolf.posY, this.mistWolf.posZ, random.nextInt(7) + 1));
            }
        }
    }
}