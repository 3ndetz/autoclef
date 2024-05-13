package adris.altoclef.chains;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasks.fix.StuckFixingTask;
import adris.altoclef.tasks.movement.GetToXZTask;
import adris.altoclef.tasksystem.TaskChain;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.ui.MessagePriority;
import adris.altoclef.ui.MessageSender;
import adris.altoclef.util.helpers.InputHelper;
import adris.altoclef.util.time.TimerGame;
import adris.altoclef.util.time.TimerReal;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.text.TranslatableText;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class DeathMenuChain extends TaskChain {

    // Sometimes we fuck up, so we might want to retry considering the death screen.
    private final TimerReal _deathRetryTimer = new TimerReal(8);
    private ServerInfo _prevServerEntry = null;
    public static boolean _needUnStuckFix = false;
    public static boolean _needDisconnect = false;
    public static boolean _needToStopTasksOnReconnect = false;
    public static boolean _reJoinAfterDisconnect = false;
    private boolean _reconnecting = false;
    private final TimerReal _reconnectTimer = new TimerReal(4);
    private final TimerReal _commandDelayTimer = new TimerReal(2);
    private int _deathCount = 0;
    private Class _prevScreen = null;

    private final TimerGame _waitOnDeathScreenBeforeRespawnTimer = new TimerGame(2);

    public DeathMenuChain(TaskRunner runner) {
        super(runner);
    }
    public static boolean NeedtoStopTasksOnDeath = false;
    public static String ServerIp = "";

    private boolean shouldAutoRespawn(AltoClef mod) {
        return mod.getModSettings().isAutoRespawn();
    }

    private boolean shouldAutoReconnect(AltoClef mod) {
        return mod.getModSettings().isAutoReconnect();
    }

    @Override
    protected void onStop(AltoClef mod) {

    }

    @Override
    public void onInterrupt(AltoClef mod, TaskChain other) {

    }

    @Override
    protected void onTick(AltoClef mod) {

    }

    @Override
    public float getPriority(AltoClef mod) {
        //MinecraftClient.getInstance().getCurrentServerEntry().address;
//        MinecraftClient.getInstance().
        Screen screen = MinecraftClient.getInstance().currentScreen;

        // This might fix Weird fail to respawn that happened only once
        if (_prevScreen == DeathScreen.class) {
            if (_deathRetryTimer.elapsed()) {
                Debug.logMessage("(RESPAWN RETRY WEIRD FIX...)");
                _deathRetryTimer.reset();
                _prevScreen = null;
            }
        } else {
            _deathRetryTimer.reset();
        }
        // Keep track of the last server we were on so we can re-connect.
        if (AltoClef.inGame()) {
            _prevServerEntry = MinecraftClient.getInstance().getCurrentServerEntry();
            if (_prevServerEntry != null)
                ServerIp = _prevServerEntry.address.toString();
            //Debug.logMessage("servip "+ServerIp);
            //НОВАЯ ЧАСТЬ
            if (mod.getPlayer().hasStatusEffect(StatusEffects.INVISIBILITY))
                //Debug.logMessage("ЫЫ "+mod.getPlayer().getStatusEffect(StatusEffects.INVISIBILITY).getAmplifier());
                if (mod.getPlayer().getStatusEffect(StatusEffects.INVISIBILITY).getAmplifier()>= 3){
                    if (NeedtoStopTasksOnDeath) {
                        //mod.cancelUserTask();
                        mod.getMessageSender().enqueueChat("/hub", MessagePriority.TIMELY);
                        //AltoClef.getCommandExecutor().execute("@stop");
                        NeedtoStopTasksOnDeath = false;
                        _commandDelayTimer.reset();
                        if(_commandDelayTimer.elapsed()){
                            _commandDelayTimer.reset();
                            Debug.logMessage("ВАЛИМ! 111");
                            //AltoClef.getCommandExecutor().execute("@stop");

                        }
                    }

                }
        }
        if (screen instanceof DeathScreen) {
            if(NeedtoStopTasksOnDeath){
                //mod.cancelUserTask();
                mod.cancelUserTask();
                //AltoClef.getCommandExecutor().execute("@stop");
                NeedtoStopTasksOnDeath = false;
                _commandDelayTimer.reset();
            }
            //if(_commandDelayTimer.elapsed()){
                //    _commandDelayTimer.reset();
                //    Debug.logMessage("ВАЛИМ! 222");
                //    mod.getMessageSender().enqueueChat("/hub", MessagePriority.ASAP);
                //}
            if (_waitOnDeathScreenBeforeRespawnTimer.elapsed()) {
                _waitOnDeathScreenBeforeRespawnTimer.reset();
                if (shouldAutoRespawn(mod)) {
                    _deathCount++;
                    Debug.logMessage("РЕСПАВН... (смерть #" + _deathCount + ")"); //TRS ESPAWNING... (this is death #" + _deathCount + "
                    assert MinecraftClient.getInstance().player != null;
                    MinecraftClient.getInstance().player.requestRespawn();
                    MinecraftClient.getInstance().setScreen(null);
                } else {
                    // Cancel if we die and are not auto-respawning.
                    mod.cancelUserTask();
                }
            }
        } else {
            _waitOnDeathScreenBeforeRespawnTimer.reset();
            //if(InputHelper.isKeyPressed(71)){
            //    Debug.logMessage("ReconnectState >> instanceof MultiplayerScreen="+(screen instanceof MultiplayerScreen)
            //            +" _reconnecting="+_reconnecting+" _reconnectTimer="+_reconnectTimer.elapsed()+" _reconnectTimerDur="+_reconnectTimer.getDuration());
            //}
            if (screen instanceof DisconnectedScreen) {
                if (shouldAutoReconnect(mod)) {
                    Debug.logMessage("RECONNECTING: Going to Multiplayer Screen");
                    _reconnecting = true;
                    _reconnectTimer.reset();
                    MinecraftClient.getInstance().setScreen(new MultiplayerScreen(new TitleScreen()));
                } else {
                    // Cancel if we disconnect and are not auto-reconnecting.
                    mod.cancelUserTask();
                }

            } else if (_needUnStuckFix) {
                if(AltoClef.inGame()) {
                    ServerInfo srv = MinecraftClient.getInstance().getCurrentServerEntry();

                    if (srv != null){
                        Debug.logMessage("Starting UNSTUCK FIX >> server="+srv.address.toString());
                        _prevServerEntry = srv;
                    }

                    Debug.logMessage("OPEN GAME MENU");
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.setScreen(new GameMenuScreen(true));
                    if(_prevServerEntry!=null) {
                    }else{}//_prevServerEntry.address = "mc.vimemc.net";}
                    this.disconnect(client);
                    //_reconnecting = true;
                    //_reconnectTimer.reset();
                    //MinecraftClient.getInstance().setScreen(new MultiplayerScreen(new TitleScreen()));
                    SelectWorldScreen worldScreen = new SelectWorldScreen(new TitleScreen());
                    MinecraftClient.getInstance().setScreen(worldScreen);
                    Debug.logMessage("worldScreen.isMouseOver() " + worldScreen.isMouseOver(0,0));
                    Debug.logMessage("worldScreen.changeFocus(true) " + worldScreen.changeFocus(true));
                    double x = worldScreen.width / 2 - 154; double y = worldScreen.height - 52;
                    worldScreen.mouseClicked(x,y,0);
                    worldScreen.mouseReleased(x,y,0);
                    if(worldScreen.hoveredElement(x,y).isPresent()){
                        Element hoveredElement =  worldScreen.hoveredElement(x,y).get();
                        //Debug.logMessage("worldScreen.hoveredElement(x,y) "+hoveredElement);
                        //hoveredElement.mouseClicked(x,y,0);
                        //hoveredElement.mouseReleased(x,y,0);
                        hoveredElement.mouseClicked(0,0,0);
                        hoveredElement.mouseReleased(0,0,0);
                        mod.cancelUserTask();
                        Runnable doOnStuckFixFinish =  new Thread(() -> {
                            MinecraftClient clientt = MinecraftClient.getInstance();
                            clientt.setScreen(new GameMenuScreen(true));

                            Debug.logMessage("[STUCKFIX] DISCONNECT STAGE 2 ");

                            this.disconnect(clientt);

                            mod.cancelUserTask();
                            mod.runUserTask(new GetToXZTask(0,0));

                            Debug.logMessage("[STUCKFIX] SET MP SCREEN");
                            MinecraftClient.getInstance().setScreen(new MultiplayerScreen(new TitleScreen()));

                            Debug.logMessage("[STUCKFIX] RECONNECT TO SERVER");
                            if(_prevServerEntry!=null) {
                            }else{
                                _prevServerEntry = srv;
                            }//_prevServerEntry.address = "mc.vimemc.net";}
                            _reconnecting = true;
                            _reconnectTimer.reset();
                        });
                        mod.runUserTask(new StuckFixingTask(),doOnStuckFixFinish);
                    }
                    _needUnStuckFix = false;
                }
            }else if (_needDisconnect) {
                if(AltoClef.inGame()) {
                    ServerInfo srv = MinecraftClient.getInstance().getCurrentServerEntry();

                    if (srv != null) {
                        Debug.logMessage("Starting DISCONNECT CHAIN >> server=" + srv.address.toString());
                        _prevServerEntry = srv;
                    }

                    Debug.logMessage("[DISCONNECT CHAIN] OPEN GAME MENU");
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.setScreen(new GameMenuScreen(true));
                    if (_prevServerEntry != null) {
                        if(_reJoinAfterDisconnect){
                            _reconnecting = true;
                            _reconnectTimer.reset();
                            _reJoinAfterDisconnect = false;
                        }
                    } else {
                    }//_prevServerEntry.address = "mc.vimemc.net";}
                    this.disconnect(client);
                    MinecraftClient.getInstance().setScreen(new MultiplayerScreen(new TitleScreen()));
                    _needDisconnect = false;
                }

            } else if (screen instanceof MultiplayerScreen && _reconnecting && _reconnectTimer.elapsed()) {

                Debug.logMessage("RECONNECTING: Going ");
                _reconnecting = false;

                if (_prevServerEntry == null) {
                    Debug.logWarning("Failed to re-connect to server, no server entry cached.");
                } else {
                    Debug.logMessage("RECONNECTING!: "+_prevServerEntry.address.toString());
                    MinecraftClient client = MinecraftClient.getInstance();
                    ConnectScreen.connect(screen, client, ServerAddress.parse(_prevServerEntry.address), _prevServerEntry);
                    if(_needToStopTasksOnReconnect){
                        mod.cancelUserTask();
                        _needToStopTasksOnReconnect = false;
                    }
                    //client.setScreen(new ConnectScreen(screen, client, _prevServerEntry));
                }
            }
        }
        if (screen != null)
            _prevScreen = screen.getClass();
        return Float.NEGATIVE_INFINITY;
    }

    public void disconnect(MinecraftClient client){
        if(AltoClef.inGame()&&client.world!=null) {
            Debug.logMessage("DISCONNECT");
            boolean bl = client.isInSingleplayer();
            client.world.disconnect();
            if (bl) {
                client.disconnect(new SaveLevelScreen(new TranslatableText("menu.savingLevel")));
            } else {
                client.disconnect();
            }
        }else {
            Debug.logMessage("Tried to disconnect >>> world is null or not in game");
        }
    }

    public static void StuckFixActivate(){
        _needUnStuckFix = true;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public String getName() {
        return "Death Menu Respawn Handling";
    }
}
