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
    val text = Text.of( content).copy()
    text.apply {
        val style = this@toMutableText.get("style") as? LuaTable ?: throw IllegalArgumentException("Invalid style")
        val siblings =
            this@toMutableText.get("siblings") as? LuaTable ?: throw IllegalArgumentException("Invalid siblings")
        this.style = style.toStyle()
        for (i in 1..siblings.length()) {
            val sibling = siblings[i] as? LuaTable ?: throw IllegalArgumentException("Invalid sibling")
            this.append(sibling.toMutableText())
        }
    }
    return text
}