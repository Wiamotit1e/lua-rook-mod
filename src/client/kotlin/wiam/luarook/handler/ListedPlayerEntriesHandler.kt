package wiam.luarook.handler

import net.minecraft.client.network.PlayerListEntry

object ListedPlayerEntriesHandler {
    @JvmStatic
    var playerListEntryModified: (MutableList<PlayerListEntry>, Int, PlayerListEntry) -> PlayerListEntry = { entries, index, it -> it }
    
}