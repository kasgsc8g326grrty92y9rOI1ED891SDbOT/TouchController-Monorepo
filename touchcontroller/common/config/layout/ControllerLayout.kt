package top.fifthlight.touchcontroller.common.config.layout

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.Serializable
import top.fifthlight.touchcontroller.common.config.layout.LayoutLayer
import top.fifthlight.touchcontroller.common.config.serializer.ControllerLayoutSerializer

@JvmInline
@Serializable(with = ControllerLayoutSerializer::class)
value class ControllerLayout(
    val layers: PersistentList<LayoutLayer> = persistentListOf(),
) : PersistentList<LayoutLayer> by layers

fun controllerLayoutOf(vararg layers: LayoutLayer?) = ControllerLayout(layers.mapNotNull { it }.toPersistentList())
