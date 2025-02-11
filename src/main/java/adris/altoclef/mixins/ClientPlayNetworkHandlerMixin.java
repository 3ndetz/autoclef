package adris.altoclef.mixins;

import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.AnimEvent;
import adris.altoclef.eventbus.events.DamageEvent;
import adris.altoclef.eventbus.events.multiplayer.ItemUseEvent;
import adris.altoclef.eventbus.events.multiplayer.ProjectileEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.s2c.play.*;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow
    private ClientWorld world;


    // bow event journey
/*
    onEntityTrackerUpdate
    caught pickup, bow state change, pose change

    * Дёрн, values [SerializedEntry[id=8, serializer=net.minecraft.entity.data.TrackedDataHandlerRegistry$1@2785cdde, value=1 minecraft:grass_block]]
      NetTyan, values [SerializedEntry[id=6, serializer=net.minecraft.entity.data.TrackedDataHandler$$Lambda/0x000001ffdf8ce778@3e1b7b5d, value=CROUCHING]]
    *
    * arrow shoot
    * [SerializedEntry[id=0, serializer=net.minecraft.entity.data.TrackedDataHandler$$Lambda/0x000001ffdf8ce778@5ca5c880, value=8]]
      [SerializedEntry[id=0, serializer=net.minecraft.entity.data.TrackedDataHandler$$Lambda/0x000001ffdf8ce778@5ca5c880, value=0]]
    *

    bow start using
    [SerializedEntry[id=8, serializer=net.minecraft.entity.data.TrackedDataHandler$$Lambda/0x000001ffdf8ce778@5ca5c880, value=1]]

    bow release
    [SerializedEntry[id=8, serializer=net.minecraft.entity.data.TrackedDataHandler$$Lambda/0x000001ffdf8ce778@5ca5c880, value=0]]

    */
    /*

    ERROR!! FIX IMMIDEATELY!!!!

    Failed to handle packet net.minecraft.network.packet.s2c.play.BundleS2CPacket@b54e0c3
 java.lang.ClassCastException: class net.minecraft.item.ItemStack cannot be cast to class java.lang.Byte (net.minecraft.item.ItemStack is in unnamed module of loader net.fabricmc.loader.impl.launch.knot.KnotClassLoader @1817d444; java.lang.Byte is in module java.base of loader 'bootstrap')

     */
    @Inject(method = "onEntityTrackerUpdate", at = @At("TAIL"))
    public void onEntityTrackerUpdate(EntityTrackerUpdateS2CPacket packet, CallbackInfo ci) {
        try {
            Entity entity = world != null ? world.getEntityById(packet.id()) : null;
            if (entity != null) {
                // Keep existing debug
                //Debug.logMessage("EntityTrackerUpdateS2CPacket: " + entity.getName().getString()
                //+ ", values " + packet.trackedValues().toString());

                if ((entity instanceof PlayerEntity player && player.getName() != null) ||
                        (entity instanceof ProjectileEntity)) {
                    // if this's bow or projectile or itemuse (if player)?
                    if (packet.trackedValues() != null &&
                            packet.trackedValues().size() == 1 &&
                            packet.trackedValues().getFirst() != null &&
                            packet.trackedValues().getFirst().id() == 8 &&
                            packet.trackedValues().getFirst().value() instanceof Byte byteVal
                    ) {
                        int value = (byte) byteVal & 1;
                        if (value >= 0) {
                            boolean released;
                            if (value > 0) {
                                //Debug.logMessage("ITEM START USING: " + entity.getName().getString());
                                released = false;

                            } else {  // if (value == 0) {
                                released = true;
                                // if bow - bow release
                                //post here animation event
                                //Debug.logMessage("ITEM STOP USING : " + entity.getName().getString());
                            }
                            if (entity instanceof PlayerEntity)
                                EventBus.publish(new ItemUseEvent(entity, released));
                            if (entity instanceof ProjectileEntity projectile && projectile.getPos() != null)
                                EventBus.publish(new ProjectileEvent(projectile, released));
                        }
                    }
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    @Inject(method = "onPlaySound", at = @At("TAIL"))
    public void onPlaySound(PlaySoundS2CPacket packet, CallbackInfo ci) {
        // normal working, catching bow
        //Debug.logMessage("PlaySoundS2CPacket: " + packet.getX() +  packet.getY() + packet.getZ() + packet.getSound() + packet.getCategory() + packet.getVolume() + packet.getPitch() + packet.getSeed());
    }

/*  // worked for npc's idk what's this but not bow in any way 100% (i tested)
    @Inject(method = "onEntityStatus", at = @At("TAIL"))
    public void onEntityStatus(EntityStatusS2CPacket packet, CallbackInfo ci) {
        Entity entity = world != null ? packet.getEntity(world) : null;
        if(entity != null && entity.getName() != null) {
            Debug.logMessage("EntityStatusS2CPacket: " + packet.getStatus() + " from entity " + entity.getName().getString());
        }
    }
*/

/* // not worked in any way
    @Inject(method = "onProjectilePower", at = @At("TAIL"))
    public void onProjectilePower(ProjectilePowerS2CPacket packet, CallbackInfo ci) {
        Entity entity = world != null ? world.getEntityById(packet.getEntityId()) : null;
        if (entity != null && entity.getName() != null)
            Debug.logMessage("[DEBUG MIXIN] Got projectile power " + packet.getAccelerationPower() + " from entity " + entity.getName().getString());

    }
*/
    // not work in any way idk why
    //@Inject(method = "onPlaySoundFromEntity", at = @At("TAIL"))
    //public void onPlaySoundFromEntity(PlaySoundFromEntityS2CPacket packet, CallbackInfo ci) {
    //    Entity entity = world != null ? world.getEntityById(packet.getEntityId()) : null;
    //    if (entity != null && packet.getSound() != null) {
    //        if (entity instanceof PlayerEntity player && player.getName() != null)
    //            Debug.logMessage("[DEBUG SOUND MIXIN] Got sound "+ packet.getSound().toString() + " from entity" + player.getName().getString());
    //    }
    //}

// get text / chat / title events

    // TODO all working proper, but saving color codes...
    @Inject(method = "onOverlayMessage", at = @At("TAIL"))
    public void onOverlayMessage(OverlayMessageS2CPacket packet, CallbackInfo ci) {
        if(packet != null && packet.text() != null){
            //Debug.logMessage("OverlayMessageS2CPacket: " + packet.text().getString());
        }
    }
    @Inject(method = "onTitle", at = @At("TAIL"))
    public void onTitle(TitleS2CPacket packet, CallbackInfo ci) {
        if(packet != null && packet.text() != null){
            //EventBus.publish(new CustomMessage(entity));
            //Map<String,String> messageDict = new HashMap<>();
            ////if()
            //messageDict.put("parse_type","unparsed");
            //messageDict.put("message_type","chat");
            //messageDict.put("msg",packet.text().getString());
            //Debug.logMessage("TitleS2CPacket: " + packet.text().getString());
        }
    }
    @Inject(method = "onSubtitle", at = @At("TAIL"))
    public void onSubtitle(SubtitleS2CPacket packet, CallbackInfo ci) {
        if(packet != null && packet.text() != null){
            //Debug.logMessage("SubtitleS2CPacket: " + packet.text().getString());
        }
    }


    // combat / damage / attack events

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
    @Inject(method = "onEndCombat", at = @At("TAIL"))
    private void onEndCombat(EndCombatS2CPacket packet, CallbackInfo ci) {
        //Debug.logObject(packet);
    }
    // THIS CLIENT ONLY
    // FIRES, waits 3 sec, then runs combat end
    @Inject(method = "onEnterCombat", at = @At("TAIL"))
    private void onEnterCombat(EnterCombatS2CPacket packet, CallbackInfo ci) {
        //Debug.logObject(packet);
    }

    @Inject(method = "createEntity", at = @At("TAIL"))
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
    @Inject(method = "onPlayerRemove", at = @At("TAIL"))
    private void onPlayerRemove(PlayerRemoveS2CPacket packet, CallbackInfo ci) {
        //Debug.logObject(packet);
    }

    // THIS CLIENT ONLY
    //@Inject(method = "onPlayerRespawn", at = @At("HEAD"))
    //private void onPlayerRespawn(PlayerRespawnS2CPacket packet, CallbackInfo ci) {
    //    Debug.logObject(packet);
    //}

    @Inject(method = "onEntityDamage", at = @At("TAIL"))
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
    @Inject(method = "onEntityAnimation", at = @At("TAIL"))
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
