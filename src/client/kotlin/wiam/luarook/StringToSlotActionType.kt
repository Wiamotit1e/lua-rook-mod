package wiam.luarook

import net.minecraft.screen.slot.SlotActionType

fun String.toSlotActionType(): SlotActionType {
    return when (this) {
        "PICKUP" -> SlotActionType.PICKUP
        "QUICK_MOVE" -> SlotActionType.QUICK_MOVE
        "SWAP" -> SlotActionType.SWAP
        "CLONE" -> SlotActionType.CLONE
        "THROW" -> SlotActionType.THROW
        "QUICK_CRAFT" -> SlotActionType.QUICK_CRAFT
        "PICKUP_ALL" -> SlotActionType.PICKUP_ALL
        else -> throw IllegalArgumentException("Invalid SlotActionType: $this")
    }
}