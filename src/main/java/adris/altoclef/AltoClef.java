package adris.altoclef;

import adris.altoclef.butler.Butler;
import adris.altoclef.butler.WhisperChecker;
import adris.altoclef.chains.*;
import adris.altoclef.commandsystem.CommandExecutor;
import adris.altoclef.control.InputControls;
import adris.altoclef.control.PlayerExtraController;
import adris.altoclef.control.SlotHandler;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.*;
import adris.altoclef.eventbus.events.multiplayer.ItemUseEvent;
import adris.altoclef.eventbus.events.multiplayer.ProjectileEvent;
import adris.altoclef.mixins.MinecraftClientSessionMixin;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.trackers.*;
import adris.altoclef.trackers.storage.ContainerSubTracker;
import adris.altoclef.trackers.storage.ItemStorageTracker;
import adris.altoclef.ui.CommandStatusOverlay;
import adris.altoclef.ui.MessagePriority;
import adris.altoclef.ui.MessageSender;
import adris.altoclef.util.agent.Pipeline;
import adris.altoclef.util.helpers.InputHelper;
import adris.altoclef.util.helpers.LookHelper;
import baritone.Baritone;
import baritone.altoclef.AltoClefSettings;
import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import baritone.api.utils.Rotation;
import io.netty.handler.codec.marshalling.DefaultUnmarshallerProvider;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.session.Session;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Consumer;
import py4j.GatewayServer;

import static adris.altoclef.util.helpers.StringHelper.mcTextToString;
import static adris.altoclef.util.helpers.StringHelper.removeMCFormatCodes;

/**
 * Central access point for AltoClef
 */
public class AltoClef implements ModInitializer {
    public static final String MOD_ID = "altoclef";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    // Static access to altoclef
    private static final Queue<Consumer<AltoClef>> _postInitQueue = new ArrayDeque<>();
    public static Rotation getCameraRotationModifer(){
        return _cameraRotationModifer;
    }
    public static void setCameraRotationModifer(Rotation rotation){
        _cameraRotationModifer = rotation;
    }
    public static void resetCameraRotationModifer(){
        _cameraRotationModifer = null;
    }
    public static Rotation _cameraRotationModifer = null;
    public static Vec3d getCameraPositionModifer(){
        return _cameraPositionModifer;
    }
    public static void setCameraPositionModifer(Vec3d pos){
        _cameraPositionModifer = pos;
    }
    public static void resetCameraPositionModifer(){
        _cameraPositionModifer = null;
    }
    public static Vec3d _cameraPositionModifer = null;

    // Central Managers
    private static CommandExecutor _commandExecutor;
    private TaskRunner _taskRunner;
    private TrackerManager _trackerManager;
    private BotBehaviour _botBehaviour;
    private PlayerExtraController _extraController;
    // Task chains
    private UserTaskChain _userTaskChain;
    public SupervisorTaskChain _supervisorTaskChain;
    private FoodChain _foodChain;
    private MobDefenseChain _mobDefenseChain;
    private DeathMenuChain _deathMenuChain;
    private MLGBucketFallChain _mlgBucketChain;
    public PlayerInteractionFixChain _playerInteractionFixChain;
    public WorldSurvivalChain _worldSurvivalChain;
    public GameMenuTaskChain _gameMenuTaskChain;

    // Trackers
    private ItemStorageTracker _storageTracker;
    private ContainerSubTracker _containerSubTracker;
    private EntityTracker _entityTracker;
    private DamageTracker _damageTracker;
    private BlockTracker _blockTracker;
    private SimpleChunkTracker _chunkTracker;
    private MiscBlockTracker _miscBlockTracker;
    // Renderers
    private CommandStatusOverlay _commandStatusOverlay;
    // Settings
    private adris.altoclef.Settings _settings;
    // Misc managers/input
    private MessageSender _messageSender;
    private InputControls _inputControls;
    private SlotHandler _slotHandler;
    // Butler
    private Butler _butler;
    private static GatewayServer _gatewayServer;
    private static Py4jEntryPoint _py4jEntryPoint;
    public static Pipeline _pipeline = Pipeline.None;

    @NotNull
    public static Pipeline getPipeline(){
        return _pipeline;
    }
    // Are we in game (playing in a server/world)
    public static boolean inGame() {
        MinecraftClient client = MinecraftClient.getInstance();

        return client != null && client.player != null
                && client.getNetworkHandler() != null
                && client.world != null;
    }
    // TODO UNTESTED MAY CAUSE ERRORS
    // NEED TO WORK IN MENUS!
    public static String getSelfName(){
        // Сначала проверяем клиент
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return "";

        // Проверяем сессию
        if (client.getSession() != null) {
            return client.getSession().getUsername();
        }

        // Проверяем профиль
        if (client.getGameProfile() != null) {
            return client.getGameProfile().getName();
        }

        // Проверяем загруженного игрока
        if (client.player != null) {
            return client.player.getName().getString();
        }

        return "";
    }
    public double getCurrentBaritoneHeuristic() {
        if (getClientBaritone() != null && getClientBaritone().getPathingBehavior() != null){
            Optional<Double> ticksRemainingOp = getClientBaritone().getPathingBehavior().ticksRemainingInSegment();
            return ticksRemainingOp.orElse(Double.POSITIVE_INFINITY);
        }
        return Double.POSITIVE_INFINITY;
    }
    public static boolean isManualInputFound(){
        //may be unstable, not tested, new
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return false;
        if (client.isWindowFocused()) {
            return true;
            // Human input detected.
            //Debug.logInternal("[IdleTask] Window is focused, resuming.");
        } else {
            return false;
            // No human input – window is unfocused.
            //Debug.logInternal("[IdleTask] Window is unfocused, pausing.");
        }
    }
    /**
     * Executes commands (ex. `@get`/`@gamer`)
     */
    public static CommandExecutor getCommandExecutor() {
        return _commandExecutor;
    }

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // As such, nothing will be loaded here but basic initialization.
        EventBus.subscribe(TitleScreenEntryEvent.class, evt -> onInitializeLoad());

    }

    public String _modPrefixNoCodes = "[AutoClef]";
    public void onInitializeLoad() {
        // This code should be run after Minecraft loads everything else in.
        // This is the actual start point, controlled by a mixin.


        // changing only for debug mode when username is like "PlayerNNN"
        if (AltoClef.getSelfName() != null
        && AltoClef.getSelfName().toLowerCase().contains("player")) {
            changePlayerName("NetTyan");
        }

        initializeBaritoneSettings();

        // Central Managers
        _commandExecutor = new CommandExecutor(this);
        _taskRunner = new TaskRunner(this);
        _trackerManager = new TrackerManager(this);
        _botBehaviour = new BotBehaviour(this);
        _extraController = new PlayerExtraController(this);

        // Task chains
        _userTaskChain = new UserTaskChain(_taskRunner);
        _supervisorTaskChain = new SupervisorTaskChain(_taskRunner);
        _mobDefenseChain = new MobDefenseChain(_taskRunner);
        _deathMenuChain = new DeathMenuChain(_taskRunner);
        _gameMenuTaskChain = new GameMenuTaskChain(_taskRunner);
        _playerInteractionFixChain = new PlayerInteractionFixChain(_taskRunner);
        _mlgBucketChain = new MLGBucketFallChain(_taskRunner);
        _worldSurvivalChain = new WorldSurvivalChain(_taskRunner);
        _foodChain = new FoodChain(_taskRunner);

        // Trackers
        _storageTracker = new ItemStorageTracker(this, _trackerManager, container -> _containerSubTracker = container);
        _entityTracker = new EntityTracker(_trackerManager);
        _damageTracker = new DamageTracker(_trackerManager);
        _blockTracker = new BlockTracker(this, _trackerManager);
        _chunkTracker = new SimpleChunkTracker(this);
        _miscBlockTracker = new MiscBlockTracker(this);

        // Renderers
        _commandStatusOverlay = new CommandStatusOverlay();

        // Misc managers
        _messageSender = new MessageSender();
        _inputControls = new InputControls();
        _slotHandler = new SlotHandler(this);

        _butler = new Butler(this);

        initializeCommands();

        // Load settings
        adris.altoclef.Settings.load(newSettings -> {
            _settings = newSettings;
            // Baritone's `acceptableThrowawayItems` should match our own.
            List<Item> baritoneCanPlace = Arrays.stream(_settings.getThrowawayItems(this, true))
                    .filter(item -> item != Items.SOUL_SAND && item != Items.MAGMA_BLOCK && item != Items.SAND && item != Items.GRAVEL)
                    // Don't place soul sand or magma blocks, that messes us up.
                    .toList();
            getClientBaritoneSettings().acceptableThrowawayItems.value.addAll(baritoneCanPlace);
            // If we should run an idle command...
            if ((!getUserTaskChain().isActive() || getUserTaskChain().isRunningIdleTask()) && getModSettings().shouldRunIdleCommandWhenNotActive()) {
                getUserTaskChain().signalNextTaskToBeIdleTask();
                getCommandExecutor().executeWithPrefix(getModSettings().getIdleCommand());
            }
            // Don't break blocks or place blocks where we are explicitly protected.
            getExtraBaritoneSettings().avoidBlockBreak(blockPos -> _settings.isPositionExplicitlyProtected(blockPos));
            getExtraBaritoneSettings().avoidBlockPlace(blockPos -> _settings.isPositionExplicitlyProtected(blockPos));
        });

        // Receive + cancel chat
        EventBus.subscribe(SendChatEvent.class, evt -> {
            String line = evt.message;
            if (getCommandExecutor().isClientCommand(line)) {
                evt.cancel();
                getCommandExecutor().execute(line);
            }
        });

        // Debug jank/hookup
        Debug.jankModInstance = this;
        AltoclefVoicechat.jankModInstance = this;

        // Tick with the client
        EventBus.subscribe(ClientTickEvent.class, evt -> onClientTick());
        // Render
        EventBus.subscribe(ClientRenderEvent.class, evt -> onClientRenderOverlay(evt.stack));
        // only for sp =(
        //ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
        //    Debug.logMessage("onChatMessage DEBUG!!!! MSG CHAT CharReadMixin" + message);
        //    //ChatMessageEvent evt = new ChatMessageEvent(message, sender, params);
        //    //EventBus.publish(evt);
        //}
        //);
        _modPrefixNoCodes = removeMCFormatCodes(this.getModSettings().getCommandPrefix());
        String _modChatPrefixNoCodes = removeMCFormatCodes(this.getModSettings().getChatLogPrefix());
        // only work on vanilla servers and only for PLAYER CHAT messages, /tell goes here too
        // PROBLEM: stupid message doubling
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            String msg = mcTextToString(message);
            // Check if this is a whisper/tell message
            //Debug.logMessage("ALLOW_CHAT DEBUG!!!! MSG CHAT CharReadMixin:\n==" + msg);
            if (!msg.startsWith(_modPrefixNoCodes)) {
                if (msg.startsWith("[Baritone] Failed")){
                    return false;  // FIX THIS BARITONE SPAM!!!!
                }
                //if (message instanceof PrivateMessage)
                ChatMessageEvent evt = new ChatMessageEvent(msg);  //new ChatMessageEvent(msg, signedMessage, sender, params);
                EventBus.publish(evt);
                //Debug.logMessage("DEBUG WHISPER " + msg);
                boolean isValidWhisperCommand = this.getButler().recieveWhisperCommand(AltoClef.getSelfName(), msg);
                if (isValidWhisperCommand) {
                    // Process the whisper message but don't display it in chat
                    //processWhisperMessage(msg);
                    return false; // Don't show in chat
                }
            }

            return true;
        });
        // MAIN FOR SERVERS (definitely ALL MSGS including altoclef messages...)
        // on vanilla servers NOT shows PLAYER standart chat and /tell messages
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {

                String msg = mcTextToString(message);

                if (msg.startsWith("[Baritone] Failed")){
                    return false;  // FIX THIS BARITONE SPAM!!!!
                }
                //Debug.logInternal("ALLOW_GAME DEBUG!!!! MSG CHAT CharReadMixin:\n==" + msg);
            // ISSUE WITH SYMBOL CODES!!!
                if (!msg.contains(_modChatPrefixNoCodes)) {
                    //.logInternal("ALLOW_GAME DEBUG!!!! MSG CHAT CharReadMixin:\n==\n" + msg + "\n==" + msg.contains(" -> я] "));
                    // TODO TEMP, DEBUG TESTING!!
                    // MinecraftClient.getInstance().inGameHud.getChatHud().restoreChatState();
                    boolean isValidWhisperCommand = this.getButler().recieveWhisperCommand(AltoClef.getSelfName(), msg);
                    if (isValidWhisperCommand) {
                        // Process the whisper message but don't display it in chat
                        // processWhisperMessage(msg);
                        return false; // Don't show in chat
                    }
                    if (!this.getModSettings().showChat()) {
                        // Debug.logMessage("lol got chat, removing it");
                        return false;
                    }

                    ChatMessageEvent evt = new ChatMessageEvent(msg, overlay);
                    EventBus.publish(evt);
                }

                return true;
            }
        );
        // Playground
        Playground.IDLE_TEST_INIT_FUNCTION(this);

        // External mod initialization
        runEnqueuedPostInits();
        DamageEventHandler.registerDamagePacketReceiver(this);

        // Add block place tracking for survival chain
        EventBus.subscribe(BlockPlaceEvent.class, evt -> {
                _worldSurvivalChain.onBlockPlaced(this, evt.blockPos, evt.blockState);
        });
        EventBus.subscribe(BlockBrokenEvent.class, evt -> {
                _worldSurvivalChain.onBlockBroken(this, evt.blockPos, evt.blockState, evt.player);
        });

        EventBus.subscribe(ItemUseEvent.class, evt -> {
            getMobDefenseChain().onPlayerItemUse(this, evt.entity, evt.released);
        });
        EventBus.subscribe(ProjectileEvent.class, evt -> {
            getMobDefenseChain().onProjectileLaunched(this, evt.entity, evt.sticked);
        });

        initializePythonSender();  // moved to end since we need loaded settings (and also its logical)
    }
    public void timelyDisableBlockBreaking(double timeoutSeconds){
        getClientBaritoneSettings().allowBreak.value = false;
    }
    public void timelyDisableBlockPlacing(double timeoutSeconds){
        getClientBaritoneSettings().allowPlace.value = false;
    }

    public void initializePythonSender() {
        _py4jEntryPoint = new Py4jEntryPoint(this);
        // 25333
        // TODO get gateway ports from config
        // default ports: gatewayport DEFAULT_PORT 25333, pythonGatewayPort DEFAULT_PYTHON_PORT 25334
        final int JAVA_GATEWAY_PORT = this.getModSettings().getPythonGatewayPort();
        final int PYTHON_CALLBACK_PORT = this.getModSettings().getPythonGatewayPort() + 1;
        _gatewayServer = new GatewayServer(_py4jEntryPoint);
        // TODO UNTESTED
        _gatewayServer = new py4j.GatewayServer(
                _py4jEntryPoint,
                JAVA_GATEWAY_PORT,
                PYTHON_CALLBACK_PORT,
                py4j.GatewayServer.DEFAULT_CONNECT_TIMEOUT,
                py4j.GatewayServer.DEFAULT_READ_TIMEOUT,
                null // customCommands
        );
        _gatewayServer.start();
        if (_gatewayServer != null ) {
            System.out.println("Gateway Server started on port "+_gatewayServer.getPort() + ". Listeting port: "+_gatewayServer.getListeningPort() + 
                ", Python callback port=" + _gatewayServer.getPythonPort());
        }

        _py4jEntryPoint.InitPythonCallback();
    }
    public void stopPythonSender() {
        System.out.println("Gateway STOP Initiated...");
        _gatewayServer.shutdown();
    }

    public void reloadPythonSender() {
        System.out.println("Gateway Reload Initiated...");
        _py4jEntryPoint = null;
        stopPythonSender();
        _gatewayServer = null;
        initializePythonSender();
    }
    // Client tick
    private void onClientTick() {
        runEnqueuedPostInits();

        _inputControls.onTickPre();

        // Cancel shortcut
        if (InputHelper.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL) && InputHelper.isKeyPressed(GLFW.GLFW_KEY_K)) {
            _userTaskChain.cancel(this);
            if (_taskRunner.getCurrentTaskChain() != null) {
                _taskRunner.getCurrentTaskChain().stop(this);
            }
        }

        // TODO: should this go here?
        _storageTracker.setDirty();
        _containerSubTracker.onServerTick();
        _miscBlockTracker.tick();

        _trackerManager.tick();
        _damageTracker.tick();
        _blockTracker.preTickTask();
        _taskRunner.tick();
        _blockTracker.postTickTask();

        _butler.tick();
        _messageSender.tick();

        _inputControls.onTickPost();
        _gameMenuTaskChain.onTickPost(this);
    }

// ...existing code...

    /**
     * Changes the player's username (requires restart to take effect on servers)
     * WARNING: This is for offline mode only and may not work on authenticated servers
     */
    public static boolean changePlayerName(String newUsername) {
        if(newUsername == null || newUsername.isBlank()) {
            Debug.logWarning("Cannot change username: New username is null or blank");
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            Debug.logWarning("Cannot change username: MinecraftClient is null");
            return false;
        }
        
        try {
            // Create new session with different username but same UUID
            Session currentSession = client.getSession();
            if (currentSession == null) {
                Debug.logWarning("Cannot change username: Current session is null");
                return false;
            }
            
            // For offline mode servers - create new session with same UUID but different name
            Session newSession = new Session(
                newUsername,
                currentSession.getUuidOrNull(), // Keep same UUID
                currentSession.getAccessToken(),
                currentSession.getXuid(),
                currentSession.getClientId(),
                currentSession.getAccountType()
            );
            
            // Use mixin to set new session
            MinecraftClientSessionMixin clientMixin = (MinecraftClientSessionMixin) client;
            clientMixin.setSession(newSession);
            
            Debug.logMessage("Username changed to: " + newUsername);
            return true;
            
        } catch (Exception e) {
            Debug.logError("Failed to change username: " + e.getMessage());
            return false;
        }
    }

// ...existing code...


    /// GETTERS AND SETTERS

    private void onClientRenderOverlay(MatrixStack matrixStack) {
        _commandStatusOverlay.render(this, matrixStack);
        LookHelper.updateWindMouseRotation(this);
    }

    private void initializeBaritoneSettings() {
        getExtraBaritoneSettings().canWalkOnEndPortal(false);
        getClientBaritoneSettings().freeLook.value = false;
        getClientBaritoneSettings().overshootTraverse.value = false;
        getClientBaritoneSettings().allowOvershootDiagonalDescend.value = true;
        getClientBaritoneSettings().allowInventory.value = true;
        // TODO NEW VERY DANGEROUS!
        // TODO UNTESTED
        // TODO DANGER PARKOUR BARITONE
        // OFF MARKED ON ISSUES
        getClientBaritoneSettings().allowParkour.value = true; // was false
        getClientBaritoneSettings().allowParkourAscend.value = true; // was false
        getClientBaritoneSettings().allowParkourPlace.value = false;
        getClientBaritoneSettings().allowDiagonalDescend.value = false;
        getClientBaritoneSettings().allowDiagonalAscend.value = false;
        getClientBaritoneSettings().blocksToAvoid.value = List.of(Blocks.FLOWERING_AZALEA, Blocks.AZALEA,
                Blocks.POWDER_SNOW, Blocks.BIG_DRIPLEAF, Blocks.BIG_DRIPLEAF_STEM, Blocks.CAVE_VINES,
                Blocks.CAVE_VINES_PLANT, Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, Blocks.SWEET_BERRY_BUSH,
                Blocks.WARPED_ROOTS, Blocks.VINE, Blocks.GRASS_BLOCK, Blocks.FERN, Blocks.TALL_GRASS, Blocks.LARGE_FERN,
                Blocks.SMALL_AMETHYST_BUD, Blocks.MEDIUM_AMETHYST_BUD, Blocks.LARGE_AMETHYST_BUD,
                Blocks.AMETHYST_CLUSTER, Blocks.SCULK, Blocks.SCULK_VEIN, Blocks.SUNFLOWER, Blocks.LILAC,
                Blocks.ROSE_BUSH, Blocks.PEONY);
        // Let baritone move items to hotbar to use them
        // Reduces a bit of far rendering to save FPS
        getClientBaritoneSettings().fadePath.value = true;
        // Don't let baritone scan dropped items, we handle that ourselves.
        getClientBaritoneSettings().mineScanDroppedItems.value = false;
        // Don't let baritone wait for drops, we handle that ourselves.
        getClientBaritoneSettings().mineDropLoiterDurationMSThanksLouca.value = 0L;

        // Water bucket placement will be handled by us exclusively
        getExtraBaritoneSettings().configurePlaceBucketButDontFall(true);

        // For render smoothing
        getClientBaritoneSettings().randomLooking.value = 0.0;
        getClientBaritoneSettings().randomLooking113.value = 0.0;

        // Give baritone more time to calculate paths. Sometimes they can be really far away.
        // Was: 2000L
        getClientBaritoneSettings().failureTimeoutMS.reset();
        // Was: 5000L
        getClientBaritoneSettings().planAheadFailureTimeoutMS.reset();
        // Was 100
        getClientBaritoneSettings().movementTimeoutTicks.reset();
    }

    // List all command sources here.
    private void initializeCommands() {
        try {
            // This creates the commands. If you want any more commands feel free to initialize new command lists.
            new AltoClefCommands();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs the highest priority task chain
     * (task chains run the task tree)
     */
    public TaskRunner getTaskRunner() {
        return _taskRunner;
    }

    /**
     * The user task chain (runs your command. Ex. Get Diamonds, Beat the Game)
     */
    public UserTaskChain getUserTaskChain() {
        return _userTaskChain;
    }


    /**
     * Get current task depending on taskChains
     */
    public Task getCurrentTask() {
        //if (getUserTaskChain() != null && getUserTaskChain().isActive() && getUserTaskChain().getCurrentTask() != null) {
        //    return getUserTaskChain().getCurrentTask();
        //}
        if (getTaskRunner() != null && getTaskRunner().getCurrentTaskChain() != null && getTaskRunner().getCurrentTaskChain().isActive()) {
            if ( getTaskRunner().getCurrentTaskChain() instanceof SingleTaskChain chain
                && chain.getCurrentTask() != null)
                return chain.getCurrentTask();
        }
        return null;
    }

    /**
     * Controls bot behaviours, like whether to temporarily "protect" certain blocks or items
     */
    public BotBehaviour getBehaviour() {
        return _botBehaviour;
    }

    /**
     * Tracks items in your inventory and in storage containers.
     */
    public ItemStorageTracker getItemStorage() {
        return _storageTracker;
    }

    /**
     * Tracks loaded entities
     */
    public EntityTracker getEntityTracker() {
        return _entityTracker;
    }

    /**
     * Tracks blocks and their positions
     */
    public BlockTracker getBlockTracker() {
        return _blockTracker;
    }

    /**
     * Tracks of whether a chunk is loaded/visible or not
     */
    public SimpleChunkTracker getChunkTracker() {
        return _chunkTracker;
    }

    /**
     * Tracks random block things, like the last nether portal we used
     */
    public MiscBlockTracker getMiscBlockTracker() {
        return _miscBlockTracker;
    }

    /**
     * Baritone access (could just be static honestly)
     */
    public Baritone getClientBaritone() {
        if (getPlayer() == null) {
            return (Baritone) BaritoneAPI.getProvider().getPrimaryBaritone();
        }
        return (Baritone) BaritoneAPI.getProvider().getBaritoneForPlayer(getPlayer());
    }
    public Py4jEntryPoint getInfoSender() {return _py4jEntryPoint; }
    public GatewayServer getGateway(){return _gatewayServer;}
    /**
     * Baritone settings access (could just be static honestly)
     */
    public Settings getClientBaritoneSettings() {
        return Baritone.settings();
    }

    /**
     * Baritone settings special to AltoClef (could just be static honestly)
     */
    public AltoClefSettings getExtraBaritoneSettings() {
        return AltoClefSettings.getInstance();
    }

    /**
     * AltoClef Settings
     */
    public adris.altoclef.Settings getModSettings() {
        return _settings;
    }

    /**
     * Butler controller. Keeps track of users and lets you receive user messages
     */
    public Butler getButler() {
        return _butler;
    }

    /**
     * Sends chat messages (avoids auto-kicking)
     */
    public MessageSender getMessageSender() {
        return _messageSender;
    }

    /**
     * Does Inventory/container slot actions
     */
    public SlotHandler getSlotHandler() {
        return _slotHandler;
    }

    /**
     * Minecraft player client access (could just be static honestly)
     */
    public ClientPlayerEntity getPlayer() {
        return MinecraftClient.getInstance().player;
    }

    /**
     * Minecraft world access (could just be static honestly)
     */
    public ClientWorld getWorld() {
        return MinecraftClient.getInstance().world;
    }

    /**
     * Minecraft client interaction controller access (could just be static honestly)
     */
    public ClientPlayerInteractionManager getController() {
        return MinecraftClient.getInstance().interactionManager;
    }

    /**
     * Extra controls not present in ClientPlayerInteractionManager. This REALLY should be made static or combined with something else.
     */
    public PlayerExtraController getControllerExtras() {
        return _extraController;
    }

    /**
     * Manual control over input actions (ex. jumping, attacking)
     */
    public InputControls getInputControls() {
        return _inputControls;
    }

    // For timeout command handling
    private boolean _isTimeoutTask = false;
    public static double DEFAULT_TIMEOUT_SECONDS = 30;
    public static double TIMEOUT_SECONDS = DEFAULT_TIMEOUT_SECONDS;

    public void setTimeoutTaskFlag(boolean isTimeout) {
        _isTimeoutTask = isTimeout;
    }
    public void setTimeoutTask(double timeoutSeconds) {
        TIMEOUT_SECONDS = timeoutSeconds;
        setTimeoutTaskFlag(true);
    }
    /**
     * Run a user task
     */
    public void runUserTask(Task task) {
        runUserTask(task, () -> {});
    }

    /**
     * Run a user task
     */
    public void runUserTask(Task task, Runnable onFinish) {
        if (_isTimeoutTask) {
            // If this is a timeout task, wrap it in a forced task
            _supervisorTaskChain.runTask(this, task, TIMEOUT_SECONDS);
            _isTimeoutTask = false; // Reset flag
            TIMEOUT_SECONDS = DEFAULT_TIMEOUT_SECONDS; // Reset timeout
            // TODO DEAL WITH onFinish in _supervisorTaskChain
            // CURRENT APPROACH IS CRITICAL: WE JUST DONT HAVE onFinish!!!
        } else {
            // Normal execution
            _userTaskChain.runTask(this, task, onFinish);
        }
    }
    public void runForcedTask(Task task, double time) {
            _supervisorTaskChain.runTask(this, task, time);
    }
    public void runForcedTask(Task task) {
        _supervisorTaskChain.runTask(this, task);
    }
    /**
     * Cancel currently running user task
     */
    public void cancelUserTask() {
        _userTaskChain.cancel(this);
    }

    /**
     * Takes control away to eat food
     */
    public FoodChain getFoodChain() {
        return _foodChain;
    }
    public DamageTracker getDamageTracker(){
        return _damageTracker;
    }
    public DeathMenuChain getDeathMenuChain() {
        return _deathMenuChain;
    }

    /**
     * Takes control away to defend against mobs
     */
    public MobDefenseChain getMobDefenseChain() {
        return _mobDefenseChain;
    }

    /**
     * Takes control away to perform bucket saves
     */
    public MLGBucketFallChain getMLGBucketChain() {
        return _mlgBucketChain;
    }
    public GameMenuTaskChain getGameMenuTaskChain() {return _gameMenuTaskChain;}

    public void log(String message) {
        log(message, MessagePriority.TIMELY);
    }

    /**
     * Logs to the console and also messages any player using the bot as a butler.
     */
    public void log(String message, MessagePriority priority) {
        Debug.logMessage(message);
        _butler.onLog(message, priority);
    }

    public void logWarning(String message) {
        logWarning(message, MessagePriority.TIMELY);
    }

    /**
     * Logs a warning to the console and also alerts any player using the bot as a butler.
     */
    public void logWarning(String message, MessagePriority priority) {
        Debug.logWarning(message);
        _butler.onLogWarning(message, priority);
    }

    private void runEnqueuedPostInits() {
        synchronized (_postInitQueue) {
            while (!_postInitQueue.isEmpty()) {
                _postInitQueue.poll().accept(this);
            }
        }
    }

}
