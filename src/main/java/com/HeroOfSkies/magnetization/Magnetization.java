package com.HeroOfSkies.magnetization;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;

@Mod(modid = "magnetization", name = "Magnetization", version = "1.0", acceptedMinecraftVersions = "1.7.10")
public class Magnetization
{
	public static Item heroMagnet;
	public static CreativeTabs magnetizationModCreativeTab = new CreativeTabs("magnetizationModCreativeTab")
	{
		@Override
		public Item getTabIconItem()
		{
			return new ItemStack(heroMagnet).getItem();
		}
	};
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		//Item/Block init and registering
		//Config handling
		MagnetizationConfig.init(event.getSuggestedConfigurationFile());
		heroMagnet = new ItemMagnet();
		GameRegistry.registerItem(heroMagnet, heroMagnet.getUnlocalizedName().substring(5)); //5 to remove item. from the name
		HeroMagnetEventHandler eventHandler = new HeroMagnetEventHandler();
		MinecraftForge.EVENT_BUS.register(eventHandler);
		FMLCommonHandler.instance().bus().register(eventHandler);
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		//Proxy, TileEntity, Entity, GUI and Packet Registering
		Object heroMagnetRecipe[] = new Object[]
		{
			"o o",
			"r r",
			"rrr",
			'o', Blocks.obsidian,
			'r', Items.redstone
		};
		GameRegistry.addRecipe(new ItemStack(heroMagnet, 1), heroMagnetRecipe);
		
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		
	}
}
