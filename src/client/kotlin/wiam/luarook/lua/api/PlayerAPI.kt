package wiam.luarook.lua.api

import net.minecraft.client.MinecraftClient
import org.luaj.vm2.LuaInteger
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.LuaValue.NIL
import wiam.luarook.lua.LuaApi
import wiam.luarook.lua.adapt.toItemStackComprehensively
import wiam.luarook.lua.adapt.toLuaTable
import wiam.luarook.lua.adapt.toLuaValueComprehensively
import wiam.luarook.toSlotActionType

class PlayerApi : LuaApi("player") {

    var blockAttackingStatus = false
        private set

    private val mc get() = MinecraftClient.getInstance()
    private val interactionManager get() = mc.interactionManager

    override fun register(t: LuaTable) {
        t.fn0("doAttack") { LuaValue.valueOf(mc.doAttack()) }
        t.fn0("doItemUse") { mc.doItemUse(); NIL }
        t.fn0("getAsEntity") { mc.player?.toLuaTable() ?: NIL }
        t.fn0("getPitch") { mc.player?.let { LuaValue.valueOf(it.pitch.toDouble()) } ?: NIL }
        t.fn0("getYaw")   { mc.player?.let { LuaValue.valueOf(it.yaw.toDouble()) } ?: NIL }
        t.fn1("setPitch") { mc.player?.pitch = it.todouble().toFloat(); NIL }
        t.fn1("setYaw")   { mc.player?.yaw = it.todouble().toFloat(); NIL }
        t.fn1("setBlockAttacking") { blockAttackingStatus = it.toboolean(); NIL }
        t.fn0("getInventoryStacks") {
            val table = LuaTable()
            val slots = mc.player?.playerScreenHandler?.slots ?: return@fn0 table
            for (i in slots.indices) table.set(i, slots[i].stack.toLuaTable())
            table
        }
        t.fn0("getInventoryStacksComprehensively") {
            val table = LuaTable()
            val slots = mc.player?.playerScreenHandler?.slots ?: return@fn0 table
            for (i in slots.indices) {
                table.set(i, slots[i].stack.toLuaValueComprehensively())
            }
            table
        }
        t.fn0("getSelectedSlot") {
            val slot = mc.player?.inventory?.selectedSlot ?: return@fn0 NIL
            LuaInteger.valueOf(36 + slot)
        }
        t.fn3("clickSlot") { slotId, button, actionType ->
            val syncId = mc.player?.playerScreenHandler?.syncId ?: return@fn3 NIL
            interactionManager?.clickSlot(
                syncId, slotId.checkint(), button.checkint(),
                actionType.checkjstring().toSlotActionType(), mc.player
            )
            NIL
        }
        t.event("damaged")
        t.event("death")
        t.event("clientTick")
    }
}
