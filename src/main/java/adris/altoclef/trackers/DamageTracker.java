package adris.altoclef.trackers;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.Py4jEntryPoint;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.*;
import adris.altoclef.util.helpers.InputHelper;
import adris.altoclef.util.time.TimerReal;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DamageTracker extends Tracker {
    private final HashMap<String, PlayerEntity> _playerMap = new HashMap<>();
    private final HashMap<String, Float> _prevPlayerHealth = new HashMap<>();

    private List<AbstractClientPlayerEntity> _prevPlayerList = new ArrayList<>();
    public String _lastAttackingPlayerName = "undefined";
    private Vec3d _lastAttackingPlayerEye;
    private Vec3d _lastAttackingPlayerDir;
    private Vec3d _lastAttackingPlayerMyEye;
    private Vec3d _lastAttackingPlayerMyDir;
    private double _lastAttackingPlayerIsLookingProbablity;
    private double _lastAttackingPlayerMyLookingProbablity;
    private String _lastRemovedPlayer = "";

    public DamageTracker(TrackerManager manager) {
        super(manager);
        EventBus.subscribe(ClientDamageEvent.class, evt -> onClientDamage());
        EventBus.subscribe(ClientHandSwingEvent.class, evt -> onHandSwing());
        //EventBus.subscribe(ChangeHealthEvent.class, evt -> onChangeHealth(_mod.getPlayer().getName().getString(),evt.OldHealth,evt.NewHealth));
        //EventBus.subscribe(DeathEvent.class, evt -> OnDeath());
    }

    public final TimerReal _resentDamageTimer = new TimerReal(0.05);
    public final TimerReal _attackCheckTimer = new TimerReal(0.7);
    private PlayerEntity _attackerCheck;
    private boolean _attackerCheckHit = false;

    public void DamageTimerReset(){
        _resentDamageTimer.reset();

    }
    public boolean WasResentlyDamaged(String name){

        if(_mod.getPlayer().getName().getString().equals(name))
            return !_resentDamageTimer.elapsed();
        else {
            if(_playerMap.get(name)!= null && _playerMap.get(name).getRecentDamageSource() != null) // в идеале бы добавить проверку есть ли игрок в списке
            {
                //Debug.logMessage("ПОЛУЧАЛ УРОН НЕДАВНО "+name);
                return true;
            }
            else{
                //Debug.logMessage("НЕ ПОЛУЧАЛ УРОН НЕДАВНО "+name);
                return false;
            }
        }
    }
    public void onHandSwing(){
        LivingEntity attacking = _mod.getPlayer().getAttacking();
        if(attacking != null){//если есть атакуемый
            //attacking.getHealth();
        }
    }
    public void onMeleeAttack(Entity target){
        if(target!= null && target instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) target;
            _attackerCheck = player;
            _attackCheckTimer.reset();
            _attackerCheckHit = true;
            //if(_prevPlayerList)
        }
        //ToolItem handtool = (ToolItem) _mod.getPlayer().getActiveItem().getItem();
        //handtool.getMaterial().getAttackDamage();
    }



    public void onClientDamage(){
        //Debug.logMessage("ПУК!");
        _resentDamageTimer.reset();
        //Debug.logMessage("Получен урон "+Py4jEntryPoint.lastDamage);
    }
    public double lastDamage = 0.0;
    public void onChangeHealth(String name, float oldHealth, float newHealth){
        float changed = newHealth - oldHealth;

        if(newHealth==0.0f &&WasResentlyDamaged(name)){ // 100% смэрт
            onDeath(name);
        } else if(Math.floor(changed*20)==0){
            //нет существенных изменений
        }else if(changed>0){ //получили хил
            //Debug.logMessage("Захилились на "+changed);
            if((oldHealth+changed==_mod.getPlayer().getMaxHealth()||_lastRemovedPlayer.equals(name))&&WasResentlyDamaged(name)){
                onDeath(name);
            }

            //EventBus.publish(new DeathEvent());
        }else {
            //Debug.logMessage("Ебнулись на "+(-changed)+" таймер дамага "+WasResentlyDamaged());
            lastDamage = -changed;
            if (WasResentlyDamaged(name)) {
                onDamage(name,-changed);
                //Debug.logMessage("ДОСТОВЕРНО УРОН ПОЛУЧЕН = "+lastDamage);
            }
        }
    }
    public void onDeath(String name){

        //Debug.logMessage("---===*"+name +" ЗДОХ*===---");
        if (_mod.getPlayer().getName().getString().equals(name))
        {
            Debug.logMessage("чо ? здохла "+_lastAttackingPlayerIsLookingProbablity+" mda "+_lastAttackingPlayerName);
            String killername = "undefined";
            if(_lastAttackingPlayerName != null && _lastAttackingPlayerIsLookingProbablity>0.70D) {killername =_lastAttackingPlayerName;}
            onClientDeath(killername);
        }
        else if (_lastAttackingPlayerName != null && _lastAttackingPlayerName.equals(name)  && _lastAttackingPlayerMyLookingProbablity>0.70D)
        {
            Debug.logMessage("чо ? килл "+_lastAttackingPlayerMyLookingProbablity+" mda "+_lastAttackingPlayerName);
            onClientKill(name);
        }
    }
    public void onClientDeath(String killername){
        Debug.logMessage("YA ZDOXLA OT "+killername);
        if(!killername.equals("undefined")){
            _mod.getInfoSender().onDeath(killername);
        }
        else if(Math.random()>0.5d){
            _mod.getInfoSender().onDeath("неизвестный");
        }
    }
    public void onClientKill(String name){
        Debug.logMessage("GOTOV -"+name);
        if(!name.equals("undefined")){
            _mod.getInfoSender().onKill(name);}
    }

    public void onDamage(String name, float amount){
        if(name.equals(_mod.getPlayer().getName().getString())){
            _mod.getInfoSender().onDamage(amount);
        }
        if(amount>1&&name.equals(_lastAttackingPlayerName)&&!_attackCheckTimer.elapsed()){
            Debug.logMessage("Урон по "+_lastAttackingPlayerName+" прошел!");
            _attackerCheckHit = false;
        }
        //Debug.logMessage("Получен урон "+name+ " "+amount);
    }
    public double getLookingProbability(PlayerEntity plyFrom, PlayerEntity plyTo){

        return getLookingProbability(plyFrom.getEyePos(),plyTo.getEyePos(),plyFrom.getRotationVec(0));
        //return dot > 0.95D;
    }
    public double getLookingProbability(Vec3d eyeFrom, Vec3d eyeTo, Vec3d RotationFrom){
        Vec3d toEntity = eyeTo.subtract(eyeFrom);
        double dot = toEntity.normalize().dotProduct(RotationFrom);
        return dot; //0.8 60 град, 0.9 30 град 0.95 15 град (точный взгляд
    }
    public void tick() {

        if(AltoClef.inGame() && MinecraftClient.getInstance().world != null) {
            List<AbstractClientPlayerEntity> playerList = MinecraftClient.getInstance().world.getPlayers();
            if(!_prevPlayerList.equals(playerList)) {
                _prevPlayerList.removeAll(playerList);
                for(AbstractClientPlayerEntity player : _prevPlayerList){
                    //Debug.logMessage("Удалились игроки: "+player);
                    if (player != null) {
                        _lastRemovedPlayer = player.getName().getString();
                        if(WasResentlyDamaged(_lastRemovedPlayer)){
                            onDeath(_lastRemovedPlayer);
                        }
                        //Debug.logMessage("Удалились игроки: " + _lastRemovedPlayer+" WasResentlyDamaged "+WasResentlyDamaged(_lastRemovedPlayer));
                    }
                }
                _prevPlayerList.clear();
                for (AbstractClientPlayerEntity player : playerList) {
                    _prevPlayerList.add(player);
                    if (player != null && player.getName() != null) {
                        String name = player.getName().getString();
                        if(!_prevPlayerHealth.containsKey(name))
                            _prevPlayerHealth.put(name, player.getHealth());
                        float prevHealth = _prevPlayerHealth.get(name);
                        float health = player.getHealth();
                        //if (player != _mod.getPlayer())
                        //Debug.logMessage("найден игрок " + name + " хп " + health + " прошлое хп "+prevHealth);
                        _playerMap.put(name, player);
                    }
                    //Debug.logMessage("Число элементов "+playerList.size());
                }
            }else{
                //Debug.logMessage("листы равны.");
            }
            LivingEntity attacking = _mod.getPlayer().getAttacking();
            if(attacking != null && attacking instanceof PlayerEntity) {
                _lastAttackingPlayerName = attacking.getName().getString();
                //_lastAttackingPlayerEye = attacking.getEyePos();
                //_lastAttackingPlayerDir = attacking.getRotationVector();
                if(_mod.getPlayer() != null) {
                    //_lastAttackingPlayerMyDir = _mod.getPlayer().getRotationVector();
                    //_lastAttackingPlayerMyEye = _mod.getPlayer().getEyePos();
                    _lastAttackingPlayerIsLookingProbablity = getLookingProbability((PlayerEntity)attacking, _mod.getPlayer());
                    _lastAttackingPlayerMyLookingProbablity = getLookingProbability(_mod.getPlayer(), (PlayerEntity) attacking);
                }
                if(_attackerCheckHit&&_attackCheckTimer.elapsed()){
                    Debug.logMessage("Урон по "+_lastAttackingPlayerName+" НЕ прошел!");
                    _attackerCheckHit = false;
                }
                //Debug.logMessage("ыы "+_lastAttackingPlayerName);

            }

            for (AbstractClientPlayerEntity player : playerList) {
                if (player != null && player.getName() != null) {
                    String name = player.getName().getString();
                    if(_lastRemovedPlayer.equals(name)) _lastRemovedPlayer = "";
                    if(!_prevPlayerHealth.containsKey(name))
                        _prevPlayerHealth.put(name, player.getHealth());
                    float prevHealth = _prevPlayerHealth.get(name);
                    float health = player.getHealth();
                    if(prevHealth!=player.getHealth()){
                        onChangeHealth(name,prevHealth,health);
                        _prevPlayerHealth.put(name, player.getHealth());
                    }
                    //if (InputHelper.isKeyPressed(71)){
                        //    //Debug.logMessage("looking "+name+" ?"+isLookingAt(player,_mod.getPlayer()));
                        //}
                }

            }
        }
    }

    @Override
    protected synchronized void updateState() {
        //Debug.logMessage("Обновлен стейт");
    }
    @Override
    protected void reset() {
        // Dirty clears everything else.

    }

}
