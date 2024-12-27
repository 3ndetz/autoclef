package adris.altoclef;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;


// no events https://github.com/ToroCraft/ToroHealth
// FOUND HERE maybe we need it but old 1.17 https://github.com/hamusuke0323/DamageIndicatorFabric/tree/1.18/src/main/java/com/hamusuke/damageindicator/network



public class DamageEventHandler {
    //private static AltoClef _mod;
    public static void registerDamagePacketReceiver(AltoClef mod) {
        //_mod = mod;
          /*
        Debug.logInternal("GLOBAL RECEIVERS" + ClientPlayNetworking.getGlobalReceivers());

        CustomPayload.Id<CustomPayload> packetId = new CustomPayload.Id<>(PlayPackets.DAMAGE_EVENT.id());
        ClientPlayNetworking.registerGlobalReceiver(packetId, (packet, context) -> {
            Debug.logMessage("LOLOLOLOLOL");
            //context.client().execute(() -> {Debug.logMessage("LOLOLOLOLOL");});
        });

java.lang.IllegalArgumentException: Cannot register handler as no payload type has been registered with name "minecraft:damage_event" for CLIENTBOUND PLAY
	at net.fabricmc.fabric.impl.networking.GlobalReceiverRegistry.assertPayloadType(GlobalReceiverRegistry.java:218) ~[fabric-networking-api-v1-4.2.0+ab7edbacd1.jar:?]
	at net.fabricmc.fabric.impl.networking.GlobalReceiverRegistry.registerGlobalReceiver(GlobalReceiverRegistry.java:80) ~[fabric-networking-api-v1-4.2.0+ab7edbacd1.jar:?]
	at net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(ClientPlayNetworking.java:72) ~[fabric-networking-api-v1-4.2.0+ab7edbacd1.jar:?]
	at adris.altoclef.DamageEventHandler.registerDamagePacketReceiver(DamageEventHandler.java:22) ~[main/:?]
	at adris.altoclef.AltoClef.onInitializeLoad(AltoClef.java:232) ~[main/:?]
	at adris.altoclef.AltoClef.lambda$onInitialize$0(AltoClef.java:106) ~[main/:?]
	at adris.altoclef.eventbus.Subscription.accept(Subscription.java:15) ~[main/:?]
	at adris.altoclef.eventbus.EventBus.publish(EventBus.java:46) ~[main/:?]
	at net.minecraft.client.gui.screen.TitleScreen.handler$zlp002$altoclef$init(TitleScreen.java:522) ~[minecraft-merged-ffc470daea-1.21-net.fabricmc.yarn.1_21.1.21+build.9-v2.jar:?]
	at net.minecraft.client.gui.screen.TitleScreen.init(TitleScreen.java) ~[minecraft-merged-ffc470daea-1.21-net.fabricmc.yarn.1_21.1.21+build.9-v2.jar:?]
	at net.minecraft.client.gui.screen.Screen.init(Screen.java:309) ~[minecraft-merged-ffc470daea-1.21-net.fabricmc.yarn.1_21.1.21+build.9-v2.jar:?]
	at net.minecraft.client.MinecraftClient.setScreen(MinecraftClient.java:1143) ~[minecraft-merged-ffc470daea-1.21-net.fabricmc.yarn.1_21.1.21+build.9-v2.jar:?]
	at net.minecraft.client.MinecraftClient.method_53528(MinecraftClient.java:737) ~[minecraft-merged-ffc470daea-1.21-net.fabricmc.yarn.1_21.1.21+build.9-v2.jar:?]
	at net.minecraft.client.MinecraftClient.collectLoadTimes(MinecraftClient.java:722) ~[minecraft-merged-ffc470daea-1.21-net.fabricmc.yarn.1_21.1.21+build.9-v2.jar:?]
	at net.minecraft.client.MinecraftClient.onFinishedLoading(MinecraftClient.java:711) ~[minecraft-merged-ffc470daea-1.21-net.fabricmc.yarn.1_21.1.21+build.9-v2.jar:?]
	at net.minecraft.client.MinecraftClient.method_29338(MinecraftClient.java:701) ~[minecraft-merged-ffc470daea-1.21-net.fabricmc.yarn.1_21.1.21+build.9-v2.jar:?]
	at net.minecraft.util.Util.ifPresentOrElse(Util.java:597) ~[minecraft-merged-ffc470daea-1.21-net.fabricmc.yarn.1_21.1.21+build.9-v2.jar:?]
	at net.minecraft.client.MinecraftClient.method_53522(MinecraftClient.java:696) ~[minecraft-merged-ffc470daea-1.21-net.fabricmc.yarn.1_21.1.21+build.9-v2.jar:?]
	at net.minecraft.client.gui.screen.SplashOverlay.render(SplashOverlay.java:149) [minecraft-merged-ffc470daea-1.21-net.fabricmc.yarn.1_21.1.21+build.9-v2.jar:?]
	at net.minecraft.client.render.GameRenderer.render(GameRenderer.java:902) [minecraft-merged-ffc470daea-1.21-net.fabricmc.yarn.1_21.1.21+build.9-v2.jar:?]
	at net.minecraft.client.MinecraftClient.render(MinecraftClient.java:1285) [minecraft-merged-ffc470daea-1.21-net.fabricmc.yarn.1_21.1.21+build.9-v2.jar:?]
	at net.minecraft.client.MinecraftClient.run(MinecraftClient.java:882) [minecraft-merged-ffc470daea-1.21-net.fabricmc.yarn.1_21.1.21+build.9-v2.jar:?]
	at net.minecraft.client.main.Main.main(Main.java:256) [minecraft-merged-ffc470daea-1.21-net.fabricmc.yarn.1_21.1.21+build.9-v2.jar:?]
	at net.fabricmc.loader.impl.game.minecraft.MinecraftGameProvider.launch(MinecraftGameProvider.java:470) [fabric-loader-0.15.11.jar:?]
	at net.fabricmc.loader.impl.launch.knot.Knot.launch(Knot.java:74) [fabric-loader-0.15.11.jar:?]
	at net.fabricmc.loader.impl.launch.knot.KnotClient.main(KnotClient.java:23) [fabric-loader-0.15.11.jar:?]
	at net.fabricmc.devlaunchinjector.Main.main(Main.java:86) [dev-launch-injector-0.2.1+build.8.jar:?]
     */

        //MinecraftClient.getInstance().player.networkHandler.onEntityDamage(EntityDamageS2CPacket);


        //ClientPlayNetworking.registerGlobalReceiver(NetworkManager.DAMAGE_PACKET_ID, (client, handler, buf, responseSender) -> {
        //        //DamageIndicatorPacket packet = new DamageIndicatorPacket(buf);
        //        //Entity entity = client.world.getEntityById(packet.getEntityId());
        //        //Debug.logMessage(packet.getSource(), packet.isCrit());
//
//
        //});


        //CustomPayload.Id<EntityDamageS2CPacket.ID> p = new CustomPayload.Id(Identifier.of("1","altoclef"));
        //ClientPlayNetworking.registerReceiver(CustomPayload.Id(net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket.class), (payload, context) -> {
        //ClientPlayNetworking.registerReceiver(p, (payload, context) -> {
        //    Debug.logMessage("fjdkgdf" + payload.toString());
        //});
    }
//https://github.com/MeteorDevelopment/meteor-client/blob/a8483f82275a917122b82a8b6ef70e117ac729de/src/main/java/meteordevelopment/meteorclient/utils/network/PacketUtils.java#L27
    public static void handleDamagePacket(EntityDamageS2CPacket packet) {

        World world = MinecraftClient.getInstance().world;
        DamageSource dmgSource = packet.createDamageSource(world);
        if(dmgSource.getSource() != null){
            Debug.logMessage("DEBUG DamagedName C1" + dmgSource.getSource().getName().getString());
        }
        if(world.getEntityById(packet.entityId()) != null){
            Debug.logMessage("DEBUG DamagedName C2" + world.getEntityById(packet.entityId()).getName().getString());
        }
        //Debug.logMessage("Entity took " + " damage" + packet.getPacketId().toString());
        //Debug.logMessage("entityId " + packet.entityId());
        //Debug.logMessage("sourceCauseId " + packet.sourceCauseId());
        //Debug.logMessage("sourceDirectId " + packet.sourceDirectId());
        //Debug.logMessage("sourceType " + packet.sourceType().getIdAsString());
        //// generic ALL minecraft:on_fire / on_fire
        //if(packet.sourcePosition().isPresent()) {
        //    Debug.logMessage("sourcePos " + packet.sourcePosition().get().toString());
        //}
//
        //

        // works only in singleplayer
        //if(dmgSource.getAttacker() != null && dmgSource.getSource() != null) {
        //    Debug.logMessage(dmgSource.getSource().getName().getString());
        //    Debug.logMessage("Attacker " + dmgSource.getAttacker().getName().getString());
        //    Debug.logMessage("Exhastion? " +dmgSource.getExhaustion());
        //    Debug.logMessage("Type " +dmgSource.getType().toString());
        //    Debug.logMessage("Pos " +dmgSource.getPosition().toString());
        //}
    }

}
