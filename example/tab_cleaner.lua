-- ============================================================
-- tab_cleaner.lua — 干掉延迟为 0 的 TabList 条目
-- #tabclean  — 开关
-- #tabdump   — 打印所有条目的原始数据
-- #tabping n — 测试：把延迟为 n 的条目改名为红色
-- #log_tab_player_entries — 打印所有玩家条目
-- ============================================================

local enabled = true
local removed = 0
local debugDump = false

local chat_logger = require "libs.chat_logger"

local logger = chat_logger.getLogger("Tab Cleaner")

tabList.onPlayerListEntriesModified(function(entry)
    if debugDump then
        local dn = "nil"
        if type(entry.displayName) == "table" then
            dn = entry.displayName.content or "(no content)"
        end
        print(string.format("[TabDump] order=%s  latency=%s  displayName=%s",
            tostring(entry.listOrder),
            tostring(entry.latency),
            tostring(dn)))
    end

    if not enabled then return entry end

    if entry.latency == 0 then
        removed = removed + 1
        return nil
    end
    return entry
end)

world.onTickStarted(function()
    if removed > 0 then
        print("[TabClean] removed " .. removed .. " zero-latency entries this tick")
        removed = 0
    end
end)

chat.onAllowChatSent(function(msg)
    if msg == "#tabclean" then
        enabled = not enabled
        debugDump = false
        if enabled then
            print("[TabClean] ON — removing latency=0 entries")
        else
            print("[TabClean] OFF")
        end
        return false
    end

    if msg == "#tabdump" then
        debugDump = not debugDump
        if debugDump then
            enabled = false
            print("[TabDump] ON — printing all entry fields (filter disabled)")
        else
            print("[TabDump] OFF")
        end
        return false
    end

    if msg == "#log_tab_player_entries" then
        local entries = tabList.getListedPlayerListEntries()
        for i, v in ipairs(entries) do
            logger:info(i .. " " .. v.displayName.content .. " " .. v.latency)
        end
        return false
    end

    return true
end)

print("[tab_cleaner.lua] loaded.  #tabclean 开关  #tabdump 查看原始数据")
