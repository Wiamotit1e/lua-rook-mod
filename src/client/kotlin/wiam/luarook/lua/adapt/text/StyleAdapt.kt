package wiam.luarook.lua.adapt.text

import net.minecraft.text.Style
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue

fun Style.toLuaTable(): LuaTable {
    val table = LuaTable()
    table.apply {
        set("color", color?.toLuaTable() ?: LuaValue.NIL)
        set("shadowColor", shadowColor?.let { LuaValue.valueOf(it) } ?: LuaValue.NIL)
        set("bold", LuaValue.valueOf(isBold))
        set("italic", LuaValue.valueOf(isItalic))
        set("underlined", LuaValue.valueOf(isUnderlined))
        set("strikethrough", LuaValue.valueOf(isStrikethrough))
        set("obfuscated", LuaValue.valueOf(isObfuscated))
    }
    return table
}

fun LuaTable.toStyle(): Style {
    val value1 = this.get("color") as? LuaTable
    val color = value1?.toTextColor()
    return Style.EMPTY
        .withColor(color)
        .withShadowColor(get("shadowColor").toint())
        .withBold(get("bold").toboolean())
        .withItalic(get("italic").toboolean())
        .withUnderline(get("underlined").toboolean())
        .withStrikethrough(get("strikethrough").toboolean())
        .withObfuscated(get("obfuscated").toboolean())
}