package top.fifthlight.touchcontroller.common.config.preset

import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlin.collections.map
import kotlin.collections.toMap

data class PresetsContainer(
    val orderedEntries: kotlinx.collections.immutable.PersistentList<kotlin.Pair<kotlin.uuid.Uuid, top.fifthlight.touchcontroller.common.config.preset.LayoutPreset>> = kotlinx.collections.immutable.persistentListOf(),
) : kotlinx.collections.immutable.PersistentMap<kotlin.uuid.Uuid, top.fifthlight.touchcontroller.common.config.preset.LayoutPreset> by orderedEntries.toMap().toPersistentMap() {
    val order: kotlinx.collections.immutable.ImmutableList<kotlin.uuid.Uuid>
        get() = orderedEntries.map { it.first }.toPersistentList()
}

fun PresetsContainer(
    presets: ImmutableMap<Uuid, LayoutPreset>,
    order: ImmutableList<Uuid>,
): PresetsContainer {
    val orderedEntries = mutableListOf<Pair<Uuid, LayoutPreset>>()
    val entries = presets.toMutableMap()
    for (uuid in order) {
        val preset = entries.remove(uuid) ?: continue
        orderedEntries += Pair(uuid, preset)
    }
    for ((uuid, preset) in entries.entries.sortedBy { (id, _) -> id.toJavaUuid() }) {
        orderedEntries += uuid to preset
    }
    return PresetsContainer(orderedEntries.toPersistentList())
}