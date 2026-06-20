package wiam.luarook.mixin.client;

import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wiam.luarook.lua.LuaInputOverride;


@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {

    @Inject(method = "tick", at = @At("RETURN"))
    private void applyLuaInput(CallbackInfo ci) {
        if (!LuaInputOverride.INSTANCE.getActive()) return;

        KeyboardInput self = (KeyboardInput) (Object) this;
        PlayerInput kb = self.playerInput;

        boolean fwd = kb.forward()  || bool(LuaInputOverride.INSTANCE.getForward());
        boolean bck = kb.backward() || bool(LuaInputOverride.INSTANCE.getBackward());
        boolean lft = kb.left()     || bool(LuaInputOverride.INSTANCE.getLeft());
        boolean rgt = kb.right()    || bool(LuaInputOverride.INSTANCE.getRight());
        boolean jmp = kb.jump()     || bool(LuaInputOverride.INSTANCE.getJump());
        boolean snk = kb.sneak()    || bool(LuaInputOverride.INSTANCE.getSneak());
        boolean spr = kb.sprint()   || bool(LuaInputOverride.INSTANCE.getSprint());

        self.playerInput = new PlayerInput(fwd, bck, lft, rgt, jmp, snk, spr);

        float f = fwd && !bck ? 1.0f : !fwd && bck ? -1.0f : 0.0f;
        float s = lft && !rgt ? 1.0f : !lft && rgt ? -1.0f : 0.0f;
        self.movementVector = new Vec2f(s, f).normalize();
    }

    @Unique
    private static boolean bool(Boolean b) {
        return b != null && b;
    }
}
