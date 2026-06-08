package wiam.luarook.lua

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.entity.damage.DamageSource
import org.luaj.vm2.LuaValue
import wiam.luarook.lua.adapt.toLuaTable
import wiam.luarook.lua.api.ChatApi
import wiam.luarook.lua.api.PlayerApi
import wiam.luarook.lua.api.TabListApi
import wiam.luarook.lua.api.WorldApi

/**
 * Central event dispatcher. Registers all Fabric events ONCE (via [initialize]),
 * then delegates to active API instances via generic [LuaApi.fire] / [LuaApi.fireAll]
 * / [LuaApi.fireModify] methods.
 */
object ApiBridge {

    private val chatApis = mutableListOf<ChatApi>()
    private val playerApis = mutableListOf<PlayerApi>()
    private val worldApis = mutableListOf<WorldApi>()
    private val tabListApis = mutableListOf<TabListApi>()

    fun register(session: ApiSession) {
        chatApis.add(session.chat)
        playerApis.add(session.player)
        worldApis.add(session.world)
        tabListApis.add(session.tabList)
    }

    fun unregister(session: ApiSession) {
        chatApis.remove(session.chat)
        playerApis.remove(session.player)
        worldApis.remove(session.world)
        tabListApis.remove(session.tabList)
        session.dispose()
    }

    fun clearAll() {
        chatApis.toList().forEach { it.dispose() }
        playerApis.toList().forEach { it.dispose() }
        worldApis.toList().forEach { it.dispose() }
        tabListApis.toList().forEach { it.dispose() }
        chatApis.clear()
        playerApis.clear()
        worldApis.clear()
        tabListApis.clear()
    }

    // ---------- Fabric event registration (called once at mod init) ----------

    fun initialize() {
        registerChatReceived()
        registerGameReceived()
        registerAllowChatReceived()
        registerAllowGameReceived()
        registerAllowChatSent()
        registerAllowCommandSent()
        registerModifyChatSent()
        registerModifyCommandSent()
        registerChatSent()
        registerCommandSent()
        registerWorldTick()
        registerClientTick()
    }

    // ---- ChatApi ----

    private fun registerChatReceived() {
        ClientReceiveMessageEvents.CHAT.register { message, _, _, _, _ ->
            chatApis.forEach { it.fire("chatReceived", LuaValue.valueOf(message.string)) }
        }
    }

    private fun registerGameReceived() {
        ClientReceiveMessageEvents.GAME.register { message, overlay ->
            chatApis.forEach { it.fire("gameReceived", LuaValue.valueOf(message.string), LuaValue.valueOf(overlay)) }
        }
    }

    private fun registerAllowChatReceived() {
        ClientReceiveMessageEvents.ALLOW_CHAT.register { message, _, _, _, _ ->
            chatApis.all { it.fireAll("allowChatReceived", LuaValue.valueOf(message.string)) }
        }
    }

    private fun registerAllowGameReceived() {
        ClientReceiveMessageEvents.ALLOW_GAME.register { message, overlay ->
            chatApis.all { it.fireAll("allowGameReceived", LuaValue.valueOf(message.string), LuaValue.valueOf(overlay)) }
        }
    }

    private fun registerAllowChatSent() {
        ClientSendMessageEvents.ALLOW_CHAT.register { message ->
            chatApis.all { it.fireAll("allowChatSent", LuaValue.valueOf(message)) }
        }
    }

    private fun registerAllowCommandSent() {
        ClientSendMessageEvents.ALLOW_COMMAND.register { message ->
            chatApis.all { it.fireAll("allowCommandSent", LuaValue.valueOf(message)) }
        }
    }

    private fun registerModifyChatSent() {
        ClientSendMessageEvents.MODIFY_CHAT.register { message ->
            var result = message
            chatApis.forEach { result = it.fireModify("modifyChatSent", result) }
            result
        }
    }

    private fun registerModifyCommandSent() {
        ClientSendMessageEvents.MODIFY_COMMAND.register { message ->
            var result = message
            chatApis.forEach { result = it.fireModify("modifyCommandSent", result) }
            result
        }
    }

    private fun registerChatSent() {
        ClientSendMessageEvents.CHAT.register { message ->
            chatApis.forEach { it.fire("chatSent", LuaValue.valueOf(message)) }
        }
    }

    private fun registerCommandSent() {
        ClientSendMessageEvents.COMMAND.register { message ->
            chatApis.forEach { it.fire("commandSent", LuaValue.valueOf(message)) }
        }
    }

    // ---- WorldApi ----

    private fun registerWorldTick() {
        ClientTickEvents.START_WORLD_TICK.register { _ ->
            worldApis.forEach { it.fire("tickStarted") }
        }
        ClientTickEvents.END_WORLD_TICK.register { _ ->
            worldApis.forEach { it.fire("tickEnded") }
        }
    }

    // ---- PlayerApi ----

    private fun registerClientTick() {
        ClientTickEvents.START_CLIENT_TICK.register { _ ->
            var shouldMine = false
            playerApis.forEach { if (it.blockAttackingStatus) shouldMine = true }
            if (shouldMine) {
                MinecraftClient.getInstance().handleBlockBreaking(true)
            }
            playerApis.forEach { it.fire("clientTick") }
        }
    }

    // ---- Called by mixins (LivingEntityMixin) ----

    @JvmStatic
    fun onPlayerDamaged(damageSource: DamageSource) {
        playerApis.forEach { it.fire("damaged", damageSource.toLuaTable()) }
    }

    @JvmStatic
    fun onPlayerDeath(damageSource: DamageSource) {
        playerApis.forEach { it.fire("death", damageSource.toLuaTable()) }
    }

    // ---- Called by mixin (ClientPlayerNetworkHandlerMixin) ----

    @JvmStatic
    fun onPlayerListEntriesModified(
        entries: MutableList<PlayerListEntry>,
        index: Int,
        entry: PlayerListEntry
    ) {
        var current = entry
        tabListApis.forEach { api ->
            current = api.firePlayerListEntryModified(entries, index, current)
        }
    }
}
