package com.zoyluo.guns.client.mixin;

import com.zoyluo.guns.GunsClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
	private void guns$cancelScopedAttackSwing(CallbackInfoReturnable<Boolean> cir) {
		if (GunsClient.handleGunAttack((MinecraftClient) (Object) this)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
	private void guns$cancelScopedUseSwing(CallbackInfo ci) {
		if (GunsClient.handleScopedUse((MinecraftClient) (Object) this)) {
			ci.cancel();
		}
	}
}
