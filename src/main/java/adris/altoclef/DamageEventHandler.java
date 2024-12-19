package adris.altoclef;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.util.Identifier;

public class DamageEventHandler {
    public static void registerDamagePacketReceiver() {
        //CustomPayload.Id<EntityDamageS2CPacket.ID> p = new CustomPayload.Id(Identifier.of("1","altoclef"));
        //ClientPlayNetworking.registerReceiver(CustomPayload.Id(net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket.class), (payload, context) -> {
        //ClientPlayNetworking.registerReceiver(p, (payload, context) -> {
        //    Debug.logMessage("fjdkgdf" + payload.toString());
        //});
    }
//https://github.com/MeteorDevelopment/meteor-client/blob/a8483f82275a917122b82a8b6ef70e117ac729de/src/main/java/meteordevelopment/meteorclient/utils/network/PacketUtils.java#L27
    private static void handleDamagePacket(String lol, ClientPlayNetworking.Context ctx) {
        // Get the damage information
        //float damage = packet.;

        // Your custom damage handling logic here
        Debug.logMessage("Entity took " + lol.toString() + " damage");

        // You can get more information about the damage from the packet
        // Such as damage type, source entity, etc.
    }
}
