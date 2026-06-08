package wiam.luarook.lua.api

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaInteger
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.LuaValue.NIL
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.ThreeArgFunction
import org.luaj.vm2.lib.ZeroArgFunction
import wiam.luarook.handler.ClientPlayerHurtHandler
import wiam.luarook.lua.adapt.text.toLuaTable
import wiam.luarook.lua.adapt.toLuaTable
import wiam.luarook.toSlotActionType

object PlayerAPI {
    
    fun inject(globals: Globals) {
        val player = LuaTable()
        player["doAttack"] = object : ZeroArgFunction() {
            override fun call(): LuaValue {
                return valueOf(doAttack())
            }
        }
        player["doItemUse"] = object : ZeroArgFunction() {
            override fun call(): LuaValue {
                doItemUse()
                return NIL
            }
        }
        player["getAsEntity"] = object : ZeroArgFunction() {
            override fun call(): LuaValue {
                return getAsEntity()
            }
        }
        player["getPitch"] = object : ZeroArgFunction() {
            override fun call(): LuaValue {
                val player = MinecraftClient.getInstance().player ?: return NIL
                return valueOf(player.pitch.toDouble())
            }
        }
        player["getYaw"] = object : ZeroArgFunction() {
            override fun call(): LuaValue {
                val player = MinecraftClient.getInstance().player ?: return NIL
                return valueOf(player.yaw.toDouble())
            }
        }
        player["setPitch"] = object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                MinecraftClient.getInstance().player?.pitch = arg.todouble().toFloat()
                return NIL
            }
        }
        player["setYaw"] = object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                MinecraftClient.getInstance().player?.yaw = arg.todouble().toFloat()
                return NIL
            }
        }
        player["setBlockAttacking"] = object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                blockAttackingStatus = arg.toboolean()
                return NIL
            }
        }
        player["getInventoryStacks"] = object : ZeroArgFunction() {
            override fun call(): LuaValue {
                return getInventoryStacks()
            }
        }
        player["getSelectedSlot"] = object : ZeroArgFunction() {
            override fun call(): LuaValue {
                return getSelectedSlot()
            }
        }
        player["clickSlot"] = object : ThreeArgFunction() {
            override fun call(slotId: LuaValue, button: LuaValue, actionType: LuaValue): LuaValue {
                clickSlot(slotId.checkint(), button.checkint(), actionType.checkjstring())
                return NIL
            }
        }
        player["onDamaged"] = object : OneArgFunction() {
            override fun call(listener: LuaValue): LuaValue {
                if (listener.isfunction()) {
                    damagedListeners.add(listener)
                }
                return NIL
            }
        }
        player["onDeath"] = object : OneArgFunction() {
            override fun call(listener: LuaValue): LuaValue {
                if (listener.isfunction()) {
                    deathListeners.add(listener)
                }
                return NIL
            }
        }
        
        globals["player"] = player
    }
    
    init {
        ClientTickEvents.START_CLIENT_TICK.register { _ ->
            if (blockAttackingStatus) {
                MinecraftClient.getInstance().handleBlockBreaking(true)
            }
        }
        processDamaged()
        processDeath()
    }
    
    private fun doAttack(): Boolean {
        return MinecraftClient.getInstance().doAttack()
    }
    
    private fun doItemUse() {
        MinecraftClient.getInstance().doItemUse()
    }
    
    private fun getAsEntity(): LuaValue {
        return MinecraftClient.getInstance().player?.toLuaTable() ?: NIL
    }
    
    private var blockAttackingStatus = false
    private fun getInventoryStacks(): LuaTable {
        val table = LuaTable()
        val slots = MinecraftClient.getInstance().player?.playerScreenHandler?.slots ?: return table
        for (i in slots.indices) {
            table.set(i, slots[i].stack.toLuaTable())
        }
        return table
    }
    
    private fun getSelectedSlot(): LuaValue {
        val slot = MinecraftClient.getInstance().player?.inventory?.selectedSlot ?: return NIL
        return LuaInteger.valueOf(36 + slot)
    }
    
    private fun clickSlot(slotId: Int,button: Int, slotActionType: String) {
        val syncId = MinecraftClient.getInstance().player?.playerScreenHandler?.syncId ?: return
        interactionManager?.clickSlot(
            syncId,
            slotId,
            button,
            slotActionType.toSlotActionType(),
            MinecraftClient.getInstance().player
        )
    }
    
    private val interactionManager
        get() = MinecraftClient.getInstance().interactionManager
    
    private val damagedListeners = mutableListOf<LuaValue>()
    private val deathListeners = mutableListOf<LuaValue>()
    
    private fun processDamaged() {
        ClientPlayerHurtHandler.playerDamaged = {
            for (listener in damagedListeners) {
                listener.call(it.toLuaTable())
            }
        }
    }
    
    private fun processDeath() {
        ClientPlayerHurtHandler.playerDeath = {
            for (listener in deathListeners) {
                listener.call(it.toLuaTable())
            }
        }
    }
}