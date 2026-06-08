package wiam.luarook.handler

import net.minecraft.entity.damage.DamageSource

object ClientPlayerHurtHandler {
    @JvmStatic
    var playerDamaged: (DamageSource) -> Unit = {}
    @JvmStatic
    var playerDeath: (DamageSource) -> Unit = {}
}