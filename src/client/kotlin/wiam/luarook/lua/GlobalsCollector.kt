package wiam.luarook.lua

import org.luaj.vm2.Globals

object GlobalsCollector {
    private val sessions = mutableMapOf<String, ApiSession>()

    val allGlobals: Map<String, Globals>
        get() = sessions.mapValues { it.value.globals }

    fun get(name: String): Globals? = sessions[name]?.globals

    fun getSession(name: String): ApiSession? = sessions[name]

    fun put(name: String, session: ApiSession) {
        sessions[name]?.let { ApiBridge.unregister(it) }
        sessions[name] = session
        ApiBridge.register(session)
    }

    fun remove(name: String): ApiSession? {
        val session = sessions.remove(name)
        session?.let { ApiBridge.unregister(it) }
        return session
    }

    fun listNames(): Set<String> = sessions.keys.toSet()

    fun clear() {
        sessions.values.forEach { ApiBridge.unregister(it) }
        sessions.clear()
    }
}
