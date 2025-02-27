package top.fifthlight.touchcontroller.config

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import top.fifthlight.combine.data.Identifier
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.control.ControllerWidget
import top.fifthlight.touchcontroller.ext.ControllerLayoutSerializer
import top.fifthlight.touchcontroller.ext.LayerConditionSerializer
import top.fifthlight.touchcontroller.ext.LayoutLayerSerializer

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
}

@Serializable(with = LayerConditionSerializer::class)
@JvmInline
value class LayoutLayerCondition(
    val conditions: PersistentMap<LayerConditionKey, LayerConditionValue> = persistentMapOf()
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

fun layoutLayerConditionOf(vararg pairs: Pair<LayerConditionKey, LayerConditionValue>) =
    LayoutLayerCondition(persistentMapOf(*pairs))

@Serializable(with = LayoutLayerSerializer::class)
data class LayoutLayer(
    val name: String = DEFAULT_LAYER_NAME,
    val widgets: PersistentList<ControllerWidget> = persistentListOf(),
    val condition: LayoutLayerCondition = LayoutLayerCondition(),
) {
    companion object {
        const val DEFAULT_LAYER_NAME = "Unnamed layer"
    }
}

@JvmInline
@Serializable(with = ControllerLayoutSerializer::class)
value class ControllerLayout(
    val layers: PersistentList<LayoutLayer> = persistentListOf(),
) : PersistentList<LayoutLayer> by layers

fun controllerLayoutOf(vararg layers: LayoutLayer) = ControllerLayout(persistentListOf(*layers))
