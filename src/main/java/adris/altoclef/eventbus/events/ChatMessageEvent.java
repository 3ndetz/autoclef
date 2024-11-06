package adris.altoclef.eventbus.events;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;

/**
 * Whenever chat appears
 */
public class ChatMessageEvent {
    SignedMessage message;
    String message_raw;
    boolean overlay = false;
    GameProfile sender;
    MessageType.Parameters messageType;

    public ChatMessageEvent(String msg, SignedMessage message, GameProfile sender, MessageType.Parameters messageType) {
        if(message != null){
            this.message = message;
        }else{
            this.message_raw = msg;
        }
        this.sender = sender;
        this.messageType = messageType;
    }
    public ChatMessageEvent(String message, boolean overlay) {
        this.message_raw = message;
        this.overlay = overlay;
    }
    public ChatMessageEvent(String message) {
        this.message_raw = message;
        this.overlay = false;
    }
    public String messageRawContent(){
        return message_raw;
    }
    public String messageContent() {
        if (this.message != null) {
            return message.getContent().getString();
        }else{
            return message_raw;
        }
    }

    public String senderName() {
        return sender.getName();
    }

    public MessageType messageType() {
        return messageType.type().value();
    }
}
