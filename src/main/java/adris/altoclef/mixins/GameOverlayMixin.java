package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.GameOverlayEvent;
import adris.altoclef.eventbus.events.TitleEvent;
import adris.altoclef.eventbus.events.TitleScreenEntryEvent;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class GameOverlayMixin {

    @Inject(
            method = "setOverlayMessage",
            at = @At("HEAD")
    )
    public void onSetOverlayMessage(Text message, boolean tinted, CallbackInfo ci) {
        String text = message.getString();
        EventBus.publish(new GameOverlayEvent(text));
    }

    @Inject(
            method = "setTitle",
            at = @At("HEAD")
    )
    public void onSetTitle(Text title, CallbackInfo ci) {
        String text = title.getString();
        EventBus.publish(new TitleEvent(text));
    }
}
