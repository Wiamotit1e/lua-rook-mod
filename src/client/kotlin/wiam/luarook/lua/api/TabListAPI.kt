package wiam.luarook.lua.api

import net.minecraft.client.network.PlayerListEntry
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import wiam.luarook.lua.adapt.text.toLuaTable
import wiam.luarook.lua.adapt.text.with

class TabListApi {

    private val playerListEntriesModifiedListeners = mutableListOf<LuaValue>()

    fun inject(globals: Globals) {
        val tabList = LuaValue.tableOf()
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
        playerListEntriesModifiedListeners.forEach { listener ->
            if (listener.isfunction()) {
                try {
                    val modified = listener.call(current.toLuaTable())
                    if (modified is LuaTable) {
                        entries[index] = current.with(modified)
                        current = entries[index]
                    } else {
                        entries.removeAt(index)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return current
    }
}
