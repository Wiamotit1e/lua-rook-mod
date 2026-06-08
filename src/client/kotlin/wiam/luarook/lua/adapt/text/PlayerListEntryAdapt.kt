package wiam.luarook.lua.adapt.text

import net.minecraft.client.network.PlayerListEntry
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue

fun PlayerListEntry.toLuaTable(): LuaTable {
    val table = LuaTable()
    table.apply {
        set("displayName", displayName?.copy()?.toLuaTable() ?: LuaValue.NIL)
        set("latency", latency)
        set("listOrder", listOrder)
    }
    return table
}

fun PlayerListEntry.with(luaTable: LuaTable): PlayerListEntry {
    val displayName = luaTable.get("displayName") as? LuaTable
    val latency = luaTable.get("latency").toint()
    val listOrder = luaTable.get("listOrder").toint()
    this.apply {
        this.displayName = displayName?.toMutableText()
        this.latency = latency
        this.listOrder = listOrder
    }
    return this
}