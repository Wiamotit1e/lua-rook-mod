package wiam.luarook.lua.api

import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.PlayerListEntry
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.LuaValue.NIL
import wiam.luarook.lua.ErrorReporter
import wiam.luarook.lua.LuaApi
import wiam.luarook.lua.adapt.text.toLuaTable
import wiam.luarook.lua.adapt.text.with
import wiam.luarook.lua.adapt.toLuaTable

class TabListApi : LuaApi("tabList") {

    override fun register(t: LuaTable) {
        t.fn0("getListedPlayerListEntries") {
            MinecraftClient.getInstance().networkHandler?.listedPlayerListEntries
                ?.map { it.toLuaTable() }?.toLuaTable() ?: NIL
        }
        t.event("playerListEntriesModified")
    }

    /**
     * Custom fire for tab list modification. Unlike simple events, each listener
     * receives a copy of the current entry and can return a modified table or nil
     * (to remove the entry). Listener results are chained: the next listener sees
     * the previous listener's modifications.
     */
    internal fun firePlayerListEntryModified(
        entries: MutableList<PlayerListEntry>,
        index: Int,
        entry: PlayerListEntry
    ): PlayerListEntry {
        var current = entry
        for (fn in listeners["playerListEntriesModified"] ?: return current) {
            if (!fn.isfunction()) continue
            try {
                val modified = fn.call(current.toLuaTable())
                if (modified is LuaTable) {
                    entries[index] = current.with(modified)
                    current = entries[index]
                } else {
                    // Listener returned nil → remove entry and stop
                    entries.removeAt(index)
                    return current
                }
            } catch (e: Exception) {
                ErrorReporter.reportRuntimeError(scriptName, "tabList.playerListEntriesModified", e)
            }
        }
        return current
    }
}
