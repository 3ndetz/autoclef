package adris.altoclef;

import adris.altoclef.util.agent.VoiceChatIntegration;
import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.*;
import de.maxhenkel.voicechat.plugins.impl.VoicechatClientApiImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


// THAT DID ALL THE THING
// IDK HOW ????? WHATS THIS????
@ForgeVoicechatPlugin
public class AltoclefVoicechat implements VoicechatPlugin {
    public static VoicechatClientApi CLIENT_API = VoicechatClientApiImpl.instance();
    //@Nullable
    public static VoicechatApi voicechatApi;
    @Nullable
    public static AltoClef jankModInstance;
    private ExecutorService executorService;

    public AltoclefVoicechat(){
        executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setName("AltoclefMicrophoneProcessThread");
            thread.setUncaughtExceptionHandler((t, e) -> {
                AltoClef.LOGGER.error("Error in altoclef microphone process thread", e);
            });
            thread.setDaemon(true);
            return thread;
        });
    }

    // TODO audio convert then https://github.com/henkelmax/audio-player/blob/master/src/main/java/de/maxhenkel/audioplayer/AudioConverter.java
    /**
     * @return the ID of this plugin - Has to be unique
     */
    @Override
    public String getPluginId() {
        return AltoClef.MOD_ID;
    }

    @Override
    public void initialize(VoicechatApi api) {
        if (jankModInstance == null) {
            jankModInstance = Debug.jankModInstance;
        }

        if (api instanceof VoicechatClientApi clientApi) {
            System.out.println("[RAW ALTO VOICE PLUGIN DEBUG] INITIALIZING CLIENT API");
            //voicechatApi = clientApi;
            // WHY THIS NOT PRINTING FOR CLIENT??????
            //Debug.logMessage("Voicechat API initialized as client!!!");
        }
        if (api instanceof VoicechatServerApi serverApi){
            // always performs only this -_-
            Debug.logMessage("Voicechat API initialized as server!!!");
        }
        voicechatApi = api;
        Debug.logMessage("Voicechat API initialized!!! AltoClef normal=" + (jankModInstance != null));
        // VoicechatPlugin.super.initialize(api);

    }


    @Override
    public void registerEvents(EventRegistration registration) {

        // TODO FIND CLIENT EVENTS!
        // TODO test
        // Register other client processing events

        // working! but how we can get the enity??
        // registration.registerEvent(OpenALSoundEvent.class, this::onOpenALSound);
        //ClientVoicechatSocket.on
        // working, but nothing normal info provided
        // registration.registerEvent(MergeClientSoundEvent.class, this::onMergeClientSound);


        // this runs i don't know when, never maybe?..()
        // registration.registerEvent(ClientSoundEvent.class, this::onClientSoundEvent);
        //registration.registerEvent(StaticSoundPacketEvent.class, this::onStaticSound);
        //registration.registerEvent(LocationalSoundPacketEvent.class, this::onLocationalSound);
        // registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
        // looks like this abstract thing never works....
        //registration.registerEvent(ClientReceiveSoundEvent.class, this::onReceiveAudio);
        registration.registerEvent(ClientReceiveSoundEvent.EntitySound.class, this::onReceiveAudio);
        //registration.registerEvent(ClientReceiveSoundEvent.LocationalSound.class, this::onReceiveAudio);
        //registration.registerEvent(ClientReceiveSoundEvent.StaticSound.class, this::onReceiveAudio);
        //registration.registerEvent(MicrophonePacketEvent.class, microphonePacketEvent -> {
        //    executorService.submit(() -> {
        //        AltoClef.LOGGER.error("microphonePacketEvent{}", microphonePacketEvent.toString());
        //    });
        //});

        // THIS EVENTS RUNS SERVERSIDE ONLY
        // registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
        // registration.registerEvent(EntitySoundPacketEvent.class, this::onReceiveAudioEntity);

    }
    // public void onReceiveAudioEntity(EntitySoundPacketEvent event) {
    //     if (event != null && event.getSenderConnection() != null && event.getSenderConnection().getPlayer() != null)
    //         Debug.logMessage("Received audio from player " + event.getSenderConnection().getPlayer().toString());
    // }
    // private void onMergeClientSound(MergeClientSoundEvent event) {
    //     // This event will fire when audio is being processed on the client
    //     Debug.logMessage("Client sound merging event received");
    // }
    // private void onClientSoundEvent(ClientSoundEvent event) {
    //     //System.out.println("[RAW ALTO VOICE PLUGIN ClientSoundEvent] " + (event != null ? event.toString() : "null"));
    //     // This event fires when a sound is received
    //     //if (event != null) {
    //     //    Debug.logMessage("Client sound event received at: " + event.getPacket().toString());
    //     //}
    // }
    // private void onStaticSound(StaticSoundPacketEvent event) {
    //     //System.out.println("[RAW ALTO VOICE PLUGIN StaticSoundPacketEvent] " + (event != null ? event.toString() : "null"));
    //     // This event fires when a static sound is received
    //     if (event != null) {
    //         Debug.logMessage("Static sound received at: " + event.getPacket().toString());
    //     }
    // }
    // private void onLocationalSound(LocationalSoundPacketEvent event) {
    //     //System.out.println("[RAW ALTO VOICE PLUGIN LocationalSoundPacketEvent] " + (event != null ? event.toString() : "null"));
    //     // This event fires when a locational sound is received
    //     if (event != null) {
    //         Debug.logMessage("Locational sound received at: " + event.getPacket().toString());
    //     }
    // }
    private String getStringNullable(Object obj) {
        if (obj != null) {
            return obj.toString();
        }
        return "null";
    }
    /*
    private void onOpenALSound(OpenALSoundEvent event) {
        // event.getVoicechat().
    }
    private void onOpenALSoundDebug(OpenALSoundEvent event) {
        // This event fires for each sound being processed by OpenAL
        if (event.getChannelId() != null) {
            // gracefully print all we have here with null checks
            Debug.logMessage("[SND] Sound got. Category="
                    + getStringNullable(event.getCategory())
            + "; SourceId=" + getStringNullable(event.getSource())
            + "; channelId=" + getStringNullable(event.getChannelId()));

            if (event.getPosition() != null) {
                Debug.logMessage("[SND] Sound position: x"
                        + event.getPosition().getX() + " y"
                        + event.getPosition().getY() + " z"
                        + event.getPosition().getZ());
            }

            ClientWorld world = MinecraftClient.getInstance().world;

            Entity entity = world != null ? world.getPlayerByUuid(event.getChannelId()) : null;
            if (entity != null) {
                Debug.logMessage("[SND !!!] Sound from entity: " + entity.toString());
            }
            // working, but we cannot determine the sound sender entity =(
            // only a position if it's present
            //event.getVoicechat().
            //Debug.logMessage("OpenAL processing sound for channel: " + event.getChannelId());
        }
    }
    private void onMicrophonePacket(MicrophonePacketEvent event) {
        System.out.println("[RAW ALTO VOICE PLUGIN DEBUG] " + (event != null ? event.toString() : "null"));
        // Debug logging to verify packet reception
        if (event != null && event.getSenderConnection() != null) {
            Debug.logMessage("Mic packet from: " + event.getSenderConnection().getPlayer().toString());
        }
    }
*/
    private void onReceiveAudio(ClientReceiveSoundEvent event) {
        // System.out.println("[RAW ALTO VOICE PLUGIN DEBUG] " + (event != null ? event.toString() : "null"));
        if (event != null) {
            if (jankModInstance == null) return;

            // Debug.logMessage("Received sound from UUID: " + event.getId());

            // Check sound type
            if (event instanceof ClientReceiveSoundEvent.EntitySound entitySound) {
                VoiceChatIntegration.onSound(jankModInstance, entitySound);
                // Debug.logMessage("Entity sound received, whispering: " + entitySound.isWhispering());

            } else if (event instanceof ClientReceiveSoundEvent.LocationalSound) {
                ClientReceiveSoundEvent.LocationalSound locationalSound = (ClientReceiveSoundEvent.LocationalSound) event;
                // Debug.logMessage("Locational sound received at: " + locationalSound.getPosition());
            }

            // For debugging, you can analyze the raw audio data
            // short[] audioData = event.getRawAudio();
            // Debug.logMessage("Audio data length: " + audioData.length);
        }
    }

//
    //public void onServerStarted(VoicechatServerStartedEvent event) {
    //    Debug.logMessage("[VC AC PLG] VoiceChat Altoclef Plugin Server started" +event.getVoicechat().toString());
    //}
}
