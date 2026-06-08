package wiam.luarook

import net.minecraft.util.Formatting

fun String.toFormatting(): Formatting {
    return Formatting.valueOf(this)
}