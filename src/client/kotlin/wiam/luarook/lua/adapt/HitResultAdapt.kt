package wiam.luarook.lua.adapt

import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue

fun HitResult.toLuaTable(): LuaTable {
    val table = LuaTable()
    when (this.type) {
        HitResult.Type.BLOCK -> {
            table.apply {
                val v1 = this@toLuaTable as BlockHitResult
                set("type", LuaValue.valueOf("block"))
                set("side", LuaValue.valueOf(v1.side.name))
                set("bx", v1.blockPos.x)
                set("by", v1.blockPos.y)
                set("bz", v1.blockPos.z)
                set("x", v1.pos.x)
                set("y", v1.pos.y)
                set("z", v1.pos.z)
            }
        }
        HitResult.Type.ENTITY -> {
            table.apply {
                val v1 = this@toLuaTable as EntityHitResult
                set("type", LuaValue.valueOf("entity"))
                set("entity", v1.entity.toLuaTable())
                set("x", v1.pos.x)
                set("y", v1.pos.y)
                set("z", v1.pos.z)
            }
        }
        else -> {
            table.apply {
                set("type", LuaValue.valueOf("miss"))
                set("x", this@toLuaTable.pos.x)
                set("y", this@toLuaTable.pos.y)
                set("z", this@toLuaTable.pos.z)
            }
        }
    }
    return table
}