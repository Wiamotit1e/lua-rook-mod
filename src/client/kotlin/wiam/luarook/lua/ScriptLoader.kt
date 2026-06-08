package wiam.luarook.lua

import net.fabricmc.loader.api.FabricLoader
import org.luaj.vm2.LuaError
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

object ScriptLoader {
    private val logger = LoggerFactory.getLogger("lua-rook/ScriptLoader")

    val scriptsDir: Path by lazy {
        FabricLoader.getInstance().gameDir.resolve("lua-rook-scripts")
    }

    fun ensureScriptsDir() {
        if (!Files.exists(scriptsDir)) {
            Files.createDirectories(scriptsDir)
            logger.info("Created scripts directory: $scriptsDir")
        }
    }

    fun createExampleScript() {
        val exampleFile = scriptsDir.resolve("example.lua")
        if (!exampleFile.exists()) {
            Files.writeString(exampleFile, EXAMPLE_SCRIPT)
            logger.info("Created example script: example.lua")
        }
    }

    fun loadAll() {
        ensureScriptsDir()

        val luaFiles = Files.list(scriptsDir)
            .filter { it.fileName.toString().endsWith(".lua") }
            .sorted()
            .toList()
        
        if (luaFiles.isEmpty()) {
            createExampleScript()
            logger.info("No .lua files found, created example.lua")
            val exampleFile = scriptsDir.resolve("example.lua")
            loadScriptFile(exampleFile)
            return
        }

        luaFiles.forEach { file -> loadScriptFile(file) }
        logger.info("Loaded ${GlobalsCollector.listNames().size} script(s)")
    }

    fun loadScript(name: String): Boolean {
        ensureScriptsDir()
        val file = scriptsDir.resolve("$name.lua")
        if (!file.exists()) {
            logger.warn("Script not found: $name.lua")
            return false
        }
        loadScriptFile(file)
        return true
    }

    fun reloadAll() {
        GlobalsCollector.clear()
        loadAll()
        logger.info("All scripts reloaded")
    }

    fun getScriptFiles(): List<String> {
        if (!Files.exists(scriptsDir)) return emptyList()
        return Files.list(scriptsDir)
            .filter { it.fileName.toString().endsWith(".lua") }
            .map { it.fileName.toString().removeSuffix(".lua") }
            .sorted()
            .toList()
    }

    private fun loadScriptFile(file: Path) {
        val name = file.fileName.toString().removeSuffix(".lua")
        val code: String
        try {
            code = Files.readString(file)
        } catch (e: Exception) {
            ErrorReporter.reportLoadError(name, "Failed to read file: ${e.message}")
            return
        }

        val session = ApiSession(name)
        try {
            session.globals.load(code).call()
            GlobalsCollector.put(name, session)
            logger.info("Loaded script: $name")
        } catch (e: LuaError) {
            session.dispose()
            ErrorReporter.reportLoadError(name, e.message ?: "Unknown Lua error")
        } catch (e: Exception) {
            session.dispose()
            ErrorReporter.reportLoadError(name, e.message ?: "Unknown error")
        }
    }

    private val EXAMPLE_SCRIPT = """
        -- ============================================================
        -- Lua Rook Example Script
        -- This script demonstrates the Lua Rook API.
        -- ============================================================

        local tickCount = 0
        local greeted = false

        world.onTickStarted(function()
            tickCount = tickCount + 1

            -- Greet once after 2 seconds (40 ticks)
            if tickCount == 40 and not greeted then
                greeted = true
                print("[LuaRook] Hello from example.lua!")
                print("[LuaRook] Try /rook list, /rook run <name>, /rook reload")
            end
        end)

        -- Log chat messages
        chat.onChatReceived(function(msg)
            print("[Chat] " .. msg)
        end)

        -- Player damage / death notifications
        player.onDamaged(function(source)
            print("[Player] Damaged by: " .. source.type)
        end)

        player.onDeath(function(source)
            print("[Player] Died to: " .. source.type)
        end)
    """.trimIndent()
}
