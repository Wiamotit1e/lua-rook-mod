local text_helper = require "libs.text_helper"

local M = {}

local function getStringToLog(name, message) return "[" .. name .. "] " .. message end

M.getLogger = function(name)
    local logger = {}
    logger.name = name
    logger.info = function(self, message)
        chat.sendChatMessageClientOnly(
                text_helper.makeText(getStringToLog(self.name, message), text_helper.colorStyle("blue")),
                false
        )
    end
    logger.error = function(self, message)
        chat.sendChatMessageClientOnly(
                text_helper.makeText(getStringToLog(self.name, message), text_helper.colorStyle("red")),
                false
        )
    end
    logger.warn = function(self, message)
        chat.sendChatMessageClientOnly(
                text_helper.makeText(getStringToLog(self.name, message), text_helper.colorStyle("yellow")),
                false
        )
    end
    return logger
end

return M