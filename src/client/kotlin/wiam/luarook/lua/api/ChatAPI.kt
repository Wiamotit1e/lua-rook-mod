package wiam.luarook.lua.api

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.text.Text
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.TwoArgFunction
import wiam.luarook.lua.GlobalsCollector
import wiam.luarook.lua.adapt.text.toMutableText

object ChatAPI {
    
    fun inject(globals: Globals) {
        val chat = LuaTable()
        chat["sendChatMessage"] = object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                val msg = arg.checkjstring()
                MinecraftClient.getInstance().execute {
                    sendChatMessage(msg)
                }
                return NIL
            }
        }
        
        chat["sendChatMessageClientOnly"] = object : TwoArgFunction() {
            override fun call(arg1: LuaValue, arg2: LuaValue): LuaValue {
                val msg = arg1 as LuaTable
                val overlay = arg2.optboolean(false)
                MinecraftClient.getInstance().execute {
                    sendChatMessageClientOnly(msg.toMutableText(), overlay)
                }
                return NIL
            }
        }
        
        chat["sendChatCommand"] = object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                val cmd = arg.checkjstring()
                MinecraftClient.getInstance().execute {
                    sendChatCommand(cmd)
                }
                return NIL
            }
        }
        chat["onChatReceived"] = object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                if (arg.isfunction()) {
                    chatReceivedListeners.add(arg)
                }
                return NIL
            }
        }
        chat["onGameReceived"] = object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                if (arg.isfunction()) {
                    gameReceivedListeners.add(arg)
                }
                return NIL
            }
        }
        chat["onAllowChatReceived"] = object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                if (arg.isfunction()) {
                    allowChatListeners.add(arg)
                }
                return NIL
            }
        }
        chat["onAllowGameReceived"] = object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                if (arg.isfunction()) {
                    allowGameListeners.add(arg)
                }
                return NIL
            }
        }
        chat["onAllowChatSent"] = object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                if (arg.isfunction()) {
                    allowChatSentListeners.add(arg)
                }
                return NIL
            }
        }
        chat["onAllowCommandSent"] = object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                if (arg.isfunction()) {
                    allowCommandSentListeners.add(arg)
                }
                return NIL
            }
        }
        chat["onModifyChatSent"] = object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                if (arg.isfunction()) {
                    modifyChatSentListeners.add(arg)
                }
                return NIL
            }
        }
        chat["onModifyCommandSent"] = object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                if (arg.isfunction()) {
                    modifyCommandSentListeners.add(arg)
                }
                return NIL
            }
        }
        chat["onChatSent"] = object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                if (arg.isfunction()) {
                    chatSentListeners.add(arg)
                }
                return NIL
            }
        }
        chat["onCommandSent"] = object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                if (arg.isfunction()) {
                    commandSentListeners.add(arg)
                }
                return NIL
            }
        }
        globals["chat"] = chat
    }
    
    init {
        processChatReceived()
        processGameReceived()
        processAllowChatReceived()
        processAllowGameReceived()
        processAllowChatSent()
        processAllowCommandSent()
        processModifyChatSent()
        processModifyCommandSent()
        processChatSent()
        processCommandSent()
    }
    
    private val clientNetWorkHandler: ClientPlayNetworkHandler?
        get() = MinecraftClient.getInstance().networkHandler
    
    private fun sendChatMessage(message: String) {
        clientNetWorkHandler?.sendChatMessage(message)
    }
    
    private fun sendChatMessageClientOnly(message: Text, overlay: Boolean) {
        MinecraftClient.getInstance().player?.sendMessage(message, overlay)
    }
    
    private fun sendChatCommand(command: String) {
        clientNetWorkHandler?.sendChatCommand(command)
    }
    
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
    
    private fun processChatReceived() {
        ClientReceiveMessageEvents.CHAT.register { message, _, _, _, _ ->
            chatReceivedListeners.forEach { listener ->
                if (listener.isfunction()) {
                    try {
                        listener.call(LuaValue.valueOf(message.string))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    
    private fun processGameReceived() {
        ClientReceiveMessageEvents.GAME.register { message, overlay ->
            gameReceivedListeners.forEach { listener ->
                if (listener.isfunction()) {
                    try {
                        listener.call(LuaValue.valueOf(message.string), LuaValue.valueOf(overlay))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    
    private fun processAllowChatReceived() {
        ClientReceiveMessageEvents.ALLOW_CHAT.register { message, _, _, _, _ ->
            var result = true
            allowChatListeners.forEach { listener ->
                if (listener.isfunction()) {
                    try {
                        val value = listener.call(LuaValue.valueOf(message.string))
                        if (!value.toboolean()) {
                            result = false
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            result
        }
    }
    
    private fun processAllowGameReceived() {
        ClientReceiveMessageEvents.ALLOW_GAME.register { message, overlay ->
            var result = true
            allowGameListeners.forEach { listener ->
                if (listener.isfunction()) {
                    try {
                        val value = listener.call(LuaValue.valueOf(message.string), LuaValue.valueOf(overlay))
                        if (!value.toboolean()) {
                            result = false
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            result
        }
        
    }
    
    private fun processAllowChatSent() {
        ClientSendMessageEvents.ALLOW_CHAT.register { message ->
            var result = true
            allowChatSentListeners.forEach { listener ->
                if (listener.isfunction()) {
                    try {
                        val value = listener.call(LuaValue.valueOf(message))
                        if (!value.toboolean()) {
                            result = false
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            result
        }
    }
    
    private fun processAllowCommandSent() {
        ClientSendMessageEvents.ALLOW_COMMAND.register { message ->
            var result = true
            allowCommandSentListeners.forEach { listener ->
                if (listener.isfunction()) {
                    try {
                        val value = listener.call(LuaValue.valueOf(message))
                        if (!value.toboolean()) {
                            result = false
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            result
        }
    }
    
    private fun processModifyChatSent() {
        ClientSendMessageEvents.MODIFY_CHAT.register { message ->
            var v1 = message
            modifyChatSentListeners.forEach { listener ->
                if (listener.isfunction()) {
                    try {
                        val value = listener.call(LuaValue.valueOf(v1))
                        v1 = value.tojstring()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            v1
        }
    }
    
    private fun processModifyCommandSent() {
        ClientSendMessageEvents.MODIFY_COMMAND.register { message ->
            var v1 = message
            modifyCommandSentListeners.forEach { listener ->
                if (listener.isfunction()) {
                    try {
                        val value = listener.call(LuaValue.valueOf(v1))
                        v1 = value.tojstring()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            v1
        }
    }
    
    private fun processChatSent() {
        ClientSendMessageEvents.CHAT.register { message ->
            chatSentListeners.forEach { listener ->
                if (listener.isfunction()) {
                    try {
                        listener.call(LuaValue.valueOf(message))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    
    private fun processCommandSent() {
        ClientSendMessageEvents.COMMAND.register { message ->
            commandSentListeners.forEach { listener ->
                if (listener.isfunction()) {
                    try {
                        listener.call(LuaValue.valueOf(message))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}
