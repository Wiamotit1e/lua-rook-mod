package wiam.luarook

import net.fabricmc.api.ClientModInitializer
import org.slf4j.LoggerFactory
import wiam.luarook.command.LuaRookCommand
import wiam.luarook.lua.ScriptLoader

object LuaRookClient : ClientModInitializer {
    private val logger = LoggerFactory.getLogger("lua-rook")

    override fun onInitializeClient() {
        logger.info("Lua Rook initializing...")

        // Load all .lua scripts from lua-rook-scripts/
        ScriptLoader.loadAll()

        // Register /rook command
        LuaRookCommand.register()

        logger.info("Lua Rook ready. Use /rook list to see scripts.")
    }
}
