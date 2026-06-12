package wiam.luarook.lua.api

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.LuaValue.NIL
import wiam.luarook.lua.ErrorReporter
import wiam.luarook.lua.LuaApi
import wiam.luarook.lua.adapt.drawing.drawWith
import wiam.luarook.lua.adapt.drawing.toLuaTable
import wiam.luarook.lua.adapt.text.toMutableText

class HudApi: LuaApi("hud") {
    override fun register(t: LuaTable) {
        t.event("hudRendered")

        t.fn1("measureText") { arg ->
            val textTable = arg as? LuaTable ?: return@fn1 NIL
            val text = textTable.toMutableText()
            val tr = MinecraftClient.getInstance().textRenderer
            LuaTable().apply {
                set("width", tr.getWidth(text))
                set("height", tr.fontHeight)
            }
        }
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