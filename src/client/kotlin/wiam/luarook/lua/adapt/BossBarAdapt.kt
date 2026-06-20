package wiam.luarook.lua.adapt

import net.minecraft.entity.boss.BossBar
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import wiam.luarook.lua.adapt.text.toLuaTable

fun BossBar.toLuaTable(): LuaTable {
    val table = LuaTable()
    table.set("name", name.copy().toLuaTable())
    table.set("percent", LuaValue.valueOf(percent.toDouble()))
    table.set("color", LuaValue.valueOf(color.name))
    table.set("style", LuaValue.valueOf(style.name))
    table.set("darkenSky", LuaValue.valueOf(shouldDarkenSky()))
    table.set("dragonMusic", LuaValue.valueOf(hasDragonMusic()))
    table.set("thickenFog", LuaValue.valueOf(shouldThickenFog()))
    return table
}