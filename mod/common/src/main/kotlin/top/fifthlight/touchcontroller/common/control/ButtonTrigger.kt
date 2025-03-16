package top.fifthlight.touchcontroller.common.control

import kotlinx.serialization.Serializable

@Serializable
data class ButtonTrigger(
    val down: WidgetTriggerAction? = null,
    val press: String? = null,
    val release: WidgetTriggerAction? = null,
    val doubleClick: DoubleClickTrigger = DoubleClickTrigger(),
) {
    @Serializable
    data class DoubleClickTrigger(
        val interval: Int = 7,
        val action: WidgetTriggerAction? = null,
    )
}
