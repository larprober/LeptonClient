package dev.lepton.mixin;

import dev.lepton.Lepton;
import dev.lepton.event.events.CharTypedEvent;
import dev.lepton.event.events.KeyEvent;
import net.minecraft.client.Keyboard;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKey(long window, int action, KeyInput input, CallbackInfo ci) {
        KeyEvent.KeyAction keyAction = switch (action) {
            case GLFW.GLFW_PRESS -> KeyEvent.KeyAction.Press;
            case GLFW.GLFW_REPEAT -> KeyEvent.KeyAction.Repeat;
            default -> KeyEvent.KeyAction.Release;
        };

        KeyEvent event = Lepton.EVENTS.post(KeyEvent.get(input.key(), input.modifiers(), keyAction));
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "onChar", at = @At("HEAD"), cancellable = true)
    private void onChar(long window, CharInput input, CallbackInfo ci) {
        if (!input.isValidChar()) return;

        CharTypedEvent event = Lepton.EVENTS.post(CharTypedEvent.get((char) input.codepoint()));
        if (event.isCancelled()) ci.cancel();
    }
}
