package wiam.luarook.lua

/**
 * Lua-set input keys. null = not set by Lua (keyboard takes over).
 */
object LuaInputOverride {

    var forward: Boolean? = null
    var backward: Boolean? = null
    var left: Boolean? = null
    var right: Boolean? = null
    var jump: Boolean? = null
    var sneak: Boolean? = null
    var sprint: Boolean? = null

    val active: Boolean get() = forward != null || backward != null || left != null
            || right != null || jump != null || sneak != null || sprint != null

    fun set(forward: Boolean?, backward: Boolean?, left: Boolean?, right: Boolean?,
            jump: Boolean?, sneak: Boolean?, sprint: Boolean?) {
        this.forward = forward; this.backward = backward
        this.left = left; this.right = right
        this.jump = jump; this.sneak = sneak; this.sprint = sprint
    }

    fun clear() {
        forward = null; backward = null; left = null; right = null
        jump = null; sneak = null; sprint = null
    }
}
