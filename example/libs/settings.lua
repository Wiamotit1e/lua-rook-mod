

local json = require "libs.json.json"
local io = require "io"

local M = {}

local CONFIG_DIR = "lua-rook-scripts/config/"

local function ensureConfigDir()
    local File = luajava.bindClass("java.io.File")
    local dir = luajava.new(File, CONFIG_DIR)
    if not dir:exists() then
        dir:mkdirs()
    end
end

M.isExist = function(name)
    local file = io.open(CONFIG_DIR .. name, "r")
    if not file then return false end
    file:close()
    return true
end

M.get = function(name)
    local file = io.open(CONFIG_DIR .. name, "r")
    if not file then return nil end
    local content = file:read("*a")
    file:close()
    if not content or content == "" then return nil end
    return json.decode(content)
end

M.set = function(name, settings)
    ensureConfigDir()
    local file = io.open(CONFIG_DIR .. name, "w")
    if not file then return false end
    file:write(json.encode(settings))
    file:close()
    return true
end

return M