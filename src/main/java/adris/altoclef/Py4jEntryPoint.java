package adris.altoclef;

import adris.altoclef.butler.WhisperChecker;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.ui.MessagePriority;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.WorldHelper;
import baritone.api.pathing.calc.IPath;
import baritone.api.utils.BetterBlockPos;
import baritone.api.utils.Rotation;

import java.util.*;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class Py4jEntryPoint {
    AltoClef _mod;
    PythonCallback _cb;

    public Py4jEntryPoint(AltoClef mod)
    {
        _mod = mod;
        resetValues();
    }
    public void resetValues(){
        CentralGameInfoDict.put("server", "universal");
        CentralGameInfoDict.put("serverMode", "survival");
        CentralGameInfoDict.put("chatType", "lobby");
        //if(DeathMenuChain.ServerIp!=null)
        //    if(!DeathMenuChain.ServerIp.isEmpty())
        //        CentralGameInfoDict.put("server", DeathMenuChain.ServerIp);

    }

    public void setPerspective(int perspectiveNum) {
        //Perspective perspective = Perspective.values()[perspectiveNum] быстрое решение но нужна проверка
        Perspective perspective = Perspective.FIRST_PERSON;
        switch (perspectiveNum){
            case 0:
                perspective = Perspective.FIRST_PERSON;
                break;
            case 1:
                perspective = Perspective.THIRD_PERSON_BACK;
                break;
            case 2:
                perspective = Perspective.THIRD_PERSON_FRONT;
                break;
            default:
                Debug.logMessage("запрошена неизвестная перспектива: "+perspectiveNum);
        }
        MinecraftClient.getInstance().options.setPerspective(perspective);

    }
    //public Map<String,String> getIngameInfo(){
    //    Map<String,String> result_dict = new HashMap<>();
    //    result_dict.put("task_chain",getTaskChainString());
    //    result_dict.put("ground_block",getGroundBlock());
    //    result_dict.put("held_item",getHeldItem());
    //    return result_dict;
    //}

    public String getTaskChainString (){

        String tasks_string = "Ничего не происходит";
        try {
            if (_mod.getTaskRunner().getCurrentTaskChain() != null) {
                List<Task> tasks = _mod.getTaskRunner().getCurrentTaskChain().getTasks();
                if (tasks.size() > 0) {
                    tasks_string = "";
                    int i = 0;
                    for (Task task : tasks) {
                        tasks_string += (i+1)+") "+task.toString();
                        if(i<tasks.size()-1){tasks_string+="\n";}
                        i++;
                    }
                }

            }
        }catch (Exception e) {tasks_string = "Ошибка при получении списка игровых подзадач! Скрипт сломался!";}

        return tasks_string;
    }

    public String getGroundBlock (){
            if (AltoClef.inGame() && _mod.getPlayer()!=null && _mod.getWorld() != null) {
                //MinecraftClient.getInstance().options.setPerspective(Perspective.FIRST_PERSON);
                //MinecraftClient.getInstance().options.setPerspective(Perspective.THIRD_PERSON_BACK); //ЗАДНИЦА
                //MinecraftClient.getInstance().options.setPerspective(Perspective.THIRD_PERSON_FRONT); //ВСЕМ ПРИВЕТ
                String blockName = WorldHelper.getGroundBlockName(_mod);
                if(_mod.getPlayer().isOnGround() && blockName.equals("воздух")){
                    return "земля";
                }else{
                    return blockName;
                }


            }else{
                return "пустота";
            }
    }
    public String getHeldItem(){
            if (AltoClef.inGame() && _mod.getPlayer()!=null && _mod.getPlayer().getHandItems()!=null) {
                for (ItemStack item : _mod.getPlayer().getHandItems()){
                    if(item.getItem()!=null){

                        String itemName = item.getItem().getName().getString().toLowerCase();
                        if(!itemName.equals("воздух")){
                            if(item.contains(DataComponentTypes.CUSTOM_NAME)) {
                                String itemCustomName = item.getName().getString().toLowerCase();
                                return itemName+" (с названием " + itemCustomName+")";
                            }

                            //Debug.logMessage("ITEM CUSTOM NAME = "+itemCustomName);
                            return itemName;
                        }


                    }
                }
                return "ничего";

            }else{
                return "ничего";
            }
    }
    public String getInfo(){
        String result = "";
        for (String value : CentralGameInfoDict.values()){
            if(!value.isBlank()){
                result+=value+" ";
            }
        }
        if(callbackstarted)
            result+="CB=ON";
        return result.strip();
    }
    public String getInfo(String key){return getInfo(key,"");}
    public String getInfo(String key, String defolt){
        return CentralGameInfoDict.getOrDefault(key, defolt);
    }
    public void InitPythonCallback(){
        _cb = (PythonCallback) _mod.getGateway().getPythonServerEntryPoint(new Class[] {PythonCallback.class});
    }
    boolean callbackstarted = false;
    public boolean IsCallbackServerStarted(){
        boolean result = false;
        try {
            _cb.isStarted();
            result = true;
        }catch (Exception e) {}
        callbackstarted = result;
        return result;
    }
    public PythonCallback get_cb(){
        return _cb;
    }
    String _state = "starting";
    public String saayHellooo(String name) {
        return "Hello, " + name + "!" + Items.SOUL_SAND.getName().getString();
    }

    public String getState(){
        return _state;
    }
    public void setState(String state){
        _state = state;
    }
    public static boolean inGame(){
        return AltoClef.inGame();
    }
    public void onStrongChatMessage(WhisperChecker.MessageResult message){
        if(IsCallbackServerStarted()) {
            Map<String,String> messageDict = new HashMap<>();
            //if()
            messageDict.put("user",message.from);
            messageDict.put("msg",message.message);
            if(message.clan != null) messageDict.put("clan",message.clan);
            if(message.team != null) messageDict.put("team",message.team);
            if(message.starter_prefix != null) messageDict.put("pre",message.starter_prefix);
            if(message.rank != null) messageDict.put("rank",message.rank);
            if(message.serverExactPrediction != null) messageDict.put("precision",message.serverExactPrediction);
            if(message.server != null) messageDict.put("server",message.server);
            if(message.serverMode != null) messageDict.put("serverMode",message.serverMode);
            if(message.chat_type != null) messageDict.put("chat_type",message.chat_type);
            _cb.onVerifedChat(messageDict);
        }
    }
    public void ChatMessage(String msg){
        if(AltoClef.inGame())
        _mod.getMessageSender().enqueueChat(msg, MessagePriority.ASAP);
        //Object myPythonClass =  _mod.getGateway().getPythonServerEntryPoint(new Class[]{MyPythonClass.class});
    }
    public void RunInnerCommand(String command){
        AltoClef.getCommandExecutor().execute(command); //@stop
    }
    public void CaptchaSolvedSend(String msg, double accuracy){
        if(AltoClef.inGame()) {
            Debug.logMessage("GOT CAPTCHA SOLVING! >"+msg+"< acc="+accuracy);
            _mod.getMessageSender().enqueueChat(msg, MessagePriority.ASAP);
        }
        //Object myPythonClass =  _mod.getGateway().getPythonServerEntryPoint(new Class[]{MyPythonClass.class});
    }

    public void ExecuteCommand(String cmd){
        _mod.getCommandExecutor().execute(cmd);
    }
    public Map<String,String> CentralGameInfoDict = new HashMap<>();
    public void UpdateServerInfo(String field, String value){
        if (!field.isBlank() && !value.isBlank()) {
            if (CentralGameInfoDict.containsKey(field)) {
                if (!CentralGameInfoDict.get(field).equals(value)) {
                    Debug.logMessage("changed srv INFO f>" + field + ", v>" + value);
                    putInfo(field, value);
                }
            } else {
                Debug.logMessage("added srv INFO f>" + field + ", v>" + value);//, dict="+CentralGameInfoDict.toString());
                putInfo(field, value);
            }
        }
    }
    void putInfo(String field, String value){
        CentralGameInfoDict.put(field, value);
        if(IsCallbackServerStarted()) {
            _cb.onUpdateServerInfo(CentralGameInfoDict);
        }
    }

    public void onChatMessage(String msg){
        if(IsCallbackServerStarted()) {
            _cb.onChatMessage(msg);
        }
    }
    public void onDeath(String killer){
        if(IsCallbackServerStarted()) {
            _cb.onDeath(killer);
        }
    }
    public void onKill(String killed){
        if(IsCallbackServerStarted()) {
            _cb.onKill(killed);
        }
    }
    public void onCaptchaSolveRequest(byte[] image_bytes){
        if(IsCallbackServerStarted()) {
            Debug.logMessage("SENDING TO CALLBACK!");
            _cb.onCaptchaSolveRequest(image_bytes);
        }
    }
    public void onDamage(float amount){
        if(IsCallbackServerStarted()) {
            _cb.onDamage(amount);
        }
    }
    public Vec3d Nuller(){
        return null;
    }
    public Rotation getGoalRotation(){


        Rotation result = null;
        if (AltoClef.inGame()){
            Vec3d goal = getCurrentGoal();
            if(goal != null){
                Rotation targetrot = LookHelper.getLookRotation(_mod,goal);
                result = LookHelper.getLookRotation().subtract(targetrot);
            }
        }
        return result;
    }
    public Vec3d getCurrentGoal(){
        Vec3d result = null;
        if (AltoClef.inGame()) {
            Optional<IPath> pathq = _mod.getClientBaritone().getPathingBehavior().getPath();
            BetterBlockPos goalpos = null;

            if (pathq.isPresent()) {
                List<BetterBlockPos> pathlist = pathq.get().positions();
                if (pathlist.size() > 0) {
                    goalpos = pathlist.get(pathlist.size() - 1);
                    result = new Vec3d(goalpos.getX(), goalpos.getY(), goalpos.getZ());
                    //Debug.logMessage("goalpos x="+goalpos.getX()+" y="+goalpos.getY());
                }
            }
        }
        return result;
        //_mod.getClientBaritone().getCustomGoalProcess().getGoal().toString();
        //return _mod.getClientBaritone().getGetToBlockProcess().GetToBlockCalculationContext.;
        //_mod.getTaskRunner().getCurrentTaskChain().getTasks().
    }
    public void callPythonMethod(){
        //_mod.getGateway().getGateway().getCallbackClient().sendCommand("trysi"); //command, blocking?
    }
    public double getHealth(){
        return _mod.getPlayer() == null ? 0 :(double)_mod.getPlayer().getHealth();
    }
    public double getSpeed(){
        return _mod.getPlayer() == null ? 0 :(double)_mod.getPlayer().getMovementSpeed();
    }
    public Vec3d getSpeedVector(){
        return _mod.getPlayer() == null ? new Vec3d(0,0,0) : _mod.getPlayer().getVelocity();
    }
    public double getPitch(){
        return _mod.getPlayer() == null ? 0 : _mod.getPlayer().getPitch();
    }
    public double getPitch(double TickDelta){
        return _mod.getPlayer() == null ? 0 :_mod.getPlayer().getPitch((float)TickDelta);
    }
    public double getYaw(){
        return _mod.getPlayer() == null ? 0 :_mod.getPlayer().getYaw();
    }
    public double getYaw(double TickDelta){
        return _mod.getPlayer() == null ? 0 :_mod.getPlayer().getYaw((float)TickDelta);
    }
    public Vec3d getAngVector(){
        return _mod.getPlayer() == null ? new Vec3d(0,0,0) :_mod.getPlayer().getRotationVector();
    }
    public double getSpeedX(){
        return _mod.getPlayer() == null ? 0 :_mod.getPlayer().getVelocity().getX();
    }
    public double getSpeedY(){
        return _mod.getPlayer() == null ? 0 :_mod.getPlayer().getVelocity().getY();
    }
    public double getSpeedZ(){
        return _mod.getPlayer() == null ? 0 :_mod.getPlayer().getVelocity().getZ();
    }
    public double getSpeedXZ(){
        return _mod.getPlayer() == null ? 0 :Math.sqrt(Math.pow(_mod.getPlayer().getVelocity().getX(),2)+Math.pow(_mod.getPlayer().getVelocity().getZ(),2));
    }
    public Map<String, Map<String, Float>> getPlayersInfo(){
        PlayerEntity self = _mod.getPlayer();
        Map<String, Map<String, Float>> map = new HashMap<>();
        if (self != null) {
            Vec3d selfPos = self.getPos();
            if (selfPos != null) {
                List<AbstractClientPlayerEntity> playerList = _mod.getDamageTracker().getPlayerList();
                for (AbstractClientPlayerEntity player : playerList) {
                    if (player != null && player.getName() != null) {
                        Vec3d position = player.getPos();
                        Map<String, Float> playerInfoMap = new HashMap<>();
                        playerInfoMap.put("health", player.getHealth());
                        playerInfoMap.put("distance", (float) position.distanceTo(self.getPos()));
                        map.put(player.getName().getString(), playerInfoMap);
                    }
                }
            }
        }
        return map;
    }
    public String parsePlayersInfoToString(Map<String, Map<String, Float>> playersInfo) {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, Map<String, Float>> entry : playersInfo.entrySet()) {
            String playerName = entry.getKey();
            Map<String, Float> playerInfo = entry.getValue();

            Float health = playerInfo.get("health");
            Float distance = playerInfo.get("distance");

            result.append("Имя: ").append(playerName)
                    .append(", Здоровье: ").append(health != null ? health : "N/A")
                    .append(", Дистанция: ").append(distance != null ? distance : "N/A")
                    .append("\n");
        }

        return result.toString();
    }



}
