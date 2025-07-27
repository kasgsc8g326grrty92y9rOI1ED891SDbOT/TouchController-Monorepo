package top.fifthlight.touchcontroller.common.ext

import kotlinx.collections.immutable.toPersistentMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SealedSerializationApi
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import top.fifthlight.touchcontroller.common.config.LayerConditionValue
import top.fifthlight.touchcontroller.common.config.LayoutLayerCustomCondition
import kotlin.uuid.Uuid

class LayoutLayerCustomConditionSerializer : KSerializer<LayoutLayerCustomCondition> {
    @OptIn(SealedSerializationApi::class)
    private class PersistentMapDescriptor :
        SerialDescriptor by serialDescriptor<Map<Uuid, LayerConditionValue>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "top.fifthlight.touchcontroller.common.config.LayoutLayerCustomCondition"
    }

    private val keySerializer = serializer<Uuid>()
    private val valueSerializer = serializer<LayerConditionValue>()
    private val mapSerializer = MapSerializer(keySerializer, valueSerializer)

    override val descriptor: SerialDescriptor = PersistentMapDescriptor()

    override fun serialize(encoder: Encoder, value: LayoutLayerCustomCondition) =
        mapSerializer.serialize(encoder, value)

    override fun deserialize(decoder: Decoder) =
        LayoutLayerCustomCondition(mapSerializer.deserialize(decoder).toPersistentMap())
}