package wiam.luarook.lua.adapt

import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue

fun Entity.toLuaTable(): LuaTable {
    val username = if(this is PlayerEntity) LuaValue.valueOf(this.name.string) else LuaValue.NIL
    val type = LuaValue.valueOf(this.type.name.string)
    val maxHealth = if(this is LivingEntity) LuaValue.valueOf(this.maxHealth.toDouble()) else LuaValue.NIL
    val health = if(this is LivingEntity) LuaValue.valueOf(this.health.toDouble()) else LuaValue.NIL
    val equipment: LuaTable = LuaTable().apply {
        set("mainHand",
            if (this@toLuaTable is LivingEntity) this@toLuaTable.getEquippedStack(EquipmentSlot.MAINHAND)
                .toLuaTable() else LuaValue.NIL
        )
        set("offHand",
            if (this@toLuaTable is LivingEntity) this@toLuaTable.getEquippedStack(EquipmentSlot.OFFHAND)
                .toLuaTable() else LuaValue.NIL
        )
        set("head",
            if (this@toLuaTable is LivingEntity) this@toLuaTable.getEquippedStack(EquipmentSlot.HEAD)
                .toLuaTable() else LuaValue.NIL
        )
        set("chest",
            if (this@toLuaTable is LivingEntity) this@toLuaTable.getEquippedStack(EquipmentSlot.CHEST)
                .toLuaTable() else LuaValue.NIL
        )
        set("legs",
            if (this@toLuaTable is LivingEntity) this@toLuaTable.getEquippedStack(EquipmentSlot.LEGS)
                .toLuaTable() else LuaValue.NIL
        )
        set("feet",
            if (this@toLuaTable is LivingEntity) this@toLuaTable.getEquippedStack(EquipmentSlot.FEET)
                .toLuaTable() else LuaValue.NIL
        )
        set("body",
            if (this@toLuaTable is LivingEntity) this@toLuaTable.getEquippedStack(EquipmentSlot.BODY)
                .toLuaTable() else LuaValue.NIL
        )
        set("saddle",
            if (this@toLuaTable is LivingEntity) this@toLuaTable.getEquippedStack(EquipmentSlot.SADDLE)
                .toLuaTable() else LuaValue.NIL
        )
    }
    val asItem = if(this is ItemEntity) this.stack.toLuaTable() else LuaValue.NIL
    val table: LuaTable = LuaTable().apply {
        set("id", LuaValue.valueOf(id))
        set("username", username)
        set("type", type)
        set("positionX", LuaValue.valueOf(x))
        set("positionY", LuaValue.valueOf(y))
        set("positionZ", LuaValue.valueOf(z))
        set("velocityX", LuaValue.valueOf(velocity.x))
        set("velocityY", LuaValue.valueOf(velocity.y))
        set("velocityZ", LuaValue.valueOf(velocity.z))
        set("yaw", LuaValue.valueOf(yaw.toDouble()))
        set("pitch", LuaValue.valueOf(pitch.toDouble()))
        set("height", LuaValue.valueOf(height.toDouble()))
        set("width", LuaValue.valueOf(width.toDouble()))
        set("onGround", LuaValue.valueOf(isOnGround))
        set("maxHealth", maxHealth)
        set("health", health)
        set("equipment", equipment)
        set("asItem", asItem)
    }
    return table
}