package wiam.luarook.lua

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.entity.damage.DamageSource
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket
import org.luaj.vm2.LuaValue
import org.slf4j.LoggerFactory
import wiam.luarook.lua.adapt.text.toLuaValueComprehensively
import wiam.luarook.lua.adapt.toLuaTable

/**
 * Central event dispatcher. Registers all Fabric events ONCE (via [initialize]),
 * then delegates to active [ApiSession] instances.
 *
 * A single [MutableList] of sessions is the **only** state — event handlers
 * reach the correct API via the session property (e.g. `s.chat`, `s.hud`).
 * Adding a new API requires adding a field to [ApiSession] and wiring Fabric
 * events here — but there is **no** separate list to forget.
 */
object ApiBridge {
    
    private val sessions = mutableListOf<ApiSession>()
    
    fun register(session: ApiSession) {
        sessions.add(session)
    }
    
    fun unregister(session: ApiSession) {
        sessions.remove(session)
        session.dispose()
    }
    
    fun clearAll() {
        sessions.toList().forEach { it.dispose() }
        sessions.clear()
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
            sessions.forEach {
                it.chat.fire(
                    "chatReceived",
                    LuaValue.valueOf(message.string),
                    message.toLuaValueComprehensively()
                )
            }
        }
    }
    
    private fun registerGameReceived() {
        ClientReceiveMessageEvents.GAME.register { message, overlay ->
            sessions.forEach {
                it.chat.fire(
                    "gameReceived",
                    LuaValue.valueOf(message.string),
                    LuaValue.valueOf(overlay),
                    message.toLuaValueComprehensively()
                )
            }
        }
    }
    
    private fun registerAllowChatReceived() {
        ClientReceiveMessageEvents.ALLOW_CHAT.register { message, _, _, _, _ ->
            sessions.all {
                it.chat.fireAll(
                    "allowChatReceived",
                    LuaValue.valueOf(message.string),
                    message.toLuaValueComprehensively()
                )
            }
        }
    }
    
    private fun registerAllowGameReceived() {
        ClientReceiveMessageEvents.ALLOW_GAME.register { message, overlay ->
            sessions.all {
                it.chat.fireAll(
                    "allowGameReceived",
                    LuaValue.valueOf(message.string),
                    LuaValue.valueOf(overlay),
                    message.toLuaValueComprehensively()
                )
            }
        }
    }
    
    private fun registerAllowChatSent() {
        ClientSendMessageEvents.ALLOW_CHAT.register { message ->
            sessions.all { it.chat.fireAll("allowChatSent", LuaValue.valueOf(message)) }
        }
    }
    
    private fun registerAllowCommandSent() {
        ClientSendMessageEvents.ALLOW_COMMAND.register { message ->
            sessions.all { it.chat.fireAll("allowCommandSent", LuaValue.valueOf(message)) }
        }
    }
    
    private fun registerModifyChatSent() {
        ClientSendMessageEvents.MODIFY_CHAT.register { message ->
            var result = message
            sessions.forEach { result = it.chat.fireModify("modifyChatSent", result) }
            result
        }
    }
    
    private fun registerModifyCommandSent() {
        ClientSendMessageEvents.MODIFY_COMMAND.register { message ->
            var result = message
            sessions.forEach { result = it.chat.fireModify("modifyCommandSent", result) }
            result
        }
    }
    
    private fun registerChatSent() {
        ClientSendMessageEvents.CHAT.register { message ->
            sessions.forEach { it.chat.fire("chatSent", LuaValue.valueOf(message)) }
        }
    }
    
    private fun registerCommandSent() {
        ClientSendMessageEvents.COMMAND.register { message ->
            sessions.forEach { it.chat.fire("commandSent", LuaValue.valueOf(message)) }
        }
    }
    
    // ---- WorldApi ----
    
    private fun registerWorldTick() {
        ClientTickEvents.START_WORLD_TICK.register { _ ->
            sessions.forEach { it.world.fire("tickStarted") }
        }
        ClientTickEvents.END_WORLD_TICK.register { _ ->
            sessions.forEach { it.world.fire("tickEnded") }
        }
    }
    
    // ---- PlayerApi ----
    
    private fun registerClientTick() {
        ClientTickEvents.START_CLIENT_TICK.register { _ ->
            var shouldMine = false
            sessions.forEach { if (it.player.blockAttackingStatus) shouldMine = true }
            if (shouldMine) {
                MinecraftClient.getInstance().handleBlockBreaking(true)
            }
            sessions.forEach { it.player.fire("clientTick") }
        }
    }
    
    // ---- Called by mixins (LivingEntityMixin) ----
    
    @JvmStatic
    fun onPlayerDamaged(damageSource: DamageSource) {
        sessions.forEach { it.player.fire("damaged", damageSource.toLuaTable()) }
    }
    
    @JvmStatic
    fun onPlayerDeath(damageSource: DamageSource) {
        sessions.forEach { it.player.fire("death", damageSource.toLuaTable()) }
    }
    
    // ---- Called by mixin (ClientPlayerNetworkHandlerMixin) ----
    
    @JvmStatic
    fun onPlayerListEntriesModified(
        entries: MutableList<PlayerListEntry>,
        index: Int,
        entry: PlayerListEntry
    ) {
        var current = entry
        for (session in sessions) {
            val sizeBefore = entries.size
            current = session.tabList.firePlayerListEntryModified(entries, index, current)
            if (entries.size != sizeBefore) return  // entry removed — stop chaining
        }
    }
    
    @JvmStatic
    fun onEntityStatusPacketReceived(packet: EntityStatusS2CPacket) {
        sessions.forEach { it.world.fire("entityStatusPacketReceived", packet.getEntity(MinecraftClient.getInstance().world)?.toLuaTable() ?: LuaValue.NIL,
            LuaValue.valueOf(packet.status.toDouble())) }
    }
    
    // ---- Called by mixin (InGameHudMixin) ----
    
    @JvmStatic
    fun onHudRendered(context: DrawContext, tickCounter: RenderTickCounter) {
        sessions.forEach { it.hud.fireHudRendered(context, tickCounter) }
    }
}
