package com.zoyluo.guns.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class UpgradeTemplateItem extends Item {
	public UpgradeTemplateItem(Settings settings) {
		super(settings);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
		tooltip.add(Text.translatable("tooltip.guns.upgrade_template").formatted(Formatting.GRAY));
	}
}
