package adris.altoclef.mixins;

import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.ChatMessageEvent;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;


@Mixin(MessageHandler.class)
public final class ChatReadMixin {
    @Inject(
            method = "onChatMessage",
            at = @At("HEAD")
    )
    // Since 1.21 WORKS ONLY FOR SINGLEPLAYER
    private void onChatMessage(SignedMessage message, GameProfile sender, MessageType.Parameters params, CallbackInfo ci) {
        //Debug.logMessage("onChatMessage DEBUG!!!! MSG CHAT CharReadMixin" + message);
        //ChatMessageEvent evt = new ChatMessageEvent(message, sender, params);
        //EventBus.publish(evt);
    }
    @Inject(
            method = "onProfilelessMessage",
            at = @At("HEAD")
    )
    // Working only for vanilla server with vanilla chat
    private void onProfilelessMessage(Text content, MessageType.Parameters params, CallbackInfo ci) {
        //Debug.logMessage("onProfilelessMessage DEBUG!!!! MSG CHAT CharReadMixin" + content);
        //ChatMessageEvent evt = new ChatMessageEvent(message, sender, params);
        //EventBus.publish(evt);
    }
    // NOT WORK WHY!!!????
    //@Inject(
    //        method = "onGameMessage",
    //        at = @At("HEAD")
    //)
    //// All messag
    //private void onGameMessage(Text message, boolean overlay) {
    //    //Debug.logMessage("onProfilelessMessage DEBUG!!!! MSG CHAT CharReadMixin" + content);
    //    //ChatMessageEvent evt = new ChatMessageEvent(message, sender, params);
    //    //EventBus.publish(evt);
    //}
}