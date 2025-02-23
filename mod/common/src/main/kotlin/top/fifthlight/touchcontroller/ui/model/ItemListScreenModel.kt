package top.fifthlight.touchcontroller.ui.model

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.plus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import top.fifthlight.combine.data.Item

class ItemListScreenModel(
    initialValue: PersistentList<Item>,
    private val onValueChanged: (PersistentList<Item>) -> Unit,
) : TouchControllerScreenModel() {
    private val _value = MutableStateFlow(initialValue)
    val value = _value.asStateFlow()

    fun addItem(item: Item) {
        val newValue = _value.getAndUpdate {
            if (item !in it) {
                it + item
            } else {
                it
            }
        }
        onValueChanged(newValue)
    }

    fun removeItem(index: Int) {
        val newValue = _value.getAndUpdate { it.removeAt(index) }
        onValueChanged(newValue)
    }
}