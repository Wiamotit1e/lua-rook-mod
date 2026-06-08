package wiam.luarook.lua.adapt

import net.minecraft.item.ItemStack
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import kotlin.math.max

fun ItemStack.toLuaTable(): LuaTable {
    val enchantments = LuaTable()
    var index = 1
    this.enchantments.enchantmentEntries.forEach {
        val enchantment: LuaTable = LuaTable().apply {
            set("name", LuaValue.valueOf(it.key.value().description.string))
            set("level", LuaValue.valueOf(it.intValue))
        }
        enchantments.set(index, enchantment)
        index++
    }
    val table: LuaTable = LuaTable().apply {
        set("type", LuaValue.valueOf(item.translationKey))
        set("count", LuaValue.valueOf(count))
        set("maxCount", LuaValue.valueOf(maxCount))
        set("itemName", LuaValue.valueOf(itemName.string))
        set("name", LuaValue.valueOf(name.string))
        set("enchantments", enchantments)
        set("isDamageable", LuaValue.valueOf(isDamageable))
        set("damage", LuaValue.valueOf(damage))
        set("maxDamage", LuaValue.valueOf(maxDamage))
    }
    return table
}