package com.zoyluo.guns.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;

public class SniperRifleItem extends Item {
	public static final float BASE_DAMAGE = 10.0F;
	public static final double RANGE = 128.0D;
	public static final int MAX_ZOOM_LEVEL = 16;

	public SniperRifleItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult use(World world, PlayerEntity user, Hand hand) {
		return hand == Hand.MAIN_HAND ? ActionResult.FAIL : ActionResult.PASS;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
		tooltip.add(Text.translatable("tooltip.guns.sniper_rifle.damage", (int) BASE_DAMAGE).formatted(Formatting.GRAY));
		tooltip.add(Text.translatable("tooltip.guns.sniper_rifle.zoom").formatted(Formatting.DARK_GRAY));
	}
}
