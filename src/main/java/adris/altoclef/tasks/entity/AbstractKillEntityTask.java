package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClef;
import adris.altoclef.chains.DeathMenuChain;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.*;
import baritone.BaritoneProvider;
import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.utils.input.Input;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import adris.altoclef.Debug;
/**
 * Attacks an entity, but the target entity must be specified.
 */
public abstract class AbstractKillEntityTask extends AbstractDoToEntityTask {

    private static final double OTHER_FORCE_FIELD_RANGE = 2;

    // Not the "striking" distance, but the "ok we're close enough, lower our guard for other mobs and focus on this one" range.
    private static final double CONSIDER_COMBAT_RANGE = 10;

    private static final Item[] WEAPON_ITEMS = new Item[]{
            Items.DIAMOND_SWORD,
            Items.IRON_SWORD,
            Items.STONE_SWORD,
            Items.WOODEN_SWORD,
            Items.GOLDEN_SWORD,
            Items.DIAMOND_AXE,
            Items.IRON_AXE,
            Items.STONE_AXE,
            Items.WOODEN_AXE,
            Items.GOLDEN_AXE
    };

    public AbstractKillEntityTask() {
        this(CONSIDER_COMBAT_RANGE, OTHER_FORCE_FIELD_RANGE);
    }

    public AbstractKillEntityTask(double combatGuardLowerRange, double combatGuardLowerFieldRadius) {
        super(combatGuardLowerRange, combatGuardLowerFieldRadius);
    }

    public AbstractKillEntityTask(double maintainDistance, double combatGuardLowerRange, double combatGuardLowerFieldRadius) {
        super(maintainDistance, combatGuardLowerRange, combatGuardLowerFieldRadius);
    }

    public static void equipWeapon(AltoClef mod) {
        if (!mod.getFoodChain().isTryingToEat()){
        for (Item item : WEAPON_ITEMS) {
            if (mod.getItemStorage().hasItem(item)) {
                mod.getSlotHandler().forceEquipItem(item);
                return;
            }
        }}
    }
    boolean _threadRunning = false;
    @Override
    protected Task onEntityInteract(AltoClef mod, Entity entity) {
        //Debug.logMessage("Navodka on");
        boolean LOS_Close2 = LookHelper.cleanLineOfSight(entity.getEyePos(),5.0);

        if (LOS_Close2){
            LookHelper.SmoothLookAt(mod,0.05f,true,entity);

            if (WorldHelper.isHellHole(mod,entity.getBlockPos())){
                //Debug.logMessage("Цель над пропастью!");
            }else{
                boolean RotatedJump = entity.squaredDistanceTo(mod.getPlayer()) < 3.2 * 3.2;
                KillAuraHelper.GoJump(mod,RotatedJump);
            }

        }
        Task[] _ztask = {null};
        if (!_threadRunning){
        new Thread(() -> {


                //Debug.logMessage("LOLLZ: " + LookHelper.cleanLineOfSight(entity.getPos(),5.0) + " " + entity.getEntityName().toString());
                _threadRunning = true;
                float hitProg = 0;
                if(DeathMenuChain.ServerIp.equals("mc.vimemc.net")) { //||DeathMenuChain.ServerIp == "mc.mineblaze.net"
                    hitProg = 1;
                    //if(!KillAuraHelper.ElapsedPvpCD()){
                        //    hitProg = 0;
                        //    }
                    //else {
                        //    Debug.logMessage("Click!"+DeathMenuChain.ServerIp+"<");
                        //    hitProg = 1;
                        //    KillAuraHelper.ResetPvpCD();}
                    //
                }
                else hitProg = mod.getPlayer().getAttackCooldownProgress(1-1.5f*(float)Math.random());//-0.2f+(float)Math.random()*0.4f);// НОРМ РАБОТАЛО
            //mod.getPlayer().getAtt
                //entity.is
                        //LivingAttackEvent

            // Equip weapo
                //for(ItemStack item : entity.getItemsHand()){
                //    if(item.getItem() == Items.SHIELD)
                //        Debug.logMessage("У ЦЕЛИ ЩИТ!");
                //}
            equipWeapon(mod);


            if (hitProg >= 0.99) {
                //if(mod.getClientBaritone().getCustomGoalProcess().isActive()){
                //    KillAuraHelper.TimerStop();
                //}
                boolean attacked = false;

                //Debug.logMessage("attacked: " + attacked);
                boolean LOS_Close = LookHelper.cleanLineOfSight(entity.getEyePos(),5.0);

                if (LOS_Close){
                    try {
                        attacked = mod.getControllerExtras().attack(entity, false); //!!!! java.lang.ArrayIndexOutOfBoundsException: Index -1 out of bounds for length 2
                    }catch (Exception e){
                        Debug.logWarning("!!! ERROR WHEN ATTACKING !!! [OFTEN CRASH AFTER THAT!!!!!!!!!]");
                        e.printStackTrace();
                    }
                    //if(mod.getClientBaritone().getCustomGoalProcess().isActive()){
                    //    //Debug.logMessage("Baritone disabled");
                    //    //mod.getClientBaritone().getPathingBehavior().forceCancel();
                    //    mod.getClientBaritone().getCustomGoalProcess().setGoalAndPath(null);
                    //    //mod.getClientBaritone().getPathingControlManager().cancelEverything();
                    //}

                    //mod.getClientBaritone().getCustomGoalProcess().set
                }

                if (attacked == true){
                   // //mod.getClientBaritone().getCommandManager().execute("stop");
//
                   // //KillAuraHelper.GoJump(mod,true);
                   // double sleeptime = 0.05+Math.random()*0.8;
//
                   // new Thread(() -> {
                   //     if(WorldHelper.inRangeXZ(entity.getPos(),mod.getPlayer().getPos(),1.5)) {
                   //         //mod.getInputControls().hold(Input.SNEAK);
                   //         //sleepSec(0.3);
                   //         //mod.getInputControls().release(Input.SNEAK);
                   //     }
                   //     else{
                   //         //mod.getInputControls().hold(Input.MOVE_FORWARD);
                   //         //mod.getInputControls().hold(Input.SPRINT);
                   //         //sleepSec(0.3);
                   //         //mod.getInputControls().hold(Input.JUMP);
                   //         //sleepSec(0.2);
                   //         //mod.getInputControls().release(Input.MOVE_FORWARD);
                   //         //mod.getInputControls().release(Input.SPRINT);
//
                   //         //mod.getInputControls().release(Input.JUMP);
                   //     }
//
                   // }).start();
                   // sleepSec(sleeptime);
                   // //Debug.logMessage("sleeping for" + sleeptime);
                }
                else{
                    if (!LOS_Close){
                        //Debug.logMessage("BlockPos" + mod.getClientBaritoneSettings().followRadius.value);

                       // Debug.logMessage("BlockPos" + mod.getClientBaritoneSettings().followRadius.value);
                        //mod.getClientBaritoneSettings().followRadius.value = oldVal;
                        //mod.getExtraBaritoneSettings().
                        //int oldVal = mod.getClientBaritoneSettings().followRadius.value;
                        //if(mod.getClientBaritoneSettings().followRadius.value != 1) {
                        //new Thread(() -> {
                        //        mod.getClientBaritoneSettings().followRadius.value = 1;
                        //    sleepSec(1);
                        //    mod.getClientBaritoneSettings().followRadius.value = oldVal;
                        //}).start();}
                        //mod.getClientBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(entity.getBlockPos()));
                        _ztask[0] = new GetToBlockTask(entity.getBlockPos());
                    }
                }
            }
            _threadRunning = false;
        }).start();
    }

        return _ztask[0];
    }
    private static void sleepSec(double seconds) {
        try {
            Thread.sleep((int) (1000 * seconds));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
