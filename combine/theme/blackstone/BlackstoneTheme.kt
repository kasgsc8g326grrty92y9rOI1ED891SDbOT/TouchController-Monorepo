package top.fifthlight.combine.theme.blackstone

import top.fifthlight.combine.theme.Theme
import top.fifthlight.combine.ui.style.DrawableSet
import top.fifthlight.combine.ui.style.TextureSet

val BlackstoneTheme = run {
    val textures = BlackstoneTexturesFactory.of()
    Theme(
        drawables = Theme.Drawables(
            button = DrawableSet(
                normal = textures.WIDGET_BUTTON_BUTTON,
                hover = textures.WIDGET_BUTTON_BUTTON_HOVER,
                active = textures.WIDGET_BUTTON_BUTTON_ACTIVE,
                disabled = textures.WIDGET_BUTTON_BUTTON_DISABLED,
            ),
            guideButton = DrawableSet(
                normal = textures.WIDGET_BUTTON_BUTTON_GUIDE,
                hover = textures.WIDGET_BUTTON_BUTTON_GUIDE_HOVER,
                active = textures.WIDGET_BUTTON_BUTTON_ACTIVE,
                disabled = textures.WIDGET_BUTTON_BUTTON_DISABLED,
            ),
            warningButton = DrawableSet(
                normal = textures.WIDGET_BUTTON_BUTTON_WARNING,
                hover = textures.WIDGET_BUTTON_BUTTON_WARNING_HOVER,
                active = textures.WIDGET_BUTTON_BUTTON_ACTIVE,
                disabled = textures.WIDGET_BUTTON_BUTTON_DISABLED,
            ),

            uncheckedCheckBox = DrawableSet(
                normal = textures.WIDGET_CHECKBOX_CHECKBOX,
                hover = textures.WIDGET_CHECKBOX_CHECKBOX_HOVER,
                active = textures.WIDGET_CHECKBOX_CHECKBOX_ACTIVE,
                disabled = textures.WIDGET_CHECKBOX_CHECKBOX,
            ),
            checkboxChecked = DrawableSet(
                normal = textures.WIDGET_CHECKBOX_CHECKBOX_CHECKED,
                hover = textures.WIDGET_CHECKBOX_CHECKBOX_CHECKED_HOVER,
                active = textures.WIDGET_CHECKBOX_CHECKBOX_CHECKED_ACTIVE,
                disabled = textures.WIDGET_CHECKBOX_CHECKBOX_CHECKED,
            ),
            checkBoxButton = DrawableSet(
                normal = textures.WIDGET_CHECKBOX_CHECKBOX_BUTTON,
                hover = textures.WIDGET_CHECKBOX_CHECKBOX_BUTTON_HOVER,
                active = textures.WIDGET_CHECKBOX_CHECKBOX_BUTTON_ACTIVE,
                disabled = textures.WIDGET_CHECKBOX_CHECKBOX_BUTTON_DISABLED,
            ),

            radioUnchecked = DrawableSet(
                normal = textures.WIDGET_RADIO_RADIO,
                hover = textures.WIDGET_RADIO_RADIO_HOVER,
                active = textures.WIDGET_RADIO_RADIO_ACTIVE,
                disabled = textures.WIDGET_RADIO_RADIO,
            ),
            radioChecked = DrawableSet(
                normal = textures.WIDGET_RADIO_RADIO_CHECKED,
                hover = textures.WIDGET_RADIO_RADIO_CHECKED_HOVER,
                active = textures.WIDGET_RADIO_RADIO_CHECKED_ACTIVE,
                disabled = textures.WIDGET_RADIO_RADIO_CHECKED,
            ),

            switchFrame = DrawableSet(
                normal = textures.WIDGET_SWITCH_FRAME,
                hover = textures.WIDGET_SWITCH_FRAME_HOVER,
                active = textures.WIDGET_SWITCH_FRAME_ACTIVE,
                disabled = textures.WIDGET_SWITCH_FRAME_DISABLED,
            ),
            switchBackground = TextureSet(
                normal = textures.WIDGET_SWITCH_SWITCH,
                hover = textures.WIDGET_SWITCH_SWITCH_HOVER,
                active = textures.WIDGET_SWITCH_SWITCH_ACTIVE,
                disabled = textures.WIDGET_SWITCH_SWITCH_DISABLED,
            ),

            editText = DrawableSet(
                normal = textures.WIDGET_TEXTFIELD_TEXTFIELD,
                hover = textures.WIDGET_TEXTFIELD_TEXTFIELD_HOVER,
                active = textures.WIDGET_TEXTFIELD_TEXTFIELD_ACTIVE,
                disabled = textures.WIDGET_TEXTFIELD_TEXTFIELD_DISABLED,
            ),

            sliderActiveTrack = DrawableSet(
                normal = textures.WIDGET_SLIDER_SLIDER_ACTIVE,
                hover = textures.WIDGET_SLIDER_SLIDER_ACTIVE_HOVER,
                active = textures.WIDGET_SLIDER_SLIDER_ACTIVE_ACTIVE,
                disabled = textures.WIDGET_SLIDER_SLIDER_ACTIVE_DISABLED,
            ),
            sliderInactiveTrack = DrawableSet(
                normal = textures.WIDGET_SLIDER_SLIDER_INACTIVE,
                hover = textures.WIDGET_SLIDER_SLIDER_INACTIVE_HOVER,
                active = textures.WIDGET_SLIDER_SLIDER_INACTIVE_ACTIVE,
                disabled = textures.WIDGET_SLIDER_SLIDER_INACTIVE_DISABLED,
            ),
            sliderHandle = DrawableSet(
                normal = textures.WIDGET_HANDLE_HANDLE,
                hover = textures.WIDGET_HANDLE_HANDLE_HOVER,
                active = textures.WIDGET_HANDLE_HANDLE_ACTIVE,
                disabled = textures.WIDGET_HANDLE_HANDLE_DISABLED,
            ),

            tab = DrawableSet(
                normal = textures.WIDGET_TAB_TAB,
                hover = textures.WIDGET_TAB_TAB_HOVER,
                active = textures.WIDGET_TAB_TAB_ACTIVE,
                disabled = textures.WIDGET_TAB_TAB_DISABLED,
            ),

            selectMenuBox = DrawableSet(
                normal = textures.WIDGET_SELECT_SELECT,
                hover = textures.WIDGET_SELECT_SELECT_HOVER,
                active = textures.WIDGET_SELECT_SELECT_ACTIVE,
                disabled = textures.WIDGET_SELECT_SELECT_DISABLED,
            ),

            selectFloatPanel = textures.WIDGET_BACKGROUND_FLOAT_WINDOW,
            selectIconUp = textures.WIDGET_HANDLE_HANDLE,
            selectIconDown = textures.WIDGET_HANDLE_HANDLE,

            radioBoxBorder = textures.WIDGET_HANDLE_HANDLE,

            iconButton = DrawableSet(
                normal = textures.WIDGET_ICON_BUTTON_ICON_BUTTON,
                hover = textures.WIDGET_ICON_BUTTON_ICON_BUTTON_HOVER,
                active = textures.WIDGET_ICON_BUTTON_ICON_BUTTON_ACTIVE,
                disabled = textures.WIDGET_ICON_BUTTON_ICON_BUTTON_DISABLED,
            ),
            selectedIconButton = DrawableSet(
                normal = textures.WIDGET_ICON_BUTTON_ICON_BUTTON_ACTIVE,
                hover = textures.WIDGET_ICON_BUTTON_ICON_BUTTON_ACTIVE,
                active = textures.WIDGET_ICON_BUTTON_ICON_BUTTON_ACTIVE,
                disabled = textures.WIDGET_ICON_BUTTON_ICON_BUTTON_DISABLED,
            ),

            selectItemUnselected = DrawableSet(
                normal = textures.WIDGET_LIST_LIST,
                hover = textures.WIDGET_LIST_LIST_HOVER,
                active = textures.WIDGET_LIST_LIST_ACTIVE,
                disabled = textures.WIDGET_LIST_LIST_DISABLED,
            ),
            selectItemSelected = DrawableSet(
                normal = textures.WIDGET_LIST_LIST_ACTIVE,
                hover = textures.WIDGET_LIST_LIST_ACTIVE,
                active = textures.WIDGET_LIST_LIST_ACTIVE,
                disabled = textures.WIDGET_LIST_LIST_DISABLED,
            ),

            colorPickerHandleChoice = textures.WIDGET_COLOR_PICKER_HANDLE_CHOICE,
            colorPickerSliderHandleHollow = DrawableSet(
                normal = textures.WIDGET_COLOR_PICKER_HOLLOW_HANDLE,
                hover = textures.WIDGET_COLOR_PICKER_HOLLOW_HANDLE_HOVER,
                active = textures.WIDGET_COLOR_PICKER_HOLLOW_HANDLE_ACTIVE,
                disabled = textures.WIDGET_COLOR_PICKER_HOLLOW_HANDLE_DISABLED,
            ),

            alertDialogBackground = textures.WIDGET_BACKGROUND_FLOAT_WINDOW,

            itemGridBackground = textures.BACKGROUND_BACKPACK,
        )
    )
}
