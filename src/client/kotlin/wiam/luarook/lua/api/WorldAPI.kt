package wiam.luarook.lua.api

import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.LuaValue.NIL
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.ZeroArgFunction
import wiam.luarook.lua.adapt.toLuaTable

class WorldApi {

    private val tickStartedListeners = mutableListOf<LuaValue>()
    private val tickEndedListeners = mutableListOf<LuaValue>()

    fun inject(globals: Globals) {
        val world = LuaTable()
        world["getEntities"] = object : ZeroArgFunction() {
            override fun call(): LuaValue {
                val table = LuaTable()
                var index = 1
                MinecraftClient.getInstance().world?.entities?.forEach {
                    table.set(index, it.toLuaTable())
                    index++
                }
                return table
            }
        }
        world["getWeather"] = object : ZeroArgFunction() {
            override fun call(): LuaValue {
                val w = MinecraftClient.getInstance().world ?: return NIL
                return when {
                    w.isThundering -> LuaValue.valueOf("thunder")
                    w.isRaining -> LuaValue.valueOf("rain")
                    w.canHaveWeather() -> LuaValue.valueOf("clear")
                    else -> LuaValue.valueOf("noWeather")
                }
            }
        }
        world["getTime"] = object : ZeroArgFunction() {
            override fun call(): LuaValue {
                val w = MinecraftClient.getInstance().world ?: return NIL
                return LuaValue.valueOf(w.time.toDouble())
            }
        }
        world["getTimeOfDay"] = object : ZeroArgFunction() {
            override fun call(): LuaValue {
                val w = MinecraftClient.getInstance().world ?: return NIL
                return LuaValue.valueOf(w.timeOfDay.toDouble())
            }
        }
        world["logOut"] = object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                MinecraftClient.getInstance().execute {
                    MinecraftClient.getInstance().disconnect(Text.of(arg.tojstring()))
                }
                return NIL
            }
        }
        world["onTickStarted"] = object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                if (arg.isfunction()) tickStartedListeners.add(arg)
                return NIL
            }
        }
        world["onTickEnded"] = object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                if (arg.isfunction()) tickEndedListeners.add(arg)
                return NIL
            }
        }
        globals["world"] = world
    }

    fun dispose() {
        tickStartedListeners.clear()
        tickEndedListeners.clear()
    }

    internal fun fireTickStarted() {
        tickStartedListeners.forEach { fn -> try { fn.call() } catch (e: Exception) { e.printStackTrace() } }
    }

    internal fun fireTickEnded() {
        tickEndedListeners.forEach { fn -> try { fn.call() } catch (e: Exception) { e.printStackTrace() } }
    }
}
