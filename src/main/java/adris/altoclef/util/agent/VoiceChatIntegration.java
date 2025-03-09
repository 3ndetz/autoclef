package adris.altoclef.util.agent;

import adris.altoclef.AltoClef;
import adris.altoclef.AltoclefVoicechat;
import adris.altoclef.Debug;
import adris.altoclef.Py4jEntryPoint;
import de.maxhenkel.voicechat.api.audio.AudioConverter;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.ClientSoundEvent;
import de.maxhenkel.voicechat.plugins.impl.audio.AudioConverterImpl;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.AudioRecorder;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.util.UUID;

public class VoiceChatIntegration {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static final AudioRecorder recorder = AudioRecorder.create();
    public static void onSound(AltoClef mod, ClientReceiveSoundEvent.EntitySound event) {
        if (MC.player == null) {
            return;
        }
        //UUID id = ClientManager.getPlayerStateManager().getOwnID();
        UUID senderId = event.getId();
        // short[] rawAudio = event.getRawAudio();
        // TODO RECOGNITION BY NICKNAME
        // if (id.equals(senderId))
        //     return;  // self
        //Debug.logMessage("GOT ");
        PlayerState state = ClientManager.getPlayerStateManager().getState(senderId);
        if (state != null) {
            String playerName = state.getName();
            if (playerName != null) {
                short[] audio = event.getRawAudio();
                if (audio != null && audio.length > 0) {
                    AudioConverter converter = new AudioConverterImpl();
                    byte[] bytes = converter.shortsToBytes(audio);
                            // the raw 16 bit PCM audio frame
                    Py4jEntryPoint.last_talking_player = playerName;
                    mod.getInfoSender().onVoiceFeed(playerName, bytes);
                }

            }
        }
        /*
        try {
            recorder.appendChunk(id, 1, rawAudio);
        } catch (IOException exception) {
            exception.printStackTrace();
        }


        if (AltoclefVoicechat.CLIENT_API.getGroup() != null) {
            //send(new StaticSoundPacket(id, rawAudio));
        } else {
            //send(new EntitySoundPacket(id, rawAudio, event.isWhispering(), (float) AltoclefVoicechat.CLIENT_API.getVoiceChatDistance()));
        }
        */
    }
}
