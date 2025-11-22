package top.fifthlight.armorstand.ui.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Checkbox
import net.minecraft.network.chat.Component
import top.fifthlight.armorstand.ui.component.CheckBoxButton
import top.fifthlight.armorstand.ui.model.ViewModel
import top.fifthlight.armorstand.ui.screen.ArmorStandScreen

fun <T : ArmorStandScreen<T, M>, M : ViewModel> ArmorStandScreen<T, M>.checkbox(
    text: Component,
    value: Flow<Boolean>,
    enabled: Boolean = true,
    maxWidth: Int? = null,
    onValueChanged: (Boolean) -> Unit,
): Checkbox = Checkbox.builder(text, Minecraft.getInstance().font)
    .apply {
        onValueChange { checkbox, checked -> onValueChanged(checked) }
        maxWidth?.let { maxWidth(it) }
    }
    .build()
    .apply {
        active = enabled
        scope.launch {
            value.collect {
                selected = it
            }
        }
    }

fun <T : ArmorStandScreen<T, M>, M : ViewModel> ArmorStandScreen<T, M>.checkbox(
    value: Flow<Boolean>,
    enabled: Boolean = true,
    onChecked: () -> Unit,
) = CheckBoxButton(false, onChecked).apply {
    active = enabled
    scope.launch {
        value.collect {
            checked = it
        }
    }
}
