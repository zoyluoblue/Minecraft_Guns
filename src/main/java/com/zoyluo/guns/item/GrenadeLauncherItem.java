package com.zoyluo.guns.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.List;

public class GrenadeLauncherItem extends GunItem {
	public static final float EXPLOSION_POWER = 4.0F;
	public static final float PROJECTILE_SPEED = 1.8F;

	public GrenadeLauncherItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult use(World world, PlayerEntity user, Hand hand) {
		return hand == Hand.MAIN_HAND ? ActionResult.FAIL : ActionResult.PASS;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
		tooltip.add(Text.translatable("tooltip.guns.grenade_launcher.explosion").formatted(Formatting.GRAY));
		tooltip.add(Text.translatable("tooltip.guns.grenade_launcher.trajectory").formatted(Formatting.DARK_GRAY));
		appendUpgradeTooltips(stack, tooltip);
	}
}
