package wiam.luarook.lua.api

import net.minecraft.client.MinecraftClient
import net.minecraft.entity.damage.DamageSource
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaInteger
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.LuaValue.NIL
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.ThreeArgFunction
import org.luaj.vm2.lib.ZeroArgFunction
import wiam.luarook.lua.adapt.toLuaTable
import wiam.luarook.toSlotActionType

class PlayerApi {

    var blockAttackingStatus = false
        private set

    private val damagedListeners = mutableListOf<LuaValue>()
    private val deathListeners = mutableListOf<LuaValue>()
    private val tickListeners = mutableListOf<LuaValue>()

    private val interactionManager
        get() = MinecraftClient.getInstance().interactionManager

    fun inject(globals: Globals) {
        val player = LuaTable()
        player["doAttack"] = object : ZeroArgFunction() {
            override fun call(): LuaValue = valueOf(MinecraftClient.getInstance().doAttack())
        }
        player["doItemUse"] = object : ZeroArgFunction() {
            override fun call(): LuaValue {
                MinecraftClient.getInstance().doItemUse()
                return NIL
            }
        }
        player["getAsEntity"] = object : ZeroArgFunction() {
            override fun call(): LuaValue =
                MinecraftClient.getInstance().player?.toLuaTable() ?: NIL
        }
        player["getPitch"] = object : ZeroArgFunction() {
            override fun call(): LuaValue {
                val p = MinecraftClient.getInstance().player ?: return NIL
                return valueOf(p.pitch.toDouble())
            }
        }
        player["getYaw"] = object : ZeroArgFunction() {
            override fun call(): LuaValue {
                val p = MinecraftClient.getInstance().player ?: return NIL
                return valueOf(p.yaw.toDouble())
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
                val table = LuaTable()
                val slots = MinecraftClient.getInstance().player?.playerScreenHandler?.slots ?: return table
                for (i in slots.indices) {
                    table.set(i, slots[i].stack.toLuaTable())
                }
                return table
            }
        }
        player["getSelectedSlot"] = object : ZeroArgFunction() {
            override fun call(): LuaValue {
                val slot = MinecraftClient.getInstance().player?.inventory?.selectedSlot ?: return NIL
                return LuaInteger.valueOf(36 + slot)
            }
        }
        player["clickSlot"] = object : ThreeArgFunction() {
            override fun call(slotId: LuaValue, button: LuaValue, actionType: LuaValue): LuaValue {
                val syncId = MinecraftClient.getInstance().player?.playerScreenHandler?.syncId ?: return NIL
                interactionManager?.clickSlot(
                    syncId, slotId.checkint(), button.checkint(),
                    actionType.checkjstring().toSlotActionType(),
                    MinecraftClient.getInstance().player
                )
                return NIL
            }
        }
        player["onDamaged"] = object : OneArgFunction() {
            override fun call(listener: LuaValue): LuaValue {
                if (listener.isfunction()) damagedListeners.add(listener)
                return NIL
            }
        }
        player["onDeath"] = object : OneArgFunction() {
            override fun call(listener: LuaValue): LuaValue {
                if (listener.isfunction()) deathListeners.add(listener)
                return NIL
            }
        }
        player["onClientTick"] = object : OneArgFunction() {
            override fun call(listener: LuaValue): LuaValue {
                if (listener.isfunction()) tickListeners.add(listener)
                return NIL
            }
        }
        globals["player"] = player
    }

    fun dispose() {
        damagedListeners.clear()
        deathListeners.clear()
        tickListeners.clear()
        blockAttackingStatus = false
    }

    internal fun fireDamaged(source: DamageSource) {
        damagedListeners.forEach { fn -> try { fn.call(source.toLuaTable()) } catch (e: Exception) { e.printStackTrace() } }
    }

    internal fun fireDeath(source: DamageSource) {
        deathListeners.forEach { fn -> try { fn.call(source.toLuaTable()) } catch (e: Exception) { e.printStackTrace() } }
    }

    internal fun fireClientTick() {
        tickListeners.forEach { fn -> try { fn.call() } catch (e: Exception) { e.printStackTrace() } }
    }
}
