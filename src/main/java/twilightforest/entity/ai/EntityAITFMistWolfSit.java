package twilightforest.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAISit;
import twilightforest.entity.EntityTFMistWolf;

public class EntityAITFMistWolfSit extends EntityAISit
{
    private final EntityTFMistWolf mistWolf;
    /** If the EntityTameable is sitting. */
    private boolean isSitting;

    public EntityAITFMistWolfSit(EntityTFMistWolf mistWolfIn)
    {
		super(mistWolfIn);
		this.mistWolf = mistWolfIn;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute()
    {
        if (!this.mistWolf.isTamed())
        {
            return false;
        }
        else if (this.mistWolf.isTamedHostile())
        {
        	return false;
        }
        else if (this.mistWolf.isInWater())
        {
            return false;
        }
        else if (!this.mistWolf.onGround)
        {
            return false;
        }
        else
        {
            EntityLivingBase entitylivingbase = this.mistWolf.getOwner();

            if (entitylivingbase == null)
            {
                return true;
            }
            else
            {
                return this.mistWolf.getDistanceSq(entitylivingbase) < 144.0D && entitylivingbase.getRevengeTarget() != null ? false : this.isSitting;
            }
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting()
    {
        this.mistWolf.getNavigator().clearPath();
        this.mistWolf.setSitting(true);
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    @Override
    public void resetTask()
    {
        this.mistWolf.setSitting(false);
    }

    /**
     * Sets the sitting flag.
     */
    @Override
    public void setSitting(boolean sitting)
    {
        this.isSitting = sitting;
    }
}