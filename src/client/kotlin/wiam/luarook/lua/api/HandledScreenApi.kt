package wiam.luarook.lua.api

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.registry.RegistryKeys
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.LuaValue.NIL
import wiam.luarook.lua.LuaApi
import wiam.luarook.lua.adapt.toLuaTable
import wiam.luarook.lua.adapt.toLuaValueComprehensively
import wiam.luarook.toSlotActionType

class HandledScreenApi : LuaApi("handledScreen") {
    
    private val mc get() = MinecraftClient.getInstance()
    
    private val interactionManager get() = mc.interactionManager
    
    private val handledScreen get() = (mc.currentScreen as? HandledScreen<*>)
    
    private val screenHandler get() = handledScreen?.screenHandler
    override fun register(t: LuaTable) {
        t.fn0("getSyncId") {
            val screen = mc.currentScreen
            if (screenHandler != null) {
                LuaValue.valueOf(screenHandler!!.syncId)
            } else {
                NIL
            }
        }
        t.fn0("getAllSlots") {
            val table = LuaTable()
            val slots = screenHandler?.slots ?: return@fn0 table
            for (i in slots.indices) table.set(i, slots[i].stack.toLuaTable())
            table
        }
        t.fn0("getAllSlotsComprehensively") {
            val table = LuaTable()
            val slots = screenHandler?.slots ?: return@fn0 table
            for (i in slots.indices) {
                table.set(i, slots[i].stack.toLuaValueComprehensively())
            }
            table
        }
        t.fn0("getFocusedSlotIndex") {
            val slot = handledScreen?.focusedSlot ?: return@fn0 NIL
            return@fn0 LuaValue.valueOf(slot.index)
        }
        t.fn0("getEnchantmentButtons") {
            val table = LuaTable()
            val screen = mc.currentScreen
            if (screen is EnchantmentScreen) {
                for (i in 0..2) {
                    val enchantmentId = screen.screenHandler!!.enchantmentId[i]
                    val enchantmentName =
                        mc.world?.registryManager?.getOrThrow(RegistryKeys.ENCHANTMENT)?.get(enchantmentId)
                            ?.description()?.string
                    val enchantmentLevel = screen.screenHandler!!.enchantmentLevel[i]
                    table.set(
                        i,
                        LuaTable().apply {
                            set("name", LuaValue.valueOf(enchantmentName ?: "unknown"))
                            set("level", enchantmentLevel)
                        })
                }
                return@fn0 table
            }
            return@fn0 table
        }
        t.fn3("clickSlot") { slotId, button, actionType ->
            if (screenHandler != null) {
                val syncId = screenHandler!!.syncId
                interactionManager?.clickSlot(
                    syncId, slotId.checkint(), button.checkint(),
                    actionType.checkjstring().toSlotActionType(), mc.player
                )
            }
            NIL
        }
        t.fn1("clickButton") { buttonId ->
            if (screenHandler != null) {
                val syncId = screenHandler!!.syncId
                interactionManager?.clickButton(syncId, buttonId.checkint())
            }
            NIL
        }
    }
}