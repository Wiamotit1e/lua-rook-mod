package wiam.luarook.lua.api

import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.LuaValue.NIL
import wiam.luarook.lua.LuaApi
import wiam.luarook.lua.adapt.text.toMutableText

class ChatApi : LuaApi("chat") {

    private val mc get() = MinecraftClient.getInstance()
    private val networkHandler: ClientPlayNetworkHandler?
        get() = mc.networkHandler

    override fun register(t: LuaTable) {
        t.fn1("sendChatMessage") { msg ->
            mc.execute { networkHandler?.sendChatMessage(msg.checkjstring()) }
            NIL
        }
        t.fn2("sendChatMessageClientOnly") { msg, overlay ->
            mc.execute {
                mc.player?.sendMessage(
                    (msg as LuaTable).toMutableText(),
                    overlay.optboolean(false)
                )
            }
            NIL
        }
        t.fn1("sendChatCommand") { cmd ->
            mc.execute { networkHandler?.sendChatCommand(cmd.checkjstring()) }
            NIL
        }
        t.event("chatReceived")
        t.event("gameReceived")
        t.event("allowChatReceived")
        t.event("allowGameReceived")
        t.event("allowChatSent")
        t.event("allowCommandSent")
        t.event("modifyChatSent")
        t.event("modifyCommandSent")
        t.event("chatSent")
        t.event("commandSent")
    }
}
