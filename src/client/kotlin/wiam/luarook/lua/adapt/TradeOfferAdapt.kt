package wiam.luarook.lua.adapt

import net.minecraft.village.TradeOffer
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue


fun TradeOffer.toLuaTable(): LuaTable {
    val table = LuaTable()
    table.set("displayedFirstBuyItem", displayedFirstBuyItem.toLuaTable())
    table.set("displayedSecondBuyItem", displayedSecondBuyItem.toLuaTable())
    table.set("sellItem", sellItem.toLuaTable())
    table.set("uses", LuaValue.valueOf(uses))
    table.set("maxUses", LuaValue.valueOf(maxUses))
    return  table
}