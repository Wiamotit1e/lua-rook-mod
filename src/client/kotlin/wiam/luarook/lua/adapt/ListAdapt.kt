package wiam.luarook.lua.adapt

import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue

fun List<LuaValue>.toLuaTable(): LuaTable {
    val table = LuaTable()
    for (i in this.indices) {
        table.set(i + 1, this[i])
    }
    return table
}

fun LuaTable.toList(): List<LuaValue> {
    val list = mutableListOf<LuaValue>()
    for (i in 1..this.length()) {
        list.add(this.get(i))
    }
    return list
}