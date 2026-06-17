package wiam.luarook.lua.api

import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.LuaValue.NIL
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.VarArgFunction
import wiam.luarook.lua.LuaApi
import wiam.luarook.lua.adapt.text.toMutableText
import wiam.luarook.lua.screen.LuaScreen

/**
 * Screen / GUI API for Lua scripts.
 *
 * There is one screen method ([createScreen]) and one widget method ([addWidget]).
 * What a widget *is* is decided by the Lua callbacks you give it.
 *
 * ```lua
 * local screen = gui.createScreen("My Title")
 *
 * -- a label: onRender, active = false
 * screen.addWidget({
 *     x = 10, y = 10, width = 200, height = 12,
 *     active = false,
 *     onRender = function(ctx, mx, my, dt)
 *         return { { type = "text_rendering", text = {content="Hi"}, x = 10, y = 10, color = 0xFFFFFF } }
 *     end
 * })
 *
 * -- a button: onClick + onRender, active = true
 * screen.addWidget({
 *     x = 10, y = 40, width = 100, height = 20,
 *     onClick = function(x, y) print("clicked") end,
 *     onRender = function(ctx, mx, my, dt) ... end
 * })
 *
 * screen.open()
 * ```
 */
class GuiApi : LuaApi("gui") {

    private val mc get() = MinecraftClient.getInstance()

    private val activeScreens = mutableListOf<LuaScreen>()

    override fun register(t: LuaTable) {
        t.fn1("createScreen") { titleArg -> createScreenHandle(titleArg) }

        t.fn0("closeCurrentScreen") {
            mc.execute { mc.setScreen(null) }
            NIL
        }
    }

    // ---- Screen lifecycle (called by LuaScreen) ----

    internal fun onScreenClosed(screen: LuaScreen) {
        activeScreens.remove(screen)
    }

    override fun dispose() {
        for (screen in activeScreens.toList()) {
            mc.execute {
                if (mc.currentScreen == screen) {
                    mc.setScreen(null)
                }
            }
        }
        activeScreens.clear()
        super.dispose()
    }

    // ---- Handle construction ----

    private fun createScreenHandle(titleArg: LuaValue): LuaTable {
        val title: Text = if (titleArg is LuaTable) {
            Text.of(titleArg.toMutableText())
        } else {
            Text.literal(titleArg.tojstring())
        }

        val screen = LuaScreen(title, this, scriptName)
        activeScreens.add(screen)

        return buildHandleTable(screen)
    }

    private fun buildHandleTable(screen: LuaScreen): LuaTable {
        val h = LuaTable()

        h["open"] = object : VarArgFunction() {
            override fun invoke(args: Varargs?): LuaValue {
                mc.execute { mc.setScreen(screen) }
                return NIL
            }
        }

        h["close"] = object : VarArgFunction() {
            override fun invoke(args: Varargs?): LuaValue {
                mc.execute {
                    if (mc.currentScreen == screen) {
                        mc.setScreen(null)
                    }
                }
                return NIL
            }
        }

        h["onRender"] = object : VarArgFunction() {
            override fun invoke(args: Varargs?): LuaValue {
                val fn = lastArg(args)
                if (fn.isfunction()) screen.onRenderCallback = fn
                return NIL
            }
        }

        h["onKeyPressed"] = object : VarArgFunction() {
            override fun invoke(args: Varargs?): LuaValue {
                val fn = lastArg(args)
                if (fn.isfunction()) screen.onKeyPressedCallback = fn
                return NIL
            }
        }

        h["onCharTyped"] = object : VarArgFunction() {
            override fun invoke(args: Varargs?): LuaValue {
                val fn = lastArg(args)
                if (fn.isfunction()) screen.onCharTypedCallback = fn
                return NIL
            }
        }

        h["onClosed"] = object : VarArgFunction() {
            override fun invoke(args: Varargs?): LuaValue {
                val fn = lastArg(args)
                if (fn.isfunction()) screen.onCloseCallback = fn
                return NIL
            }
        }

        h["addWidget"] = object : VarArgFunction() {
            override fun invoke(args: Varargs?): LuaValue {
                val config = lastArg(args)
                if (config is LuaTable) {
                    val id = screen.addWidget(config)
                    return LuaValue.valueOf(id)
                }
                return NIL
            }
        }

        h["removeWidget"] = object : VarArgFunction() {
            override fun invoke(args: Varargs?): LuaValue {
                val id = lastArg(args).toint()
                screen.removeWidget(id)
                return NIL
            }
        }

        h["clearWidgets"] = object : VarArgFunction() {
            override fun invoke(args: Varargs?): LuaValue {
                screen.clearWidgets()
                return NIL
            }
        }

        return h
    }

    companion object {
        private fun lastArg(args: Varargs?): LuaValue {
            if (args == null) return NIL
            for (i in args.narg() downTo 1) {
                val v = args.arg(i)
                if (!v.isnil()) return v
            }
            return NIL
        }
    }
}
