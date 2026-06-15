package wiam.luarook.lua.api

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.LuaValue.NIL
import wiam.luarook.lua.LuaApi
import wiam.luarook.lua.adapt.toLuaTable
import wiam.luarook.lua.adapt.toLuaValueComprehensively

class WorldApi : LuaApi("world") {
    
    private val mc get() = MinecraftClient.getInstance()
    
    override fun register(t: LuaTable) {
        t.fn0("getEntities") {
            val table = LuaTable()
            var index = 1
            mc.world?.entities?.forEach {
                table.set(index, it.toLuaTable())
                index++
            }
            table
        }
        t.fn0("getWeather") {
            val w = mc.world ?: return@fn0 NIL
            when {
                w.isThundering -> LuaValue.valueOf("thunder")
                w.isRaining -> LuaValue.valueOf("rain")
                w.canHaveWeather() -> LuaValue.valueOf("clear")
                else -> LuaValue.valueOf("noWeather")
            }
        }
        t.fn0("getTime") {
            val w = mc.world ?: return@fn0 NIL
            LuaValue.valueOf(w.time.toDouble())
        }
        t.fn0("getTimeOfDay") {
            val w = mc.world ?: return@fn0 NIL
            LuaValue.valueOf(w.timeOfDay.toDouble())
        }
        t.fn0("isInScreen") {
            return@fn0 if (mc.currentScreen != null) LuaValue.TRUE else LuaValue.FALSE
        }
        t.fn0("isInHandledScreen") {
            return@fn0 if (mc.currentScreen is HandledScreen<*>) LuaValue.TRUE else LuaValue.FALSE
        }
        t.fn1("logOut") { reason ->
            mc.execute { mc.disconnect(Text.of(reason.tojstring())) }
            NIL
        }
        t.fn3("getBlock") { x, y, z ->
            val w = mc.world ?: return@fn3 NIL
            val block = w.getBlockState(BlockPos(x.toint(), y.toint(), z.toint())).block.translationKey
            LuaValue.valueOf(block)
        }
        t.fn3("getBlockState") { x, y, z ->
            val w = mc.world ?: return@fn3 NIL
            w.getBlockState(BlockPos(x.toint(), y.toint(), z.toint())).toLuaValueComprehensively()
        }
        t.event("tickStarted")
        t.event("tickEnded")
    }
}
