package wiam.luarook.lua.api

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.registry.RegistryKeys
import net.minecraft.screen.AnvilScreenHandler
import net.minecraft.screen.EnchantmentScreenHandler
import net.minecraft.screen.MerchantScreenHandler
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
        t.fn0("getFocusedSlotId") {
            val slot = handledScreen?.focusedSlot ?: return@fn0 NIL
            return@fn0 LuaValue.valueOf(slot.id)
        }
        t.fn0("getEnchantmentButtons") {
            val table = LuaTable()
            val v1 = screenHandler as? EnchantmentScreenHandler ?: return@fn0 table
            for (i in 0..2) {
                val enchantmentId = v1.enchantmentId[i]
                val enchantmentIdName =
                    mc.world?.registryManager?.getOrThrow(RegistryKeys.ENCHANTMENT)?.getEntry(enchantmentId)
                        ?.orElse(null)?.idAsString ?: "unknown"
                val enchantmentLevel = v1.enchantmentLevel[i]
                table.set(
                    i,
                    LuaTable().apply {
                        set("id", LuaValue.valueOf(enchantmentIdName))
                        set("level", enchantmentLevel)
                    })
            }
            return@fn0 table
        }
        t.fn0("getTradeOffers") {
            val table = LuaTable()
            val v1 = screenHandler as? MerchantScreenHandler ?: return@fn0 table
            val v2 = v1.recipes
            for (i in v2.indices) {
                table.set(i, v2[i].toLuaTable())
            }
            return@fn0 table
        }
        t.fn1("setRecipeIndex") {
            val tradeOfferIndex = if (it.isint()) it.toint() else return@fn1 NIL
            val v1 = screenHandler as? MerchantScreenHandler ?: return@fn1 NIL
            v1.setRecipeIndex(tradeOfferIndex)
            NIL
        }
        t.fn1("setNewItemName") {
            val newItemName = if (it.isstring()) it.tojstring() else return@fn1 NIL
            val v1 = screenHandler as? AnvilScreenHandler ?: return@fn1 NIL
            v1.setNewItemName(newItemName)
            NIL
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