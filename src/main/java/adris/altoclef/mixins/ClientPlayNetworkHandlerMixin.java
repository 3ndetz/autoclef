package adris.altoclef.mixins;

import adris.altoclef.DamageEventHandler;
import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.AnimEvent;
import adris.altoclef.eventbus.events.DamageEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow
    private ClientWorld world;


    //@Inject(method = "onEntityAttributes", at = @At("HEAD"))
    //private void onEntityAttributes(EntityAttributesS2CPacket packet, CallbackInfo ci) {
        //Debug.logObject(packet);
        //codec entries
        // update max health, movement speed
        //for (EntityAttributesS2CPacket.Entry entry : packet.getEntries()) {
        //    Debug.logObject(entry);
        //    Debug.logObject(entry.attribute().getIdAsString());
        //    Debug.logObject(entry.attribute().value());
        //}

    // }

    // FOR BLOCKS (when beating blocks)
    //@Inject(method = "onPlayerActionResponse", at = @At("HEAD"))
    //private void onPlayerActionResponse(PlayerActionResponseS2CPacket packet, CallbackInfo ci) {
    //    Debug.logObject(packet);
    //}

    // THIS CLIENT ONLY
    @Inject(method = "onEndCombat", at = @At("HEAD"))
    private void onEndCombat(EndCombatS2CPacket packet, CallbackInfo ci) {
        //Debug.logObject(packet);
    }
    // THIS CLIENT ONLY
    // FIRES, waits 3 sec, then runs combat end
    @Inject(method = "onEnterCombat", at = @At("HEAD"))
    private void onEnterCombat(EnterCombatS2CPacket packet, CallbackInfo ci) {
        //Debug.logObject(packet);
    }

    @Inject(method = "createEntity", at = @At("HEAD"))
    private void createEntity(EntitySpawnS2CPacket packet, CallbackInfoReturnable cir) {
        // VERY OFTEN WORK
        //Debug.logObject(packet);
    }

    // client only?
    //@Inject(method = "onPlayerSpawnPosition", at = @At("HEAD"))
    //private void onPlayerSpawnPosition(PlayerSpawnPositionS2CPacket packet, CallbackInfo ci) {
    //    Debug.logObject(packet);
    //}


    //@Inject(method = "onHealthUpdate", at = @At("HEAD"))
    //private void onHealthUpdate(HealthUpdateS2CPacket packet, CallbackInfo ci) {
    //    // ONLY SELF UPDATE
    //    Debug.logObject(packet);
    //}

    // only on ply logout FULL from tab
    @Inject(method = "onPlayerRemove", at = @At("HEAD"))
    private void onPlayerRemove(PlayerRemoveS2CPacket packet, CallbackInfo ci) {
        //Debug.logObject(packet);
    }

    // THIS CLIENT ONLY
    //@Inject(method = "onPlayerRespawn", at = @At("HEAD"))
    //private void onPlayerRespawn(PlayerRespawnS2CPacket packet, CallbackInfo ci) {
    //    Debug.logObject(packet);
    //}

    @Inject(method = "onEntityDamage", at = @At("HEAD"))
    private void onEntityDamage(EntityDamageS2CPacket packet, CallbackInfo ci) {
        // WOOOOORKIIIIING !!!!!!!!!!!!!!! but doubling event (x2 times in a moment)
        //DamageEventHandler.handleDamagePacket(packet);
        Entity entity = world != null ? world.getEntityById(packet.entityId()) : null;
        if(entity != null) {
            EventBus.publish(new DamageEvent(entity));
        }
        //Debug.logObject(packet);
        //int entityId = packet.entityId();
        //int attackerId = packet.sourceCauseId();
        //packet.createDamageSource()
        //Optional<Vec3d> pos = packet.sourcePosition();
        //
        //float damage = packet.sourcePosition();
        //Debug.logMessage("Entity {} took {} damage (Mixin)"), entityId, damage);
    }


    //DOESN'T GET SELF ANIMATIONS!
    @Inject(method = "onEntityAnimation", at = @At("HEAD"))
    public void onEntityAnimation(EntityAnimationS2CPacket packet, CallbackInfo ci) {
        Entity entity = world != null ? world.getEntityById(packet.getEntityId()) : null;
        if(entity != null) {
            EventBus.publish(new AnimEvent(entity, packet.getAnimationId()));
        }
        //РАБОТАЕТ все анимации ударов ловит (кроме своих)
        //Debug.logObject(packet);
        //Entity entity = world.getEntityById(packet.getId());
        ////world.getServer().
        ////Debug.logMessage("OnEntityAnimation");
        //if(packet.getAnimationId()==0||packet.getAnimationId()==1||packet.getAnimationId()==2||packet.getAnimationId()==4||packet.getAnimationId()==5){
        //    if (entity != null) {
        //        Debug.logMessage("AnimationID="+packet.getAnimationId()+";\nEntName="+entity.getName().getString());
        //        if (entity.isPlayer()){
        //            LivingEntity ply = (LivingEntity) entity;
        //            LivingEntity attacker = ply.getAttacker();
        //            LivingEntity attacking = ply.getAttacking();
        //            Debug.logMessage("ANIMATION id1 ply="+ply.getName().getString());
        //            if(attacking!=null){
        //                Debug.logMessage("attackingName="+attacking.getName().getString());
        //            }
        //            if(attacker!=null){
        //                Debug.logMessage("attackerName="+attacker.getName().getString());
        //            }
        //            //
        //        }
//
//
        //    }
        //}
    }

    //@Inject(method = "onEntityDamage", at = @At("HEAD"))
    //public void test(EntityDamageS2CPacket packet, CallbackInfo ci) {
    //    Entity entity = world.getEntityById(packet.entityId());
    //    if (entity != null) {
    //        if (entity.isPlayer()) {
    //            System.out.println("Cause: " + world.getEntityById(packet.sourceCauseId()));
    //        }
    //    }
    //}
}
