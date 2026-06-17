local M = {}

local tasksTS = {}
local tasksTE = {}

local createTask = function(runner, loop, name, count, tasks)
    local t = {
        runner = runner,
        loop = loop,
        name = name,
        countDown = count,
        count = count,
        afterThat = function(_)
        end
    }
    tasks[name] = t
    return {
        cancel = function()
            tasks[name] = nil
        end,
        afterThat = function(fun)
            t.afterThat = fun
        end
    }
end

local deal = function(tasks)
    for k, v in pairs(tasks) do
        v.countDown = v.countDown - 1
        if v.countDown <= 0 then
            local ok, result = pcall(v.runner)
            if not ok then
                tasks[k] = nil
                print("[tick_helper] Error in task '" .. tostring(v.name) .. "': " .. tostring(result))
            else
                if v.afterThat then
                    local ok2, err2 = pcall(v.afterThat, result)
                    if not ok2 then
                        print("[tick_helper] Error in afterThat of '" .. tostring(v.name) .. "': " .. tostring(err2))
                    end
                end
                if v.loop then
                    v.countDown = v.count
                else
                    tasks[k] = nil
                end
            end
        end
    end
end

M.createTickStartedTask = function(runner, loop, name, count)
    return createTask(runner, loop, name, count, tasksTS)
end

M.createTickEndedTask = function(runner, loop, name, count)
    return createTask(runner, loop, name, count, tasksTE)
end

M.isTickStartedTaskExist = function(name)
    return tasksTS[name] ~= nil
end

M.isTickEndedTaskExist = function(name)
    return tasksTE[name] ~= nil
end

world.onTickStarted(function()
    deal(tasksTS)
end)

world.onTickEnded(function()
    deal(tasksTE)
end)

return M