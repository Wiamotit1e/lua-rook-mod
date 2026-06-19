package wiam.luarook.mixin.client;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wiam.luarook.lua.ApiBridge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayerNetworkHandlerMixin {

    @Inject(method = "getListedPlayerListEntries", at = @At("RETURN"), cancellable = true)
    public void wiam$getListedPlayerListEntries(CallbackInfoReturnable<Collection<PlayerListEntry>> cir) {
        List<PlayerListEntry> entries = new ArrayList<>(cir.getReturnValue());
        for (int i = entries.size() - 1; i >= 0; i--) {
            ApiBridge.onPlayerListEntriesModified(entries, i, entries.get(i));
        }
        cir.setReturnValue(entries);
    }

    @Inject(method = "onEntityStatus", at = @At("TAIL"))
    public void wiam$onEntityStatus(EntityStatusS2CPacket packet, CallbackInfo ci) {
        ApiBridge.onEntityStatusPacketReceived(packet);
    }
}
