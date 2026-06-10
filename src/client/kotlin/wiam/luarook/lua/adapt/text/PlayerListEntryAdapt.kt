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
    val displayNameVal = luaTable.get("displayName")
    if (displayNameVal != LuaValue.NIL) {
        this.displayName = (displayNameVal as? LuaTable)?.toMutableText()
    }
    val latencyVal = luaTable.get("latency")
    if (latencyVal != LuaValue.NIL) {
        this.latency = latencyVal.toint()
    }
    val listOrderVal = luaTable.get("listOrder")
    if (listOrderVal != LuaValue.NIL) {
        this.listOrder = listOrderVal.toint()
    }
    return this
}