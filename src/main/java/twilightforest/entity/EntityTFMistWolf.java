package twilightforest.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIBeg;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITargetNonTamed;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import scala.reflect.internal.Trees.This;
import twilightforest.TFSounds;
import twilightforest.TwilightForestMod;
import twilightforest.entity.ai.EntityAITFMistWolfFollowOwner;
import twilightforest.entity.ai.EntityAITFMistWolfMate;
import twilightforest.entity.ai.EntityAITFMistWolfNearestAttackableTarget;
import twilightforest.entity.ai.EntityAITFMistWolfSit;
import twilightforest.item.TFItems;

public class EntityTFMistWolf extends EntityTFHostileWolf {
	public static final ResourceLocation LOOT_TABLE = TwilightForestMod.prefix("entities/mist_wolf");
	private boolean tamedHostility = false;
	private boolean preventGrowthTeleport = true;

	public EntityTFMistWolf(World world) {
		super(world);
		this.setSize(1.4F, 1.9F);
		setCollarColor(EnumDyeColor.GRAY);
	}

	@Override
	protected void setAttributes() {
		super.setAttributes();

		if (this.isTamed())
		{
			
			this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
			this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
		}
		else
		{
			this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
			this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void initEntityAI() {
		
		this.aiSit = new EntityAITFMistWolfSit(this);
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, this.aiSit);
        this.tasks.addTask(4, new EntityAILeapAtTarget(this, 0.4F));
        this.tasks.addTask(5, new EntityAIAttackMelee(this, 1.0D, true));
        this.tasks.addTask(6, new EntityAITFMistWolfFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(7, new EntityAITFMistWolfMate(this, 1.0D));
        this.tasks.addTask(8, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(9, new EntityAIBeg(this, 8.0F));
        this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(10, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
        this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, true, new Class[0]));
        this.targetTasks.addTask(5, new EntityAITargetNonTamed(this, EntityAnimal.class, false, (Predicate) new Predicate<Entity>()
        {
            public boolean apply(@Nullable Entity p_apply_1_)
            {
                return p_apply_1_ instanceof EntitySheep ||
                		p_apply_1_ instanceof EntityRabbit || 
                		p_apply_1_ instanceof EntityLlama;
            }
        }));
        this.targetTasks.addTask(6, new EntityAINearestAttackableTarget(this, AbstractSkeleton.class, false));
        this.targetTasks.addTask(4, new EntityAITFMistWolfNearestAttackableTarget<>(this, EntityPlayer.class, true));

	}

	@Override
	public boolean attackEntityAsMob(Entity entity) {
		if (super.attackEntityAsMob(entity)) {
			float myBrightness = this.getBrightness();

			if (entity instanceof EntityLivingBase && myBrightness < 0.10F) {
				int effectDuration;
				switch (world.getDifficulty()) {
				case EASY:
					effectDuration = 0;
					break;
				default:
				case NORMAL:
					effectDuration = 7;
					break;
				case HARD:
					effectDuration = 15;
					break;
				}

				if (effectDuration > 0) {
					((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, effectDuration * 20, 0));
				}
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	protected float getSoundPitch() {
		return (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 0.6F;
	}

	@Override
	protected ResourceLocation getLootTable() {
		return LOOT_TABLE;
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		return stack.getItem() instanceof ItemFood && ((ItemFood)stack.getItem()).isWolfsFavoriteMeat();
	}

	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand) {
		ItemStack itemstack = player.getHeldItem(hand);
		
		ItemStack itemstackMain = player.getHeldItem(EnumHand.MAIN_HAND);
		ItemStack itemstackOff = player.getHeldItem(EnumHand.OFF_HAND);

		Item itemMain = itemstackMain.getItem();
		Item itemOff = itemstackOff.getItem();
		
		if (this.isTamed())
		{
			if (!itemstack.isEmpty())
			{
				if (itemstack.getItem() instanceof ItemFood)
				{
					ItemFood itemfood = (ItemFood)itemstack.getItem();

					if (itemfood.isWolfsFavoriteMeat())
					{
						if (this.getHealth() < 20.0F)
						{
							if (!player.capabilities.isCreativeMode)
							{
								itemstack.shrink(1);
							}

							this.heal((float)itemfood.getHealAmount(itemstack));
							return true;
						}
						else if (!this.isOwner(player))
						{
							playSound(TFSounds.MISTWOLF_IDLE, 4F, getSoundPitch());
							return true;
						}
					}
				}
				else if (itemstack.getItem() == Items.DYE)
				{
					if (this.isOwner(player))
					{
						EnumDyeColor enumdyecolor = EnumDyeColor.byDyeDamage(itemstack.getMetadata());
	
						if (enumdyecolor != this.getCollarColor())
						{
							this.setCollarColor(enumdyecolor);
	
							if (!player.capabilities.isCreativeMode)
							{
								itemstack.shrink(1);
							}
						}
					}
					else
					{
						playSound(TFSounds.MISTWOLF_IDLE, 4F, getSoundPitch());
					}
					return true;
				}
			}

			if (itemstackMain.isEmpty() && player.isSneaking())
			{
				if (this.isOwner(player))
				{
					this.startTamedHostility();
				}
				
				return true;
			}
			else if (itemstack.getItem() == Items.NAME_TAG)
			{
				if (!this.isOwner(player))
				{
					playSound(TFSounds.MISTWOLF_IDLE, 4F, getSoundPitch());
					return true;
				}
				else if (hand == EnumHand.OFF_HAND)
				{
					return true;
				}
			}
			else if (this.isOwner(player) && !this.isBreedingItem(itemstack))
			{
				boolean skipSit = false;
				
				if (itemMain instanceof ItemFood)
				{
					ItemFood itemMainFood = (ItemFood)itemMain;
					if (itemMainFood.isWolfsFavoriteMeat())
					{
						skipSit = true;
					}
				}
				
				if (!skipSit)
				{
					this.endTamedHostility();
					if (!this.world.isRemote)
					{
						this.aiSit.setSitting(!this.isSitting());
					}
					this.isJumping = false;
					this.navigator.clearPath();
					this.setAttackTarget((EntityLivingBase)null);
				}
				
				return true;
			}
		}
		else if (itemMain == Items.BONE && 
				(itemOff == TFItems.lifedrain_scepter || 
				itemOff == TFItems.shield_scepter || 
				itemOff == TFItems.twilight_scepter || 
				itemOff == TFItems.zombie_scepter) && !this.isAngry())
		{

			if (!this.world.isRemote)
			{
				if (!player.capabilities.isCreativeMode)
				{
					itemstack.shrink(1);
				}
				
				if (this.rand.nextInt(1000) == 0)
				{
					this.setTamedBy(player);
					this.endTamedHostility();
					this.navigator.clearPath();
					this.setAttackTarget((EntityLivingBase)null);
					this.aiSit.setSitting(true);
					this.setHealth(20.0F);
					this.playTameEffect(true);
					this.world.setEntityState(this, (byte)7);
				}
				else
				{
					this.playTameEffect(false);
					this.world.setEntityState(this, (byte)6);
				}
			}

			return true;
		}
		
		if (itemstackMain.isEmpty())
		{
			return true;
		}
		else if (itemMain == Items.BONE)
		{
			return true;
		}

		return super.processInteract(player, hand);
	}

	private void startTamedHostility () {
		if (!this.world.isRemote)
		{
			this.aiSit.setSitting(false);
		}
		this.setTamedHostile(true);
		this.playTameEffect(false);
	}
	
	private void endTamedHostility () {
		this.setTamedHostile(false);
	}

	@Override
	public EntityWolf createChild(EntityAgeable entityanimal) {

		EntityTFMistWolf entityMistWolf = new EntityTFMistWolf(this.world);
		UUID uuid = this.getOwnerId();

		if (uuid != null)
		{
			entityMistWolf.setOwnerId(uuid);
			entityMistWolf.setTamed(true);
		}

		return entityMistWolf;
	}
	
	@Override
	protected void onGrowingAdult()
    {
        if (!this.inWater && !this.onGround)
        {
            this.setPreventGrowthTeleport(true);
        }

        super.onGrowingAdult();
    }

	@Override
	public boolean canMateWith(EntityAnimal otherAnimal)
	{
		if (otherAnimal == this)
		{
			return false;
		}
		else if (otherAnimal.getClass() != this.getClass())
		{
			return false;
		}
		else
		{
			EntityTFMistWolf otherMistWolf = (EntityTFMistWolf)otherAnimal;
			return this.isInLove(true) && otherMistWolf.isInLove(true);
		}
	}
	
	@Override
	public boolean isInLove()
	{
		return false;
	}
	
	public boolean isInLove(boolean isMistWolf)
	{
		return isMistWolf ? super.isInLove() : false;
	}
	
	@Override
	public void setTamed(boolean tamed)
	{
		super.setTamed(tamed);

		if (tamed)
		{
			this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
			this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
			if (this.getHealth()>this.getMaxHealth())
			{
				this.setHealth(this.getMaxHealth());
			}
		}
		else
		{
			this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
			this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
		}

	}
	
	public boolean getPreventGrowthTeleport ()
	{
		return this.preventGrowthTeleport;
	}
	
	public void setPreventGrowthTeleport (boolean preventGrowthTeleport)
	{
		this.preventGrowthTeleport = preventGrowthTeleport;
	}
	
	public boolean isTamedHostile ()
	{
		return this.tamedHostility;
	}
	
	public void setTamedHostile (boolean tamedHostileState)
	{
		this.tamedHostility = tamedHostileState;
	}
	
    @Override
    public void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);

        compound.setBoolean("TamedHostile", this.isTamedHostile());
    }
    
    @Override
    public void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);
        
        this.setTamedHostile(compound.getBoolean("TamedHostile"));
    }
	
	
	class AIAvoidEntity<T extends Entity> extends EntityAIAvoidEntity<T>
    {
        private final EntityWolf wolf;

        public AIAvoidEntity(EntityWolf wolfIn, Class<T> p_i47251_3_, float p_i47251_4_, double p_i47251_5_, double p_i47251_7_)
        {
            super(wolfIn, p_i47251_3_, p_i47251_4_, p_i47251_5_, p_i47251_7_);
            this.wolf = wolfIn;
        }

        public boolean shouldExecute()
        {
            if (super.shouldExecute() && this.closestLivingEntity instanceof EntityLlama)
            {
                return !this.wolf.isTamed() && this.avoidLlama((EntityLlama)this.closestLivingEntity);
            }
            else
            {
                return false;
            }
        }

        private boolean avoidLlama(EntityLlama p_190854_1_)
        {
            return p_190854_1_.getStrength() >= EntityTFMistWolf.this.rand.nextInt(5);
        }

        public void startExecuting()
        {
        	EntityTFMistWolf.this.setAttackTarget((EntityLivingBase)null);
            super.startExecuting();
        }

        public void updateTask()
        {
        	EntityTFMistWolf.this.setAttackTarget((EntityLivingBase)null);
            super.updateTask();
        }
        
    }

}


