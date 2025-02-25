package top.fifthlight.touchcontroller.control

import androidx.compose.runtime.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.data.TextFactory
import top.fifthlight.combine.data.Texture
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.layout.Arrangement
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.placement.fillMaxWidth
import top.fifthlight.combine.modifier.placement.padding
import top.fifthlight.combine.modifier.placement.width
import top.fifthlight.combine.modifier.scroll.verticalScroll
import top.fifthlight.combine.widget.base.layout.Column
import top.fifthlight.combine.widget.base.layout.FlowRow
import top.fifthlight.combine.widget.base.layout.Row
import top.fifthlight.combine.widget.base.layout.Spacer
import top.fifthlight.combine.widget.ui.*
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntSize
import top.fifthlight.touchcontroller.annoations.DontTranslate
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.assets.Textures
import top.fifthlight.touchcontroller.layout.Align

@Immutable
class BooleanProperty<Config : ControllerWidget>(
    private val getValue: (Config) -> Boolean,
    private val setValue: (Config, Boolean) -> Config,
    private val message: Text
) : ControllerWidget.Property<Config, Boolean>, KoinComponent {
    @Composable
    override fun controller(modifier: Modifier, config: ControllerWidget, onConfigChanged: (ControllerWidget) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        val widgetConfig = config as Config
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(message)
            Spacer(modifier.weight(1f))
            Switch(
                value = getValue(widgetConfig),
                onValueChanged = {
                    onConfigChanged(setValue(widgetConfig, it))
                }
            )
        }
    }
}

@Immutable
class AnchorProperty<Config : ControllerWidget> : ControllerWidget.Property<Config, Align>, KoinComponent {
    @Composable
    override fun controller(modifier: Modifier, config: ControllerWidget, onConfigChanged: (ControllerWidget) -> Unit) {
        @Composable
        fun getItemText(align: Align): Text = when(align) {
            Align.LEFT_TOP -> Text.translatable(Texts.WIDGET_GENERAL_PROPERTY_ANCHOR_TOP_LEFT)
            Align.LEFT_CENTER -> Text.translatable(Texts.WIDGET_GENERAL_PROPERTY_ANCHOR_CENTER_LEFT)
            Align.LEFT_BOTTOM -> Text.translatable(Texts.WIDGET_GENERAL_PROPERTY_ANCHOR_BOTTOM_LEFT)
            Align.CENTER_TOP -> Text.translatable(Texts.WIDGET_GENERAL_PROPERTY_ANCHOR_TOP_CENTER)
            Align.CENTER_CENTER -> Text.translatable(Texts.WIDGET_GENERAL_PROPERTY_ANCHOR_CENTER_CENTER)
            Align.CENTER_BOTTOM -> Text.translatable(Texts.WIDGET_GENERAL_PROPERTY_ANCHOR_BOTTOM_CENTER)
            Align.RIGHT_TOP -> Text.translatable(Texts.WIDGET_GENERAL_PROPERTY_ANCHOR_TOP_RIGHT)
            Align.RIGHT_CENTER -> Text.translatable(Texts.WIDGET_GENERAL_PROPERTY_ANCHOR_CENTER_RIGHT)
            Align.RIGHT_BOTTOM -> Text.translatable(Texts.WIDGET_GENERAL_PROPERTY_ANCHOR_BOTTOM_RIGHT)
        }

        @Composable
        fun getItemIcon(align: Align): Texture = when(align) {
            Align.LEFT_TOP -> Textures.ICON_UP_LEFT
            Align.LEFT_CENTER -> Textures.ICON_LEFT
            Align.LEFT_BOTTOM -> Textures.ICON_DOWN_LEFT
            Align.CENTER_TOP -> Textures.ICON_UP
            Align.CENTER_CENTER -> Textures.ICON_MIDDLE
            Align.CENTER_BOTTOM -> Textures.ICON_DOWN
            Align.RIGHT_TOP -> Textures.ICON_UP_RIGHT
            Align.RIGHT_CENTER -> Textures.ICON_RIGHT
            Align.RIGHT_BOTTOM -> Textures.ICON_DOWN_RIGHT
        }

        @Suppress("UNCHECKED_CAST")
        val widgetConfig = config as Config
        Column(modifier) {
            Text(Text.translatable(Texts.WIDGET_GENERAL_PROPERTY_ANCHOR_NAME))

            var expanded by remember { mutableStateOf(false) }
            Select(
                modifier = Modifier.fillMaxWidth(),
                expanded = expanded,
                onExpandedChanged = { expanded = it },
                dropDownContent = {
                    val buttonWidth = contentWidth / 3
                    @Composable
                    fun AnchorButton(
                        anchor: Align
                    ) = IconButton(
                        minSize = IntSize(
                            width = buttonWidth,
                            height = 20,
                        ),
                        selected = config.align == anchor,
                        onClick = {
                            onConfigChanged(config.cloneBase(
                                align = anchor,
                                offset = IntOffset.ZERO,
                            ))
                        }
                    ) {
                        Icon(getItemIcon(anchor))
                    }

                    Column(
                        modifier = Modifier.width(contentWidth),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4),
                    ) {
                        FlowRow(maxColumns = 3) {
                            for (align in Align.entries){
                                AnchorButton(align)
                            }
                        }
                    }
                }
            ) {
                Text(getItemText(widgetConfig.align))
                Spacer(modifier = Modifier.weight(1f))
                SelectIcon(expanded = expanded)
            }
        }
    }
}

@Immutable
class EnumProperty<Config : ControllerWidget, T>(
    private val getValue: (Config) -> T,
    private val setValue: (Config, T) -> Config,
    private val name: Text,
    private val items: List<Pair<T, Text>>,
) : ControllerWidget.Property<Config, T>, KoinComponent {
    private val textFactory: TextFactory by inject()

    private fun getItemText(item: T): Text =
        items.firstOrNull { it.first == item }?.second ?: @DontTranslate textFactory.literal(item.toString())

    @Composable
    override fun controller(modifier: Modifier, config: ControllerWidget, onConfigChanged: (ControllerWidget) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        val widgetConfig = config as Config
        Column(modifier) {
            Text(name)

            var expanded by remember { mutableStateOf(false) }
            Select(
                modifier = Modifier.fillMaxWidth(),
                expanded = expanded,
                onExpandedChanged = { expanded = it },
                dropDownContent = {
                    val value = getValue(widgetConfig)
                    val selectedIndex = items.indexOfFirst { it.first == value }
                    SelectItemList(
                        modifier = Modifier.verticalScroll(),
                        items = items,
                        textProvider = Pair<T, Text>::second,
                        selectedIndex = selectedIndex,
                        onItemSelected = {
                            val item = items[it].first
                            onConfigChanged(setValue(widgetConfig, item))
                            expanded = false
                        }
                    )
                }
            ) {
                Text(getItemText(getValue(widgetConfig)))
                Spacer(modifier = Modifier.weight(1f))
                SelectIcon(expanded = expanded)
            }
        }
    }
}

@Immutable
class FloatProperty<Config : ControllerWidget>(
    private val getValue: (Config) -> Float,
    private val setValue: (Config, Float) -> Config,
    private val range: ClosedFloatingPointRange<Float> = 0f..1f,
    private val messageFormatter: (Float) -> Text,
) : ControllerWidget.Property<Config, Float> {

    @Composable
    override fun controller(modifier: Modifier, config: ControllerWidget, onConfigChanged: (ControllerWidget) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        val widgetConfig = config as Config
        Column(modifier) {
            val value = getValue(widgetConfig)
            Text(messageFormatter(value))
            Slider(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                range = range,
                onValueChanged = {
                    onConfigChanged(setValue(widgetConfig, it))
                }
            )
        }
    }
}

@Immutable
class IntProperty<Config : ControllerWidget>(
    private val getValue: (Config) -> Int,
    private val setValue: (Config, Int) -> Config,
    private val range: IntRange,
    private val messageFormatter: (Int) -> Text,
) : ControllerWidget.Property<Config, Int> {

    @Composable
    override fun controller(modifier: Modifier, config: ControllerWidget, onConfigChanged: (ControllerWidget) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        val widgetConfig = config as Config
        Column(modifier) {
            val value = getValue(widgetConfig)
            Text(messageFormatter(value))
            IntSlider(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                range = range,
                onValueChanged = {
                    onConfigChanged(setValue(widgetConfig, it))
                }
            )
        }
    }
}