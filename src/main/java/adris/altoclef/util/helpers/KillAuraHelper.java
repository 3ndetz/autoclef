package adris.altoclef.util.helpers;
import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;

public abstract class KillAuraHelper {
    static  long _timeExpires = 0;
    static double _rvalue = -1;
    public static double initialYaw = -1f;
    public static double GetNextRandomY() {


        if (System.currentTimeMillis() >= _timeExpires) {
            _timeExpires = System.currentTimeMillis() + 55;
            _rvalue = -1.0 + Math.random()*1.0;
        }


        return _rvalue;//-0.2+((-0.5+Math.random())*0.01);
    }
    public static long TimerStartTime = -1;
    public static long TimerStartExpires = 0;
    public static long TimerGoing = 0;
    static long _lastRequestTime = -1;
    public static float YawSpeed = 1;
    public static float PitchSpeed = 1;
    private static final TimerGame _inPvpAction = new TimerGame(0);
    private static final TimerGame _CooldownFor18 = new TimerGame(0.07);
    public static boolean ElapsedPvpCD(){
        return _CooldownFor18.elapsed();
    }
    public static void ResetPvpCD(){
        _CooldownFor18.setInterval(0.02+(Math.random()/30));
        _CooldownFor18.reset();

    }
    public static void TimerStop()
    {

        if(TimerStartTime!=-1) {
            //Debug.logMessage("Killaura Stopped Yaw" + initialYaw + " time was " + (System.currentTimeMillis() - TimerStartTime));
            TimerStartTime = -1;
            initialYaw = -1;
            _lastRequestTime = -1;
            _inPvpAction.reset();
        }
    }
    public static boolean IsInBattle(){
        _inPvpAction.setInterval(1);
        return _inPvpAction.elapsed();
    }
    public static boolean TimerStart(float initialYaww)
    {
        //Debug.logMessage("Killaura TimerStartTime"+TimerStartTime);
        //Debug.logMessage("Killaura nowtime"+System.currentTimeMillis());
        IsInBattle();
        if(TimerStartTime==-1){
            //_inPvpAction.setInterval(1);

            YawSpeed = 0;
            PitchSpeed = 0;

            initialYaw = initialYaww;
            TimerStartTime = System.currentTimeMillis();
            TimerStartExpires = TimerStartTime+100;
            _lastRequestTime = TimerStartTime;
            TimerGoing = 0;
            return true;
            //Debug.logMessage("Killaura Started Yaw"+initialYaw);
        }else {
            TimerGoing = (System.currentTimeMillis() - TimerStartTime);
            if (System.currentTimeMillis() > _lastRequestTime+100) {
                //Debug.logMessage("Killaura TimerAutoStopped" + (System.currentTimeMillis()-_lastRequestTime));
                //Debug.logMessage("Killaura STOP" + initialYaw);
                TimerStop();
            }
            _lastRequestTime = System.currentTimeMillis();
            return false;
        }


    }
    public static long JumpTimerStarted = -1;
    //public static boolean EmergencyStop(AltoClef mod){
    //    return true;
    //    mod.getInputControls().
    //}
    public static void GoJump(AltoClef mod, boolean rotated)
    {
        if (JumpTimerStarted==-1) {
            JumpTimerStarted = System.currentTimeMillis();
        }else if (System.currentTimeMillis()>JumpTimerStarted+810){
            boolean doJump = mod.getPlayer().isOnGround();
            boolean HighSpeed = mod.getPlayer().getVelocity().horizontalLengthSquared()>0.025 ;
        new Thread(() -> {
            mod.getInputControls().hold(Input.SPRINT);
                mod.getInputControls().hold(Input.MOVE_FORWARD);
            if( HighSpeed ){
                //Debug.logMessage("GoJump onGround?"+mod.getPlayer().isOnGround() + "HS?"+HighSpeed+" Speed "+mod.getPlayer().getVelocity().horizontalLengthSquared());
                mod.getInputControls().hold(Input.JUMP);}
            sleepSec(0.3);
            if (rotated)
                mod.getInputControls().hold(Input.MOVE_RIGHT);

            if(doJump & !HighSpeed )
                mod.getInputControls().hold(Input.JUMP);
            sleepSec(0.5);
            if (rotated)
                mod.getInputControls().release(Input.MOVE_RIGHT);
            mod.getInputControls().release(Input.MOVE_FORWARD);
            mod.getInputControls().release(Input.SPRINT);
            mod.getInputControls().release(Input.JUMP);
        }).start();
            JumpTimerStarted = -1;
        }
    }
    private static void sleepSec(double seconds) {
        try {
            Thread.sleep((int) (1000 * seconds));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
