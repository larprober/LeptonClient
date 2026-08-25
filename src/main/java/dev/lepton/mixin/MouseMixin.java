package dev.lepton.mixin;

import dev.lepton.Lepton;
import dev.lepton.event.events.KeyEvent;
import dev.lepton.event.events.MouseButtonEvent;
import dev.lepton.event.events.MouseScrollEvent;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void onMouseButton(long window, MouseInput input, int action, CallbackInfo ci) {
        KeyEvent.KeyAction keyAction = action == GLFW.GLFW_PRESS ? KeyEvent.KeyAction.Press : KeyEvent.KeyAction.Release;

        MouseButtonEvent event = Lepton.EVENTS.post(MouseButtonEvent.get(input.button(), keyAction));
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        MouseScrollEvent event = Lepton.EVENTS.post(MouseScrollEvent.get(vertical));
        if (event.isCancelled()) ci.cancel();
    }
}
