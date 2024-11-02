package adris.altoclef.mixins;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow
    private ClientWorld world;

    @Inject(method = "onEntityAnimation", at = @At("HEAD"))
    public void onEntityAnimation(EntityAnimationS2CPacket packet, CallbackInfo ci) {
        //РАБОТАЕТ все анимации ударов ловит! //НО не видит аттак обидчика((

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
