package wiam.luarook.lua.api

import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.LuaValue.NIL
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.TwoArgFunction
import wiam.luarook.lua.ErrorReporter
import wiam.luarook.lua.adapt.text.toMutableText

class ChatApi {

    var scriptName: String = "unknown"

    private val networkHandler: ClientPlayNetworkHandler?
        get() = MinecraftClient.getInstance().networkHandler

    private val chatReceivedListeners = mutableListOf<LuaValue>()
    private val gameReceivedListeners = mutableListOf<LuaValue>()
    private val allowChatListeners = mutableListOf<LuaValue>()
    private val allowGameListeners = mutableListOf<LuaValue>()
    private val allowChatSentListeners = mutableListOf<LuaValue>()
    private val allowCommandSentListeners = mutableListOf<LuaValue>()
    private val modifyChatSentListeners = mutableListOf<LuaValue>()
    private val modifyCommandSentListeners = mutableListOf<LuaValue>()
    private val chatSentListeners = mutableListOf<LuaValue>()
    private val commandSentListeners = mutableListOf<LuaValue>()

    fun inject(globals: Globals) {
        val chat = LuaTable()
        chat["sendChatMessage"] = object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                MinecraftClient.getInstance().execute {
                    networkHandler?.sendChatMessage(arg.checkjstring())
                }
                return NIL
            }
        }
        chat["sendChatMessageClientOnly"] = object : TwoArgFunction() {
            override fun call(arg1: LuaValue, arg2: LuaValue): LuaValue {
                val msg = arg1 as LuaTable
                val overlay = arg2.optboolean(false)
                MinecraftClient.getInstance().execute {
                    MinecraftClient.getInstance().player
                        ?.sendMessage(msg.toMutableText(), overlay)
                }
                return NIL
            }
        }
        chat["sendChatCommand"] = object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                MinecraftClient.getInstance().execute {
                    networkHandler?.sendChatCommand(arg.checkjstring())
                }
                return NIL
            }
        }
        chat["onChatReceived"] = listenerRegistrar(chatReceivedListeners)
        chat["onGameReceived"] = listenerRegistrar(gameReceivedListeners)
        chat["onAllowChatReceived"] = listenerRegistrar(allowChatListeners)
        chat["onAllowGameReceived"] = listenerRegistrar(allowGameListeners)
        chat["onAllowChatSent"] = listenerRegistrar(allowChatSentListeners)
        chat["onAllowCommandSent"] = listenerRegistrar(allowCommandSentListeners)
        chat["onModifyChatSent"] = listenerRegistrar(modifyChatSentListeners)
        chat["onModifyCommandSent"] = listenerRegistrar(modifyCommandSentListeners)
        chat["onChatSent"] = listenerRegistrar(chatSentListeners)
        chat["onCommandSent"] = listenerRegistrar(commandSentListeners)
        globals["chat"] = chat
    }

    fun dispose() {
        chatReceivedListeners.clear()
        gameReceivedListeners.clear()
        allowChatListeners.clear()
        allowGameListeners.clear()
        allowChatSentListeners.clear()
        allowCommandSentListeners.clear()
        modifyChatSentListeners.clear()
        modifyCommandSentListeners.clear()
        chatSentListeners.clear()
        commandSentListeners.clear()
    }

    // ---- Internal: called by ApiBridge ----

    internal fun fireChatReceived(message: String) =
        chatReceivedListeners.forEach { safeCall1(it, LuaValue.valueOf(message), "chat.onChatReceived") }

    internal fun fireGameReceived(message: String, overlay: Boolean) =
        gameReceivedListeners.forEach { safeCall2(it, LuaValue.valueOf(message), LuaValue.valueOf(overlay), "chat.onGameReceived") }

    internal fun fireAllowChatReceived(message: String): Boolean =
        allowChatListeners.all { safeCallBool1(it, LuaValue.valueOf(message), "chat.onAllowChatReceived") }

    internal fun fireAllowGameReceived(message: String, overlay: Boolean): Boolean =
        allowGameListeners.all { safeCallBool2(it, LuaValue.valueOf(message), LuaValue.valueOf(overlay), "chat.onAllowGameReceived") }

    internal fun fireAllowChatSent(message: String): Boolean =
        allowChatSentListeners.all { safeCallBool1(it, LuaValue.valueOf(message), "chat.onAllowChatSent") }

    internal fun fireAllowCommandSent(message: String): Boolean =
        allowCommandSentListeners.all { safeCallBool1(it, LuaValue.valueOf(message), "chat.onAllowCommandSent") }

    internal fun fireModifyChatSent(message: String): String {
        var result = message
        modifyChatSentListeners.forEach { result = safeCallString1(it, LuaValue.valueOf(result), "chat.onModifyChatSent") }
        return result
    }

    internal fun fireModifyCommandSent(message: String): String {
        var result = message
        modifyCommandSentListeners.forEach { result = safeCallString1(it, LuaValue.valueOf(result), "chat.onModifyCommandSent") }
        return result
    }

    internal fun fireChatSent(message: String) =
        chatSentListeners.forEach { safeCall1(it, LuaValue.valueOf(message), "chat.onChatSent") }

    internal fun fireCommandSent(message: String) =
        commandSentListeners.forEach { safeCall1(it, LuaValue.valueOf(message), "chat.onCommandSent") }

    // ---- Helpers ----

    private fun listenerRegistrar(list: MutableList<LuaValue>) = object : OneArgFunction() {
        override fun call(arg: LuaValue): LuaValue {
            if (arg.isfunction()) list.add(arg)
            return NIL
        }
    }

    private fun safeCall1(fn: LuaValue, a: LuaValue, context: String) {
        try { fn.call(a) } catch (e: Exception) { ErrorReporter.reportRuntimeError(scriptName, context, e) }
    }

    private fun safeCall2(fn: LuaValue, a: LuaValue, b: LuaValue, context: String) {
        try { fn.call(a, b) } catch (e: Exception) { ErrorReporter.reportRuntimeError(scriptName, context, e) }
    }

    private fun safeCallBool1(fn: LuaValue, a: LuaValue, context: String): Boolean =
        try { fn.call(a).toboolean() } catch (e: Exception) { ErrorReporter.reportRuntimeError(scriptName, context, e); true }

    private fun safeCallBool2(fn: LuaValue, a: LuaValue, b: LuaValue, context: String): Boolean =
        try { fn.call(a, b).toboolean() } catch (e: Exception) { ErrorReporter.reportRuntimeError(scriptName, context, e); true }

    private fun safeCallString1(fn: LuaValue, a: LuaValue, context: String): String =
        try { fn.call(a).tojstring() } catch (e: Exception) { ErrorReporter.reportRuntimeError(scriptName, context, e); a.tojstring() }
}
