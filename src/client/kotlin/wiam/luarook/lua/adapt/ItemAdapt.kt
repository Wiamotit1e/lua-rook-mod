package wiam.luarook.lua.adapt

import net.minecraft.client.MinecraftClient
import net.minecraft.component.DataComponentTypes
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.text.Text
import net.minecraft.util.Identifier
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
        set("id", LuaValue.valueOf(Registries.ITEM.getId(item).toString()))
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

fun LuaTable.toItemStack(): ItemStack? {
    val idStr = this.get("id")?.tojstring() ?: return null
    val item = Registries.ITEM.get(Identifier.of(idStr))
    val count = this.get("count")?.toint()?.coerceIn(1, item.maxCount) ?: 1
    val stack = ItemStack(item, count)
    val damage = this.get("damage")?.toint()
    if (damage != null && damage > 0 && stack.isDamageable) {
        stack.setDamage(damage.coerceAtMost(stack.maxDamage))
    }
    val customName = this.get("itemName")?.tojstring()
    if (customName != null) {
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(customName))
    }
    val enchantments = this.get("enchantments") as? LuaTable
    if (enchantments != null) {
        val enchantmentRegistry = MinecraftClient.getInstance().world
            ?.registryManager
            ?.getOrThrow(RegistryKeys.ENCHANTMENT)
            ?: return stack
        for (i in 1..enchantments.length()) {
            val e = enchantments[i] as? LuaTable ?: continue
            val enchId = e.get("id")?.tojstring() ?: continue
            val level = e.get("level")?.toint() ?: 1
            val entry: RegistryEntry<Enchantment> = enchantmentRegistry
                .getEntry(Identifier.of(enchId))
                .orElse(null) ?: continue
            stack.addEnchantment(entry, level)
        }
    }
    
    return stack
}