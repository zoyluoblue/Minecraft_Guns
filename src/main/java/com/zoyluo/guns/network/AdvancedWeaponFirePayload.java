package com.zoyluo.guns.network;

import com.zoyluo.guns.Guns;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record AdvancedWeaponFirePayload(Weapon weapon) implements CustomPayload {
	public static final CustomPayload.Id<AdvancedWeaponFirePayload> ID = new CustomPayload.Id<>(Guns.id("advanced_weapon_fire"));
	public static final PacketCodec<RegistryByteBuf, AdvancedWeaponFirePayload> CODEC = CustomPayload.codecOf(AdvancedWeaponFirePayload::write, AdvancedWeaponFirePayload::read);

	private static AdvancedWeaponFirePayload read(RegistryByteBuf buf) {
		return new AdvancedWeaponFirePayload(buf.readEnumConstant(Weapon.class));
	}

	private void write(RegistryByteBuf buf) {
		buf.writeEnumConstant(weapon);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}

	public enum Weapon {
		SMG,
		FLAMETHROWER,
		RAILGUN
	}
}
