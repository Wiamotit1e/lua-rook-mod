package wiam.luarook.lua.api

import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.PlayerListEntry
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.ZeroArgFunction
import wiam.luarook.lua.adapt.text.toLuaTable
import wiam.luarook.lua.adapt.text.with
import wiam.luarook.lua.ErrorReporter
import wiam.luarook.lua.adapt.toLuaTable

class TabListApi {

    var scriptName: String = "unknown"
    
    private val playerListEntriesModifiedListeners = mutableListOf<LuaValue>()
    
    fun inject(globals: Globals) {
        val tabList = LuaValue.tableOf()
        tabList["getListedPlayerListEntries"] = object : ZeroArgFunction() {
            override fun call(): LuaValue {
                return MinecraftClient.getInstance().networkHandler?.listedPlayerListEntries?.map { it.toLuaTable() }
                    ?.toLuaTable() ?: NIL
            }
        }
        tabList["onPlayerListEntriesModified"] = object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                if (arg.isfunction()) playerListEntriesModifiedListeners.add(arg)
                return NIL
            }
        }
        globals["tabList"] = tabList
    }
    
    fun dispose() {
        playerListEntriesModifiedListeners.clear()
    }
    
    internal fun firePlayerListEntryModified(
        entries: MutableList<PlayerListEntry>,
        index: Int,
        entry: PlayerListEntry
    ): PlayerListEntry {
        var current = entry
        for (listener in playerListEntriesModifiedListeners) {
            if (!listener.isfunction()) continue
            try {
                val modified = listener.call(current.toLuaTable())
                if (modified is LuaTable) {
                    entries[index] = current.with(modified)
                    current = entries[index]
                } else {
                    // Listener returned nil → remove entry and stop processing further listeners
                    entries.removeAt(index)
                    return current
                }
            } catch (e: Exception) {
                ErrorReporter.reportRuntimeError(scriptName, "tabList.onPlayerListEntriesModified", e)
            }
        }
        return current
    }
}
