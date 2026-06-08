package wiam.luarook.mixin.client;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wiam.luarook.handler.ListedPlayerEntriesHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayerNetworkHandlerMixin {

    @Inject(method = "getListedPlayerListEntries", at = @At("RETURN"), cancellable = true)
    public void wiam$getListedPlayerListEntries(CallbackInfoReturnable<Collection<PlayerListEntry>> cir) {
        List<PlayerListEntry> entries = new ArrayList<>(cir.getReturnValue());
        for (int i = 0; i < entries.size(); i++) {
            ListedPlayerEntriesHandler.getPlayerListEntryModified().invoke(entries, i, entries.get(i));
        }
        cir.setReturnValue(entries);
    }
}
