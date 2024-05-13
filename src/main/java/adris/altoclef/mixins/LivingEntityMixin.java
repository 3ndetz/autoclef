package adris.altoclef.mixins;

import adris.altoclef.Debug;
import adris.altoclef.eventbus.events.BlockBreakingEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.EntityDamageEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world){
        super(type,world);
    }
    //РАБОТАЕТ ТОЛЬКО ДЛЯ МОБОВ!

    @Inject(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setHealth(F)V", shift = At.Shift.AFTER))
    public void applyDamage(DamageSource source, float amount, CallbackInfo ci) {
        //Debug.logMessage("LIVINGENTEVENT " + MathHelper.ceil(amount) + source.getName() + source.getAttacker());
    }
    @Inject(method = "damage", at = @At("RETURN"))//at = @At("HEAD"))
    public void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> callback) {
        //Debug.logMessage("LIVING ENT урон получен "+source.getName());
        //EventBus.publish(new EntityDamageEvent(source,amount));
        //if (this instanceof PlayerEntity) {
        //    // Player is taking damage
        //    // Insert your code here
        //}
    }
    @Inject(at = @At("HEAD"), method = "onDeath")
    private void onDeath(DamageSource source, CallbackInfo ci) {
        // Handle player death event
        //Debug.logMessage("LIVING ENT *********");
    }
    @Inject(at = @At("HEAD"), method = "tryAttack")
    private void onAttack(Entity target, CallbackInfoReturnable<Boolean> callback) {
        // Handle player death event
        //Debug.logMessage("LIVING ENT АТАКОВАЛ ******");
    }
    ////@Inject(method = "attack", at = @At("HEAD"))
    ////public void onPlayerAttacked(Entity target,  CallbackInfo callback) {
    ////    if (target instanceof PlayerEntity) {
    ////        Debug.logMessage("матьебаллл апавпв");
    ////        // Handle player damage by another player event
    ////        // Here, you can call the attackEntityFrom method to damage the player
    ////        //float damageAmount = 10.0f; // Change this to the amount of damage you want to deal
    ////        //((PlayerEntity) target).attackEntityFrom(DamageSource.GENERIC, damageAmount);
    ////    }
    ////}
}
