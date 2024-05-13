package adris.altoclef.mixins;

import adris.altoclef.Debug;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "damage", at = @At("HEAD"))
    public void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> ci) {
        //Debug.logMessage("SUKA MAT EBALLL");
        //if (amount > 0 && ((Object) this) instanceof LivingEntity) {
        //    if (((Object) this) instanceof PlayerEntity) {
        //        // If the entity receiving damage is a player, cast it to a PlayerEntity and do something.
        //        PlayerEntity player = (PlayerEntity) ((Object) this);
        //        //System.out.println("Player " + player.getName().getString() + " received " + amount + " damage from " + source.getName().getString());
        //    } else {
        //        // If the entity receiving damage is not a player, cast it to a LivingEntity and do something.
        //        LivingEntity livingEntity = (LivingEntity) ((Object) this);
        //        //System.out.println(livingEntity.getName().getString() + " received " + amount + " damage from " + source.getName().getString());
        //    }
        //}
    }
}
