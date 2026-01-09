package com.HeroOfSkies.magnetization;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class MagnetizationConfig
{
	public static Configuration config;
	
	//Configurable values
	public static double magnetRangeInMeters;
	public static final double MIN_MAGNET_RANGE = 1.0;
	public static final double MAX_MAGNET_RANGE = 64.0;
	public static final double DEFAULT_MAGNET_RANGE = 6.0;
	
	public static double itemPickupCooldownOnPlayerDropInSeconds;
	public static final double MIN_ITEM_PICKUP_COOLDOWN = 0.0;
	public static final double MAX_ITEM_PICKUP_COOLDOWN = 64.0;
	public static final double DEFAULT_ITEM_PICKUP_COOLDOWN = 2.0;
	
	public static void init(File configFile)
	{
		config = new Configuration(configFile);
		
		try
		{
			config.load();
			magnetRangeInMeters = config.getFloat
			(
				"Magnet range", //name
				"magnet", //category
				(float)DEFAULT_MAGNET_RANGE,
				(float)MIN_MAGNET_RANGE,
				(float)MAX_MAGNET_RANGE,
				"Range of the magnet in meters" //comment
			);
			
			itemPickupCooldownOnPlayerDropInSeconds = config.getFloat
			(
				"Item pickup cooldown", //name,
				"magnet", //category
				(float)DEFAULT_ITEM_PICKUP_COOLDOWN,
				(float)MIN_ITEM_PICKUP_COOLDOWN,
				(float)MAX_ITEM_PICKUP_COOLDOWN,
				"Seconds necessary for the items to get picked up if they are dropped by the player" //comment
			);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (config.hasChanged())
			{
				config.save();
			}
		}
	}
}
