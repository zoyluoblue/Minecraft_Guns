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

public class AdvancedWeaponItem extends Item {
	private final String tooltipKey;

	public AdvancedWeaponItem(Settings settings, String tooltipKey) {
		super(settings);
		this.tooltipKey = tooltipKey;
	}

	@Override
	public ActionResult use(World world, PlayerEntity user, Hand hand) {
		return hand == Hand.MAIN_HAND ? ActionResult.FAIL : ActionResult.PASS;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
		tooltip.add(Text.translatable(tooltipKey).formatted(Formatting.GRAY));
	}
}
