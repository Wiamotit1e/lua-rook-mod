package wiam.luarook.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wiam.luarook.lua.ApiBridge;

/**
 * Fires the screen-opened event for Lua scripts.
 *
 * <p>Hooks {@code setScreen} instead of {@code Screen#init}: init runs again on window resize,
 * which would report a false "opened". Reading {@code currentScreen} at TAIL gives the screen
 * that actually opened — the {@code setScreen(null)} argument is not always the end result,
 * vanilla resolves it to the previous chat screen, death screen, etc.
 */
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    /** Last screen already reported, used to skip setScreen calls that change nothing. */
    @Unique
    private Screen wiam$reportedScreen;

    @Inject(method = "setScreen", at = @At("TAIL"))
    private void wiam$afterSetScreen(Screen screen, CallbackInfo ci) {
        Screen current = ((MinecraftClient) (Object) this).currentScreen;
        if (current != wiam$reportedScreen) {
            wiam$reportedScreen = current;
            ApiBridge.onScreenOpened(current);
        }
    }
}
