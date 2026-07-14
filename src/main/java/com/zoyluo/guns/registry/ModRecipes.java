package com.zoyluo.guns.registry;

import com.zoyluo.guns.Guns;
import com.zoyluo.guns.recipe.GunUpgradeRecipe;
import net.minecraft.recipe.RecipeSerializer;

public final class ModRecipes {
	public static final RecipeSerializer<GunUpgradeRecipe> GUN_UPGRADE = RecipeSerializer.register(
			Guns.id("smithing_upgrade").toString(),
			new GunUpgradeRecipe.Serializer()
	);

	private ModRecipes() {
	}

	public static void initialize() {
		// Loading this class performs the static recipe serializer registration.
	}
}
