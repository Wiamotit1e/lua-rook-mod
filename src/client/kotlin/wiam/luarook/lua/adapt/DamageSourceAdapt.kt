package wiam.luarook.lua.adapt

import net.minecraft.entity.Entity
import net.minecraft.entity.damage.DamageSource
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue

fun DamageSource.toLuaTable(): LuaTable {
    val table = LuaTable().apply {
        set("type", type.msgId)
        set("attacker", attacker?.toLuaTable() ?: LuaValue.NIL)
        set("source", source?.toLuaTable() ?: LuaValue.NIL)
        if (position != null){
            set("x", LuaValue.valueOf(position!!.x))
            set("y", LuaValue.valueOf(position!!.y))
            set("z", LuaValue.valueOf(position!!.z))
        } else {
            set("x", LuaValue.NIL)
            set("y", LuaValue.NIL)
            set("z", LuaValue.NIL)
        }
    }
    return table
}