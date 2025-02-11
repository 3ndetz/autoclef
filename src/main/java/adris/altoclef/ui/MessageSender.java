package adris.altoclef.ui;

import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.SendChatEvent;
import adris.altoclef.util.time.BaseTimer;
import adris.altoclef.util.time.TimerReal;
import java.time.Instant;
import java.util.BitSet;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.packet.c2s.play.ChatCommandSignedC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.text.Text;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * We can't send messages immediately as the server will kick us.
 * As such, we will send messages in a delayed queued fashion.
 */
public class MessageSender {

    // How many messages can we send quickly before giving a little pause?
    private static final int FAST_LIMIT = 6;
    private static final int SLOW_LIMIT = 3;

    private final PriorityQueue<BaseMessage> _whisperQueue = new PriorityQueue<>(
            Comparator.comparingInt((BaseMessage msg) -> msg.priority.getImportance())
                    .thenComparingInt(msg -> msg.index)
    );
    //private final Queue<Whisper> _whisperQueue = new ArrayDeque<>();

    private final BaseTimer _fastSendTimer = new TimerReal(0.3f);
    private final BaseTimer _bigSendTimer = new TimerReal(3.5);
    private final BaseTimer _bigBigSendTimer = new TimerReal(10);

    private int _messageCounter = 0;

    private int _fastCount;
    private int _slowCount;

    public void tick() {
        if (canSendMessage()) {
            if (!_whisperQueue.isEmpty()) {
                BaseMessage msg = _whisperQueue.poll();
                //assert msg != null;
                if (msg != null)
                    sendChatUpdateTimers(msg.getChatInput());
            }
        }
    }

    public void enqueueWhisper(String username, String message, MessagePriority priority) {
        var playerInfo = MinecraftClient.getInstance().player;
        if (playerInfo != null && !username.equals(playerInfo.getName().getString())) {
            _whisperQueue.add(new Whisper(username, message, priority, _messageCounter++));
        }
    }

    public void enqueueChat(String message, MessagePriority priority) {
        _whisperQueue.add(new ChatMessage(message, priority, _messageCounter++));
    }

    private boolean canSendMessage() {
        return _bigBigSendTimer.elapsed() && _bigSendTimer.elapsed() && _fastSendTimer.elapsed();
    }

    private void sendChatUpdateTimers(String message) {
         if (message != null && !message.isBlank() && MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
             // TODO ADD KICK-RESTRICTED SYMBOLS CLEAR
             // auto kick reason: недопустимые символы в чате
             // этот параграф даже в чате не написать
             message = message.replace("§", "");
             if (message.length() > 1 && message.startsWith("/")) {
                 // remove "/" from the command
                 sendCmdInstant(message.substring(1));
             } else {
                 sendChatInstant(message);
             }

             _fastSendTimer.reset();
             _fastCount++;
             if (_fastCount >= FAST_LIMIT) {
                 _bigSendTimer.reset();
                 _fastCount = 0;
                 _slowCount++;
                 if (_slowCount >= SLOW_LIMIT) {
                     _bigBigSendTimer.reset();
                     _slowCount = 0;
                 }
             }
         }
    }

    public void sendChatInstant(String message) {
        if (MinecraftClient.getInstance().player == null) {
            Debug.logError("Failed to send chat message as no client loaded.");
            return;
        }

        MinecraftClient.getInstance().player.networkHandler.sendPacket(new ChatMessageC2SPacket(message, Instant.now(), 10, null, new LastSeenMessageList.Acknowledgment(0, new BitSet())));
    }

    public void sendCmdInstant(String message) {
        if (MinecraftClient.getInstance().player == null) {
            Debug.logError("Failed to send chat message as no client loaded.");
            return;
        }

        MinecraftClient.getInstance().player.networkHandler.sendPacket(new CommandExecutionC2SPacket(message));
    }

    private static abstract class BaseMessage {
        public MessagePriority priority;
        public int index;

        public BaseMessage(MessagePriority priority, int index) {
            this.priority = priority;
            this.index = index;
        }

        public abstract String getChatInput();
    }

    private static class Whisper extends BaseMessage {
        public String username;
        public String message;

        public Whisper(String username, String message, MessagePriority priority, int index) {
            super(priority, index);
            this.username = username;
            this.message = message;
        }

        @Override
        public String getChatInput() {
            return "/msg " + username + " " + message;
        }
    }

    private static class ChatMessage extends BaseMessage {

        public String message;

        public ChatMessage(String message, MessagePriority priority, int index) {
            super(priority, index);
            this.message = message;

        }

        @Override
        public String getChatInput() {
            return message;
        }
    }
}
