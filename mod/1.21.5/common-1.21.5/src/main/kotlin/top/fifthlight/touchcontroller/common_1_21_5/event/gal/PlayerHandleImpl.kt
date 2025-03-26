package top.fifthlight.touchcontroller.common_1_21_5.event.gal

import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.player.Inventory
import top.fifthlight.combine.platform_1_21_3_1_21_5.toCombine
import top.fifthlight.touchcontroller.common.gal.PlayerHandle
import top.fifthlight.touchcontroller.common.gal.PlayerHandleFactory
import top.fifthlight.touchcontroller.common.gal.PlayerInventory
import top.fifthlight.touchcontroller.common_1_21_3_1_21_5.event.gal.AbstractPlayerHandleImpl

class PlayerHandleImpl(
    inner: LocalPlayer,
) : AbstractPlayerHandleImpl(inner) {
    override var currentSelectedSlot: Int
        get() = inner.inventory.selectedSlot
        set(value) {
            inner.inventory.selectedSlot = value
        }

    override fun getInventory() = PlayerInventory(
        main = inner.inventory.nonEquipmentItems.map { it.toCombine() }.toPersistentList(),
        armor = Inventory.EQUIPMENT_SLOT_MAPPING
            .filterValues { it != EquipmentSlot.OFFHAND }
            .keys
            .map { inner.inventory.getItem(it).toCombine() }
            .toPersistentList(),
        offHand = inner.inventory.getItem(Inventory.SLOT_OFFHAND).toCombine(),
    )
}

object PlayerHandleFactoryImpl : PlayerHandleFactory {
    private val client = Minecraft.getInstance()

    override fun getPlayerHandle(): PlayerHandle? = client.player?.let(::PlayerHandleImpl)
}