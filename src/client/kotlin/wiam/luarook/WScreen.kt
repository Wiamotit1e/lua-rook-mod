package wiam.luarook

import net.minecraft.client.gui.*
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.input.CharInput
import net.minecraft.client.input.KeyInput
import net.minecraft.text.Text

/**
 * Base screen class with a keyed element map.
 *
 * For Lua-exposed screens, prefer [wiam.luarook.lua.api.GuiApi.createScreen]
 * which provides a Lua-friendly handle with callbacks and widgets.
 */
abstract class WScreen(title: Text?) : Screen(title) {
    val elements: MutableMap<Any, Element> = mutableMapOf()

    protected fun<T> addDrawableChild(key: Any, drawableElement: T) where T: Element, T: Drawable, T: Selectable {
        addDrawableChild(drawableElement)
        elements[key] = drawableElement
    }

    protected fun remove(key: Any) {
        remove(elements[key])
        elements.remove(key)
    }
}

@Deprecated(
    "Use gui.createScreen(title) from Lua instead. See GuiApi.",
    replaceWith = ReplaceWith("gui.createScreen", "wiam.luarook.lua.api.GuiApi")
)
fun createScreen(
    title: Text? = null,
    onInit: () -> Unit,
    onRendering: (DrawContext?, mouseX: Int, mouseY: Int, delta: Float) -> Unit
): Screen {
    return object : WScreen(title) {
        override fun init() {
            onInit()
        }

        override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, deltaTicks: Float) {
            onRendering(context, mouseX, mouseY, deltaTicks)
            super.render(context, mouseX, mouseY, deltaTicks)
        }
    }
}

@Deprecated(
    "Use screen:addWidget(config) via GuiApi. See LuaWidget.",
    replaceWith = ReplaceWith("LuaWidget", "wiam.luarook.lua.screen.LuaWidget")
)
fun createWidget(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    message: Text,
    onMouseMoved : (Double, Double) -> Unit,
    onMouseClicked : (Click?, Boolean) -> Boolean,
    onMouseReleased : (Click?) -> Boolean,
    onMouseDragged : (Click?, Double, Double) -> Boolean,
    onMouseScrolled : (Double, Double, Double, Double) -> Boolean,
    onKeyPressed: (KeyInput?) -> Boolean,
    onCharTyped: (CharInput?) -> Boolean,
    onKeyReleased: (KeyInput?) -> Boolean,
    onWidgetRendering: (DrawContext?, mouseX: Int, mouseY: Int, delta: Float) -> Unit
): ClickableWidget {


    return object : ClickableWidget(x, y, width, height, message) {
        override fun renderWidget(
            context: DrawContext?,
            mouseX: Int,
            mouseY: Int,
            deltaTicks: Float
        ) {
            onWidgetRendering(context, mouseX, mouseY, deltaTicks)
        }

        override fun mouseMoved(mouseX: Double, mouseY: Double) {
            onMouseMoved(mouseX, mouseY)
            super.mouseMoved(mouseX, mouseY)
        }

        override fun mouseClicked(click: Click?, doubled: Boolean): Boolean {
            return onMouseClicked(click, doubled)
        }

        override fun mouseReleased(click: Click?): Boolean {
            return onMouseReleased(click)
        }

        override fun mouseDragged(
            click: Click?,
            offsetX: Double,
            offsetY: Double
        ): Boolean {
            return onMouseDragged(click, offsetX, offsetY)
        }

        override fun mouseScrolled(
            mouseX: Double,
            mouseY: Double,
            horizontalAmount: Double,
            verticalAmount: Double
        ): Boolean {
            return onMouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
        }

        override fun keyPressed(input: KeyInput?): Boolean {
            return onKeyPressed(input)
        }

        override fun charTyped(input: CharInput?): Boolean {
            return onCharTyped(input)
        }

        override fun keyReleased(input: KeyInput?): Boolean {
            return onKeyReleased(input)
        }


        override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
            appendDefaultNarrations(builder)
        }

    }
}
