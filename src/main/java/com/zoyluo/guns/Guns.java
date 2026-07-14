package com.zoyluo.guns;

import com.zoyluo.guns.entity.GrenadeProjectileEntity;
import com.zoyluo.guns.network.GunsNetworking;
import com.zoyluo.guns.network.SniperRifleServer;
import com.zoyluo.guns.registry.ModEntities;
import com.zoyluo.guns.registry.ModItemGroups;
import com.zoyluo.guns.registry.ModItems;
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
	public static final EntityType<GrenadeProjectileEntity> GRENADE_PROJECTILE = ModEntities.GRENADE_PROJECTILE;
	public static final ItemGroup ZYGUNS = ModItemGroups.ZYGUNS;

	@Override
	public void onInitialize() {
		ModItems.initialize();
		ModEntities.initialize();
		ModItemGroups.initialize();
		GunsNetworking.registerPayloadTypes();
		SniperRifleServer.register();
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
