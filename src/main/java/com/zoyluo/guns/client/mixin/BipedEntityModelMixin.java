package com.zoyluo.guns.client.mixin;

import com.zoyluo.guns.GunsClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public class BipedEntityModelMixin {
	@Shadow @Final public ModelPart head;
	@Shadow @Final public ModelPart rightArm;
	@Shadow @Final public ModelPart leftArm;

	@Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;)V", at = @At("TAIL"))
	private void guns$poseScopedSniper(BipedEntityRenderState state, CallbackInfo ci) {
		if (!GunsClient.shouldUseSniperPose(state)) {
			return;
		}

		float aimPitch = head.pitch - 1.35F;
		rightArm.pitch = aimPitch;
		rightArm.yaw = head.yaw - 0.18F;
		rightArm.roll = -0.08F;
		leftArm.pitch = aimPitch + 0.08F;
		leftArm.yaw = head.yaw + 0.42F;
		leftArm.roll = 0.18F;
	}
}
