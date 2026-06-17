-- An example of config, which shows how to create a config feature for a script.

local settings = require "libs.settings"
local chat_logger = require "libs.chat_logger"

local default_settings = {
    country = "People's Republic of China",
    orientation = "patriot",
    gender = "male",
    scores = {
        math = 100,
        english = 100,
        chinese = 100
    },
    is_married = true
    -- ...
}

local logger = chat_logger.getLogger("Example Config")

local loaded_settings = {}

local loadConfig = function()
    if(settings.isExist("example.json")) then
        loaded_settings = settings.get("example.json")
        return
    end
    settings.set("example.json", default_settings)
    loaded_settings = default_settings
end

local setConfig = function(config)
    settings.set("example.json", config)
end

chat.onAllowChatSent(function(msg)
    if msg == "#example_config_load" then
        loadConfig()
        return false
    end
    if msg == "#example_config_log" then
        local part = ""
        for subject, score in pairs(loaded_settings.scores) do
            part = part .. "whose " .. subject .. " score is " .. score .. ", "
        end
        local result = part:sub(1, -3) .. "."
        local married = loaded_settings.is_married and "married" or "single"
        logger:info("A " .. married .. " person from " .. loaded_settings.country .. " is a " .. loaded_settings.orientation .. " whose gender is " .. loaded_settings.gender .. ", " .. result)
        return false
    end
    if msg == "#example_config_save" then
        setConfig(loaded_settings)
        logger:info("Saved")
        return false
    end
    local arg1, arg2 = msg:match("^#example_config_set (.-) (.+)$")
    if arg1 and arg2 then
        if type(loaded_settings[arg1]) == "boolean" then
            loaded_settings[arg1] = arg2 == "true"
        else
            loaded_settings[arg1] = arg2
        end
        logger:info("Set valueOf" .. arg1 .. " to " .. arg2)
        return false
    end
    local arg3, arg4 = msg:match("^#example_config_set_scores (.-) (.+)$")
    if arg3 and arg4 then
        loaded_settings.scores[arg3] = arg4
        logger:info("Set value of the key in the map scores " .. arg3 .. " to " .. arg4)
        return false
    end
    return true
end)

loadConfig()