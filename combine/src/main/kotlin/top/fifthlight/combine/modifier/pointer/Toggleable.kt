package top.fifthlight.combine.modifier.pointer

import androidx.compose.runtime.Composable
import top.fifthlight.combine.modifier.Modifier

@Composable
fun Modifier.toggleable(
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
) = clickable {
    onValueChange(!value)
}
