package top.fifthlight.touchcontroller.common.control

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import top.fifthlight.combine.data.TextFactory
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntSize
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.common.ext.fastRandomUuid
import top.fifthlight.touchcontroller.common.layout.Align
import top.fifthlight.touchcontroller.common.layout.Context
import top.fifthlight.touchcontroller.common.layout.PerspectiveSwitchButton
import kotlin.math.round
import kotlin.uuid.Uuid

@Serializable
enum class PerspectiveSwitchButtonStyle {
    @SerialName("classic")
    CLASSIC,

    @SerialName("new")
    NEW,
}

@Serializable
@SerialName("perspective_switch_button")
data class PerspectiveSwitchButton(
    val size: Float = 1f,
    val style: PerspectiveSwitchButtonStyle = PerspectiveSwitchButtonStyle.CLASSIC,
    override val id: Uuid = fastRandomUuid(),
    override val name: Name = Name.Translatable(Texts.WIDGET_PERSPECTIVE_SWITCH_BUTTON_NAME),
    override val align: Align = Align.CENTER_TOP,
    override val offset: IntOffset = IntOffset.ZERO,
    override val opacity: Float = 1f,
    override val lockMoving: Boolean = false,
) : ControllerWidget() {
    companion object : KoinComponent {
        private val textFactory: TextFactory by inject()

        @Suppress("UNCHECKED_CAST")
        private val _properties = properties + persistentListOf<Property<PerspectiveSwitchButton, *>>(
            FloatProperty(
                getValue = { it.size },
                setValue = { config, value -> config.copy(size = value) },
                range = .5f..4f,
                messageFormatter = {
                    textFactory.format(
                        Texts.WIDGET_PAUSE_BUTTON_PROPERTY_SIZE,
                        round(it * 100f).toString()
                    )
                }
            ),
            EnumProperty(
                getValue = { it.style },
                setValue = { config, value -> config.copy(style = value) },
                name = textFactory.format(Texts.WIDGET_PERSPECTIVE_SWITCH_BUTTON_PROPERTY_STYLE),
                items = listOf(
                    PerspectiveSwitchButtonStyle.CLASSIC to textFactory.of(Texts.WIDGET_PERSPECTIVE_SWITCH_BUTTON_PROPERTY_STYLE_CLASSIC),
                    PerspectiveSwitchButtonStyle.NEW to textFactory.of(Texts.WIDGET_PERSPECTIVE_SWITCH_BUTTON_PROPERTY_STYLE_NEW),
                ),
            ),
        ) as PersistentList<Property<ControllerWidget, *>>
    }

    override val properties
        get() = _properties

    private val textureSize
        get() = 18

    override fun size(): IntSize = IntSize((size * textureSize).toInt())

    override fun layout(context: Context) {
        context.PerspectiveSwitchButton(config = this)
    }

    override fun cloneBase(
        id: Uuid,
        name: Name,
        align: Align,
        offset: IntOffset,
        opacity: Float,
        lockMoving: Boolean
    ) = copy(
        id = id,
        name = name,
        align = align,
        offset = offset,
        opacity = opacity,
        lockMoving = lockMoving,
    )
}