package top.fifthlight.touchcontroller.common.control

import androidx.compose.runtime.*
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.koin.compose.koinInject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import top.fifthlight.combine.data.LocalTextFactory
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.data.TextFactory
import top.fifthlight.combine.data.Texture
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.layout.Arrangement
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.drawing.background
import top.fifthlight.combine.modifier.drawing.innerLine
import top.fifthlight.combine.modifier.placement.*
import top.fifthlight.combine.modifier.pointer.clickable
import top.fifthlight.combine.modifier.scroll.verticalScroll
import top.fifthlight.combine.paint.Color
import top.fifthlight.combine.paint.Colors
import top.fifthlight.combine.widget.base.layout.Column
import top.fifthlight.combine.widget.base.layout.FlowRow
import top.fifthlight.combine.widget.base.layout.Row
import top.fifthlight.combine.widget.base.layout.Spacer
import top.fifthlight.combine.widget.ui.*
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntPadding
import top.fifthlight.data.IntSize
import top.fifthlight.touchcontroller.assets.*
import top.fifthlight.touchcontroller.common.annoations.DontTranslate
import top.fifthlight.touchcontroller.common.control.ButtonTrigger.DoubleClickTrigger
import top.fifthlight.touchcontroller.common.gal.KeyBindingHandler
import top.fifthlight.touchcontroller.common.layout.Align
import top.fifthlight.touchcontroller.common.ui.component.AppBar
import top.fifthlight.touchcontroller.common.ui.component.BackButton
import top.fifthlight.touchcontroller.common.ui.component.ListButton
import top.fifthlight.touchcontroller.common.ui.component.Scaffold
import top.fifthlight.touchcontroller.common.ui.component.TabButton

@Immutable
class NameProperty<Config : ControllerWidget>(
    private val getValue: (Config) -> ControllerWidget.Name,
    private val setValue: (Config, ControllerWidget.Name) -> Config,
    private val name: Text
) : ControllerWidget.Property<Config, ControllerWidget.Name>, KoinComponent {
    @Composable
    override fun controller(modifier: Modifier, config: ControllerWidget, onConfigChanged: (ControllerWidget) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        val widgetConfig = config as Config
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            Text(name)
            EditText(
                modifier = Modifier.fillMaxWidth(),
                value = getValue(widgetConfig).asString(),
                onValueChanged = {
                    onConfigChanged(setValue(config, ControllerWidget.Name.Literal(it)))
                }
            )
        }
    }
}

@Immutable
class BooleanProperty<Config : ControllerWidget>(
    private val getValue: (Config) -> Boolean,
    private val setValue: (Config, Boolean) -> Config,
    private val name: Text
) : ControllerWidget.Property<Config, Boolean>, KoinComponent {
    @Composable
    override fun controller(modifier: Modifier, config: ControllerWidget, onConfigChanged: (ControllerWidget) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        val widgetConfig = config as Config
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(name)
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
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
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
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            Text(name)

            var expanded by remember { mutableStateOf(false) }
            Select(
                modifier = Modifier.fillMaxWidth(),
                expanded = expanded,
                onExpandedChanged = { expanded = it },
                dropDownContent = {
                    val value = getValue(widgetConfig)
                    val selectedIndex = items.indexOfFirst { it.first == value }
                    DropdownItemList(
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

@Immutable
class StringProperty<Config : ControllerWidget>(
    private val getValue: (Config) -> String,
    private val setValue: (Config, String) -> Config,
    private val name: Text,
) : ControllerWidget.Property<Config, IntPadding> {
    @Composable
    override fun controller(modifier: Modifier, config: ControllerWidget, onConfigChanged: (ControllerWidget) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        val widgetConfig = config as Config
        val value = getValue(widgetConfig)
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            Text(name)
            EditText(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChanged = { onConfigChanged(setValue(config, it)) }
            )
        }
    }
}

@Immutable
class ColorProperty<Config : ControllerWidget>(
    private val getValue: (Config) -> Color,
    private val setValue: (Config, Color) -> Config,
    private val name: Text,
) : ControllerWidget.Property<Config, IntPadding> {
    @Composable
    override fun controller(modifier: Modifier, config: ControllerWidget, onConfigChanged: (ControllerWidget) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        val widgetConfig = config as Config
        val value = getValue(widgetConfig)
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4),
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = name,
            )
            ColorPicker(
                value = value,
                onValueChanged = { onConfigChanged(setValue(config, it)) }
            )
        }
    }
}

@Immutable
class IntPaddingProperty<Config : ControllerWidget>(
    private val getValue: (Config) -> IntPadding,
    private val setValue: (Config, IntPadding) -> Config,
    private val name: Text,
) : ControllerWidget.Property<Config, IntPadding> {
    @Composable
    override fun controller(modifier: Modifier, config: ControllerWidget, onConfigChanged: (ControllerWidget) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        val widgetConfig = config as Config
        val value = getValue(widgetConfig)
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            @Composable
            fun PaddingItem(
                text: Text,
                getSize: (IntPadding) -> Int,
                setSize: (IntPadding, Int) -> IntPadding,
            ) {
                val sizeValue = getSize(value)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text)
                    IntSlider(
                        modifier = Modifier.weight(1f),
                        range = 0..32,
                        value = sizeValue,
                        onValueChanged = {
                            onConfigChanged(setValue(config, setSize(value, it)))
                        }
                    )
                    var text by remember(sizeValue) { mutableStateOf(sizeValue.toString()) }
                    LaunchedEffect(text) {
                        val newSize = text.toIntOrNull() ?: return@LaunchedEffect
                        if (newSize == sizeValue) {
                            return@LaunchedEffect
                        }
                        onConfigChanged(setValue(config, setSize(value, newSize)))
                    }
                    EditText(
                        modifier = Modifier.width(48),
                        value = text,
                        onValueChanged = { text = it },
                    )
                }
            }

            Text(name)

            PaddingItem(
                text = Text.translatable(Texts.WIDGET_TEXTURE_EXTRA_PADDING_LEFT),
                getSize = IntPadding::left,
                setSize = { padding, size -> padding.copy(left = size) },
            )
            PaddingItem(
                text = Text.translatable(Texts.WIDGET_TEXTURE_EXTRA_PADDING_TOP),
                getSize = IntPadding::top,
                setSize = { padding, size -> padding.copy(top = size) },
            )
            PaddingItem(
                text = Text.translatable(Texts.WIDGET_TEXTURE_EXTRA_PADDING_RIGHT),
                getSize = IntPadding::right,
                setSize = { padding, size -> padding.copy(right = size) },
            )
            PaddingItem(
                text = Text.translatable(Texts.WIDGET_TEXTURE_EXTRA_PADDING_BOTTOM),
                getSize = IntPadding::bottom,
                setSize = { padding, size -> padding.copy(bottom = size) },
            )
        }
    }
}

@Immutable
class TextureCoordinateProperty<Config : ControllerWidget>(
    private val getValue: (Config) -> TextureCoordinate,
    private val setValue: (Config, TextureCoordinate) -> Config,
    private val name: Text,
) : ControllerWidget.Property<Config, TextureCoordinate> {
    @Composable
    override fun controller(modifier: Modifier, config: ControllerWidget, onConfigChanged: (ControllerWidget) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        val widgetConfig = config as Config
        val value = getValue(widgetConfig)
        Row(
            modifier = Modifier
                .height(24)
                .then(modifier),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4),
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = name,
            )
            Icon(
                modifier = Modifier.fillMaxHeight(),
                drawable = value.texture,
            )
            Spacer(modifier = Modifier.width(4))
            var showDialog by remember { mutableStateOf(false) }
            Button(
                modifier = Modifier.fillMaxHeight(),
                onClick = {
                    showDialog = true
                }
            ) {
                Text(Text.translatable(Texts.TEXTURE_COORDINATE_EDIT))
            }

            if (showDialog) {
                FullScreenDialog(
                    onDismissRequest = { showDialog = false }
                ) {
                    Scaffold(
                        topBar = {
                            AppBar(
                                modifier = Modifier.fillMaxWidth(),
                                leading = {
                                    BackButton(
                                        screenName = Text.translatable(Texts.SCREEN_TEXTURE_COORDINATE_SELECT),
                                        onClick = {
                                            showDialog = false
                                        },
                                    )
                                },
                            )
                        },
                    ) { modifier ->
                        Column(
                            modifier = Modifier
                                .padding(4)
                                .background(BackgroundTextures.BRICK_BACKGROUND)
                                .then(modifier),
                            verticalArrangement = Arrangement.spacedBy(4),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4),
                            ) {
                                Text("Texture set")

                                val textFactory = LocalTextFactory.current
                                var expanded by remember { mutableStateOf(false) }
                                Select(
                                    expanded = expanded,
                                    onExpandedChanged = { expanded = it },
                                    dropDownContent = {
                                        DropdownItemList(
                                            modifier = Modifier.verticalScroll(),
                                            items = TextureSet.TextureSetKey.entries,
                                            textProvider = { textFactory.of(it.nameText) },
                                            selectedIndex = TextureSet.TextureSetKey.entries.indexOf(value.textureSet),
                                            onItemSelected = {
                                                val item = TextureSet.TextureSetKey.entries[it]
                                                onConfigChanged(setValue(config, value.copy(textureSet = item)))
                                                expanded = false
                                            }
                                        )
                                    }
                                ) {
                                    Text(Text.translatable(value.textureSet.nameText))
                                    Spacer(modifier = Modifier.width(8))
                                    SelectIcon(expanded = expanded)
                                }
                            }

                            FlowRow(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .verticalScroll()
                            ) {
                                for (key in TextureSet.TextureKey.all) {
                                    val borderModifier = if (key == value.textureItem) {
                                        Modifier.innerLine(Colors.WHITE)
                                    } else {
                                        Modifier
                                    }
                                    Column(
                                        modifier = Modifier
                                            .then(borderModifier)
                                            .width(72)
                                            .height(86)
                                            .clickable {
                                                onConfigChanged(setValue(config, value.copy(textureItem = key)))
                                            },
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4),
                                    ) {
                                        val texture = remember(
                                            value.textureSet.textureSet,
                                            key
                                        ) { key.get(value.textureSet.textureSet) }
                                        Icon(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth(),
                                            drawable = texture,
                                        )
                                        Text(Text.literal(key.name))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Immutable
class ButtonTextureProperty<Config : ControllerWidget>(
    private val getValue: (Config) -> ButtonTexture,
    private val setValue: (Config, ButtonTexture) -> Config,
    private val name: Text,
) : ControllerWidget.Property<Config, ButtonTexture>, KoinComponent {
    private val textFactory: TextFactory by inject()

    private fun <Texture : ButtonTexture> paddingProperty(
        getPadding: (ButtonTexture) -> IntPadding?,
        setPadding: (ButtonTexture, IntPadding) -> Texture,
        name: Text,
    ) = IntPaddingProperty<Config>(
        getValue = { getPadding(getValue(it)) ?: IntPadding.ZERO },
        setValue = { config, value -> setValue(config, setPadding(getValue(config), value)) },
        name = name,
    )

    private fun <Texture : ButtonTexture> intProperty(
        getInt: (ButtonTexture) -> Int?,
        setInt: (ButtonTexture, Int) -> Texture,
        range: IntRange,
        name: Text,
    ) = IntProperty<Config>(
        getValue = { getInt(getValue(it)) ?: 0 },
        setValue = { config, value -> setValue(config, setInt(getValue(config), value)) },
        range = range,
        messageFormatter = { textFactory.format(Texts.SCREEN_CONFIG_VALUE, textFactory.toNative(name), it.toString()) },
    )

    private fun <Texture : ButtonTexture> colorProperty(
        getColor: (ButtonTexture) -> Color?,
        setColor: (ButtonTexture, Color) -> Texture,
        name: Text,
    ) = ColorProperty<Config>(
        getValue = { getColor(getValue(it)) ?: Colors.BLACK },
        setValue = { config, value -> setValue(config, setColor(getValue(config), value)) },
        name = name,
    )

    private fun <Texture : ButtonTexture> textureCoordinateProperty(
        getCoordinate: (ButtonTexture) -> TextureCoordinate?,
        setCoordinate: (ButtonTexture, TextureCoordinate) -> Texture,
        name: Text,
    ) = TextureCoordinateProperty<Config>(
        getValue = { getCoordinate(getValue(it)) ?: TextureCoordinate() },
        setValue = { config, value -> setValue(config, setCoordinate(getValue(config), value)) },
        name = name,
    )

    private fun <Texture : ButtonTexture> scaleProperty(
        getScale: (ButtonTexture) -> Float?,
        setScale: (ButtonTexture, Float) -> Texture,
        range: ClosedFloatingPointRange<Float> = 0f..1f,
        name: Text,
    ) = FloatProperty<Config>(
        getValue = { getScale(getValue(it)) ?: 0f },
        setValue = { config, value -> setValue(config, setScale(getValue(config), value)) },
        range = range,
        messageFormatter = {
            textFactory.format(
                Texts.SCREEN_CONFIG_PERCENT,
                textFactory.toNative(name),
                (it * 100).toInt().toString()
            )
        },
    )

    private fun <Texture : ButtonTexture, T> enumProperty(
        getEnum: (ButtonTexture) -> T?,
        setEnum: (ButtonTexture, T) -> Texture,
        defaultValue: T,
        name: Text,
        items: List<Pair<T, Text>>,
    ) = EnumProperty<Config, T>(
        getValue = { getEnum(getValue(it)) ?: defaultValue },
        setValue = { config, value -> setValue(config, setEnum(getValue(config), value)) },
        name = name,
        items = items,
    )

    private val emptyTexturePaddingProperty = paddingProperty(
        getPadding = { (it as? ButtonTexture.Empty)?.extraPadding },
        setPadding = { texture, value ->
            when (texture) {
                is ButtonTexture.Empty -> texture.copy(extraPadding = value)
                else -> ButtonTexture.Empty(extraPadding = value)
            }
        },
        name = textFactory.of(Texts.WIDGET_TEXTURE_EXTRA_PADDING),
    )

    private val fillTextureBorderWidthProperty = intProperty(
        getInt = { (it as? ButtonTexture.Fill)?.borderWidth },
        setInt = { texture, value ->
            when (texture) {
                is ButtonTexture.Fill -> texture.copy(borderWidth = value)
                else -> ButtonTexture.Fill(borderWidth = value)
            }
        },
        range = 0..16,
        name = textFactory.of(Texts.WIDGET_TEXTURE_FILL_BORDER_WIDTH),
    )

    private val fillTexturePaddingProperty = paddingProperty(
        getPadding = { (it as? ButtonTexture.Fill)?.extraPadding },
        setPadding = { texture, value ->
            when (texture) {
                is ButtonTexture.Fill -> texture.copy(extraPadding = value)
                else -> ButtonTexture.Fill(extraPadding = value)
            }
        },
        name = textFactory.of(Texts.WIDGET_TEXTURE_EXTRA_PADDING),
    )

    private val fillTextureBorderColorProperty = colorProperty(
        getColor = { (it as? ButtonTexture.Fill)?.borderColor },
        setColor = { texture, value ->
            when (texture) {
                is ButtonTexture.Fill -> texture.copy(borderColor = value)
                else -> ButtonTexture.Fill(borderColor = value)
            }
        },
        name = textFactory.of(Texts.WIDGET_TEXTURE_FILL_BORDER_COLOR),
    )

    private val fillTextureBackgroundColorProperty = colorProperty(
        getColor = { (it as? ButtonTexture.Fill)?.backgroundColor },
        setColor = { texture, value ->
            when (texture) {
                is ButtonTexture.Fill -> texture.copy(backgroundColor = value)
                else -> ButtonTexture.Fill(backgroundColor = value)
            }
        },
        name = textFactory.of(Texts.WIDGET_TEXTURE_FILL_BACKGROUND_COLOR),
    )

    private val fixedTextureCoordinateProperty = textureCoordinateProperty(
        getCoordinate = { (it as? ButtonTexture.Fixed)?.texture },
        setCoordinate = { texture, value ->
            when (texture) {
                is ButtonTexture.Fixed -> texture.copy(texture = value)
                else -> ButtonTexture.Fixed(texture = value)
            }
        },
        name = textFactory.of(Texts.WIDGET_TEXTURE_FIXED_TEXTURE),
    )

    private val fixedTextureScaleProperty = scaleProperty(
        getScale = { (it as? ButtonTexture.Fixed)?.scale },
        setScale = { texture, value ->
            when (texture) {
                is ButtonTexture.Fixed -> texture.copy(scale = value)
                else -> ButtonTexture.Fixed(scale = value)
            }
        },
        range = .5f..4f,
        name = textFactory.of(Texts.WIDGET_TEXTURE_FIXED_SCALE),
    )

    private val ninePatchTextureTextureProperty = enumProperty(
        getEnum = { (it as? ButtonTexture.NinePatch)?.texture },
        setEnum = { texture, value ->
            when (texture) {
                is ButtonTexture.NinePatch -> texture.copy(texture = value)
                else -> ButtonTexture.NinePatch(texture = value)
            }
        },
        defaultValue = EmptyTexture.EMPTY_1,
        name = textFactory.of(Texts.WIDGET_TEXTURE_NINE_PATCH_TEXTURE),
        items = EmptyTexture.entries.map { Pair(it, textFactory.of(it.nameId)) },
    )

    private val ninePatchTexturePaddingProperty = paddingProperty(
        getPadding = { (it as? ButtonTexture.NinePatch)?.extraPadding },
        setPadding = { texture, value ->
            when (texture) {
                is ButtonTexture.NinePatch -> texture.copy(extraPadding = value)
                else -> ButtonTexture.NinePatch(extraPadding = value)
            }
        },
        name = textFactory.of(Texts.WIDGET_TEXTURE_EXTRA_PADDING),
    )

    @Composable
    override fun controller(modifier: Modifier, config: ControllerWidget, onConfigChanged: (ControllerWidget) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        val widgetConfig = config as Config
        val value = getValue(widgetConfig)
        val textFactory = LocalTextFactory.current
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            Text(name)
            var expanded by remember { mutableStateOf(false) }
            Select(
                modifier = Modifier.fillMaxWidth(),
                expanded = expanded,
                onExpandedChanged = { expanded = it },
                dropDownContent = {
                    DropdownItemList(
                        modifier = Modifier.verticalScroll(),
                        items = ButtonTexture.Type.entries,
                        textProvider = { textFactory.of(it.nameId) },
                        selectedIndex = ButtonTexture.Type.entries.indexOf(value.type),
                        onItemSelected = {
                            val item = ButtonTexture.Type.entries[it]
                            if (value.type != item) {
                                onConfigChanged(
                                    setValue(
                                        widgetConfig, when (item) {
                                            ButtonTexture.Type.EMPTY -> ButtonTexture.Empty()
                                            ButtonTexture.Type.FILL -> ButtonTexture.Fill()
                                            ButtonTexture.Type.FIXED -> ButtonTexture.Fixed()
                                            ButtonTexture.Type.NINE_PATCH -> ButtonTexture.NinePatch()
                                        }
                                    )
                                )
                            }
                            expanded = false
                        }
                    )
                }
            ) {
                Text(Text.translatable(value.type.nameId))
                Spacer(modifier = Modifier.weight(1f))
                SelectIcon(expanded = expanded)
            }

            @Composable
            fun <Config : ControllerWidget> ControllerWidget.Property<Config, *>.controller() = controller(
                modifier = Modifier.fillMaxWidth(),
                config = config,
                onConfigChanged = onConfigChanged,
            )

            when (value) {
                is ButtonTexture.Empty -> {
                    emptyTexturePaddingProperty.controller()
                }

                is ButtonTexture.Fill -> {
                    fillTexturePaddingProperty.controller()
                    fillTextureBorderWidthProperty.controller()
                    fillTextureBorderColorProperty.controller()
                    fillTextureBackgroundColorProperty.controller()
                }

                is ButtonTexture.Fixed -> {
                    fixedTextureCoordinateProperty.controller()
                    fixedTextureScaleProperty.controller()
                }

                is ButtonTexture.NinePatch -> {
                    ninePatchTextureTextureProperty.controller()
                    ninePatchTexturePaddingProperty.controller()
                }
            }
        }
    }
}

@Immutable
class ButtonActiveTextureProperty<Config : ControllerWidget>(
    private val getValue: (Config) -> ButtonActiveTexture,
    private val setValue: (Config, ButtonActiveTexture) -> Config,
    private val name: Text,
) : ControllerWidget.Property<Config, ButtonActiveTexture>, KoinComponent {
    private val textFactory: TextFactory by inject()

    private fun <Texture : ButtonActiveTexture> buttonTextureProperty(
        getTexture: (ButtonActiveTexture) -> ButtonTexture?,
        setTexture: (ButtonActiveTexture, ButtonTexture) -> Texture,
        name: Text,
    ) = ButtonTextureProperty<Config>(
        getValue = { getTexture(getValue(it)) ?: ButtonTexture.Empty() },
        setValue = { config, value -> setValue(config, setTexture(getValue(config), value)) },
        name = name,
    )

    private val textureProperty = buttonTextureProperty(
        getTexture = { (it as? ButtonActiveTexture.Texture)?.texture },
        setTexture = { texture, value ->
            when (texture) {
                is ButtonActiveTexture.Texture -> texture.copy(texture = value)
                else -> ButtonActiveTexture.Texture(texture = value)
            }
        },
        name = textFactory.of(Texts.WIDGET_ACTIVE_TEXTURE_TYPE),
    )

    @Composable
    override fun controller(modifier: Modifier, config: ControllerWidget, onConfigChanged: (ControllerWidget) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        val widgetConfig = config as Config
        val value = getValue(widgetConfig)
        val textFactory = LocalTextFactory.current
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            Text(name)
            var expanded by remember { mutableStateOf(false) }
            Select(
                modifier = Modifier.fillMaxWidth(),
                expanded = expanded,
                onExpandedChanged = { expanded = it },
                dropDownContent = {
                    DropdownItemList(
                        modifier = Modifier.verticalScroll(),
                        items = ButtonActiveTexture.Type.entries,
                        textProvider = { textFactory.of(it.nameId) },
                        selectedIndex = ButtonActiveTexture.Type.entries.indexOf(value.type),
                        onItemSelected = {
                            val item = ButtonActiveTexture.Type.entries[it]
                            if (value.type != item) {
                                onConfigChanged(
                                    setValue(
                                        widgetConfig, when (item) {
                                            ButtonActiveTexture.Type.SAME -> ButtonActiveTexture.Same
                                            ButtonActiveTexture.Type.GRAY -> ButtonActiveTexture.Gray
                                            ButtonActiveTexture.Type.TEXTURE -> ButtonActiveTexture.Texture()
                                        }
                                    )
                                )
                            }
                            expanded = false
                        }
                    )
                }
            ) {
                Text(Text.translatable(value.type.nameId))
                Spacer(modifier = Modifier.weight(1f))
                SelectIcon(expanded = expanded)
            }

            @Composable
            fun <Config : ControllerWidget> ControllerWidget.Property<Config, *>.controller() = controller(
                modifier = Modifier.fillMaxWidth(),
                config = config,
                onConfigChanged = onConfigChanged,
            )

            if (value is ButtonActiveTexture.Texture) {
                textureProperty.controller()
            }
        }
    }
}

@Immutable
class KeyBindingProperty<Config : ControllerWidget>(
    private val getValue: (Config) -> String?,
    private val setValue: (Config, String?) -> Config,
    private val name: Text,
) : ControllerWidget.Property<Config, String?>, KoinComponent {
    @Composable
    override fun controller(modifier: Modifier, config: ControllerWidget, onConfigChanged: (ControllerWidget) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        val widgetConfig = config as Config
        val value = getValue(widgetConfig)

        val keyBindingHandler: KeyBindingHandler = koinInject()
        val keyBindings = remember(keyBindingHandler) { keyBindingHandler.getAllStates() }
        val (keyBindingsWithCategories, keyCategories) = remember(keyBindings) {
            val keyBindingsWithCategories = keyBindings.values
                .groupBy { it.categoryId }
                .mapValues { (_, bindings) -> bindings.sortedBy { it.id } }
            val keyCategories = keyBindingsWithCategories.keys.toList().sorted()
            Pair(keyBindingsWithCategories, keyCategories)
        }

        val keyBinding = remember(value, keyBindings) { value?.let { keyBindings[it] } }

        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = name,
            )

            if (value == null) {
                Text(Text.translatable(Texts.WIDGET_KEY_BINDING_EMPTY))
            } else {
                Text(text = keyBinding?.name ?: Text.translatable(Texts.WIDGET_KEY_BINDING_UNKNOWN))
            }

            var showDialog by remember { mutableStateOf(false) }
            IconButton(onClick = {
                showDialog = true
            }) {
                Icon(Textures.ICON_EDIT)
            }

            IconButton(onClick = {
                onConfigChanged(setValue(config, null))
            }) {
                Icon(Textures.ICON_DELETE)
            }

            if (showDialog) {
                AlertDialog(
                    modifier = Modifier
                        .fillMaxWidth(.6f)
                        .fillMaxHeight(.8f),
                    onDismissRequest = {
                        showDialog = false
                    },
                    title = {
                        Text(Text.translatable(Texts.WIDGET_KEY_BINDING_SELECT_TITLE))
                    },
                    action = {
                        Button(onClick = {
                            showDialog = false
                        }) {
                            Text(Text.translatable(Texts.WIDGET_KEY_BINDING_SELECT_FINISH))
                        }
                    },
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4),
                    ) {
                        var selectedCategory by remember {
                            mutableStateOf(
                                keyBinding?.categoryId ?: keyCategories.first()
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(3f)
                                .verticalScroll(),
                        ) {
                            for (category in keyCategories) {
                                val firstKey = keyBindingsWithCategories[category]?.firstOrNull() ?: continue
                                TabButton(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { selectedCategory = category }
                                ) {
                                    Text(firstKey.categoryName)
                                }
                            }
                        }
                        Column(
                            modifier = Modifier
                                .weight(7f)
                                .verticalScroll(),
                        ) {
                            val categoryKeys = keyBindingsWithCategories[selectedCategory] ?: return@Column
                            for (key in categoryKeys) {
                                ListButton(
                                    modifier = Modifier.fillMaxWidth(),
                                    checked = value == key.id,
                                    onClick = {
                                        onConfigChanged(setValue(config, key.id))
                                    },
                                ) {
                                    Text(key.name)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Immutable
class TriggerActionProperty<Config : ControllerWidget>(
    private val getValue: (Config) -> WidgetTriggerAction?,
    private val setValue: (Config, WidgetTriggerAction?) -> Config,
    private val name: Text,
) : ControllerWidget.Property<Config, WidgetTriggerAction?>, KoinComponent {
    private val textFactory: TextFactory by inject()

    private fun keyBindingProperty(
        getKeyBinding: (WidgetTriggerAction?) -> String?,
        setKeyBinding: (WidgetTriggerAction?, String?) -> WidgetTriggerAction?,
        name: Text,
    ) = KeyBindingProperty<Config>(
        getValue = { getKeyBinding(getValue(it)) },
        setValue = { config, value -> setValue(config, setKeyBinding(getValue(config), value)) },
        name = name,
    )

    private val keyClickBindingProperty = keyBindingProperty(
        getKeyBinding = { (it as? WidgetTriggerAction.Key.Click)?.keyBinding },
        setKeyBinding = { config, value ->
            when (config) {
                is WidgetTriggerAction.Key.Click -> config.copy(keyBinding = value)
                else -> config
            }
        },
        name = textFactory.of(Texts.WIDGET_TRIGGER_KEY_BINDING),
    )

    private val keyLockBindingProperty = keyBindingProperty(
        getKeyBinding = { (it as? WidgetTriggerAction.Key.Lock)?.keyBinding },
        setKeyBinding = { config, value ->
            when (config) {
                is WidgetTriggerAction.Key.Lock -> config.copy(keyBinding = value)
                else -> config
            }
        },
        name = textFactory.of(Texts.WIDGET_TRIGGER_KEY_BINDING),
    )

    @Composable
    override fun controller(
        modifier: Modifier,
        config: ControllerWidget,
        onConfigChanged: (ControllerWidget) -> Unit
    ) {
        @Suppress("UNCHECKED_CAST")
        val widgetConfig = config as Config
        val value = getValue(widgetConfig)

        @Composable
        fun <Config : ControllerWidget> ControllerWidget.Property<Config, *>.controller() = controller(
            modifier = Modifier.fillMaxWidth(),
            config = config,
            onConfigChanged = onConfigChanged,
        )

        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(name)

                var expanded by remember { mutableStateOf(false) }
                Select(
                    expanded = expanded,
                    onExpandedChanged = { expanded = it },
                    dropDownContent = {
                        DropdownItemList(
                            modifier = Modifier.verticalScroll(),
                            items = persistentListOf(
                                Pair(Text.translatable(WidgetTriggerAction.Type.NONE.nameId)) {
                                    onConfigChanged(setValue(config, null))
                                },
                                Pair(Text.translatable(WidgetTriggerAction.Type.KEY.nameId)) {
                                    if (value !is WidgetTriggerAction.Key) {
                                        onConfigChanged(setValue(config, WidgetTriggerAction.Key.Click()))
                                    }
                                },
                                Pair(Text.translatable(WidgetTriggerAction.Type.GAME.nameId)) {
                                    if (value !is WidgetTriggerAction.Game) {
                                        onConfigChanged(setValue(config, WidgetTriggerAction.Game.GameMenu))
                                    }
                                },
                                Pair(Text.translatable(WidgetTriggerAction.Type.PLAYER.nameId)) {
                                    if (value !is WidgetTriggerAction.Player) {
                                        onConfigChanged(setValue(config, WidgetTriggerAction.Player.StartSprint))
                                    }
                                },
                            ),
                        )
                    }
                ) {
                    val actionType = value?.actionType ?: WidgetTriggerAction.Type.NONE
                    Text(Text.translatable(actionType.nameId))
                    SelectIcon(expanded = expanded)
                }
            }
            when (value) {
                is WidgetTriggerAction.Key -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = Text.translatable(Texts.WIDGET_TRIGGER_KEY_TYPE)
                        )
                        RadioRow {
                            RadioBoxItem(
                                value = value is WidgetTriggerAction.Key.Click,
                                onValueChanged = { checked ->
                                    if (checked && value !is WidgetTriggerAction.Key.Click) {
                                        onConfigChanged(setValue(config, WidgetTriggerAction.Key.Click()))
                                    }
                                },
                            ) {
                                Text(Text.translatable(Texts.WIDGET_TRIGGER_KEY_CLICK))
                            }
                            RadioBoxItem(
                                value = value is WidgetTriggerAction.Key.Lock,
                                onValueChanged = { checked ->
                                    if (checked && value !is WidgetTriggerAction.Key.Lock) {
                                        onConfigChanged(setValue(config, WidgetTriggerAction.Key.Lock()))
                                    }
                                },
                            ) {
                                Text(Text.translatable(Texts.WIDGET_TRIGGER_KEY_LOCK))
                            }
                        }
                    }
                    when (value) {
                        is WidgetTriggerAction.Key.Click -> {
                            keyClickBindingProperty.controller()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = Text.translatable(Texts.WIDGET_TRIGGER_KEY_KEEP_FOR_CLIENT_TICK),
                                )
                                Switch(
                                    value = value.keepInClientTick,
                                    onValueChanged = {
                                        onConfigChanged(setValue(config, value.copy(keepInClientTick = it)))
                                    }
                                )
                            }
                        }

                        is WidgetTriggerAction.Key.Lock -> {
                            keyLockBindingProperty.controller()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = Text.translatable(Texts.WIDGET_TRIGGER_KEY_LOCK_TYPE),
                                )
                                var expanded by remember { mutableStateOf(false) }
                                Select(
                                    expanded = expanded,
                                    onExpandedChanged = { expanded = it },
                                    dropDownContent = {
                                        DropdownItemList(
                                            modifier = Modifier.verticalScroll(),
                                            items = WidgetTriggerAction.Key.Lock.LockActionType.entries.map {
                                                Pair(Text.translatable(it.nameId)) {
                                                    onConfigChanged(setValue(config, value.copy(lockType = it)))
                                                }
                                            }.toPersistentList()
                                        )
                                    }
                                ) {
                                    Text(Text.translatable(value.lockType.nameId))
                                    SelectIcon(expanded = expanded)
                                }
                            }
                        }
                    }
                }

                is WidgetTriggerAction.Game -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = Text.translatable(Texts.WIDGET_TRIGGER_GAME_ACTION_TYPE),
                        )
                        var expanded by remember { mutableStateOf(false) }
                        Select(
                            expanded = expanded,
                            onExpandedChanged = { expanded = it },
                            dropDownContent = {
                                DropdownItemList(
                                    modifier = Modifier.verticalScroll(),
                                    items = WidgetTriggerAction.Game.all.map {
                                        Pair(Text.translatable(it.nameId)) {
                                            onConfigChanged(setValue(config, it))
                                        }
                                    }.toPersistentList()
                                )
                            }
                        ) {
                            Text(Text.translatable(value.nameId))
                            SelectIcon(expanded = expanded)
                        }
                    }
                }

                is WidgetTriggerAction.Player -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = Text.translatable(Texts.WIDGET_TRIGGER_PLAYER_ACTION_TYPE),
                        )
                        var expanded by remember { mutableStateOf(false) }
                        Select(
                            expanded = expanded,
                            onExpandedChanged = { expanded = it },
                            dropDownContent = {
                                DropdownItemList(
                                    modifier = Modifier.verticalScroll(),
                                    items = WidgetTriggerAction.Player.all.map {
                                        Pair(Text.translatable(it.nameId)) {
                                            onConfigChanged(setValue(config, it))
                                        }
                                    }.toPersistentList()
                                )
                            }
                        ) {
                            Text(Text.translatable(value.nameId))
                            SelectIcon(expanded = expanded)
                        }
                    }
                }

                null -> {}
            }
        }
    }
}

@Immutable
class DoubleClickTriggerProperty<Config : ControllerWidget>(
    private val getValue: (Config) -> DoubleClickTrigger,
    private val setValue: (Config, DoubleClickTrigger) -> Config,
    private val name: Text,
) : ControllerWidget.Property<Config, DoubleClickTrigger>, KoinComponent {
    private val textFactory: TextFactory by inject()

    private fun triggerActionProperty(
        getAction: (DoubleClickTrigger) -> WidgetTriggerAction?,
        setAction: (DoubleClickTrigger, WidgetTriggerAction?) -> DoubleClickTrigger,
        name: Text,
    ) = TriggerActionProperty<Config>(
        getValue = { getAction(getValue(it)) },
        setValue = { config, value -> setValue(config, setAction(getValue(config), value)) },
        name = name,
    )

    private val actionProperty = triggerActionProperty(
        getAction = { it.action },
        setAction = { config, value -> config.copy(action = value) },
        name = textFactory.of(Texts.WIDGET_DOUBLE_TRIGGER_ACTION)
    )

    @Composable
    override fun controller(modifier: Modifier, config: ControllerWidget, onConfigChanged: (ControllerWidget) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        val widgetConfig = config as Config
        val value = getValue(widgetConfig)

        @Composable
        fun <Config : ControllerWidget> ControllerWidget.Property<Config, *>.controller() = controller(
            modifier = Modifier.fillMaxWidth(),
            config = config,
            onConfigChanged = onConfigChanged,
        )

        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            Text(name)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4),
            ) {
                Text(Text.translatable(Texts.WIDGET_DOUBLE_TRIGGER_INTERVAL))
                IntSlider(
                    modifier = Modifier.weight(1f),
                    range = 1..40,
                    value = value.interval,
                    onValueChanged = {
                        onConfigChanged(setValue(config, value.copy(interval = it)))
                    }
                )
                var text by remember(value.interval) { mutableStateOf(value.interval.toString()) }
                LaunchedEffect(text) {
                    val newInterval = text.toIntOrNull() ?: return@LaunchedEffect
                    if (newInterval == value.interval) {
                        return@LaunchedEffect
                    }
                    onConfigChanged(setValue(config, value.copy(interval = newInterval)))
                }
                EditText(
                    modifier = Modifier.width(48),
                    value = text,
                    onValueChanged = { text = it },
                )
            }
            actionProperty.controller()
        }
    }
}

@Immutable
class ButtonTriggerProperty<Config : ControllerWidget>(
    private val getValue: (Config) -> ButtonTrigger,
    private val setValue: (Config, ButtonTrigger) -> Config,
) : ControllerWidget.Property<Config, ButtonTrigger>, KoinComponent {
    private val textFactory: TextFactory by inject()

    private fun triggerActionProperty(
        getAction: (ButtonTrigger) -> WidgetTriggerAction?,
        setAction: (ButtonTrigger, WidgetTriggerAction?) -> ButtonTrigger,
        name: Text,
    ) = TriggerActionProperty<Config>(
        getValue = { getAction(getValue(it)) },
        setValue = { config, value -> setValue(config, setAction(getValue(config), value)) },
        name = name,
    )

    private fun keyBindingProperty(
        getKeyBinding: (ButtonTrigger) -> String?,
        setKeyBinding: (ButtonTrigger, String?) -> ButtonTrigger,
        name: Text,
    ) = KeyBindingProperty<Config>(
        getValue = { getKeyBinding(getValue(it)) },
        setValue = { config, value -> setValue(config, setKeyBinding(getValue(config), value)) },
        name = name,
    )

    private fun doubleClickActionProperty(
        getAction: (ButtonTrigger) -> DoubleClickTrigger,
        setAction: (ButtonTrigger, DoubleClickTrigger) -> ButtonTrigger,
        name: Text,
    ) = DoubleClickTriggerProperty<Config>(
        getValue = { getAction(getValue(it)) },
        setValue = { config, value -> setValue(config, setAction(getValue(config), value)) },
        name = name,
    )

    private val downTriggerActionProperty = triggerActionProperty(
        getAction = { it.down },
        setAction = { config, value -> config.copy(down = value) },
        name = textFactory.of(Texts.WIDGET_TRIGGER_DOWN)
    )

    private val pressKeyBindingProperty = keyBindingProperty(
        getKeyBinding = { it.press },
        setKeyBinding = { config, value -> config.copy(press = value) },
        name = textFactory.of(Texts.WIDGET_TRIGGER_PRESS)
    )

    private val releaseTriggerActionProperty = triggerActionProperty(
        getAction = { it.release },
        setAction = { config, value -> config.copy(release = value) },
        name = textFactory.of(Texts.WIDGET_TRIGGER_RELEASE)
    )

    private val doubleClickTriggerActionProperty = doubleClickActionProperty(
        getAction = { it.doubleClick },
        setAction = { config, value -> config.copy(doubleClick = value) },
        name = textFactory.of(Texts.WIDGET_TRIGGER_DOUBLE_CLICK)
    )

    @Composable
    override fun controller(modifier: Modifier, config: ControllerWidget, onConfigChanged: (ControllerWidget) -> Unit) {
        @Composable
        fun <Config : ControllerWidget> ControllerWidget.Property<Config, *>.controller() = controller(
            modifier = Modifier.fillMaxWidth(),
            config = config,
            onConfigChanged = onConfigChanged,
        )

        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            downTriggerActionProperty.controller()
            pressKeyBindingProperty.controller()
            releaseTriggerActionProperty.controller()
            doubleClickTriggerActionProperty.controller()
        }
    }
}