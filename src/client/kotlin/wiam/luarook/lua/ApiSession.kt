package wiam.luarook.lua

import net.fabricmc.loader.api.FabricLoader
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.jse.JsePlatform
import wiam.luarook.lua.api.ChatApi
import wiam.luarook.lua.api.LoggerApi
import wiam.luarook.lua.api.PlayerApi
import wiam.luarook.lua.api.TabListApi
import wiam.luarook.lua.api.WorldApi

/**
 * Holds all API instances and their Lua [Globals] for one script.
 * Register with [ApiBridge.register] to activate event listeners.
 */
class ApiSession(val name: String) {
    
    val chat = ChatApi()
    val player = PlayerApi()
    val world = WorldApi()
    val tabList = TabListApi()
    val logger = LoggerApi()
    val globals: Globals = createRegularGlobals()
    
    /** All API instances in registration order. Add new APIs here. */
    private val all: List<LuaApi> = listOf(chat, player, world, tabList, logger)
    
    init {
        for (api in all) {
            api.scriptName = name
            api.inject(globals)
        }
    }
    
    fun dispose() {
        all.forEach { it.dispose() }
    }
}

private fun createRegularGlobals(): Globals {
    val globals = JsePlatform.standardGlobals()
    val scriptsDir = FabricLoader.getInstance().gameDir.resolve("lua-rook-scripts").toString().replace("\\", "/")
    val currentPath = globals.get("package").get("path").tojstring()
    globals.get("package").set("path", LuaValue.valueOf("$scriptsDir/?.lua;$scriptsDir/?/init.lua;$currentPath"))
    return globals
}