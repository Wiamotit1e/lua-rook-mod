package wiam.luarook.lua.api

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.LuaValue.NIL
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.ZeroArgFunction
import wiam.luarook.lua.adapt.text.toLuaTable
import wiam.luarook.lua.adapt.toLuaTable

object WorldAPI {
    
    fun inject(globals: Globals) {
        val world = LuaTable()
        world["getEntities"] = object : ZeroArgFunction() {
            override fun call(): LuaValue {
                return getEntities()
            }
        }
        world["getWeather"] = object : ZeroArgFunction() {
            override fun call(): LuaValue {
                return getWeather()
            }
        }
        world["getTime"] = object : ZeroArgFunction() {
            override fun call(): LuaValue {
                return getTime()
            }
        }
        world["getTimeOfDay"] = object : ZeroArgFunction() {
            override fun call(): LuaValue {
                return getTimeOfDay()
            }
        }
        world["logOut"] = object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                logOut(arg.tojstring())
                return NIL
            }
        }
        world["onTickStarted"] = object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                if (arg.isfunction()) {
                    tickStartedListeners.add(arg)
                }
                return NIL
            }
        }
        world["onTickEnded"] = object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                if (arg.isfunction()) {
                    tickEndedListeners.add(arg)
                }
                return NIL
            }
        }
        globals["world"] = world
    }
    init {
        processTickStarted()
        processTickEnded()
    }
    
    private fun getEntities(): LuaTable {
        val table = LuaTable()
        var index = 1
        MinecraftClient.getInstance().world?.entities?.forEach {
            val v1 = it.toLuaTable()
            table.set(index, v1)
            index++
        }
        return table
    }
    
    private fun getWeather(): LuaValue {
        val world = MinecraftClient.getInstance().world ?: return NIL
        return when {
            world.isThundering -> LuaValue.valueOf("thunder")
            world.isRaining -> LuaValue.valueOf("rain")
            world.canHaveWeather() -> LuaValue.valueOf("clear")
            else -> LuaValue.valueOf("noWeather")
        }
    }
    
    private fun getTime(): LuaValue {
        val world = MinecraftClient.getInstance().world ?: return NIL
        return LuaValue.valueOf(world.time.toDouble())
    }
    
    private fun getTimeOfDay(): LuaValue {
        val world = MinecraftClient.getInstance().world ?: return NIL
        return LuaValue.valueOf(world.timeOfDay.toDouble())
    }
    
    private fun logOut(message: String) {
        MinecraftClient.getInstance().disconnect(Text.of(message))
    }
    
    private val tickStartedListeners = mutableListOf<LuaValue>()
    private val tickEndedListeners = mutableListOf<LuaValue>()
    
    private fun processTickStarted() {
        ClientTickEvents.START_WORLD_TICK.register { _ ->
            tickStartedListeners.forEach { listener ->
                if (listener.isfunction()) {
                    try {
                        listener.call()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    
    private fun processTickEnded() {
        ClientTickEvents.END_WORLD_TICK.register { _ ->
            tickEndedListeners.forEach { listener ->
                if (listener.isfunction()) {
                    try {
                        listener.call()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}