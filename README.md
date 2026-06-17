# Lua Rook

一个 Fabric 客户端模组，通过 Lua 脚本扩展 Minecraft。基于 [LuaJ](http://www.luaj.org/) 运行时，提供聊天、世界、渲染、GUI 等 API。

## 快速开始

1. 将模组放入 `mods/` 文件夹
2. 启动 Minecraft
3. Lua 脚本放在 `.minecraft/lua-rook-scripts/` 下
4. 游戏中输入 `/rook list` 查看已加载脚本，`/rook reload` 热重载

## 指令

| 指令 | 说明 |
|---|---|
| `/rook list` | 列出所有 .lua 文件及其加载状态 |
| `/rook run <name>` | 加载指定脚本（不需 .lua 后缀） |
| `/rook unload <name>` | 卸载已加载脚本 |
| `/rook reload` | 重载所有脚本 |

## 架构

每个 `.lua` 文件有独立的 Lua 全局环境和 API 实例，脚本间互不干扰。事件从 Minecraft 进入 `ApiBridge`，派发到所有已加载的 `ApiSession`。

```
Minecraft Fabric Events → ApiBridge → [Session A, Session B, ...] → Lua
```

## API

### chat — 聊天

```lua
-- 发送消息
chat.sendChatMessage("hello")
chat.sendChatCommand("/toggle")

-- 服务端消息（支持富文本 TextStyle）
chat.sendChatMessageClientOnly({ content = "hi", style = { color = { color = "red" } } }, false)

-- 接收消息
chat.onChatReceived(function(message) end)
chat.onGameReceived(function(message, overlay) end)

-- 拦截 / 修改消息（return false 阻止显示）
chat.onAllowChatReceived(function(message) return true end)
chat.onAllowGameReceived(function(message, overlay) return true end)

-- 拦截 / 修改发出的消息
chat.onAllowChatSent(function(message) return true end)
chat.onAllowCommandSent(function(message) return true end)
chat.onModifyChatSent(function(message) return "modified " .. message end)
chat.onModifyCommandSent(function(message) return message end)

-- 发出后通知（不可取消）
chat.onChatSent(function(message) end)
chat.onCommandSent(function(message) end)
```

### player — 玩家

```lua
-- 动作
player.doAttack()     -- 攻击 / 破坏方块
player.doItemUse()    -- 使用物品

-- 朝向
local pitch = player.getPitch()
local yaw = player.getYaw()
player.setPitch(45)
player.setYaw(90)

-- 准星目标
local target = player.getCrosshairTarget()
-- target.type = "block" | "entity" | "miss"

-- 玩家实体信息
local me = player.getAsEntity()
-- me.id, me.uuid, me.type, me.positionX/Y/Z, me.health, me.maxHealth,
-- me.equipment.mainHand/offHand/head/chest/legs/feet, me.yaw, me.pitch, ...

-- 背包
local stacks = player.getInventoryStacks()
for i, stack in ipairs(stacks) do
    print(stack.id, stack.count, stack.itemName, stack.enchantments)
end

-- 背包（完整 codec 序列化）
local full = player.getInventoryStacksComprehensively()

-- 选中槽位 & slot 操作
local slot = player.getSelectedSlot()
player.clickSlot(slotId, button, "PICKUP")  -- actionType: PICKUP/QUICK_MOVE/SWAP/CLONE/THROW/QUICK_CRAFT/PICKUP_ALL

-- 持续挖掘（set true 开始，set false 停止）
player.setBlockAttacking(true)

-- 事件
player.onDamaged(function(source) end)  -- source.type, source.attacker, ...
player.onDeath(function(source) end)
player.onClientTick(function() end)
```

### world — 世界

```lua
-- 实体
local entities = world.getEntities()
for i, e in ipairs(entities) do
    -- e.id, e.type, e.positionX/Y/Z, e.health, e.maxHealth, ...
end

-- 环境
local weather = world.getWeather()  -- "thunder" | "rain" | "clear" | "noWeather"
local time = world.getTimeOfDay()
local total = world.getTime()

-- 方块
local id = world.getBlock(x, y, z)
local state = world.getBlockState(x, y, z)  -- 完整 block state

-- 登出
world.logOut("reason")

-- GUI 状态
if world.isInScreen() then ... end
if world.isInHandledScreen() then ... end

-- 事件
world.onTickStarted(function() end)
world.onTickEnded(function() end)
```

### hud — HUD 渲染

```lua
-- 自定义 HUD（返回 draw command 数组）
hud.onHudRendered(function(context, tickCounter)
    return {
        { type = "fill_rendering", x1 = 0, y1 = 0, x2 = 200, y2 = 200, color = 0x80000000 },
        { type = "text_rendering", text = { content = "Hello" }, x = 50, y = 50, color = 0xFFFFFF, shadow = true },
        { type = "item_rendering", item = { id = "minecraft:diamond", count = 1 }, x = 100, y = 100 },
        { type = "matrix_pushed" },
        { type = "matrix_translated", x = 50, y = 0 },
        { type = "matrix_popped" },
    }
end)

-- 文本测量
local m = hud.measureText({ content = "hello" })
print(m.width, m.height)
```

### gui — 自定义屏幕

```lua
-- 创建 screen handle
local screen = gui.createScreen({ content = "My Screen" })

-- 屏幕级回调
screen.onRender(function(context, mouseX, mouseY, delta)
    return {
        { type = "fill_rendering", x1 = 0, y1 = 0, x2 = context.scaledWidth, y2 = context.scaledHeight, color = 0x80000000 }
    }
end)
screen.onKeyPressed(function(key, scancode, mods) return false end)
screen.onCharTyped(function(codepoint, mods) return false end)
screen.onClosed(function() print("closed") end)

-- Widget（所有 widget 都是同一个 LuaWidget 类，行为由 callback 决定）
local id = screen.addWidget({
    x = 10, y = 10, width = 200, height = 20,
    text = { content = "Label" },
    active = false,  -- 非交互 = label
    onRender = function(ctx, mx, my, dt) return drawCmds end,
})

-- Button
screen.addWidget({
    x = 10, y = 40, width = 100, height = 20,
    text = { content = "OK" },
    active = true,
    onClick = function(x, y) print("clicked") end,
    onRender = function(ctx, mx, my, dt) ... end,
})

-- Widget 回调完整列表
onClick         -- fn(x, y)
onRender        -- fn(ctx, mx, my, delta) → draw commands
onMouseMoved    -- fn(x, y)
onMouseReleased -- fn(x, y, button) → bool
onMouseDragged  -- fn(x, y, dx, dy, button) → bool
onMouseScrolled -- fn(x, y, horiz, vert) → bool
onKeyPressed    -- fn(key, scancode, mods) → bool
onKeyReleased   -- fn(key, scancode, mods) → bool
onCharTyped     -- fn(codepoint, mods) → bool

-- Widget 管理
screen.removeWidget(id)
screen.clearWidgets()

-- 生命周期
screen.open()
screen.close()
gui.closeCurrentScreen()
```

### handledScreen — 容器屏幕

```lua
-- 适用于打开箱子/附魔台等容器界面时
local syncId = handledScreen.getSyncId()
local slots = handledScreen.getAllSlots()
local focused = handledScreen.getFocusedSlotId()

-- 附魔台
local enchantments = handledScreen.getEnchantmentButtons()
-- { { id = "minecraft:sharpness", level = 3 }, ... }

-- Slot 操作
handledScreen.clickSlot(slotId, button, "PICKUP")
handledScreen.clickButton(buttonId)
```

### tabList — 玩家列表

```lua
-- 读取
local entries = tabList.getListedPlayerListEntries()
for i, e in ipairs(entries) do
    -- e.displayName, e.latency, e.listOrder
end

-- 修改 / 删除
tabList.onPlayerListEntriesModified(function(entry)
    entry.latency = 999
    return entry  -- return nil 删除该条目
end)
```

### logger — 日志

```lua
logger.info("MyScript", "something happened")
logger.warn("MyScript", "warning message")
logger.error("MyScript", "error message")
```

## Draw Commands

HUD 和 GUI 的 `onRender` 回调返回 draw command 数组，每种 command 是一个 table：

| type | 字段 | 说明 |
|---|---|---|
| `fill_rendering` | x1, y1, x2, y2, color | 填充矩形 |
| `text_rendering` | text, x, y, color, shadow | 渲染文本 |
| `item_rendering` | item, x, y, seed | 渲染物品 |
| `item_comprehensively_rendering` | item, x, y, seed | 完整物品渲染 |
| `item_overlay_rendering` | item, x, y, countText | 物品耐久条 |
| `item_comprehensively_overlay_rendering` | item, x, y, countText | 完整物品耐久条 |
| `scissor_enabled` | x1, y1, x2, y2 | 启用裁剪 |
| `scissor_disabled` | — | 禁用裁剪 |
| `matrix_pushed` | — | 矩阵入栈 |
| `matrix_popped` | — | 矩阵出栈 |
| `matrix_translated` | x, y | 平移 |
| `matrix_translation` | x, y | 绝对平移 |
| `matrix_scaled` | x, y | 缩放 |
| `matrix_scaled_around` | sx, sy, ox, oy | 绕点缩放 |
| `matrix_set` | matrix | 设置矩阵 |
| `matrix_inverted` | — | 翻转矩阵 |

## 数据表结构

以下 Lua table 结构由 Kotlin 适配层自动生成，脚本中直接使用。

### Entity

```lua
{
    id = 12345,           -- int
    uuid = "550e8400...", -- string
    username = "Steve",   -- string | nil (非玩家为 nil)
    type = "minecraft:zombie",
    positionX = 100.5, positionY = 64.0, positionZ = 200.0,
    velocityX = 0.0, velocityY = -0.1, velocityZ = 0.0,
    yaw = 45.0, pitch = 0.0,
    height = 1.8, width = 0.6,
    onGround = true,
    maxHealth = 20.0,     -- number | nil (LivingEntity 才有)
    health = 15.0,        -- number | nil
    equipment = {
        mainHand = ItemStack, offHand = ItemStack,
        head = ItemStack, chest = ItemStack, legs = ItemStack, feet = ItemStack,
        body = ItemStack, saddle = ItemStack,
    },
    asItem = ItemStack,   -- ItemEntity 掉落的物品 | nil
}
```

### ItemStack

```lua
{
    id = "minecraft:diamond_sword",
    type = "item.minecraft.diamond_sword",  -- translation key
    count = 1, maxCount = 1,
    itemName = "Diamond Sword",   -- 自定义名
    name = "Diamond Sword",       -- 显示名
    enchantments = {              -- 1-based array
        { id = "minecraft:sharpness", level = 5 },
    },
    isDamageable = true,
    damage = 100, maxDamage = 1561,
}
```

### ItemStack Comprehensively（Codec 序列化）

通过 `getInventoryStacksComprehensively()` 等方法获取，返回 JSON 字符串。

### HitResult（准星目标）

```lua
-- type = "block"
{ type = "block", side = "UP", bx = 10, by = 64, bz = 20, x = 10.5, y = 65.0, z = 20.3 }

-- type = "entity"
{ type = "entity", entity = Entity, x = 10.5, y = 64.0, z = 20.3 }

-- type = "miss"
{ type = "miss", x = 100.0, y = 80.0, z = 300.0 }
```

### DamageSource

```lua
{
    type = "mob",           -- damage type msgId
    attacker = Entity,      -- 攻击者 | nil
    source = Entity,        -- 来源（弹射物等）| nil
    x = 10.0, y = 64.0, z = 20.0,  -- 伤害位置 | nil
}
```

### BlockState（Comprehensively）

`getBlockState()` 返回 codec 序列化的 JSON 字符串。

### Text（富文本）

```lua
{
    content = "Hello World",      -- 翻译后的文本
    style = {
        color = { color = "red" },
        shadowColor = 0xFF000000,  -- int | nil
        bold = false, italic = false, underlined = false,
        strikethrough = false, obfuscated = false,
    },
    siblings = {                  -- 1-based array of Text
        { content = "suffix", style = {...}, siblings = {...} },
    },
}
```

### PlayerListEntry

```lua
{
    displayName = Text,  -- Text table | nil
    latency = 42,        -- int (ms)
    listOrder = 0,       -- int
}
```

### DrawContext

```lua
{
    scaledWidth = 1920, scaledHeight = 1080,
    m00 = 1.0, m01 = 0.0, m10 = 0.0, m11 = 1.0, m20 = 0.0, m21 = 0.0,
}
```

### RenderTickCounter

```lua
{
    dynamicDeltaTicks = 0.05,
    tickProgressFreezeIgnored = 0.5,
    tickProgress = 0.5,
    fixedDeltaTicks = 0.05,
}
```

## Lua 辅助库

位于 `lua-rook-scripts/libs/`，可在脚本中用 `require` 加载：

| 库 | 说明 |
|---|---|
| `libs.gui_helper` | `addButton` `addLabel` `addCheckbox` `addSlider` |
| `libs.hud_render_helper` | `RenderingPageBuilder` draw command 链式构造器 |
| `libs.tick_helper` | `createTickStartedTask` `createTickEndedTask` 定时任务 |
| `libs.chat_logger` | `getLogger(name)` 客户端聊天栏日志 |
| `libs.text_helper` | `makeText` `colorStyle` Text 对象构造 |
| `libs.settings` | `get/set/isExist` JSON 配置文件读写 |

## 安全警告

Lua 脚本拥有 **完整的 JVM 访问权限**（通过 `luajava`），可以执行任意 Java 代码。只加载你信任的脚本。

## 构建

```bash
./gradlew build
```

输出在 `build/libs/lua-rook-1.0.0.jar`。

要求：
- JDK 21+
- Minecraft 1.21.1
- Fabric Loader
- Fabric API
- Fabric Language Kotlin

## 许可证

MIT
