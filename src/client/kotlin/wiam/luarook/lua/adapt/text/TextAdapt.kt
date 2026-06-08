package wiam.luarook.lua.adapt.text

import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableTextContent
import net.minecraft.util.Language
import org.luaj.vm2.Lua
import org.luaj.vm2.LuaTable

fun MutableText.toLuaTable(): LuaTable {
    val table = LuaTable()
    table.apply {
        set("content", if (content is TranslatableTextContent) Language.getInstance().get(string) else string)
        set("style", style.toLuaTable())
        set(
            "siblings",
            LuaTable().apply {
                siblings.map { it.copy().toLuaTable() }.forEachIndexed { index, table -> this.set(index + 1, table) }
            })
    }
    return table
}

fun LuaTable.toMutableText(): MutableText {
    val content = this@toMutableText.get("content").tojstring()
    val text = Text.of(content).copy()
    val styleTable = this@toMutableText.get("style") as? LuaTable
    val siblingsTable = this@toMutableText.get("siblings") as? LuaTable

    if (styleTable != null) {
        text.style = styleTable.toStyle()
    }
    if (siblingsTable != null) {
        for (i in 1..siblingsTable.length()) {
            val sibling = siblingsTable[i] as? LuaTable ?: continue
            text.append(sibling.toMutableText())
        }
    }
    return text
}