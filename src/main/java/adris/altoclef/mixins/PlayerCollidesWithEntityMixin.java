package adris.altoclef.mixins;

import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.PlayerCollidedWithEntityEvent;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerCollidesWithEntityMixin extends LivingEntity {

    PlayerCollidesWithEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }
    @Inject(method = "damage", at = @At("RETURN"))
    private void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!this.world.isClient && !cir.getReturnValueZ() && !((Object) this instanceof ServerPlayerEntity)) {
            Debug.logMessage("PlayerCollidesWith TTEYEЫЫЫЫЫЫЫЫЫЫЫ ХАХАХАХА ");
        }
        //Debug.logMessage("PlayerCollidesWith ЫЫЫЫЫЫЫЫЫЫЫ ХАХАХАХА ");
    }
    @Inject(method = "attack", at = @At("HEAD")) //attack(Lnet/minecraft/entity/Entity;)V
    private void onAttack(Entity target, CallbackInfo ci) {
        //Debug.logMessage("COLLISION ENT Я УДАРИЛ КОГО-ТО!"+target.getName().getString()); //ИГРОК УДАРИЛ КОГОТО ИВЕНТ!!!!!!!
        //if (target instanceof LivingEntity) {
        //    // Do something with the attacked entity
        //}
    }
    // Determines a collision between items/EXP orbs/other objects within "pickup" range.
    @Redirect(
            method = "collideWithEntity",
            at = @At(value="INVOKE", target="Lnet/minecraft/entity/Entity;onPlayerCollision(Lnet/minecraft/entity/player/PlayerEntity;)V")
    )
    private void onCollideWithEntity(Entity self, PlayerEntity player) {
        //Debug.logMessage("ЫЫЫЫЫЫЫЫЫЫЫ ХАХАХАХА ");
        // TODO: Less hard-coded manual means of enforcing client side access
        if (player instanceof ClientPlayerEntity) {
            EventBus.publish(new PlayerCollidedWithEntityEvent(player, self));
        }
        // Perform the default action.
        // TODO: Figure out a cleaner way. First re-read the mixin intro documentation again.
        self.onPlayerCollision(player);
    }
}
