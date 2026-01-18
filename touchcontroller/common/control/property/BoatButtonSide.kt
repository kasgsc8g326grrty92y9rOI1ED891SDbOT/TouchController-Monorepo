package top.fifthlight.touchcontroller.common.control.property

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class BoatButtonSide {
    @SerialName("left")
    LEFT,

    @SerialName("right")
    RIGHT
}
