package wiam.luarook.command

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.text.Text
import wiam.luarook.lua.GlobalsCollector
import wiam.luarook.lua.ScriptLoader

object LuaRookCommand {

    fun register() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            val root = ClientCommandManager.literal("rook")

            // /rook list
            val listNode = ClientCommandManager.literal("list")
                .executes { listScripts(it) }

            // /rook run <name>
            val runNode = ClientCommandManager.literal("run")
                .then(
                    ClientCommandManager.argument("name", StringArgumentType.word())
                        .suggests { _, builder ->
                            ScriptLoader.getScriptFiles().forEach { builder.suggest(it) }
                            builder.buildFuture()
                        }
                        .executes { runScript(it) }
                )

            // /rook unload <name>
            val unloadNode = ClientCommandManager.literal("unload")
                .then(
                    ClientCommandManager.argument("name", StringArgumentType.word())
                        .suggests { _, builder ->
                            GlobalsCollector.listNames().forEach { builder.suggest(it) }
                            builder.buildFuture()
                        }
                        .executes { unloadScript(it) }
                )

            // /rook reload
            val reloadNode = ClientCommandManager.literal("reload")
                .executes { reloadScripts(it) }

            dispatcher.register(
                root.then(listNode).then(runNode).then(unloadNode).then(reloadNode)
            )
        }
    }

    private fun listScripts(context: CommandContext<FabricClientCommandSource>): Int {
        val loaded = GlobalsCollector.listNames()
        val files = ScriptLoader.getScriptFiles()

        if (files.isEmpty()) {
            context.source.sendFeedback(Text.literal("§e[LuaRook] No .lua files found in lua-rook-scripts/"))
            return 1
        }

        context.source.sendFeedback(Text.literal("§a[LuaRook] Script files (${files.size}):"))
        for (name in files) {
            val status = if (name in loaded) "§aloaded" else "§7not loaded"
            context.source.sendFeedback(Text.literal("  §f$name §7- $status"))
        }
        return 1
    }

    private fun runScript(context: CommandContext<FabricClientCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val success = ScriptLoader.loadScript(name)
        if (success) {
            context.source.sendFeedback(Text.literal("§a[LuaRook] Script loaded: $name"))
        } else {
            context.source.sendError(Text.literal("§c[LuaRook] Script not found: $name.lua"))
        }
        return if (success) 1 else 0
    }

    private fun unloadScript(context: CommandContext<FabricClientCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val removed = GlobalsCollector.remove(name)
        if (removed != null) {
            context.source.sendFeedback(Text.literal("§a[LuaRook] Script unloaded: $name"))
            return 1
        } else {
            context.source.sendError(Text.literal("§c[LuaRook] Script not loaded: $name"))
            return 0
        }
    }

    private fun reloadScripts(context: CommandContext<FabricClientCommandSource>): Int {
        ScriptLoader.reloadAll()
        val count = GlobalsCollector.listNames().size
        context.source.sendFeedback(Text.literal("§a[LuaRook] Reloaded $count script(s)"))
        return 1
    }
}
