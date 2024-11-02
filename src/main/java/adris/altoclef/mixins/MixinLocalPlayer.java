package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.ChangeHealthEvent;
import adris.altoclef.eventbus.events.ClientDamageEvent;
import adris.altoclef.eventbus.events.ClientHandSwingEvent;
import adris.altoclef.util.helpers.MouseMoveHelper;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinLocalPlayer extends AbstractClientPlayerEntity {
    private float _oldHealth = 20;
    public MixinLocalPlayer(ClientWorld world, GameProfile profile, PlayerPublicKey publicKey) {
        super(world, profile);
    }

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

    @Inject(method="damage", at=@At("HEAD"))
    public void onPlayerDamage(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
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
}