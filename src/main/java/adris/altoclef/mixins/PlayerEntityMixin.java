package adris.altoclef.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ServerPlayerEntity.class)
public abstract class PlayerEntityMixin extends  PlayerCollidesWithEntityMixin {
//public final class PlayerEntityMixin {
    PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }
    @Inject(method = "damage", at = @At("RETURN"))
    private void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        //Debug.logMessage("PlayerEntityMixin урон получен  ЧО БАБККЕ ААА!!!!!");
    }
    @Inject(method = "attack", at = @At("HEAD"))
    public void onPlayerDamage(Entity target, CallbackInfo ci) {
        //Debug.logMessage("PlayerEntityMixin урон получен "+target.getName());
        //EventBus.publish(new EntityDamageEvent(source,amount));
        //if (this instanceof PlayerEntity) {
        //    // Player is taking damage
        //    // Insert your code here
        //}
    }
}
///////@Mixin(PlayerEntity.class)
///////public abstract class PlayerEntityMixin extends Entity {
///////
///////    public PlayerEntityMixin(EntityType<?> entityType, World world) {
///////        super(entityType, world);
///////    }
///////
///////    @Redirect(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
///////    public boolean onDamage(PlayerEntity self, DamageSource source, float amount) {
///////        if (source.getAttacker() instanceof ServerPlayerEntity) {
///////            ServerPlayerEntity attacker = (ServerPlayerEntity) source.getAttacker();
///////            // Handle player damage by another player here
///////        }
///////        return self.applyDamage(source, amount);
///////    }
///////
///////}