package wiam.luarook.lua

import org.luaj.vm2.Globals
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.ThreeArgFunction
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.ZeroArgFunction

/**
 * Base class for Lua-exposed API modules.
 *
 * Subclasses override [register] to declare functions and events using the DSL:
 * - `t.fn0("name") { ... }` — zero-arg function
 * - `t.fn1("name") { arg -> ... }` — one-arg function
 * - `t.fn2("name") { a, b -> ... }` — two-arg function
 * - `t.fn3("name") { a, b, c -> ... }` — three-arg function
 * - `t.event("name")` — registers an `onXxx` listener setter, callable from Lua
 *
 * Events are fired externally (from [ApiBridge] or mixins) via:
 * - [fire] — notify all listeners
 * - [fireAll] — ask all listeners, returns true only if every listener returns true
 * - [fireModify] — chain string modification through all listeners
 *
 * Example subclass:
 * ```kotlin
 * class MyApi : LuaApi("myapi") {
 *     override fun register(t: LuaTable) {
 *         t.fn0("ping") { LuaValue.valueOf("pong") }
 *         t.event("somethingHappened")
 *     }
 * }
 * // Lua: myapi.ping()  -->  "pong"
 * // Lua: myapi.onSomethingHappened(function() print("fired") end)
 * ```
 */
abstract class LuaApi(val namespace: String) {

    /** Set by [ApiSession] before [inject] is called. */
    var scriptName: String = "unknown"

    /** Event name → list of Lua listener functions. Accessible to subclasses for custom fire logic. */
    protected val listeners = mutableMapOf<String, MutableList<LuaValue>>()

    // ---- Subclass contract ----

    /** Override to declare functions and events on the given [LuaTable]. */
    protected abstract fun register(t: LuaTable)

    // ---- Called by ApiSession ----

    fun inject(globals: Globals) {
        val table = LuaTable()
        register(table)
        globals[namespace] = table
    }

    open fun dispose() {
        listeners.values.forEach { it.clear() }
        listeners.clear()
    }

    // ---- Function registration DSL ----

    protected fun LuaTable.fn0(name: String, body: () -> LuaValue) {
        this[name] = object : ZeroArgFunction() {
            override fun call(): LuaValue = body()
        }
    }

    protected fun LuaTable.fn1(name: String, body: (LuaValue) -> LuaValue) {
        this[name] = object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue = body(arg)
        }
    }

    protected fun LuaTable.fn2(name: String, body: (LuaValue, LuaValue) -> LuaValue) {
        this[name] = object : TwoArgFunction() {
            override fun call(a: LuaValue, b: LuaValue): LuaValue = body(a, b)
        }
    }

    protected fun LuaTable.fn3(name: String, body: (LuaValue, LuaValue, LuaValue) -> LuaValue) {
        this[name] = object : ThreeArgFunction() {
            override fun call(a: LuaValue, b: LuaValue, c: LuaValue): LuaValue = body(a, b, c)
        }
    }

    // ---- Event registration ----

    /**
     * Registers an event listener setter. On the Lua side this creates a method
     * `onXxx` that takes a function and adds it to the listener list.
     * @param name short event name, e.g. "chatReceived" → Lua method "onChatReceived"
     */
    protected fun LuaTable.event(name: String) {
        val list = mutableListOf<LuaValue>()
        listeners[name] = list
        this["on" + name.replaceFirstChar { it.uppercase() }] = object : OneArgFunction() {
            override fun call(arg: LuaValue): LuaValue {
                if (arg.isfunction()) list.add(arg)
                return NIL
            }
        }
    }

    // ---- Fire helpers (called by ApiBridge / mixins) ----

    /** Fire event to all listeners. Errors are reported via [ErrorReporter]. */
    internal fun fire(event: String, vararg args: LuaValue) {
        listeners[event]?.forEach { fn ->
            try { fn.dispatch(args) }
            catch (e: Exception) { ErrorReporter.reportRuntimeError(scriptName, "$namespace.$event", e) }
        }
    }

    /** Fire event to all listeners; returns true only if ALL listeners return true. */
    internal fun fireAll(event: String, vararg args: LuaValue): Boolean {
        return listeners[event]?.all { fn ->
            try { fn.dispatch(args).toboolean() }
            catch (e: Exception) { ErrorReporter.reportRuntimeError(scriptName, "$namespace.$event", e); true }
        } ?: true
    }

    /** Chain-modify a string through all listeners. Each listener receives the current string, returns the modified version. */
    internal fun fireModify(event: String, initial: String): String {
        var result = initial
        listeners[event]?.forEach { fn ->
            try { result = fn.call(LuaValue.valueOf(result)).tojstring() }
            catch (e: Exception) { ErrorReporter.reportRuntimeError(scriptName, "$namespace.$event", e) }
        }
        return result
    }

    // ---- Internal ----

    /** Dispatch to a Lua function with the right [call] overload based on arg count. */
    private fun LuaValue.dispatch(args: Array<out LuaValue>): LuaValue {
        return when (args.size) {
            0 -> call()
            1 -> call(args[0])
            2 -> call(args[0], args[1])
            3 -> call(args[0], args[1], args[2])
            else -> {
                var tail: Varargs = LuaValue.NIL
                for (i in args.size - 1 downTo 0) {
                    tail = LuaValue.varargsOf(args[i], tail)
                }
                invoke(tail).arg1()
            }
        }
    }
}
