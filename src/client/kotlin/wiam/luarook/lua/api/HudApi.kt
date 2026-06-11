package wiam.luarook.lua.api

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import org.luaj.vm2.LuaTable
import wiam.luarook.lua.ErrorReporter
import wiam.luarook.lua.LuaApi
import wiam.luarook.lua.adapt.drawing.drawWith
import wiam.luarook.lua.adapt.drawing.toLuaTable

class HudApi: LuaApi("hud") {
    override fun register(t: LuaTable) {
        t.event("hudRendered")
    }
    
    
    internal fun fireHudRendered(context: DrawContext, tickCounter: RenderTickCounter) {
        val list = listeners["hudRendered"] ?: return
        for (fn in list) {
            try {
                val v1 = fn.call(context.toLuaTable(), tickCounter.toLuaTable())
                if (v1 is LuaTable) context.drawWith(v1)
            } catch (e: Exception) {
                ErrorReporter.reportRuntimeError(scriptName, "hud.hudRendered", e)
            }
        }
    }
}