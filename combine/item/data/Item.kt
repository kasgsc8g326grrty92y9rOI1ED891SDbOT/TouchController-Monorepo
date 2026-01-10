package top.fifthlight.combine.item.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.collections.immutable.PersistentList
import top.fifthlight.combine.data.Identifier
import top.fifthlight.combine.data.Text

val LocalItemFactory = staticCompositionLocalOf<ItemFactory> { error("No ItemFactory in context") }

interface ItemFactory {
    fun createItem(id: Identifier): Item?
    fun createItemStack(item: Item, amount: Int): ItemStack
    fun createItemStack(id: Identifier, amount: Int): ItemStack?
    val allItems: PersistentList<Item>
    val subclasses: PersistentList<ItemSubclass>
}

interface ItemSubclass {
    val id: String
    val configId: String
    val name: Text
    val items: PersistentList<Item>
}

@Immutable
interface Item {
    val id: Identifier
    fun isSubclassOf(subclass: ItemSubclass): Boolean
    fun matches(other: Item): Boolean = equals(other)

    @Composable
    fun toStack() = toStack(1)

    @Composable
    fun toStack(amount: Int) = LocalItemFactory.current.createItemStack(this, amount)

    companion object {
        @Composable
        fun of(id: Identifier) = LocalItemFactory.current.createItem(id)
    }
}
