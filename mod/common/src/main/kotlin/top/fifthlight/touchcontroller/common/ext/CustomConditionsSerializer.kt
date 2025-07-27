package top.fifthlight.touchcontroller.common.ext

import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SealedSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import top.fifthlight.touchcontroller.common.config.preset.CustomCondition
import top.fifthlight.touchcontroller.common.config.preset.CustomConditions

class CustomConditionsSerializer : KSerializer<CustomConditions> {
    @OptIn(SealedSerializationApi::class)
    private class PersistentListDescriptor : SerialDescriptor by serialDescriptor<List<CustomCondition>>() {
        @OptIn(ExperimentalSerializationApi::class)
        override val serialName: String = "top.fifthlight.touchcontroller.common.config.preset.CustomConditions"
    }

    private val itemSerializer = serializer<CustomCondition>()
    private val delegatedSerializer = ListSerializer(itemSerializer)

    override val descriptor: SerialDescriptor = PersistentListDescriptor()

    override fun serialize(encoder: Encoder, value: CustomConditions) =
        delegatedSerializer.serialize(encoder, value.conditions)

    override fun deserialize(decoder: Decoder) =
        CustomConditions(delegatedSerializer.deserialize(decoder).toPersistentList())
}
