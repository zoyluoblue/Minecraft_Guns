package com.zoyluo.guns.ammo;

import com.zoyluo.guns.registry.ModItems;
import net.minecraft.item.Item;
import net.minecraft.text.Text;

public enum AmmoType {
	RIFLE_ROUND("rifle_round"),
	SHOTGUN_SHELL("shotgun_shell"),
	GRENADE_ROUND("grenade_round"),
	FUEL_CELL("fuel_cell"),
	RAILGUN_CELL("railgun_cell");

	private final String id;

	AmmoType(String id) {
		this.id = id;
	}

	public Item item() {
		return switch (this) {
			case RIFLE_ROUND -> ModItems.RIFLE_ROUND;
			case SHOTGUN_SHELL -> ModItems.SHOTGUN_SHELL;
			case GRENADE_ROUND -> ModItems.GRENADE_ROUND;
			case FUEL_CELL -> ModItems.FUEL_CELL;
			case RAILGUN_CELL -> ModItems.RAILGUN_CELL;
		};
	}

	public String id() {
		return id;
	}

	public String translationKey() {
		return "item.guns." + id;
	}

	public Text displayName() {
		return Text.translatable(translationKey());
	}
}
