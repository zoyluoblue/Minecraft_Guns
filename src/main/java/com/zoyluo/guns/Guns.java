package com.zoyluo.guns;

import com.zoyluo.guns.entity.GrenadeProjectileEntity;
import com.zoyluo.guns.network.GunsNetworking;
import com.zoyluo.guns.network.SniperRifleServer;
import com.zoyluo.guns.registry.ModEntities;
import com.zoyluo.guns.registry.ModItemGroups;
import com.zoyluo.guns.registry.ModItems;
import com.zoyluo.guns.registry.ModRecipes;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;

public class Guns implements ModInitializer {
	public static final String MOD_ID = "guns";

	// Compatibility aliases retained for existing integrations and mixins.
	public static final Item SNIPER_RIFLE = ModItems.SNIPER_RIFLE;
	public static final Item SHOTGUN = ModItems.SHOTGUN;
	public static final Item GRENADE_LAUNCHER = ModItems.GRENADE_LAUNCHER;
	public static final Item SMG = ModItems.SMG;
	public static final Item FLAMETHROWER = ModItems.FLAMETHROWER;
	public static final Item RAILGUN = ModItems.RAILGUN;
	public static final Item GRENADE_ROUND = ModItems.GRENADE_ROUND;
	public static final Item RIFLE_ROUND = ModItems.RIFLE_ROUND;
	public static final Item SHOTGUN_SHELL = ModItems.SHOTGUN_SHELL;
	public static final Item FUEL_CELL = ModItems.FUEL_CELL;
	public static final Item RAILGUN_CELL = ModItems.RAILGUN_CELL;
	public static final Item UPGRADE_TEMPLATE = ModItems.UPGRADE_TEMPLATE;
	public static final Item PRECISION_BARREL = ModItems.PRECISION_BARREL;
	public static final Item COOLING_SYSTEM = ModItems.COOLING_SYSTEM;
	public static final Item REINFORCED_RECEIVER = ModItems.REINFORCED_RECEIVER;
	public static final EntityType<GrenadeProjectileEntity> GRENADE_PROJECTILE = ModEntities.GRENADE_PROJECTILE;
	public static final ItemGroup ZYGUNS = ModItemGroups.ZYGUNS;

	@Override
	public void onInitialize() {
		ModItems.initialize();
		ModRecipes.initialize();
		ModEntities.initialize();
		ModItemGroups.initialize();
		GunsNetworking.registerPayloadTypes();
		SniperRifleServer.register();
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
