package wiam.luarook.lua.adapt.text

import net.minecraft.text.TextColor
import org.luaj.vm2.LuaTable

fun TextColor.toLuaTable(): LuaTable {
    val table = LuaTable()
    table.set("color", this.name)
    return  table
}

fun LuaTable.toTextColor(): TextColor {
    val color = this.get("color").tojstring()
    val textColor = TextColor.parse( color).getOrThrow { IllegalArgumentException("Invalid color: $color") }
    return textColor
}