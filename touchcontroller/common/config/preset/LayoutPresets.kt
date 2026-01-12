package top.fifthlight.touchcontroller.common.config.preset

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import top.fifthlight.touchcontroller.common.config.preset.serializer.LayoutPresetsSerializer

@JvmInline
@Serializable(with = LayoutPresetsSerializer::class)
value class LayoutPresets(
    val presets: PersistentList<LayoutPreset> = persistentListOf(),
)

fun layoutPresetsOf(vararg pairs: LayoutPreset) = LayoutPresets(persistentListOf(*pairs))
