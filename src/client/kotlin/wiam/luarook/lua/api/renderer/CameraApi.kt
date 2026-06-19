package wiam.luarook.lua.api.renderer

import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.Vec3d
import org.luaj.vm2.LuaTable
import wiam.luarook.lua.LuaApi

class CameraApi : LuaApi("camera") {
    
    private val mc get() = MinecraftClient.getInstance()
    override fun register(t: LuaTable) {
        t.fn3("project", { x, y, z ->
            val v1 = mc.gameRenderer.project(Vec3d(x.todouble(), y.todouble(), z.todouble()))
            val v2 = LuaTable().apply {
                set("x", v1.x)
                set("y", v1.y)
                set("z", v1.z)
            }
            v2
        })
    }
}