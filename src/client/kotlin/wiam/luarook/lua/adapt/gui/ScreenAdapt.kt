package wiam.luarook.lua.adapt.gui

import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import wiam.luarook.lua.adapt.text.toLuaTable

/**
 * Snapshot of a [Screen], safe to hand to Lua after the screen closes.
 *
 * ```lua
 * {
 *     name = "ChestScreen",              -- 类名（不含包名）
 *     className = "net.minecraft....ChestScreen",
 *     isHandledScreen = true,            -- 是否为容器屏幕
 *     title = { content = "箱子", key = "container.chest", ... },
 *     syncId = 42,                       -- 仅容器屏幕
 * }
 * ```
 */
fun Screen.toLuaTable(): LuaTable {
    val table = LuaTable()
    table.set("name", LuaValue.valueOf(javaClass.simpleName))
    table.set("className", LuaValue.valueOf(javaClass.name))
    table.set("isHandledScreen", LuaValue.valueOf(this is HandledScreen<*>))
    table.set("title", getTitle().copy().toLuaTable())
    if (this is HandledScreen<*>) {
        table.set("syncId", LuaValue.valueOf(screenHandler.syncId))
    }
    return table
}
