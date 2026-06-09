package wiam.luarook.lua

import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import org.slf4j.LoggerFactory

object ErrorReporter {
    private val logger = LoggerFactory.getLogger("lua-rook/ErrorReporter")

    private const val RATE_LIMIT_MS = 5000L

    private val lastReport = object : LinkedHashMap<String, Long>(64, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Long>?): Boolean {
            return size > 128
        }
    }

    fun reportLoadError(scriptName: String, message: String) {
        logger.error("Error in $scriptName.lua: $message")
        sendToChat("§c$scriptName.lua §7— §c$message")
    }

    fun reportRuntimeError(scriptName: String, context: String, error: Exception) {
        logger.error("[$scriptName] $context error", error)

        val key = "$scriptName::$context"
        val now = System.currentTimeMillis()
        val last = lastReport[key]
        if (last != null && now - last < RATE_LIMIT_MS) return

        lastReport[key] = now
        val shortMsg = error.message ?: error.javaClass.simpleName
        sendToChat("§c$scriptName.lua §8[$context] §7— §c$shortMsg")
    }

    private fun sendToChat(message: String) {
        MinecraftClient.getInstance().execute {
            MinecraftClient.getInstance().player?.sendMessage(
                Text.literal("§7[§bLuaRook§7] $message"),
                false
            )
        }
    }
}
