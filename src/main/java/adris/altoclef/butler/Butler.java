package adris.altoclef.butler;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.chains.DeathMenuChain;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.ChatMessageEvent;
import adris.altoclef.eventbus.events.TaskFinishedEvent;
import adris.altoclef.tasks.DoToClosestBlockTask;
import adris.altoclef.tasks.movement.*;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.ui.MessagePriority;
import adris.altoclef.util.helpers.MapItemHelper;
import adris.altoclef.util.time.TimerGame;
import adris.altoclef.util.time.TimerReal;
import baritone.api.utils.input.Input;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * The butler system lets authorized players send commands to the bot to execute.
 * <p>
 * This effectively makes the bot function as a servant, or butler.
 * <p>
 * Authorization is defined in "altoclef_butler_whitelist.txt" and "altoclef_butler_blacklist.txt"
 * and depends on the "useButlerWhitelist" and "useButlerBlacklist" settings in "altoclef_settings.json"
 */
public class Butler {
    public static boolean stuck_fix_butler_allow = true;
    private static final String BUTLER_MESSAGE_START = "` ";

    private final AltoClef _mod;

    private final WhisperChecker _whisperChecker = new WhisperChecker();

    private final UserAuth _userAuth;

    private String _currentUser = null;

    // Utility variables for command logic
    private boolean _commandInstantRan = false;
    private boolean _commandFinished = false;

    public Butler(AltoClef mod) {
        _mod = mod;
        _userAuth = new UserAuth(mod);

        // Revoke our current user whenever a task finishes.
        EventBus.subscribe(TaskFinishedEvent.class, evt -> {
            if (_currentUser != null) {
                _currentUser = null;
            }
        });

        // Receive system events
        EventBus.subscribe(ChatMessageEvent.class, evt -> {
            boolean debug = ButlerConfig.getInstance().whisperFormatDebug;
            String message = evt.message.getString();
            //evt.message.
            if (debug) {
                Debug.logMessage("RECEIVED WHISPER: \"" + message + "\".");
            }
            if (AltoClef.inGame()) {
                _mod.getButler().receiveMessage(message);
                _mod.getInfoSender().onChatMessage(message);
            }
        });
    }
    public static boolean IsStuckFixAllow(){
        return stuck_fix_butler_allow&&ButlerConfig.getInstance().autoStuckFix;
    }
    private Task _lobbyMovementTask;
    private Task _autopilolTask;
    private final TimerGame _lobbyMovingTimer = new TimerGame(10);
    private TimerReal _captchaTimer = new TimerReal(4);
    public String CaptchaSolvingMode = "SOLVE_MAXIMUM"; //GET_DATASET SOLVE_DATASET_ONLY

    private void receiveMessage(String msg) {

        // Format: <USER> whispers to you: <MESSAGE>
        // Format: <USER> whispers: <MESSAGE>
        //_mod.getClientBaritone().getGetToBlockProcess().
        //_mod.getPlayer().loo
        //Debug.logMessage("msg"+msg);
        String ourName = "NetTyan";//MinecraftClient.getInstance().getName();
        if (_mod.getPlayer() != null) {
            ourName = _mod.getPlayer().getName().getString();
        }else{Debug.logMessage("[butler chat] ИГРОК в мире НЕ НАЙДЕН! Взято имя "+ourName);}
        //MinecraftClient.getInstance().getName();
        //if(_lobbyMovingTimer.elapsed() && _lobbyMovementTask!=null)
            //_lobbyMovementTask = null;
            //_mod.getCommandExecutor().execute("@stop");
            //_lobbyMovementTask.stop(_mod);
        //Debug.logMessage(ourName);
        //if (DeathMenuChain.ServerIp.equals("mc.musteryworld.net"))

        String serverAdress = "universal";
        if(MinecraftClient.getInstance().getCurrentServerEntry()!= null){
            if(MinecraftClient.getInstance().getCurrentServerEntry().address!=null) {
                serverAdress = MinecraftClient.getInstance().getCurrentServerEntry().address.toString();
                _mod.getInfoSender().UpdateServerInfo("server", serverAdress);
            }
        }
        String serverMode = _mod.getInfoSender().getInfo("serverMode","survival");//_mod.getInfoSender().UpdateServerInfo("serverMode","survival");
        List<String> availableServerModes = new ArrayList<>();
        for( String[] format : ButlerConfig.getInstance().chatFormats ){
            if (serverAdress.equals(format[0])) {
                availableServerModes.add(format[2]);//
            }
        }
        for (String mode : availableServerModes){
            if (msg.toLowerCase().contains(mode.toLowerCase())){
                serverMode = mode;
                _mod.getInfoSender().UpdateServerInfo("serverMode",serverMode);
            }
        }
        //if(msg.toLowerCase().contains("[skywars]")){
        //    if(_mod.getInfoSender().serverMode!="skywars") {
        //        serverMode = "skywars";
        //        _mod.getInfoSender().serverMode = serverMode;
        //        Debug.logMessage("servermode " + serverMode);
        //    }
//
        //}

        // INFO UPDATE
        if(msg.contains("Ⓛ")){_mod.getInfoSender().UpdateServerInfo("chatType","local");}
        else if(msg.contains("Ⓖ")){ _mod.getInfoSender().UpdateServerInfo("chatType","global");}
        else if(msg.toLowerCase().contains("лобби")){ _mod.getInfoSender().UpdateServerInfo("chatType","lobby");}
        if(ButlerConfig.getInstance().autoJoin) {
            // AUTO JOIN SOLVER
            if (msg.contains("] Вы находитесь в Лобби. Выберите сервер и пройдите в портал!") || msg.contains("Вы успешно вошли!")) {
                //_mod.getBlockTracker().PosCache.clear();
                _mod.getCommandExecutor().execute("@stop");
                Debug.logMessage("Мы в лобби!");
                _lobbyMovingTimer.reset();
                //_mod.cancelUserTask();
                //_lobbyMovementTask = new GetToXZTask(-20, 25);
                //_mod.runUserTask(_lobbyMovementTask); //MusteryWorld
                _mod.getCommandExecutor().execute("@goto -20 29");
            } else if (msg.contains("Введите капчу с картинки в чат")) {
                this.captchaActionsPerform();
            }else if(msg.contains("Войдите в игру - !!! /login [пароль]")){ // for public HIDE!
                _mod.getMessageSender().enqueueChat("/login PASSWORD", MessagePriority.TIMELY);
            } else if (msg.contains("[SkyWars] Добро пожаловать!") || msg.contains("[SkyWars] Вы покинули игру")) {
                Debug.logMessage("Мы в хабе!");
                //_mod.cancelUserTask();
                _mod.getCommandExecutor().execute("@stop");
                _lobbyMovingTimer.reset();
                //_lobbyMovementTask = new GetToXZTask(-65, -69);
                //_mod.runUserTask(_lobbyMovementTask); //MusteryWorld
                _mod.getCommandExecutor().execute("@goto -65 -69");
            } else if (msg.contains("[SkyWars] " + ourName + " присоединился к")) {
                Debug.logMessage("Мы в колбе!");
                //_lobbyMovementTask.stop(_mod);
                //_mod.cancelUserTask();
                _mod.getCommandExecutor().execute("@stop");

            } else if (msg.contains(ourName + " выиграл игру!")) {
                Debug.logMessage("ПОБЕДА!!! УРАА!!");
                //_mod.cancelUserTask();
                _mod.getCommandExecutor().execute("@stop");
                _lobbyMovingTimer.reset();
                //_lobbyMovementTask = new GetToXZTask(-65, -69);
                //_mod.runUserTask(_lobbyMovementTask); //MusteryWorld
                _mod.getCommandExecutor().execute("@goto -65 -69");
            } else if (msg.contains("1ый Убийца -")) {
                Debug.logMessage("Игра остановлена");
                //_mod.cancelUserTask();
                _mod.getCommandExecutor().execute("@stop");
                _lobbyMovingTimer.reset();
                //_lobbyMovementTask = new GetToXZTask(-65, -69);
                //_mod.runUserTask(_lobbyMovementTask); //MusteryWorld
                _mod.getCommandExecutor().execute("@goto -65 -69");

                // funny mc

            } else if (msg.contains("[SkyWars] NetTyan погиб")||msg.contains("[SkyWars] NetTyan был убит")||msg.contains("[SkyWars] NetTyan победил в битве")){
                _mod.getCommandExecutor().execute("@stop");
                _lobbyMovingTimer.reset();
                _mod.runUserTask(new LobbyMoveTask());
                //_mod.getInputControls().hold(Input.MOVE_BACK);
                //_mod.getUserTaskChain().runTask(_mod,new GetCloseToBlockTask(new BlockPos(-3, 74, 20)));
                // todo add win logic reaction

            } else if (msg.contains("[SkyWars] Игра начинается через 1")
                    ||(serverAdress.equals("funnymc.ru")&&msg.contains("Игра начинается через 1 секунду"))) {
                Debug.logMessage("Начался батл SW!");
                //_mod.cancelUserTask();
                _mod.getCommandExecutor().execute("@stop");
                _mod.getButler().ClearTeammates();
                _mod.getButler().AddNearestPlayerToFriends(_mod, 5);
                DeathMenuChain.NeedtoStopTasksOnDeath = true;
                _mod.getCommandExecutor().execute("@test killall");

                //_autopilolTask = new KillAllTask(_mod.getPlayer().getBlockPos(), 200, true);
                //_mod.runUserTask(_autopilolTask);
                //AltoClef.getCommandExecutor().execute("@stop");
            }
        }

            //                mod.getButler().ClearTeammates();
        //                mod.getButler().AddNearestPlayerToFriends(mod,5);
            //_mod.runUserTask(new KillAllTask(_mod.getPlayer().getBlockPos(), 900));
        WhisperChecker.MessageResult chatParsedResult = this._whisperChecker.receiveChat(_mod, ourName, msg, serverAdress, serverMode);
        if (chatParsedResult != null){
            if(ButlerConfig.getInstance().debugChatParseResult)
                Debug.logMessage("Chatparsedresult"+chatParsedResult.toString() + chatParsedResult.serverExactPrediction);
            //if(ButlerConfig.getInstance().whisperFormatDebug) {
            if(chatParsedResult.serverExactPrediction!= null) {
                if(ButlerConfig.getInstance().debugChatParseResult)
                    Debug.logMessage("serverExactPrediction="+chatParsedResult.serverExactPrediction+",server="+chatParsedResult.server);
                if (chatParsedResult.serverExactPrediction == "exact") {
                    Debug.logInternal("Recieved EXACT msg from " + chatParsedResult.from);
                    _mod.getInfoSender().onStrongChatMessage(chatParsedResult);
                } else if (!chatParsedResult.server.contains("musteryworld")) {
                    Debug.logInternal("Recieved >" + chatParsedResult.serverExactPrediction + "< msg from " + chatParsedResult.from);
                    _mod.getInfoSender().onStrongChatMessage(chatParsedResult);
                }
            }
            //if (chatParsedResult.rank != null)
            //    Debug.logMessage("rank " + chatParsedResult.rank);
            //if   (chatParsedResult.clan != null)
            //    Debug.logMessage("clan " + chatParsedResult.clan);
            //if (chatParsedResult.starter_prefix != null)
            //    Debug.logMessage("prefix " + chatParsedResult.starter_prefix);
            //if (chatParsedResult.server != null)
            //    Debug.logMessage("server " + chatParsedResult.server);
            //if (chatParsedResult.serverMode != null)
            //    Debug.logMessage("servermode " + chatParsedResult.serverMode);
            //if (chatParsedResult.serverExactPrediction != null)
            //    Debug.logMessage("Predict " + chatParsedResult.serverExactPrediction);
            //}
        }
        else{
            WhisperChecker.MessageResult result = this._whisperChecker.receiveMessage(_mod, ourName, msg);
            if (    result != null) {
                this.receiveWhisper(result.from, result.message);
            } else if (ButlerConfig.getInstance().whisperFormatDebug){
                //Debug.logMessage("    Not Parsing: MSG format not found.");
            }
        }
    }
    private void captchaActionsPerform(){
        if (_captchaTimer.elapsed()) {
            _captchaTimer.reset();
            Perspective old_perspective = null;
            if(CaptchaSolvingMode.equals("GET_DATASET")) { //"SOLVE_MAXIMUM"; //GET_DATASET SOLVE_DATASET_ONLY

                stuck_fix_butler_allow = false;
                //_mod.getCommandExecutor().execute("@test killall");
                Debug.logMessage("КАПЧА СБОР ДАТАСЕТА!");
                old_perspective = MinecraftClient.getInstance().options.getPerspective();
                MinecraftClient.getInstance().options.setPerspective(Perspective.FIRST_PERSON);
                MapItemHelper.saveNonExistMapToDataset(_mod);
                //_mod.getCommandExecutor().execute("@idle");
                this.reJoin(3000, _mod);
                //DeathMenuChain.disconnect(MinecraftClient.getInstance());
            }else if(CaptchaSolvingMode.contains("SOLVE")){

                Debug.logMessage("КАПЧА РЕШЕНИЕ РЕЖИМ = "+CaptchaSolvingMode);
                old_perspective = MinecraftClient.getInstance().options.getPerspective();
                MinecraftClient.getInstance().options.setPerspective(Perspective.FIRST_PERSON);
                boolean neural_captcha_solve = false;
                if(CaptchaSolvingMode.equals("SOLVE_MAXIMUM") && _mod.getInfoSender().IsCallbackServerStarted()) {
                    neural_captcha_solve = true;
                }
                String captchaImageFilename = MapItemHelper.saveNonExistMapToDataset(_mod, neural_captcha_solve);
                String captcha_solving = "";
                if (captchaImageFilename.isBlank()){ // Checks if a String is whitespace, empty ("") or null.
                    Debug.logMessage("КАПЧА НЕ НАЙДЕНА В ДАТАСЕТЕ!");
                    if(neural_captcha_solve){
                        Debug.logMessage("Отправляем в инфо сендер!!");
                        //INFO SENDER
                        //captcha_solving = AltoClef.getInfoSender().getCaptchaSolving...
                        return;
                    }else {
                        Debug.logMessage("ВЫДАЕМ РАНДОМНЫЙ НОМЕР!");
                        //если ничего не передали в решение и не решено то фигач рандом от 1000 до 99999
                        captcha_solving = Integer.toString((ThreadLocalRandom.current().nextInt(1000, 99999 + 1))); //(min, max + 1);
                    }
                }else{
                    //captcha_solving = captchaImageFilename.split("\\.")[0];
                    captcha_solving = captchaImageFilename.split(Pattern.quote("."))[0].split(Pattern.quote("_"))[0];
                }

                if (!captcha_solving.isEmpty()) { //Checks if a String is empty ("") or null.
                    Debug.logMessage("ВВОД КАПЧИ / ENTERING SOLVED CAPTCHA ="+captcha_solving);
                    _mod.getMessageSender().enqueueChat(captcha_solving, MessagePriority.TIMELY);
                }

                if (old_perspective != null){
                    MinecraftClient.getInstance().options.setPerspective(old_perspective);
                }
            }
        }else{
            Debug.logMessage("КАПЧА УЖЕ РЕШАЕТСЯ!");
        }
    }
    //public static ScheduledExecutorService Scheduler = Executors.newSingleThreadScheduledExecutor();
    public void reJoin(long delay_before, AltoClef mod){
        mod.cancelUserTask();
        //_reconnectTimer
        Debug.logMessage("[SCHEDULER] WAITING START");
        Runnable rejoinOnDelayTask = new Runnable() {
            public void run() {
                mod.runUserTask(new GetToXZTask(-1000,1000));
                //MinecraftClient clientt = MinecraftClient.getInstance();
                //Debug.logMessage("[SCHEDULER] RECONNECT CHAIN ACTIVATED");
                DeathMenuChain._needDisconnect = true;
                DeathMenuChain._reJoinAfterDisconnect = true;
                DeathMenuChain._needToStopTasksOnReconnect = true;
                //Debug.logMessage("[SCHEDULER] CLIENT INFO SP"+clientt.isInSingleplayer());
                //DeathMenuChain._reconnecting = true;
                //DeathMenuChain._reconnectTimer.reset();
            }
        };

        //Runnable rejoinOnDelayTask =  new Thread(() -> {
//
        //    MinecraftClient clientt = MinecraftClient.getInstance();
        //    Debug.logMessage("[SCHEDULER] RECONNECT CHAIN ACTIVATED");
        //    _needDisconnect = true;
        //    Debug.logMessage("[SCHEDULER] CLIENT INFO SP"+clientt.isInSingleplayer());
        //    _reconnecting = true;
        //    _reconnectTimer.reset();
//
        //});
        //_prevServerEntry = MinecraftClient.getInstance().getCurrentServerEntry();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(rejoinOnDelayTask, delay_before, TimeUnit.MILLISECONDS);
        scheduler.shutdown();

    }
    private void receiveWhisper(String username, String message) {

        boolean debug = ButlerConfig.getInstance().whisperFormatDebug;
        // Ignore messages from other bots.
        if (message.startsWith(BUTLER_MESSAGE_START)) {
            if (debug) {
                Debug.logMessage("    Rejecting: MSG is detected to be sent from another bot.");
            }
            return;
        }

        if (_userAuth.isUserAuthorized(username)) {
            executeWhisper(username, message);
        } else {
            if (debug) {
                Debug.logMessage("    Rejecting: User \"" + username + "\" is not authorized.");
            }
            if (ButlerConfig.getInstance().sendAuthorizationResponse) {
                sendWhisper(username, ButlerConfig.getInstance().failedAuthorizationResposne.replace("{from}", username), MessagePriority.UNAUTHORIZED);
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isUserAuthorized(String username) {
        return _userAuth.isUserAuthorized(username);
    }
    public boolean AddUserToWhitelist(String username){return _userAuth.addUserToWhitelist(username);}
    public boolean RemoveUserFromWhitelist(String username){return _userAuth.removeUserFromWhitelist(username);}
    private final HashSet<String> _teammates = new HashSet<>();
    public void AddNearestPlayerToFriends(AltoClef mod, double radius){

        List<PlayerEntity> players = mod.getEntityTracker().getTrackedEntities(PlayerEntity.class);
        try {
            for (Entity entity : players) {
                if (entity instanceof PlayerEntity player && mod.getPlayer().getPos().isInRange(entity.getPos(),radius)) {

                    if (!player.equals(mod.getPlayer())) {
                        String name = player.getName().getString();
                        if (!this.isUserAuthorized(name)) {
                            boolean added = this.AddUserToWhitelist(name);
                            if(added){
                                _teammates.add(name);
                                Debug.logMessage("[КЕНТЫ] +игрок " + name + " =)");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Debug.logWarning("Ошибка при работе системы поиска тимеров: проигнорирована."); //TRS Weird exception caught and ignored while doing force field
            e.printStackTrace();
        }

    }
    public void ClearTeammates(){
        for(String name : _teammates) {
            boolean removed = this.RemoveUserFromWhitelist(name);
            if (removed) {
                _teammates.add(name);
                Debug.logMessage("[КЕНТЫ] -игрок " + name + "!");
        }
    }}
    public void onLog(String message, MessagePriority priority) {
        if (_currentUser != null) {
            sendWhisper(message, priority);
        }
    }

    public void onLogWarning(String message, MessagePriority priority) {
        if (_currentUser != null) {
            sendWhisper("[WARNING:] " + message, priority);
        }
    }

    public void tick() {
        // Nothing for now.
    }

    public String getCurrentUser() {
        return _currentUser;
    }

    public boolean hasCurrentUser() {
        return _currentUser != null;
    }

    private void executeWhisper(String username, String message) {
        String prevUser = _currentUser;
        _commandInstantRan = true;
        _commandFinished = false;
        _currentUser = username;
        sendWhisper("Command Executing: " + message, MessagePriority.TIMELY);
        String prefix = ButlerConfig.getInstance().requirePrefixMsg ? _mod.getModSettings().getCommandPrefix() : "";
        AltoClef.getCommandExecutor().execute(prefix+message, () -> {
            // On finish
            sendWhisper("Command Finished: " + message, MessagePriority.TIMELY);
            if (!_commandInstantRan) {
                _currentUser = null;
            }
            _commandFinished = true;
        }, e -> {
            sendWhisper("TASK FAILED: " + e.getMessage(), MessagePriority.ASAP);
            e.printStackTrace();
            _currentUser = null;
            _commandInstantRan = false;
        });
        _commandInstantRan = false;
        // Only set the current user if we're still running.
        if (_commandFinished) {
            _currentUser = prevUser;
        }
    }

    private void sendWhisper(String message, MessagePriority priority) {
        if (_currentUser != null) {
            sendWhisper(_currentUser, message, priority);
        } else {
            Debug.logWarning("Failed to send butler message as there are no users present: " + message);
        }
    }

    private void sendWhisper(String username, String message, MessagePriority priority) {
        _mod.getMessageSender().enqueueWhisper(username, BUTLER_MESSAGE_START + message, priority);
    }
}
