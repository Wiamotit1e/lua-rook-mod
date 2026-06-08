package wiam.luarook.lua.api

import org.luaj.vm2.Globals
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import wiam.luarook.handler.ListedPlayerEntriesHandler
import wiam.luarook.lua.adapt.text.toLuaTable
import wiam.luarook.lua.adapt.text.with

object TabListAPI {
    
    fun inject(globals: Globals) {
        val tabList = LuaValue.tableOf()
        tabList["onPlayerListEntriesModified"] = object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                if (arg.isfunction()) {
                    playerListEntriesModifiedListeners.add(arg)
                }
                return NIL
            }
        }
        globals["tabList"] = tabList
    }
    
    init {
        processPlayerListEntryModified()
    }
    
    
    private val playerListEntriesModifiedListeners = mutableListOf<LuaValue>()
    
    private fun processPlayerListEntryModified() {
        ListedPlayerEntriesHandler.playerListEntryModified = {entries, index, entry ->
            playerListEntriesModifiedListeners.forEach { listener ->
                if (listener.isfunction()) {
                    try {
                        val modified = listener.call(entry.toLuaTable())
                        if (modified is LuaTable) {
                            entries[index] = entries[index].with(modified)
                        } else {
                            entries.removeAt(index)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            entry
        }
    }
}