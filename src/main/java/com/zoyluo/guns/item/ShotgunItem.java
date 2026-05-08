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

public class ShotgunItem extends Item {
	public static final double RANGE = 8.0D;
	public static final double CONE_DEGREES = 150.0D;
	public static final float BASE_DAMAGE = 15.0F;
	public static final float MIN_DAMAGE = 3.0F;
	public static final float DAMAGE_FALLOFF_PER_BLOCK = (BASE_DAMAGE - MIN_DAMAGE) / ((float) RANGE - 1.0F);
	public static final double BASE_KNOCKBACK_BLOCKS = 5.0D;
	public static final double KNOCKBACK_FALLOFF_PER_BLOCK = BASE_KNOCKBACK_BLOCKS / (RANGE - 1.0D);

	public ShotgunItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult use(World world, PlayerEntity user, Hand hand) {
		return hand == Hand.MAIN_HAND ? ActionResult.FAIL : ActionResult.PASS;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
		tooltip.add(Text.translatable("tooltip.guns.shotgun.damage", (int) BASE_DAMAGE).formatted(Formatting.GRAY));
		tooltip.add(Text.translatable("tooltip.guns.shotgun.range", (int) RANGE, (int) CONE_DEGREES).formatted(Formatting.DARK_GRAY));
	}
}
