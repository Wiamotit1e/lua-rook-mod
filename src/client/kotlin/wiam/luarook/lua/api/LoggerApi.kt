package wiam.luarook.lua.api

import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue.NIL
import org.slf4j.LoggerFactory
import wiam.luarook.lua.LuaApi

class LoggerApi : LuaApi("logger") {
    override fun register(t: LuaTable) {
        t.fn2("info") { name, message ->
            LoggerFactory.getLogger(name.tojstring()).info(message.tojstring())
            NIL
        }
        t.fn2("warn") { name, message ->
            LoggerFactory.getLogger(name.tojstring()).warn(message.tojstring())
            NIL
        }
        t.fn2("error") { name, message ->
            LoggerFactory.getLogger(name.tojstring()).error(message.tojstring())
            NIL
        }
    }
}