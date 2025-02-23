package top.fifthlight.touchcontroller.gal

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemGroups
import net.minecraft.registry.Registries
import top.fifthlight.combine.platform.TextImpl
import top.fifthlight.combine.platform.toCombine
import kotlin.jvm.optionals.getOrNull

object VanillaItemListProviderImpl : VanillaItemListProvider {
    private val client = MinecraftClient.getInstance()

    class CreativeTabImpl(group: ItemGroup) : VanillaItemListProvider.CreativeTab {
        override val type = when (group.type) {
            ItemGroup.Type.CATEGORY -> VanillaItemListProvider.CreativeTab.Type.CATEGORY
            ItemGroup.Type.INVENTORY -> VanillaItemListProvider.CreativeTab.Type.SURVIVAL_INVENTORY
            ItemGroup.Type.HOTBAR -> error("Bad item group")
            ItemGroup.Type.SEARCH -> VanillaItemListProvider.CreativeTab.Type.SEARCH
        }

        override val name = TextImpl(group.displayName)

        override val icon = group.icon.toCombine()

        override val items = group.displayStacks.map { it.toCombine() }.toPersistentList()
    }

    val ClientPlayerEntity.shouldShowOperatorTab
        get() = isCreativeLevelTwoOp && client.options.operatorItemsTab.value

    fun ClientPlayerEntity.refreshCreativeTabs(shouldShowOperatorTab: Boolean) {
        val enabledFeatures = networkHandler.enabledFeatures
        ItemGroups.updateDisplayContext(enabledFeatures, shouldShowOperatorTab, world.registryManager)
    }

    override fun getCreativeTabs(player: PlayerHandle): PersistentList<VanillaItemListProvider.CreativeTab> {
        val player = (player as PlayerHandleImpl).inner
        val shouldShowOperatorTab = player.shouldShowOperatorTab
        player.refreshCreativeTabs(shouldShowOperatorTab)
        return ItemGroups
            .getGroups()
            .filter {
                if (it.type == ItemGroup.Type.HOTBAR) {
                    return@filter false
                }
                val key = Registries.ITEM_GROUP.getKey(it).getOrNull()
                if (key == ItemGroups.OPERATOR) {
                    return@filter shouldShowOperatorTab
                }
                true
            }
            .map(::CreativeTabImpl)
            .toPersistentList()
    }
}