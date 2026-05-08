package com.zoyluo.guns.client.mixin;

import com.zoyluo.guns.GunsClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
	@Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
	private void guns$hideVanillaCrosshairWhenScoped(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		if (GunsClient.isScoped()) {
			ci.cancel();
		}
	}
}
