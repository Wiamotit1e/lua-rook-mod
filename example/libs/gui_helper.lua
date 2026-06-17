-- ============================================================
-- gui_helper.lua — widget builders for the gui API
--
-- 所有 widget 本质上都是 LuaWidget, 只是配了不同的 callback.
-- 这个文件提供常见的 widget 构造器，你可以直接拿来用，也可以
-- 自己用 screen.addWidget(config) 写自定义的。
-- ============================================================

local hud_render_helper = require "libs.hud_render_helper"

local M = {}

-- ---------- Label ----------

--- 添加一个纯文本 label（非交互）
-- @param screen  handle from gui.createScreen()
-- @param x, y    position
-- @param text    Text table, e.g. { content = "Hello" }
-- @param color   hex color, default 0xFFFFFF
-- @param shadow  bool, default false
function M.addLabel(screen, x, y, text, color, shadow)
    color = color or 0xFFFFFF
    shadow = shadow or false
    screen.addWidget({
        x = x, y = y,
        width = 200, height = 20,
        active = false,
        onRender = function(ctx, mx, my, dt)
            return {
                {
                    type = "text_rendering",
                    text = text,
                    x = x, y = y,
                    color = color,
                    shadow = shadow
                }
            }
        end
    })
end

-- ---------- Button ----------

--- 添加一个按钮
-- @param screen  handle
-- @param x, y, w, h   position and size
-- @param text    Text table
-- @param onClick function(x, y) called on click
-- @param opts    optional: { color, hoverColor, textColor, ... }
function M.addButton(screen, x, y, w, h, text, onClick, opts)
    opts = opts or {}
    local color = opts.color or 0xFF666666
    local hoverColor = opts.hoverColor or 0xFF888888
    local textColor = opts.textColor or 0xFFFFFF

    screen.addWidget({
        x = x, y = y,
        width = w, height = h,
        text = text,
        active = true,
        onClick = onClick,
        onRender = function(ctx, mx, my, dt)
            local isHover = mx >= x and mx < x + w and my >= y and my < y + h
            local page = hud_render_helper.getRenderingPageBuilder()
            page:addFillRenderingElement(x, y, x + w, y + h, isHover and hoverColor or color)
            page:addTextRenderingElement(text, x + 4, y + (h - 8) / 2, textColor, false)
            return page:build()
        end
    })
end

-- ---------- Toggle / Checkbox ----------

--- 添加一个 checkbox
-- @param screen  handle
-- @param x, y    position
-- @param label   Text table
-- @param onChange function(checked) called on toggle
-- @param checked initial state, default false
function M.addCheckbox(screen, x, y, label, onChange, checked)
    local state = checked or false
    local size = 12

    screen.addWidget({
        x = x, y = y,
        width = size, height = size,
        active = true,
        onClick = function()
            state = not state
            if onChange then onChange(state) end
        end,
        onRender = function(ctx, mx, my, dt)
            local page = hud_render_helper.getRenderingPageBuilder()
            -- box
            page:addFillRenderingElement(x, y, x + size, y + size, 0xFF444444)
            page:addFillRenderingElement(x + 1, y + 1, x + size - 1, y + size - 1, 0xFF000000)
            -- checkmark
            if state then
                page:addFillRenderingElement(x + 3, y + 3, x + size - 3, y + size - 3, 0xFF00FF00)
            end
            -- label
            page:addTextRenderingElement(label, x + size + 4, y + 2, 0xFFFFFF, false)
            return page:build()
        end
    })
end

-- ---------- Slider ----------

--- 添加一个水平滑动条
-- @param screen  handle
-- @param x, y, w, h   position and size
-- @param min, max, value   range and initial value
-- @param onChange function(value) called on drag
function M.addSlider(screen, x, y, w, h, min, max, value, onChange)
    local state = {
        v = value or min,
        dragging = false,
    }
    min = min or 0
    max = max or 100

    local function pct() return (state.v - min) / (max - min) end

    screen.addWidget({
        x = x, y = y,
        width = w, height = h,
        active = true,
        onClick = function(mx, my)
            state.dragging = true
        end,
        onMouseReleased = function(mx, my, button)
            state.dragging = false
            return true  -- consume the event
        end,
        onMouseDragged = function(mx, my, dx, dy, button)
            if state.dragging then
                local t = math.max(0, math.min(1, (mx - x - 4) / (w - 8)))
                state.v = min + t * (max - min)
                if onChange then onChange(state.v) end
            end
            return true
        end,
        onRender = function(ctx, mx, my, dt)
            local px = x + 4 + pct() * (w - 8)
            local page = hud_render_helper.getRenderingPageBuilder()
            -- track
            page:addFillRenderingElement(x, y + h / 2 - 1, x + w, y + h / 2 + 1, 0xFF444444)
            -- knob
            page:addFillRenderingElement(px - 4, y, px + 4, y + h, 0xFFAAAAAA)
            return page:build()
        end
    })
end

return M
