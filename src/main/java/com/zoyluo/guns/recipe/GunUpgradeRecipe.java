package com.zoyluo.guns.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zoyluo.guns.registry.ModRecipes;
import com.zoyluo.guns.upgrade.GunUpgrade;
import com.zoyluo.guns.upgrade.GunUpgradeService;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.SmithingRecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public final class GunUpgradeRecipe implements SmithingRecipe {
	private final Ingredient template;
	private final Ingredient base;
	private final Ingredient addition;
	private final GunUpgrade upgrade;

	public GunUpgradeRecipe(Ingredient template, Ingredient base, Ingredient addition, GunUpgrade upgrade) {
		this.template = template;
		this.base = base;
		this.addition = addition;
		this.upgrade = upgrade;
	}

	@Override
	public boolean matches(SmithingRecipeInput input, World world) {
		return template.test(input.template())
				&& base.test(input.base())
				&& addition.test(input.addition())
				&& GunUpgradeService.canInstall(input.base(), upgrade);
	}

	@Override
	public ItemStack craft(SmithingRecipeInput input, net.minecraft.registry.RegistryWrapper.WrapperLookup registries) {
		ItemStack result = input.base().copy();
		result.setCount(1);
		GunUpgradeService.install(result, upgrade);
		return result;
	}

	@Override
	public Optional<Ingredient> template() {
		return Optional.of(template);
	}

	@Override
	public Optional<Ingredient> base() {
		return Optional.of(base);
	}

	@Override
	public Optional<Ingredient> addition() {
		return Optional.of(addition);
	}

	@Override
	public RecipeSerializer<? extends SmithingRecipe> getSerializer() {
		return ModRecipes.GUN_UPGRADE;
	}

	@Override
	public IngredientPlacement getIngredientPlacement() {
		return IngredientPlacement.forMultipleSlots(List.of(template(), base(), addition()));
	}

	@Override
	public List<RecipeDisplay> getDisplays() {
		return List.of(new SmithingRecipeDisplay(
				Ingredient.toDisplay(template()),
				Ingredient.toDisplay(base()),
				Ingredient.toDisplay(addition()),
				Ingredient.toDisplay(base()),
				new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)
		));
	}

	private Ingredient templateIngredient() {
		return template;
	}

	private Ingredient baseIngredient() {
		return base;
	}

	private Ingredient additionIngredient() {
		return addition;
	}

	private GunUpgrade upgradeValue() {
		return upgrade;
	}

	public static final class Serializer implements RecipeSerializer<GunUpgradeRecipe> {
		private static final MapCodec<GunUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Ingredient.CODEC.fieldOf("template").forGetter(GunUpgradeRecipe::templateIngredient),
				Ingredient.CODEC.fieldOf("base").forGetter(GunUpgradeRecipe::baseIngredient),
				Ingredient.CODEC.fieldOf("addition").forGetter(GunUpgradeRecipe::additionIngredient),
				GunUpgrade.CODEC.fieldOf("module").forGetter(GunUpgradeRecipe::upgradeValue)
		).apply(instance, GunUpgradeRecipe::new));
		private static final PacketCodec<RegistryByteBuf, GunUpgradeRecipe> PACKET_CODEC = PacketCodec.tuple(
				Ingredient.PACKET_CODEC, GunUpgradeRecipe::templateIngredient,
				Ingredient.PACKET_CODEC, GunUpgradeRecipe::baseIngredient,
				Ingredient.PACKET_CODEC, GunUpgradeRecipe::additionIngredient,
				GunUpgrade.PACKET_CODEC, GunUpgradeRecipe::upgradeValue,
				GunUpgradeRecipe::new
		);

		@Override
		public MapCodec<GunUpgradeRecipe> codec() {
			return CODEC;
		}

		@Override
		public PacketCodec<RegistryByteBuf, GunUpgradeRecipe> packetCodec() {
			return PACKET_CODEC;
		}
	}
}
