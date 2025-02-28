package adris.altoclef.chains;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.mixins.DeathScreenAccessor;
import adris.altoclef.tasks.fix.StuckFixingTask;
import adris.altoclef.tasks.movement.GetToXZTask;
import adris.altoclef.tasksystem.TaskChain;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.ui.MessagePriority;
import adris.altoclef.util.time.TimerGame;
import adris.altoclef.util.time.TimerReal;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;

public class DeathMenuChain extends TaskChain {

    // Sometimes we fuck up, so we might want to retry considering the death screen.
    private final TimerReal _deathRetryTimer = new TimerReal(8);
    private final TimerGame _waitOnDeathScreenBeforeRespawnTimer = new TimerGame(2);
    private int _deathCount = 0;
    private Class _prevScreen = null;

    public static boolean NeedtoStopTasksOnDeath = false;
    private final TimerReal _commandDelayTimer = new TimerReal(2);

    public DeathMenuChain(TaskRunner runner) {
        super(runner);
    }

    private boolean shouldAutoRespawn(AltoClef mod) {
        return mod.getModSettings().isAutoRespawn();
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
            //НОВАЯ ЧАСТЬ
            if (mod.getPlayer().hasStatusEffect(StatusEffects.INVISIBILITY)) {
                if (mod.getPlayer().getStatusEffect(StatusEffects.INVISIBILITY).getAmplifier() >= 3) {
                    if (NeedtoStopTasksOnDeath) {
                        mod.getMessageSender().enqueueChat("/hub", MessagePriority.TIMELY);
                        NeedtoStopTasksOnDeath = false;
                        _commandDelayTimer.reset();
                        if (_commandDelayTimer.elapsed()) {
                            _commandDelayTimer.reset();
                            Debug.logMessage("ВАЛИМ! 111");
                        }
                    }

                }
            }
        }

        if (screen instanceof DeathScreen) {
            if (NeedtoStopTasksOnDeath) {
                mod.cancelUserTask();
                NeedtoStopTasksOnDeath = false;
                _commandDelayTimer.reset();
            }
            if (_waitOnDeathScreenBeforeRespawnTimer.elapsed()) {
                _waitOnDeathScreenBeforeRespawnTimer.reset();
                if (shouldAutoRespawn(mod)) {
                    _deathCount++;
                    Debug.logMessage("РЕСПАВН... (смерть #" + _deathCount
                            + ")"); //TRS ESPAWNING... (this is death #" + _deathCount + "
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

        }
        if (screen != null) {
            _prevScreen = screen.getClass();
        }
        return Float.NEGATIVE_INFINITY;
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
