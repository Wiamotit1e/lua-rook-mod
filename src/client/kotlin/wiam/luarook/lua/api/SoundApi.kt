package wiam.luarook.lua.api

import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.sound.SoundInstance
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Identifier
import net.minecraft.util.math.random.Random
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.LuaValue.NIL
import wiam.luarook.lua.LuaApi

class SoundApi : LuaApi("sound") {

    private val mc get() = MinecraftClient.getInstance()

    override fun register(t: LuaTable) {
        t.fn1("play") { arg ->
            when {
                arg.isstring() -> playSimple(arg.tojstring())
                arg.istable() -> playFromTable(arg as LuaTable)
                else -> NIL
            }
        }
    }

    private fun playSimple(idStr: String): LuaValue {
        val id = Identifier.of(idStr)
        val instance = PositionedSoundInstance(
            id,
            SoundCategory.AMBIENT,
            1.0f, 1.0f,
            Random.create(),
            false, 0,
            SoundInstance.AttenuationType.NONE,
            0.0, 0.0, 0.0,
            true
        )
        mc?.soundManager?.play(instance)
        return NIL
    }

    private fun playFromTable(t: LuaTable): LuaValue {
        val idStr = t.get("id").tojstring() ?: return NIL
        val id = Identifier.of(idStr)

        val category = try {
            SoundCategory.valueOf((t.get("category").tojstring() ?: "ambient").uppercase())
        } catch (e: IllegalArgumentException) {
            SoundCategory.AMBIENT
        }

        val volume = t.get("volume").tofloat().let { if (it == 0.0f) 1.0f else it }
        val pitch = t.get("pitch").tofloat().let { if (it == 0.0f) 1.0f else it }

        val repeat = t.get("repeat").toboolean()
        val repeatDelay = t.get("repeatDelay").toint()

        val attenuation = try {
            SoundInstance.AttenuationType.valueOf((t.get("attenuation").tojstring() ?: "none").uppercase())
        } catch (e: IllegalArgumentException) {
            SoundInstance.AttenuationType.NONE
        }

        val x = t.get("x").todouble()
        val y = t.get("y").todouble()
        val z = t.get("z").todouble()
        val relative = t.get("relative").toboolean()

        val instance = PositionedSoundInstance(
            id, category, volume, pitch,
            Random.create(),
            repeat, repeatDelay,
            attenuation,
            x, y, z,
            relative
        )
        mc?.soundManager?.play(instance)
        return NIL
    }
}