package top.fifthlight.touchcontroller.common.ui.entitypicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.collections.immutable.PersistentList
import top.fifthlight.combine.widget.layout.Row
import top.fifthlight.touchcontroller.common.gal.entity.EntityItemProvider
import top.fifthlight.touchcontroller.common.gal.entity.EntityType
import top.fifthlight.touchcontroller.common.gal.entity.EntityTypeProvider

@Composable
fun EntityPicker(
    value: PersistentList<EntityType>,
    onValueChanged: (PersistentList<EntityType>) -> Unit,
) {
    val totalEntityTypes = remember {
        EntityTypeProvider.allTypes.map {
            Pair(EntityItemProvider.getEntityIconItem(it), it)
        }
    }

    Row {

    }
}