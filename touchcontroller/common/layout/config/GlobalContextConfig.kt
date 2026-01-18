package top.fifthlight.touchcontroller.common.layout.config

import top.fifthlight.touchcontroller.common.config.ControlConfig
import top.fifthlight.touchcontroller.common.config.GlobalConfig
import top.fifthlight.touchcontroller.common.config.TouchRingConfig
import top.fifthlight.touchcontroller.common.gal.PlayerHandle
import top.fifthlight.touchcontroller.common.layout.ContextConfig

class GlobalContextConfig(val config: GlobalConfig) : ContextConfig {
    override val touchRingConfig: TouchRingConfig
        get() = config.touchRing
    override val controlConfig: ControlConfig
        get() = config.control
    override val showPointers: Boolean
        get() = config.debug.showPointers
    override val quickHandSwap: Boolean
        get() = config.regular.quickHandSwap

    override fun isHandItemUsable(player: PlayerHandle): Boolean = player.hasItemsOnHand(config.item.usableItems)
}
