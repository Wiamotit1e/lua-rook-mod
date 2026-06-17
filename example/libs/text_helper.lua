
local M = {}

-- ---------- helper: 简化 Text 对象构造 ----------
M.makeText = function(content, style, siblings)
    return {
        content = content,
        style = style or {},
        siblings = siblings or {}
    }
end

-- 预设颜色样式 (避免每次手写 { color = { color = "xxx" } })
M.colorStyle = function(name, extra)
    local s = { color = { color = name } }
    if extra then
        for k, v in pairs(extra) do s[k] = v end
    end
    return s
end

return M