package wiam.luarook.lua

import org.luaj.vm2.Globals

object GlobalsCollector {
    val allGlobals = mutableMapOf<String, Globals>()

    fun get(name: String): Globals? = allGlobals[name]

    fun remove(name: String): Globals? = allGlobals.remove(name)

    fun listNames(): Set<String> = allGlobals.keys.toSet()

    fun clear() = allGlobals.clear()
}