package wiam.luarook.lua.screen

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Click
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.input.CharInput
import net.minecraft.client.input.KeyInput
import net.minecraft.text.Text
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import wiam.luarook.lua.ErrorReporter
import wiam.luarook.lua.adapt.drawing.drawWith
import wiam.luarook.lua.adapt.drawing.toLuaTable
import wiam.luarook.lua.invoke4
import wiam.luarook.lua.invoke5

/**
 * A general-purpose widget that delegates all events to Lua callbacks.
 *
 * There is only ONE widget class. What it *is* (button, label, slider, …)
 * is decided entirely on the Lua side by which callbacks are provided.
 *
 * | callback       | Lua signature                                | MC method       |
 * |----------------|----------------------------------------------|-----------------|
 * | onClick        | fn(x, y)                                     | onClick         |
 * | onRender       | fn(ctx, mx, my, delta) → draw commands       | renderWidget    |
 * | onMouseMoved   | fn(x, y)                                     | mouseMoved      |
 * | onMouseReleased| fn(x, y, button) → bool                      | mouseReleased   |
 * | onMouseDragged | fn(x, y, dx, dy, button) → bool              | mouseDragged    |
 * | onMouseScrolled| fn(x, y, horiz, vert) → bool                 | mouseScrolled   |
 * | onKeyPressed   | fn(key, scancode, mods) → bool               | keyPressed      |
 * | onKeyReleased  | fn(key, scancode, mods) → bool               | keyReleased     |
 * | onCharTyped    | fn(codepoint, mods) → bool                   | charTyped       |
 */
class LuaWidget(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    message: Text,
    private val onClickCallback: LuaValue?,
    private val onRenderCallback: LuaValue?,
    private val onMouseMovedCallback: LuaValue?,
    private val onMouseReleasedCallback: LuaValue?,
    private val onMouseDraggedCallback: LuaValue?,
    private val onMouseScrolledCallback: LuaValue?,
    private val onKeyPressedCallback: LuaValue?,
    private val onKeyReleasedCallback: LuaValue?,
    private val onCharTypedCallback: LuaValue?,
    private val interactive: Boolean,
    private val scriptName: String
) : ClickableWidget(x, y, width, height, message) {
    
    init {
        active = interactive
    }
    
    // ---- Mouse events ----
    
    override fun onClick(click: Click?, doubled: Boolean) {
        val cb = onClickCallback ?: return
        try {
            if (click != null) {
                cb.call(LuaValue.valueOf(click.x), LuaValue.valueOf(click.y))
            } else {
                cb.call()
            }
        } catch (e: Exception) {
            ErrorReporter.reportRuntimeError(scriptName, "gui.widget.onClick", e)
        }
    }
    
    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        super.mouseMoved(mouseX, mouseY)
        val cb = onMouseMovedCallback ?: return
        try {
            cb.call(LuaValue.valueOf(mouseX), LuaValue.valueOf(mouseY))
        } catch (e: Exception) {
            // silently ignore — mouse-move is too frequent for chat spam
        }
    }
    
    override fun mouseReleased(click: Click?): Boolean {
        val cb = onMouseReleasedCallback ?: return super.mouseReleased(click)
        try {
            val x = click?.x ?: 0.0
            val y = click?.y ?: 0.0
            val btn = click?.button()?.toDouble() ?: 0.0
            return cb.call(LuaValue.valueOf(x), LuaValue.valueOf(y), LuaValue.valueOf(btn)).toboolean()
        } catch (e: Exception) {
            ErrorReporter.reportRuntimeError(scriptName, "gui.widget.mouseReleased", e)
            return false
        }
    }
    
    override fun mouseDragged(click: Click?, offsetX: Double, offsetY: Double): Boolean {
        val cb = onMouseDraggedCallback ?: return super.mouseDragged(click, offsetX, offsetY)
        try {
            val x = click?.x ?: 0.0
            val y = click?.y ?: 0.0
            val btn = click?.button()?.toDouble() ?: 0.0
            return cb.invoke5(
                LuaValue.valueOf(x), LuaValue.valueOf(y), LuaValue.valueOf(offsetX),
                LuaValue.valueOf(offsetY), LuaValue.valueOf(btn)
            ).arg1().toboolean()
        } catch (e: Exception) {
            ErrorReporter.reportRuntimeError(scriptName, "gui.widget.mouseDragged", e)
            return false
        }
    }
    
    override fun mouseScrolled(
        mouseX: Double, mouseY: Double,
        horizontalAmount: Double, verticalAmount: Double
    ): Boolean {
        val cb = onMouseScrolledCallback ?: return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
        try {
            val result = cb.invoke4(
                LuaValue.valueOf(mouseX),
                LuaValue.valueOf(mouseY), LuaValue.valueOf(horizontalAmount),
                LuaValue.valueOf(verticalAmount)
            )
            return result.toboolean()
        } catch (e: Exception) {
            // silently ignore — scroll is frequent
            return false
        }
    }
    
    // ---- Keyboard events ----
    
    override fun keyPressed(input: KeyInput?): Boolean {
        if (input == null) return false
        val cb = onKeyPressedCallback ?: return false
        try {
            return cb.call(
                LuaValue.valueOf(input.key),
                LuaValue.valueOf(input.scancode),
                LuaValue.valueOf(input.modifiers)
            ).arg1().toboolean()
        } catch (e: Exception) {
            ErrorReporter.reportRuntimeError(scriptName, "gui.widget.keyPressed", e)
            return false
        }
    }
    
    override fun keyReleased(input: KeyInput?): Boolean {
        if (input == null) return false
        val cb = onKeyReleasedCallback ?: return false
        try {
            return cb.call(
                LuaValue.valueOf(input.key),
                LuaValue.valueOf(input.scancode),
                LuaValue.valueOf(input.modifiers)
            ).arg1().toboolean()
        } catch (e: Exception) {
            ErrorReporter.reportRuntimeError(scriptName, "gui.widget.keyReleased", e)
            return false
        }
    }
    
    override fun charTyped(input: CharInput?): Boolean {
        if (input == null) return false
        val cb = onCharTypedCallback ?: return false
        try {
            return cb.call(
                LuaValue.valueOf(input.codepoint),
                LuaValue.valueOf(input.modifiers)
            ).toboolean()
        } catch (e: Exception) {
            ErrorReporter.reportRuntimeError(scriptName, "gui.widget.charTyped", e)
            return false
        }
    }
    
    // ---- Rendering ----
    
    override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        if (context == null) return
        val cb = onRenderCallback ?: return
        try {
            val result = cb.invoke4(
                context.toLuaTable(),
                LuaValue.valueOf(mouseX),
                LuaValue.valueOf(mouseY),
                LuaValue.valueOf(delta.toDouble())
            )
            if (result is LuaTable) context.drawWith(result)
        } catch (e: Exception) {
            ErrorReporter.reportRuntimeError(scriptName, "gui.widget.render", e)
        }
    }
    
    override fun appendClickableNarrations(
        builder: net.minecraft.client.gui.screen.narration.NarrationMessageBuilder?
    ) {
        appendDefaultNarrations(builder)
    }
}
