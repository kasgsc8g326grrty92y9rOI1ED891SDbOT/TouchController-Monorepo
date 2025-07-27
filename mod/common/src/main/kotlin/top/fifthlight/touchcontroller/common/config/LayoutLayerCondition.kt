package top.fifthlight.touchcontroller.common.config

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import top.fifthlight.combine.data.Identifier
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.common.ext.LayerConditionSerializer
import top.fifthlight.touchcontroller.common.ext.LayoutLayerCustomConditionSerializer
import kotlin.uuid.Uuid

@Serializable
enum class LayerConditionValue(val text: Identifier) {
    @SerialName("never")
    NEVER(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_NEVER),

    @SerialName("want")
    WANT(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_WANT),

    @SerialName("require")
    REQUIRE(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_REQUIRE);

    companion object {
        val allValues = persistentListOf(
            NEVER,
            WANT,
            REQUIRE,
            null,
        )
    }
}

fun LayerConditionValue?.text() = when (this) {
    null -> Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_IGNORE
    else -> this.text
}

@Serializable
enum class LayerConditionKey(val text: Identifier) {
    @SerialName("swimming")
    SWIMMING(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_SWIMMING),

    @SerialName("underwater")
    UNDERWATER(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_UNDERWATER),

    @SerialName("flying")
    FLYING(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_FLYING),

    @SerialName("can_fly")
    CAN_FLY(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_CAN_FLY),

    @SerialName("sneaking")
    SNEAKING(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_SNEAKING),

    @SerialName("sprinting")
    SPRINTING(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_SPRINTING),

    @SerialName("on_ground")
    ON_GROUND(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_ON_GROUND),

    @SerialName("no_on_ground")
    NOT_ON_GROUND(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_NOT_ON_GROUND),

    @SerialName("using_item")
    USING_ITEM(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_USING_ITEM),

    @SerialName("on_minecart")
    ON_MINECART(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_ON_MINECART),

    @SerialName("on_boat")
    ON_BOAT(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_ON_BOAT),

    @SerialName("on_pig")
    ON_PIG(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_ON_PIG),

    @SerialName("on_horse")
    ON_HORSE(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_ON_HORSE),

    @SerialName("on_camel")
    ON_CAMEL(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_ON_CAMEL),

    @SerialName("on_llama")
    ON_LLAMA(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_ON_LLAMA),

    @SerialName("on_strider")
    ON_STRIDER(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_ON_STRIDER),

    @SerialName("riding")
    RIDING(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_RIDING),

    @SerialName("block_selected")
    BLOCK_SELECTED(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_BLOCK_SELECTED),
}

fun layoutLayerConditionOf(vararg pairs: Pair<LayerConditionKey, LayerConditionValue>) =
    LayoutLayerCondition(persistentMapOf(*pairs))

@Serializable(with = LayerConditionSerializer::class)
@JvmInline
value class LayoutLayerCondition(
    val conditions: PersistentMap<LayerConditionKey, LayerConditionValue> = persistentMapOf(),
) : PersistentMap<LayerConditionKey, LayerConditionValue> by conditions {
    fun check(currentState: PersistentMap<LayerConditionKey, Boolean>): Boolean {
        var haveWant = false
        var haveFulfilledWant = false
        for (condition in this) {
            val current = currentState[condition.key]
            when (condition.value) {
                LayerConditionValue.NEVER -> if (current == true) {
                    return false
                }

                LayerConditionValue.WANT -> {
                    haveWant = true
                    if (current == true) {
                        haveFulfilledWant = true
                    }
                }

                LayerConditionValue.REQUIRE -> if (current != true) {
                    return false
                }
            }
        }
        return !(haveWant && !haveFulfilledWant)
    }
}

@Serializable(with = LayoutLayerCustomConditionSerializer::class)
@JvmInline
value class LayoutLayerCustomCondition(
    val conditions: PersistentMap<Uuid, LayerConditionValue> = persistentMapOf(),
) : PersistentMap<Uuid, LayerConditionValue> by conditions {
    fun check(currentState: PersistentSet<Uuid>): Boolean {
        var haveWant = false
        var haveFulfilledWant = false
        for (condition in this) {
            val current = condition.key in currentState
            when (condition.value) {
                LayerConditionValue.NEVER -> if (current) {
                    return false
                }

                LayerConditionValue.WANT -> {
                    haveWant = true
                    if (current) {
                        haveFulfilledWant = true
                    }
                }

                LayerConditionValue.REQUIRE -> if (!current) {
                    return false
                }
            }
        }
        return !(haveWant && !haveFulfilledWant)
    }
}