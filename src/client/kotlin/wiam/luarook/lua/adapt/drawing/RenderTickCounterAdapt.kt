package wiam.luarook.lua.adapt.drawing

import net.minecraft.client.render.RenderTickCounter
import org.luaj.vm2.LuaTable

fun RenderTickCounter.toLuaTable(): LuaTable {
    return LuaTable().apply {
        set("dynamicDeltaTicks", dynamicDeltaTicks.toDouble())
        set("tickProgressFreezeIgnored", getTickProgress(true).toDouble())
        set("tickProgress", getTickProgress(false).toDouble())
        set("fixedDeltaTicks", fixedDeltaTicks.toDouble())
    }
}