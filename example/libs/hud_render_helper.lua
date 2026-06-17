local M = {}

local everyFrames = {}

local RenderingPageBuilder = {
}
RenderingPageBuilder.__index = RenderingPageBuilder

function RenderingPageBuilder:new()
    local o = {}
    o.page = {}
    setmetatable(o, self)
    return o
end

function RenderingPageBuilder:add(element)
    table.insert(self.page, element)
    return self
end

function RenderingPageBuilder:addScissorEnabledElement(x1, y1, x2, y2)
    table.insert(
            self.page,
            {
                type = "scissor_enabled",
                x1 = x1,
                y1 = y1,
                x2 = x2,
                y2 = y2,
            }
    )
    return self
end

 function RenderingPageBuilder:addScissorDisabledElement()
    table.insert(
            self.page,
            {
                type = "scissor_disabled",
            }
    )
    return self
end

function RenderingPageBuilder:addFillRenderingElement(x1, y1, x2, y2, color)
    table.insert(
            self.page,
            {
                type = "fill_rendering",
                x1 = x1,
                y1 = y1,
                x2 = x2,
                y2 = y2,
                color = color
            }
    )
    return self
end

function RenderingPageBuilder:addTextRenderingElement(text, x, y, color, shadow)
    table.insert(
            self.page,
            {
                type = "text_rendering",
                text = text,
                x = x,
                y = y,
                color = color,
                shadow = shadow
            }
    )
    return self
end

function RenderingPageBuilder:addItemRenderingElement(item, x, y)
    table.insert(
            self.page,
            {
                type = "item_rendering",
                item = item,
                x = x,
                y = y
            }
    )
    return self
end

function RenderingPageBuilder:addItemComprehensivelyRenderingElement(item, x, y)
    table.insert(
            self.page,
            {
                type = "item_comprehensively_rendering",
                item = item,
                x = x,
                y = y
            }
    )
    return self
end

function RenderingPageBuilder:addItemOverlayRenderingElement(item, x, y)
    table.insert(
            self.page,
            {
                type = "item_overlay_rendering",
                item = item,
                x = x,
                y = y
            }
    )
    return self
end

function RenderingPageBuilder:addItemComprehensivelyOverlayRenderingElement(item, x, y)
    table.insert(
            self.page,
            {
                type = "item_comprehensively_overlay_rendering",
                item = item,
                x = x,
                y = y
            }
    )
    return self
end

function RenderingPageBuilder:addMatrixPushedElement()
    table.insert(
            self.page,
            {
                type = "matrix_pushed",
            }
    )
    return self
end

function RenderingPageBuilder:addMatrixPoppedElement()
    table.insert(
            self.page,
            {
                type = "matrix_popped",
            }
    )
    return self
end

function RenderingPageBuilder:addMatrixTranslatedElement(x, y)
    table.insert(
            self.page,
            {
                type = "matrix_translated",
                x = x,
                y = y
            }
    )
    return self
end

function RenderingPageBuilder:addMatrixTranslationElement(x, y)
    table.insert(
            self.page,
            {
                type = "matrix_translation",
                x = x,
                y = y
            }
    )
    return self
end

function RenderingPageBuilder:addMatrixScaledElement(x, y)
    table.insert(
            self.page,
            {
                type = "matrix_scaled",
                x = x,
                y = y
            }
    )
    return self
end

function RenderingPageBuilder:addMatrixScaledAroundElement(sx, sy, ox, oy)
    table.insert(
            self.page,
            {
                type = "matrix_scaled_around",
                sx = sx,
                sy = sy,
                ox = ox,
                oy = oy
            }
    )
    return self
end

function RenderingPageBuilder:addMatrixSetElement(matrix)
    table.insert(
            self.page,
            {
                type = "matrix_set",
                matrix = matrix
            }
    )
    return self
end

function RenderingPageBuilder:addMatrixInvertedElement()
    table.insert(
            self.page,
            {
                type = "matrix_inverted",
            }
    )
    return self
end

function RenderingPageBuilder:build()
    return self.page
end

M.getRenderingPageBuilder = function()
    return RenderingPageBuilder:new()
end

-- 注册一个 HUD 渲染器
-- runner 签名: function(context, tickCounter) → table | nil
M.register = function(name, runner)
    everyFrames[name] = runner
end

-- hud.onHudRendered 的 Kotlin 实现签名:
--   fn.call(context_table, tickCounter_table)
-- context_table 字段: scaledWidth, scaledHeight, m00~m21
-- tickCounter_table 字段: dynamicDeltaTicks, tickProgress, fixedDeltaTicks
hud.onHudRendered(function(context, tickCounter)
    local sum = {}
    for _, runner in pairs(everyFrames) do
        if runner then
            local page = runner(context, tickCounter)
            if page then
                for _, element in ipairs(page) do
                    table.insert(sum, element)
                end
            end
        end
    end
    return sum
end)

return M