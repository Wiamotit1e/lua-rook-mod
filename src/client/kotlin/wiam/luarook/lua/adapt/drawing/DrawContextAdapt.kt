package wiam.luarook.lua.adapt.drawing

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import org.luaj.vm2.LuaTable
import wiam.luarook.lua.adapt.text.toMutableText
import wiam.luarook.lua.adapt.toItemStack

fun DrawContext.toLuaTable(): LuaTable {
    return LuaTable().apply {
        set("scaledWidth", scaledWindowWidth.toDouble())
        set("scaledHeight", scaledWindowHeight.toDouble())
        set("m00", matrices.m00.toDouble())
        set("m01", matrices.m01.toDouble())
        set("m10", matrices.m10.toDouble())
        set("m11", matrices.m11.toDouble())
        set("m20", matrices.m20.toDouble())
        set("m21", matrices.m21.toDouble())
    }
}

fun DrawContext.drawWith(table: LuaTable) {
    for (i in 1..table.length()) {
        val v1 = table.get(i)
        if (v1 !is LuaTable) continue
        if (!v1["type"].isstring()) continue
        when (v1["type"].tojstring()) {
            "text_rendering" -> {
                val v2 = v1["text"] as LuaTable
                val text = v2.toMutableText()
                drawText(
                    mc.textRenderer,
                    text,
                    v1["x"].toint(),
                    v1["y"].toint(),
                    v1["color"].toint(),
                    v1["shadow"].toboolean()
                )
            }
            
            "item_rendering" -> {
                val itemTable = v1["item"] as? LuaTable ?: continue
                val stack = itemTable.toItemStack() ?: continue
                val x = v1["x"].toint()
                val y = v1["y"].toint()
                val seed = v1.get("seed")?.toint() ?: 0
                drawItemWithoutEntity(stack, x, y, seed)
            }
            
            "item_overlay_rendering" -> {
                val itemTable = v1["item"] as? LuaTable ?: continue
                val stack = itemTable.toItemStack() ?: continue
                val x = v1["x"].toint()
                val y = v1["y"].toint()
                val countText = v1.get("countText")?.tojstring()
                drawStackOverlay(mc.textRenderer, stack, x, y, countText)
            }
            
            "matrix_pushed" -> {
                matrices.pushMatrix()
            }
            
            "matrix_popped" -> {
                matrices.popMatrix()
            }
            
            "matrix_translated" -> {
                matrices.translate(v1["x"].tofloat(), v1["y"].tofloat())
            }
            
            "matrix_translation" -> {
                matrices.translation(v1["x"].tofloat(), v1["y"].tofloat())
            }
            
            "matrix_scaled" -> {
                matrices.scale(v1["x"].tofloat(), v1["y"].tofloat())
            }
            
            "matrix_scaled_around" -> {
                matrices.scaleAround(v1["sx"].tofloat(), v1["sy"].tofloat(), v1["ox"].tofloat(), v1["oy"].tofloat())
            }
            
            "matrix_set" -> {
                matrices.set(
                    v1["matrix"]["m00"].tofloat(),
                    v1["matrix"]["m01"].tofloat(),
                    v1["matrix"]["m10"].tofloat(),
                    v1["matrix"]["m11"].tofloat(),
                    v1["matrix"]["m20"].tofloat(),
                    v1["matrix"]["m21"].tofloat()
                )
            }
            
            "matrix_inverted" -> {
                matrices.invert()
            }
        }
    }
}

private val mc = MinecraftClient.getInstance()
