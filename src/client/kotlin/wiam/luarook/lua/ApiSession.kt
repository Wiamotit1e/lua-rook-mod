package wiam.luarook.lua

import org.luaj.vm2.Globals
import org.luaj.vm2.lib.jse.JsePlatform
import wiam.luarook.lua.api.ChatApi
import wiam.luarook.lua.api.PlayerApi
import wiam.luarook.lua.api.TabListApi
import wiam.luarook.lua.api.WorldApi

/**
 * Holds all API instances and their Lua [Globals] for one script.
 * Register with [ApiBridge.register] to activate, [dispose] to clean up.
 */
class ApiSession(val name: String) {

    val chat = ChatApi()
    val player = PlayerApi()
    val world = WorldApi()
    val tabList = TabListApi()
    val globals: Globals = JsePlatform.standardGlobals()

    init {
        chat.inject(globals)
        world.inject(globals)
        player.inject(globals)
        tabList.inject(globals)
    }

    fun dispose() {
        chat.dispose()
        player.dispose()
        world.dispose()
        tabList.dispose()
    }
}
