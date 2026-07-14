package com.zoyluo.guns.registry;

import com.zoyluo.guns.Guns;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public final class ModItemGroups {
	private static final RegistryKey<ItemGroup> ZYGUNS_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, Guns.id("zyguns"));

	public static final ItemGroup ZYGUNS = Registry.register(
			Registries.ITEM_GROUP,
			ZYGUNS_GROUP_KEY,
			FabricItemGroup.builder()
					.icon(() -> new ItemStack(ModItems.SNIPER_RIFLE))
					.displayName(Text.translatable("itemGroup.guns.zyguns"))
					.entries((displayContext, entries) -> {
						entries.add(ModItems.SNIPER_RIFLE);
						entries.add(ModItems.SHOTGUN);
						entries.add(ModItems.GRENADE_LAUNCHER);
						entries.add(ModItems.SMG);
						entries.add(ModItems.FLAMETHROWER);
						entries.add(ModItems.RAILGUN);
						entries.add(ModItems.RIFLE_ROUND);
						entries.add(ModItems.SHOTGUN_SHELL);
						entries.add(ModItems.GRENADE_ROUND);
						entries.add(ModItems.FUEL_CELL);
						entries.add(ModItems.RAILGUN_CELL);
						entries.add(ModItems.UPGRADE_TEMPLATE);
						entries.add(ModItems.PRECISION_BARREL);
						entries.add(ModItems.COOLING_SYSTEM);
						entries.add(ModItems.REINFORCED_RECEIVER);
					})
					.build()
	);

	private ModItemGroups() {
	}

	public static void initialize() {
		// Loading this class performs the static item-group registration.
	}
}
