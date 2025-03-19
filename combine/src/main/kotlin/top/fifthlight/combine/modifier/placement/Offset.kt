package top.fifthlight.combine.modifier.placement

import top.fifthlight.combine.layout.Measurable
import top.fifthlight.combine.layout.MeasureResult
import top.fifthlight.combine.layout.MeasureScope
import top.fifthlight.combine.modifier.Constraints
import top.fifthlight.combine.modifier.LayoutModifierNode
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.data.IntOffset

fun Modifier.offset(size: Int = 0): Modifier = offset(size, size)

fun Modifier.offset(size: IntOffset = IntOffset.ZERO): Modifier = offset(size.x, size.y)

fun Modifier.offset(x: Int = 0, y: Int = 0): Modifier = then(AbsoluteOffsetNode(x, y))

fun Modifier.offset(x: Float = 0f, y: Float = 0f): Modifier = then(RelativeOffsetNode(x, y))

private data class AbsoluteOffsetNode(
    val x: Int = 0,
    val y: Int = 0,
) : LayoutModifierNode, Modifier.Node<AbsoluteOffsetNode> {
    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            placeable.placeAt(x, y)
        }
    }
}

private data class RelativeOffsetNode(
    val x: Float = 0f,
    val y: Float = 0f,
) : LayoutModifierNode, Modifier.Node<AbsoluteOffsetNode> {
    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            placeable.placeAt((placeable.width * x).toInt(), (placeable.height * y).toInt())
        }
    }
}