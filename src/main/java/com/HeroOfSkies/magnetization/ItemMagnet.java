package com.HeroOfSkies.magnetization;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public class ItemMagnet extends Item
{
	private static final String TAG_ENABLED = "Enabled";

	public ItemMagnet()
	{
		setUnlocalizedName("HeroMagnet"); //item.ItemHeroMagnet.name
		setTextureName("magnetization:Magnet");
		setMaxStackSize(1);
		setCreativeTab(Magnetization.magnetizationModCreativeTab);
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced)
	{
		//display the range and says shift+right click to toggle
		list.add(EnumChatFormatting.AQUA + "Shift+Right Click to toggle");
		list.add(EnumChatFormatting.GREEN + "Radius: " + EnumChatFormatting.GRAY + MagnetizationConfig.magnetRangeInMeters + " meters");
		list.add(EnumChatFormatting.GREEN + "Wait time for items dropped by player: " + EnumChatFormatting.GRAY + MagnetizationConfig.itemPickupCooldownOnPlayerDropInSeconds + " seconds");
	}
	
	@Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
		if (world.isRemote)
		{
			return stack;
		}
		
		if (player.isSneaking()) //toggle the magnet
		{
			toggleEnabled(stack);
			player.addChatMessage(new ChatComponentText(isEnabled(stack) ? "Magnet enabled" : "Magnet disabled"));
		}
		
		return stack;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack, int pass)
	{
	    return isEnabled(stack);
	}

	
	public static boolean isEnabled(ItemStack stack)
	{
	    if (!stack.hasTagCompound())
	    {
	    	return false;
	    }
	    
	    return stack.getTagCompound().getBoolean(TAG_ENABLED);
	}

	public static void setEnabled(ItemStack stack, boolean enabled)
	{
	    if (!stack.hasTagCompound())
	    {
	        stack.setTagCompound(new NBTTagCompound());
	    }
	    
	    stack.getTagCompound().setBoolean(TAG_ENABLED, enabled);
	}

	public static void toggleEnabled(ItemStack stack)
	{
	    setEnabled(stack, !isEnabled(stack));
	}
}
