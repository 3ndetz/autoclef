package adris.altoclef.mixins;


import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.ChangeHealthEvent;
import adris.altoclef.eventbus.events.ClientDamageEvent;
import adris.altoclef.eventbus.events.ClientHandSwingEvent;
import adris.altoclef.eventbus.events.EntityDamageEvent;
import adris.altoclef.util.helpers.MouseMoveHelper;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinLocalPlayer extends AbstractClientPlayerEntity {

    public MixinLocalPlayer(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }
    //@Inject(method="heal", at=@At("HEAD"))//@Inject(method="damage", at=@At("HEAD"))
    //private void onAttack(Entity target, CallbackInfo info) {
    //    lastAttacker = null;
    //}
//
    //@Inject(method = "attack", at = @At("TAIL"))
    //private void onAttackEnd(Entity target, CallbackInfo info) {
    //    if (lastAttacker != null) {
    //        Debug.logMessage("**** **** **** АТТАКЕР **** *****");
    //        // Do something with lastAttacker
    //    }
    //}
//
    //@Inject(method = "handleDamage", at = @At("HEAD"))
    //private void onHandleDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
    //    if (source.getAttacker() != null) {
    //        lastAttacker = source.getAttacker();
    //    }
    //}
    private float _oldHealth = 20;
    @Inject(method="damage", at=@At("HEAD"))
    public void onPlayerDamage(DamageSource source, float amount,CallbackInfoReturnable<Float> cir) {
        //Debug.logMessage("Bump!"+source.getName()+amount);
        EventBus.publish(new ClientDamageEvent(source,amount));
    }
    @Inject(method="swingHand", at=@At("HEAD"))
    public void onHandSwing(Hand hand, CallbackInfo ci) {
        if(hand.toString()=="MAIN_HAND")
            EventBus.publish(new ClientHandSwingEvent(hand));
    }
    @Inject(method="updateHealth", at=@At("HEAD"))
    public void onHealthChange(float health,CallbackInfo ci) {

        //Debug.logMessage("Здоровье изменилось: "+_oldHealth+" -> "+health);
        EventBus.publish(new ChangeHealthEvent(_oldHealth,health));
        _oldHealth = health;
    }
    //Debug.logMessage("MIXIN LOCAL PLAYER ПОЛУЧИЛ УРОН! "+amount); //ИГРОК ПОЛУЧИЛ УРОН ИВЕНТ!!!!!!!
    //EventBus.publish(new ClientDamageEvent(source,amount));

    @Inject(method="getPitch", at=@At("RETURN"), cancellable = true)
    public void getPitch(float tickDelta, CallbackInfoReturnable<Float> cir) {
        if(MouseMoveHelper.RotationEnabled)
            cir.setReturnValue(super.getPitch(tickDelta));
    }

    @Inject(method="getYaw", at=@At("RETURN"), cancellable = true)
    public void getYaw(float tickDelta, CallbackInfoReturnable<Float> cir) {
        if(MouseMoveHelper.RotationEnabled)
            cir.setReturnValue(super.getYaw(tickDelta));
    }
    private Entity lastAttacker;
}