package wiam.luarook.lua

import org.luaj.vm2.Globals
import org.luaj.vm2.lib.jse.JsePlatform
import wiam.luarook.lua.api.ChatAPI
import wiam.luarook.lua.api.PlayerAPI
import wiam.luarook.lua.api.TabListAPI
import wiam.luarook.lua.api.WorldAPI

fun createRegularGlobals(): Globals {
    val globals: Globals = JsePlatform.standardGlobals()
    ChatAPI.inject(globals)
    WorldAPI.inject(globals)
    PlayerAPI.inject(globals)
    TabListAPI.inject(globals)
    return globals
}