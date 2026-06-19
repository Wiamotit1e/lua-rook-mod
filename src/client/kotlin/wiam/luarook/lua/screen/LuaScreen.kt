package wiam.luarook.lua.screen

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.input.CharInput
import net.minecraft.client.input.KeyInput
import net.minecraft.text.Text
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import wiam.luarook.lua.ErrorReporter
import wiam.luarook.lua.adapt.drawing.drawWith
import wiam.luarook.lua.adapt.drawing.toLuaTable
import wiam.luarook.lua.adapt.text.toMutableText
import wiam.luarook.lua.api.renderer.GuiApi

/**
 * A Minecraft [Screen] whose behavior and widgets are driven entirely by Lua.
 *
 * There is one widget class — [LuaWidget] — and three methods:
 * [addWidget] (returns an ID), [removeWidget], and [clearWidgets].
 * What each widget *does* is decided on the Lua side by which callbacks it provides.
 */
class LuaScreen(
    title: Text,
    private val guiApi: GuiApi,
    private val scriptName: String
) : Screen(title) {

    // ---- Lua callbacks (set via handle setters) ----

    var onRenderCallback: LuaValue? = null
    var onKeyPressedCallback: LuaValue? = null
    var onCharTypedCallback: LuaValue? = null
    var onCloseCallback: LuaValue? = null

    // ---- Widget storage (id → entry) ----

    private var nextWidgetId = 0
    private val widgetEntries = linkedMapOf<Int, WidgetEntry>()

    private class WidgetEntry(val config: LuaTable, var widget: LuaWidget?)

    // ---- Internal state ----

    private var closeFired = false
    private var screenInitialized = false

    // ---- Lifecycle ----

    override fun init() {
        screenInitialized = true
        for ((id, entry) in widgetEntries) {
            if (entry.widget == null) {
                entry.widget = buildWidget(entry.config)
            }
            entry.widget?.let { addDrawableChild(it) }
        }
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)

        if (context == null) return
        val cb = onRenderCallback ?: return

        try {
            val result = LuaWidget.invoke4(
                cb,
                context.toLuaTable(),
                LuaValue.valueOf(mouseX),
                LuaValue.valueOf(mouseY),
                LuaValue.valueOf(delta.toDouble())
            )
            if (result is LuaTable) context.drawWith(result)
        } catch (e: Exception) {
            ErrorReporter.reportRuntimeError(scriptName, "gui.screen.render", e)
        }
    }

    override fun keyPressed(input: KeyInput?): Boolean {
        if (input == null) return super.keyPressed(null)
        val cb = onKeyPressedCallback ?: return super.keyPressed(input)
        try {
            val result = cb.call(
                LuaValue.valueOf(input.key),
                LuaValue.valueOf(input.scancode),
                LuaValue.valueOf(input.modifiers)
            )
            if (result.toboolean()) return true
        } catch (e: Exception) {
            ErrorReporter.reportRuntimeError(scriptName, "gui.screen.keyPressed", e)
        }
        return super.keyPressed(input)
    }

    override fun charTyped(input: CharInput?): Boolean {
        if (input == null) return super.charTyped(null)
        val cb = onCharTypedCallback ?: return super.charTyped(input)
        try {
            val result = cb.call(
                LuaValue.valueOf(input.codepoint),
                LuaValue.valueOf(input.modifiers)
            )
            if (result.toboolean()) return true
        } catch (e: Exception) {
            ErrorReporter.reportRuntimeError(scriptName, "gui.screen.charTyped", e)
        }
        return super.charTyped(input)
    }

    override fun removed() {
        fireCloseOnce()
        guiApi.onScreenClosed(this)
        clearCallbacks()
        super.removed()
    }

    override fun close() {
        fireCloseOnce()
        super.close()
    }

    override fun shouldPause(): Boolean = false

    // ---- Widget management ----

    /**
     * Adds a widget, returns its unique ID (1-based, never reused).
     *
     * Config fields:
     * | field          | type     | default | Lua signature |
     * |----------------|----------|---------|---------------|
     * | x, y           | int      | required | |
     * | width, height  | int      | required | |
     * | text           | Text tbl | ""      | |
     * | active         | bool     | true    | |
     * | onClick        | function | nil     | fn(x, y) |
     * | onRender       | function | nil     | fn(ctx, mx, my, delta) → draw commands |
     * | onMouseMoved   | function | nil     | fn(x, y) |
     * | onMouseReleased| function | nil     | fn(x, y, button) → bool |
     * | onMouseDragged | function | nil     | fn(x, y, dx, dy, button) → bool |
     * | onMouseScrolled| function | nil     | fn(x, y, horiz, vert) → bool |
     * | onKeyPressed   | function | nil     | fn(key, scancode, mods) → bool |
     * | onKeyReleased  | function | nil     | fn(key, scancode, mods) → bool |
     * | onCharTyped    | function | nil     | fn(codepoint, mods) → bool |
     */
    fun addWidget(config: LuaTable): Int {
        val id = ++nextWidgetId
        val entry = WidgetEntry(config, null)
        widgetEntries[id] = entry
        if (screenInitialized) {
            entry.widget = buildWidget(config)
            entry.widget?.let { addDrawableChild(it) }
        }
        return id
    }

    /** Removes the widget with the given [id]. No-op if the id is unknown. */
    fun removeWidget(id: Int) {
        val entry = widgetEntries.remove(id) ?: return
        entry.widget?.let { remove(it) }
    }

    /** Removes every widget from the screen. */
    fun clearWidgets() {
        for (entry in widgetEntries.values) {
            entry.widget?.let { remove(it) }
        }
        widgetEntries.clear()
    }

    // ---- Internal ----

    private fun buildWidget(config: LuaTable): LuaWidget {
        val x = config["x"].toint()
        val y = config["y"].toint()
        val w = config["width"].toint()
        val h = config["height"].toint()

        val textTable = config["text"] as? LuaTable
        val label = if (textTable != null) Text.of(textTable.toMutableText()) else Text.literal("")

        val interactive = config["active"]?.toboolean() ?: true

        return LuaWidget(
            x, y, w, h, label,
            fnOrNull(config["onClick"]),
            fnOrNull(config["onRender"]),
            fnOrNull(config["onMouseMoved"]),
            fnOrNull(config["onMouseReleased"]),
            fnOrNull(config["onMouseDragged"]),
            fnOrNull(config["onMouseScrolled"]),
            fnOrNull(config["onKeyPressed"]),
            fnOrNull(config["onKeyReleased"]),
            fnOrNull(config["onCharTyped"]),
            interactive,
            scriptName
        )
    }

    private fun fnOrNull(v: LuaValue?): LuaValue? =
        if (v?.isfunction() == true) v else null

    private fun fireCloseOnce() {
        if (closeFired) return
        closeFired = true
        val cb = onCloseCallback ?: return
        try {
            cb.call()
        } catch (e: Exception) {
            ErrorReporter.reportRuntimeError(scriptName, "gui.screen.close", e)
        }
    }

    fun clearCallbacks() {
        onRenderCallback = null
        onKeyPressedCallback = null
        onCharTypedCallback = null
        onCloseCallback = null
        clearWidgets()
    }
}
