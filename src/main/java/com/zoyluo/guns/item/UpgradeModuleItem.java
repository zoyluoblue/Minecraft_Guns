package com.zoyluo.guns.item;

import com.zoyluo.guns.upgrade.GunUpgrade;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class UpgradeModuleItem extends Item {
	private final GunUpgrade upgrade;

	public UpgradeModuleItem(Settings settings, GunUpgrade upgrade) {
		super(settings);
		this.upgrade = upgrade;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
		tooltip.add(Text.translatable("tooltip.guns.upgrade_module").formatted(Formatting.GRAY));
		tooltip.add(Text.translatable(upgrade.translationKey()).formatted(Formatting.AQUA));
	}
}
