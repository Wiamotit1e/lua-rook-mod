local chat_logger = require "libs.chat_logger"
local tick_helper = require "libs.tick_helper"

local taskHandler = nil
local log = chat_logger.getLogger("example_tick_helper")

chat.onAllowChatSent(function(msg)
    if msg == "#greet_task" then
        tick_helper.createTickStartedTask(
                function()
                    log:info("Hello, 3 秒到了！")
                end,
                false, -- 不循环
                "greet_task", -- 任务名
                60                  -- 60 ticks = 3 秒（20 ticks/秒）
        )
        return false
    end
    if msg == "#pos_reporter" then
        taskHandler = tick_helper.createTickStartedTask(
                function()
                    local entity = player.getAsEntity()
                    if entity then
                        log:info("Successfully get position")
                        return entity
                    else
                        log:info("Failed getting position")
                        return nil
                    end
                end,
                true, -- 循环执行
                "pos_reporter", -- 任务名
                100                  -- 100 ticks = 5 秒
        )
        taskHandler.afterThat(function(entity)
            tick_helper.createTickStartedTask(
                    function()
                        if entity ~= nil then
                            log:info(string.format("Position: (%.1f, %.1f, %.1f)  Yaw: %.1f  Pitch: %.1f",
                                    entity.positionX, entity.positionY, entity.positionZ,
                                    entity.yaw, entity.pitch))
                        else
                            log:info("Entity not available.")
                        end
                    end,
                    false,
                    "finish_pos_report_task",
                    20
            )
        end)
        return false
    end
    if msg == "#cancel_pos_reporter" then
        if taskHandler then
            taskHandler:cancel()
        end
        return false
    end
    return true
end)