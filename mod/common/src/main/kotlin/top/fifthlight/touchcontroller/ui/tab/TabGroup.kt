package top.fifthlight.touchcontroller.ui.tab

import kotlinx.collections.immutable.persistentListOf

sealed class TabGroup(
    val title: String
) {
    data object LayoutGroup : TabGroup("Layout")
    data object GeneralGroup : TabGroup("General")
    data object ItemGroup : TabGroup("Item")

    companion object {
        val allTabs = persistentListOf<TabGroup>(
            LayoutGroup,
            GeneralGroup,
            ItemGroup,
        )
    }
}
