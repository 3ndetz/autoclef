package adris.altoclef.mixins;

import adris.altoclef.Debug;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo info) {
        //реагирует только на собственные и только удары. Лук не реагирует. Только когда ты кого-то атакуешь ближней атакой любую сущность даже если урон не прошел
        //Debug.logMessage("onAttackEntityCatch!");
        //if(player!=null && target !=null && info != null) {
        //    Debug.logMessage("onAttackEntity\nplayer=" + player.getName().getString() + ";\ntarget=" + target.getName().getString() + ";\ninfo=" + info.toString() + ";");
        //}
    }
}
