package com.zoyluo.guns.item;

import com.zoyluo.guns.upgrade.GunUpgradeService;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

/** Common base for weapons that can receive Guns Smithing Table modules. */
public class GunItem extends Item {
	public GunItem(Settings settings) {
		super(settings);
	}

	protected final void appendUpgradeTooltips(ItemStack stack, List<Text> tooltip) {
		GunUpgradeService.appendTooltip(stack, tooltip);
	}
}
