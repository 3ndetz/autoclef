package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.Playground;
import adris.altoclef.mixins.LivingEntityMixin;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.WorldHelper;
import baritone.api.pathing.goals.Goal;
import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.utils.Rotation;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.mixin.gametest.MinecraftServerMixin;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageRecord;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.*;
import net.minecraft.util.math.BlockPos;
import adris.altoclef.util.helpers.LookHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import baritone.api.utils.BetterBlockPos;

import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.util.Optional;

/**
 * Do nothing.
 */
public class IdleTask extends Task {
    @Override
    protected void onStart(AltoClef mod) {
        Debug.logMessage("(EZZ: started)");
    }

    @Override
    protected Task onTick(AltoClef mod) {
        // Do nothing except maybe test code
        Playground.IDLE_TEST_TICK_FUNCTION(mod);
        //DamageSource dmgs = mod.getPlayer().getRecentDamageSource();
        ClientPlayerEntity bot = mod.getPlayer();
        LivingEntity living = (LivingEntity) bot;

        PlayerEntity srvply = (PlayerEntity) bot;
        //if(srvply.getDamageTracker(). != 43)
        //    Debug.logMessage("УРААААААУ2 ******"+srvply.getDamageTracker().getBiggestAttackerId());
        if(srvply.getAttacker() != null)
            Debug.logMessage("УРААААААУ *****");
        //if(srvply.getAttacking() != null)
        //    Debug.logMessage("АТАКУЕМЫЙ НАЙДЕН ********");
        LivingEntity att = living.getAttacker();
        //getLastAttacker(srvply);
        //Debug.logMessage("CEPB ");
        //Server
        //for (ServerPlayerEntity player: MinecraftClient.getInstance().getServer().getPlayerManager().getPlayerList()){
        //    Debug.logMessage("ИГРОКИ: "+player.toString());
        //}
        //РАБОЧИЙ СПОСОБ ПОЛУЧИТЬ СПИСОК ИГРОКОВ
        //for (PlayerListEntry ply : MinecraftClient.getInstance().getNetworkHandler().getPlayerList()){
        //    if(ply.getDisplayName() != null)
        //        Debug.logMessage("ИГРОК PLY "+ply.getDisplayName().getString());
        //    Debug.logMessage(ply.getProfile().getName().toString());
        //}
        Optional<LivingEntity> checkTracking = getEntityTarget(mod,"HyperMozgh");
        LivingEntity TrackingEnt;
        //getLastAttacker();
        if (MinecraftClient.getInstance().player.getServer() != null)
            Debug.logMessage("STRING SERVER"+MinecraftClient.getInstance().player.getServer().toString());
        if(checkTracking.isPresent()) {
            TrackingEnt = checkTracking.get();
                LivingEntity attacked = TrackingEnt.getAttacking();
                //Debug.logMessage("с***"+TrackingEnt.getAttacking());
                if (attacked != null) {
                    Debug.logMessage("Тракер атаковал " + attacked.toString());
                    if (attacked.getRecentDamageSource() != null && attacked.getRecentDamageSource().getSource() != null)
                        Debug.logMessage("Тракер атаковал " + attacked.toString());
                }
        }
        //mod.getPlayer().getDamageTracker().getEntity() //РАБОТАЕТ
        //LivingEntity att2 = mod.getPlayer().getAttacking(); // РАБОТАЕТ!!!!
        //Debug.logMessage("dolbaeb3 = " +mod.getPlayer().getLastAttackTime()); // РАБОТАЕТ!!!
        //living.getLastAttackTime() // ТОЖЕ ВРОДЕ КАК РОБИТ
        //if(att2 != null)
        //    Debug.logMessage("222УУУ с****ааааааааа");





        //mod.getEntityTracker().get
        ////Entity srcent;
        ////Entity attent;
        ////if(dmgs != null) {
        ////    srcent = dmgs.getSource();
        ////    if (srcent != null)
        ////        Debug.logMessage("dmgs = " + srcent.getName().getString());
        ////    attent = dmgs.getAttacker();
        ////    if (attent != null)
        ////        Debug.logMessage("dmgs = " + attent.getName().getString());
        ////    Debug.logMessage("name = " +dmgs.getName()+dmgs.getExhaustion());
        ////}

        //MinecraftClient.getInstance().player.getVelocity()
        //Debug.logMessage("(EZZ: " + mod.getMobDefenseChain().isActive() + ")");
        //mod.getBlockTracker().
               // baritone.api.pathing.goals
        //Goal goal = mod.getClientBaritone().getCustomGoalProcess().getGoal();
        //BetterBlockPos goalPos = goal.toBlockPos();
        ////LookHelper.SmoothLookDirectionaly(mod, 0.0075f);
        return null;
    }
    protected Optional<LivingEntity> getEntityTarget(AltoClef mod, String plyName) {
        if (mod.getEntityTracker().isPlayerLoaded(plyName)) {
            return mod.getEntityTracker().getPlayerEntity(plyName).map(LivingEntity.class::cast);
        }
        return Optional.empty();
    }
    //public static void getLastAttacker(PlayerEntity player) {
    //    DamageSource lastDamageSource = player.getRecentDamageSource();
    //    if (lastDamageSource != null) {
    //        System.out.println("Last attacker: " + lastDamageSource.getName());
    //    } else {
    //        System.out.println("No recent attacker found.");
    //    }
    //}
    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        Debug.logMessage("(EZZ: stopped)");
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        // Never finish
        //Debug.logMessage("(EZZ: finished)");
        return false;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof IdleTask;
    }

    @Override
    protected String toDebugString() {
        return "Idle";
    }
}
