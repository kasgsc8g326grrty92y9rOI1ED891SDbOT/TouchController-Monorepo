package top.fifthlight.touchcontroller.common.config

import kotlinx.serialization.Serializable
import top.fifthlight.combine.paint.Color
import top.fifthlight.combine.paint.Colors.WHITE

@Serializable
data class ChatConfig(
    val lineSpacing: Int = 0,
    val textColor: Color = WHITE,
)