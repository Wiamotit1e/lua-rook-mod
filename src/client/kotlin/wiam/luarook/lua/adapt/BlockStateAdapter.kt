package wiam.luarook.lua.adapt

import com.mojang.serialization.JsonOps
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.registry.RegistryOps
import org.luaj.vm2.LuaValue

fun BlockState.toLuaValueComprehensively(): LuaValue {
    var v1 = LuaValue.NIL
    val registryOps = RegistryOps.of(JsonOps.INSTANCE, MinecraftClient.getInstance().world?.registryManager)
    BlockState.CODEC.encodeStart(registryOps, this)
        .ifSuccess {
            v1 = LuaValue.valueOf(it.toString())
        }
        .ifError {
            v1 = LuaValue.valueOf("error:$it")
        }
    return v1
}