package wiam.luarook.mixin.client;


import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wiam.luarook.lua.ApiBridge;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(at = @At("TAIL"), method = "onDamaged")
    public void wiam$onDamaged(DamageSource damageSource, CallbackInfo ci) {
        if ((Object) this instanceof ClientPlayerEntity) {
            ApiBridge.onPlayerDamaged(damageSource);
        }
    }

    @Inject(at = @At("TAIL"), method = "onDeath")
    public void wiam$onDeath(DamageSource damageSource, CallbackInfo ci) {
        if ((Object) this instanceof ClientPlayerEntity) {
            ApiBridge.onPlayerDeath(damageSource);
        }
    }
}
