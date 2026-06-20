package wiam.luarook.lua

import org.luaj.vm2.LuaValue
import org.luaj.vm2.LuaValue.NIL

/** Call a Lua function with 4 args (LuaJ [LuaValue.call] only supports 0–3). */
fun LuaValue.invoke4(a1: LuaValue, a2: LuaValue, a3: LuaValue, a4: LuaValue): LuaValue {
    val v1 = LuaValue.varargsOf(a4, NIL)
    val v2 = LuaValue.varargsOf(a3, v1)
    val v3 = LuaValue.varargsOf(a2, v2)
    return invoke(LuaValue.varargsOf(a1, v3)).arg1()
}

/** Call a Lua function with 5 args (LuaJ [LuaValue.call] only supports 0–3). */
fun LuaValue.invoke5(a1: LuaValue, a2: LuaValue, a3: LuaValue, a4: LuaValue, a5: LuaValue): LuaValue {
    val v1 = LuaValue.varargsOf(a5, NIL)
    val v2 = LuaValue.varargsOf(a4, v1)
    val v3 = LuaValue.varargsOf(a3, v2)
    val v4 = LuaValue.varargsOf(a2, v3)
    return invoke(LuaValue.varargsOf(a1, v4)).arg1()
}
