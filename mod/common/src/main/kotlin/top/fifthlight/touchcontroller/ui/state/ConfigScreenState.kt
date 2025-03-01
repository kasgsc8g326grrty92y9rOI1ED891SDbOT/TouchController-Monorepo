package top.fifthlight.touchcontroller.ui.state

import top.fifthlight.touchcontroller.config.GlobalConfig

data class ConfigScreenState(
    val originalConfig: GlobalConfig,
    val config: GlobalConfig = originalConfig,
)