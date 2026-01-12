package top.fifthlight.touchcontroller.common.control

import androidx.compose.runtime.Immutable
import top.fifthlight.data.IntOffset

@Immutable
@Serializable
sealed class ControllerWidget {
    abstract val id: kotlin.uuid.Uuid
    abstract val name: Name
    abstract val align: Align
    abstract val offset: IntOffset
    abstract val opacity: Float
    abstract val lockMoving: Boolean

    @Immutable
    @Serializable
    sealed class Name {
        @Serializable
        @SerialName("translatable")
        data class Translatable(val identifier: Identifier) : Name()

        @Serializable
        @SerialName("literal")
        data class Literal(val string: String) : Name()

        fun getText(textFactory: TextFactory) = when (this) {
            is Translatable -> textFactory.of(identifier)
            is Literal -> textFactory.literal(string)
        }

        @Composable
        fun getText() = getText(TextFactory.current)

        fun asString(textFactory: TextFactory) = getText(textFactory).string

        @Composable
        fun asString() = getText().string
    }

    abstract class Property<Config : ControllerWidget, Value>(
        val getValue: (Config) -> Value,
        val setValue: (Config, Value) -> Config,
    ) {
        @Composable
        abstract fun controller(
            modifier: Modifier,
            config: ControllerWidget,
            currentPreset: LayoutPreset?,
            onConfigChanged: (ControllerWidget) -> Unit,
        )
    }

    companion object {
        private val textFactory = TextFactoryFactory.of()

        val properties = persistentListOf<Property<ControllerWidget, *>>(
            NameProperty(
                getValue = { it.name },
                setValue = { config, value ->
                    config.cloneBase(name = value)
                },
                name = textFactory.of(Texts.WIDGET_GENERAL_PROPERTY_NAME),
            ),
            BooleanProperty(
                getValue = { it.lockMoving },
                setValue = { config, value ->
                    config.cloneBase(lockMoving = value)
                },
                name = textFactory.of(Texts.WIDGET_GENERAL_PROPERTY_LOCK_MOVING),
            ),
            AnchorProperty(),
            FloatProperty(
                getValue = { it.opacity },
                setValue = { config, value -> config.cloneBase(opacity = value) },
                messageFormatter = { opacity ->
                    textFactory.format(
                        Texts.WIDGET_GENERAL_PROPERTY_OPACITY,
                        kotlin.math.round(opacity * 100f).toInt().toString()
                    )
                }
            )
        )
    }

    @kotlin.jvm.Transient
    open val properties: PersistentList<Property<ControllerWidget, *>> = Companion.properties

    abstract fun size(): IntSize

    abstract fun layout(context: Context)

    abstract fun cloneBase(
        id: kotlin.uuid.Uuid = this.id,
        name: Name = this.name,
        align: Align = this.align,
        offset: IntOffset = this.offset,
        opacity: Float = this.opacity,
        lockMoving: Boolean = this.lockMoving,
    ): ControllerWidget

    open fun newId() = cloneBase(
        id = fastRandomUuid(),
    )
}