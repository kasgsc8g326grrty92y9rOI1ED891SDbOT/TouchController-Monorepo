package top.fifthlight.armorstand.ui.component

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ContainerObjectSelectionList
import net.minecraft.client.gui.components.StringWidget
import net.minecraft.network.chat.Component

class IkList(
    client: Minecraft,
    width: Int = 0,
    height: Int = 0,
    x: Int = 0,
    y: Int = 0,
    val onClicked: (Int, Boolean) -> Unit,
) : ContainerObjectSelectionList<IkList.Entry>(
    client,
    width,
    height,
    y,
    24,
) {
    init {
        this.x = x
    }

    override fun renderListSeparators(context: GuiGraphics) {}

    override fun renderListBackground(context: GuiGraphics) {}

    override fun scrollBarX() = right - 6

    override fun getRowLeft() = x

    override fun getRowWidth() = if (scrollbarVisible()) {
        width - 6
    } else {
        width
    }

    fun setList(list: List<Pair<String?, Boolean>>) {
        for ((index, item) in list.withIndex()) {
            if (index >= itemCount) {
                addEntry(Entry().apply {
                    name = item.first
                    enabled = item.second
                    onEnabledChanged = {
                        onClicked(index, it)
                    }
                })
            } else {
                getEntry(index).apply {
                    name = item.first
                    enabled = item.second
                }
            }
        }
        while (itemCount > list.size) {
            remove(itemCount - 1)
        }
    }

    inner class Entry : ContainerObjectSelectionList.Entry<Entry>() {
        var name: String? = null
            set(value) {
                field = value
                nameLabel.message = value?.let { Component.literal(value) }
                    ?: Component.translatable("armorstand.animation.unnamed_ik_node")
            }
        var enabled: Boolean = false
            set(value) {
                field = value
                checkbox.checked = value
            }
        var onEnabledChanged: ((Boolean) -> Unit)? = null

        private val nameLabel = StringWidget(Component.empty(), Minecraft.getInstance().font).alignLeft()
        private val checkbox = CheckBoxButton(enabled) { onEnabledChanged?.invoke(!enabled) }

        private val selectableChildren = listOf(checkbox)
        private val children = listOf(nameLabel, checkbox)
        override fun narratables() = selectableChildren
        override fun children() = children

        override fun render(
            context: GuiGraphics,
            index: Int,
            y: Int,
            x: Int,
            entryWidth: Int,
            entryHeight: Int,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickProgress: Float,
        ) {
            val gap = 8
            nameLabel.setPosition(x + gap, y + (entryHeight - nameLabel.height) / 2)
            nameLabel.setSize(entryWidth - checkbox.width - gap * 3, nameLabel.height)
            checkbox.setPosition(x + entryWidth - checkbox.width - gap, y + (entryHeight - checkbox.height) / 2)
            nameLabel.render(context, mouseX, mouseY, tickProgress)
            checkbox.render(context, mouseX, mouseY, tickProgress)
        }
    }
}
