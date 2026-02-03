package top.fifthlight.touchcontroller.version_1_21_11.gal

import net.minecraft.client.Minecraft
import net.minecraft.world.phys.HitResult
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.gal.view.CrosshairTarget
import top.fifthlight.touchcontroller.common.gal.view.ViewActionProvider
import top.fifthlight.touchcontroller.version_1_21_11.extensions.GameModeWithBreakingProgress

@ActualImpl(ViewActionProvider::class)
object ViewActionProviderImpl : ViewActionProvider {
    @JvmStatic
    @ActualConstructor
    fun of(): ViewActionProvider = this

    private val client = Minecraft.getInstance()

    override fun getCrosshairTarget(): CrosshairTarget? {
        val target = client.hitResult ?: return null
        return when (target.type) {
            HitResult.Type.ENTITY -> CrosshairTarget.ENTITY
            HitResult.Type.BLOCK -> CrosshairTarget.BLOCK
            HitResult.Type.MISS -> CrosshairTarget.MISS
            else -> return null
        }
    }

    override fun getCurrentBreakingProgress(): Float {
        val manager = client.gameMode
        val accessor = manager as GameModeWithBreakingProgress
        return accessor.`touchcontroller$getBreakingProgress`()
    }
}