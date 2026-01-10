package com.HeroOfSkies.magnetization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.item.ItemTossEvent;

public class HeroMagnetEventHandler
{
	private class Pickupable
	{
		public EntityItem item; //The item itself
		public int targetTick = -1; //The tick we will put the item into the player's inventory if possible
	}
	
	private final int TICKS_PER_SECOND = 20;
	private final String PLAYER_DROP_TAG = "DroppedByPlayer";

	private int itemDetectedTick = -1, pullTargetTick = -1;
	private List<Pickupable> toPickup = new ArrayList<Pickupable>();
	private boolean magnetEnableStateOnPreviousTick = false;
	
	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		if (event.phase != TickEvent.Phase.END)
		{
			return;
		}
		
		EntityPlayer player = event.player;
		if (player.worldObj.isRemote) //If call is from client, skip it (we are dealing with NBT data)
		{
			return;
		}
		
		if (!player.inventory.hasItem(Magnetization.heroMagnet))
		{
			return;
		}
		
		ItemStack magnet = findMagnetInPlayerInventory(player);
		if (magnet == null)
		{
			//Since by this point the player has the magnet in the inventory, this shouldn't be null
			//but we are checking just in case
			return;
		}
		if (magnetEnableStateOnPreviousTick != ItemMagnet.isEnabled(magnet))
		{
			float pitch = ItemMagnet.isEnabled(magnet) ? 1.2F : 0.8F;
			player.worldObj.playSoundAtEntity(player, "random.orb", 0.5f, pitch);
			magnetEnableStateOnPreviousTick = ItemMagnet.isEnabled(magnet);
		}
		if (!ItemMagnet.isEnabled(magnet) || player.isSneaking())
		{
			return;
		}
		
		//Picking up items need extra setup because the player might drop items and they need to be collected on a time delay
		List<EntityItem> items = player.worldObj.getEntitiesWithinAABB(EntityItem.class, player.boundingBox.expand(MagnetizationConfig.magnetRangeInMeters, MagnetizationConfig.magnetRangeInMeters, MagnetizationConfig.magnetRangeInMeters));
		//Update the list with the currently existing items
		for (EntityItem item : items)
		{
			if (!item.getEntityData().getBoolean(PLAYER_DROP_TAG)) //We need a different delay if the player dropped it, so skip adding it here
			{
				registerPickupableItemIfUnregistered(item, player, 0.0); //pick it up with no delay
			}
		}
		pickupItems(player);
		
		//XP orbs can be picked up much simpler because player cannot drop them and we can immediately collect every one of them
		pickupXPOrbs(player);
	}
	
	@SubscribeEvent
	public void onItemToss(ItemTossEvent event)
	{	
		EntityPlayer player = event.player;
		EntityItem tossedItem = event.entityItem;
		if (player.worldObj.isRemote) //If we are on the client, skip it (we are going to change NBT data)
		{
			return;
		}
		
		tossedItem.getEntityData().setBoolean(PLAYER_DROP_TAG, true);
		registerPickupableItemIfUnregistered(tossedItem, event.player, MagnetizationConfig.itemPickupCooldownOnPlayerDropInSeconds);
	}
	
	private void pickupXPOrbs(EntityPlayer player)
	{
		World world = player.worldObj;
		List<EntityXPOrb> orbs = world.getEntitiesWithinAABB(EntityXPOrb.class, player.boundingBox.expand(MagnetizationConfig.magnetRangeInMeters, MagnetizationConfig.magnetRangeInMeters, MagnetizationConfig.magnetRangeInMeters));
		int oldLevel = player.experienceLevel;
		for (EntityXPOrb orb : orbs)
		{
			if (orb.isDead)
			{
				continue;
			}
			player.addExperience(orb.getXpValue());
			orb.setDead();
			world.playSoundAtEntity(player, "random.orb", 0.1f, 0.5f + world.rand.nextFloat() * 0.5f);
			if (player.experienceLevel > oldLevel)
			{
				world.playSoundAtEntity(player, "random.levelup", 0.75f, 1.0f);
				oldLevel = player.experienceLevel;
			}
		}
	}
	
	private void pickupItems(EntityPlayer player)
	{
		List<Integer> indicesToRemove = new ArrayList<Integer>();
		//Process all the pickupables
		int index = -1; //Start from -1 because in the first iteration, first thing we will increase this by 1, which should make it 0 to match the first element
		for (Pickupable pickupable : toPickup)
		{
			index++; //Set the index to the current item
			//Wait until the time to pickup the item has come
			if (player.ticksExisted < pickupable.targetTick)
			{
				continue;
			}
			
			//Check if item is still in range
			if (getItemDistanceToPlayer(pickupable.item, player) > MagnetizationConfig.magnetRangeInMeters)
			{
				continue;
			}
			
			EntityItem item = pickupable.item;
			if (item.isDead)
			{
				indicesToRemove.add(index);
				continue;
			}
			
			ItemStack stack = item.getEntityItem();
			if (stack == null)
			{
				continue;
			}
			if (!player.worldObj.isRemote) //do it only if executing from server
			{
				item.delayBeforeCanPickup = 0;
				item.setPosition(player.posX, player.posY, player.posZ);
				//At this point the player might not have picked up the item (inventory full or for some other reason)
				//so don't update indices to remove. If it is picked up, it will be set dead and it will be added to
				//indices to remove on the next iteration of the loop.
			}
		}
		
		indicesToRemove.sort(Collections.reverseOrder()); //Sort the indices from max to min (descending) so that there won't be index shifting issues
		for (int idx : indicesToRemove)
		{
			toPickup.remove(idx);
		}
	}
	
	private void registerPickupableItemIfUnregistered(EntityItem item, EntityPlayer player, double pickupDelayInSeconds)
	{
		if (!pickupableAlreadyDetected(item))
		{
			Pickupable pickupable = new Pickupable();
			pickupable.item = item;
			pickupable.targetTick = player.ticksExisted + (int)(pickupDelayInSeconds * TICKS_PER_SECOND);
			toPickup.add(pickupable);
		}
	}
	
	private double getItemDistanceToPlayer(EntityItem item, EntityPlayer player)
	{
		double dX = Math.abs(player.posX - item.posX);
		double dY = Math.abs(player.posY - item.posY);
		double dZ = Math.abs(player.posZ - item.posZ);
		double magnitude = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
		return magnitude;
	}
	
	private boolean pickupableAlreadyDetected(EntityItem item)
	{
		for (Pickupable pickupable : toPickup)
		{
			if (pickupable.item.getEntityId() == item.getEntityId())
			{
				return true;
			}
		}
		return false;
	}
	
	private ItemStack findMagnetInPlayerInventory(EntityPlayer player)
	{
		for (ItemStack stack : player.inventory.mainInventory)
		{
	        if (stack != null && stack.getItem() == Magnetization.heroMagnet)
	        {
	            return stack;
	        }
	    }
	    return null;
	}
}
