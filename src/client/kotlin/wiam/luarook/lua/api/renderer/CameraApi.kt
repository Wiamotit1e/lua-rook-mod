package wiam.luarook.lua.api.renderer

import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.Vec3d
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
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
                set("w", clipW(x.todouble(), y.todouble(), z.todouble()))
            }
            v2
        })

        // 世界坐标 → 屏幕坐标（GUI 缩放坐标，可直接用于 hud 绘制）
        t.fn3("worldToScreen", { x, y, z ->
            val wx = x.todouble()
            val wy = y.todouble()
            val wz = z.todouble()
            val v = mc.gameRenderer.project(Vec3d(wx, wy, wz))
            val w = clipW(wx, wy, wz)
            val inFront = w > 0.0
            val window = mc.window
            LuaTable().apply {
                set("x", (v.x + 1.0) * 0.5 * window.scaledWidth)
                set("y", (1.0 - v.y) * 0.5 * window.scaledHeight)
                // 到相机的轴向距离（格），也是投影前的 w
                set("w", w)
                set("inFront", LuaValue.valueOf(inFront))
                // 在相机前方且落在视锥内 —— 可直接据此决定是否绘制
                set("visible", LuaValue.valueOf(
                    inFront &&
                        v.x in -1.0..1.0 && v.y in -1.0..1.0 &&
                        v.z in -1.0..1.0
                ))
            }
        })
    }

    /**
     * 目标点相对相机的轴向距离，等价于投影矩阵里除掉的 w：> 0 表示在相机前方。
     * 相机朝向已在 [net.minecraft.client.render.Camera.getHorizontalPlane] 里算好，
     * 直接点乘即可，不必再走一遍投影矩阵。
     */
    private fun clipW(x: Double, y: Double, z: Double): Double {
        val camera = mc.gameRenderer.camera
        val pos = camera.cameraPos
        val forward = camera.horizontalPlane
        return (x - pos.x) * forward.x() + (y - pos.y) * forward.y() + (z - pos.z) * forward.z()
    }
}
