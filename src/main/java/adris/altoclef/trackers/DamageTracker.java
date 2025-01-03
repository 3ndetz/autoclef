package adris.altoclef.trackers;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.*;
import adris.altoclef.tasks.stupid.MurderMysteryTask;
import adris.altoclef.trackers.threats.DamageTrackerStrategy;
import adris.altoclef.trackers.threats.PlayerThreat;
import adris.altoclef.trackers.threats.ThreatTable;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.time.TimerReal;

import java.util.*;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import static adris.altoclef.util.helpers.LookHelper.getLookingProbability;

public class DamageTracker extends Tracker {
    private final HashMap<String, PlayerEntity> _playerMap = new HashMap<>();
    private final HashMap<String, Float> _prevPlayerHealth = new HashMap<>();
    private final TimerReal _recentDamageTimer = new TimerReal(0.05);
    private List<AbstractClientPlayerEntity> _prevPlayerList = new ArrayList<>();
    public String _lastAttackingPlayerName = "undefined";
    private double _lastAttackingPlayerIsLookingProbability;
    private double _lastAttackingPlayerMyLookingProbability;
    public final TimerReal _attackCheckTimer = new TimerReal(0.7);
    //public final TimerReal _clientEventLimitTimer = new TimerReal(1.2);
    //TODO do something with too often onClientDeath / onClientKill (may spam bcs bad system work)
    private PlayerEntity _attackerCheck;
    private boolean _attackerCheckHit = false;
    // Add a map to track recent damage for each player
    private final HashMap<String, TimerReal> _playerDamageTimers = new HashMap<>();
    private final HashMap<String, TimerReal> _playerAttackTimers = new HashMap<>();
    private final float DEATH_HEALTH_THRESHOLD = 10.0f; // Configurable threshold
    private final float FULL_HEALTH = 20.0f;
    public final ThreatTable threatTable = new ThreatTable(_mod);

    public DamageTracker(TrackerManager manager) {
        super(manager);
        EventBus.subscribe(ClientDamageEvent.class, evt -> onClientDamage());
        EventBus.subscribe(ClientHandSwingEvent.class, evt -> onClientHandSwing());
        EventBus.subscribe(DamageEvent.class, evt -> onAnyDamage(evt._entity));
        EventBus.subscribe(AnimEvent.class, evt -> onSwing(evt._entity, evt._type));

        //EventBus.subscribe(PlayerRemoveEvent.class, this::onPlayerRemove);

    }
    public void onClientHandSwing(){
        LivingEntity attacking = _mod.getPlayer().getAttacking();
        if(attacking != null){//если есть атакуемый
            recordOnSwing(_mod.getPlayer());
            //attacking.getHealth();
        }
    }

    public void recordOnSwing(Entity entity){


        if(_mod.getBehaviour().getDamageTrackerStrategy().equals(DamageTrackerStrategy.MurderMystery)){
            // add check
            if (entity instanceof PlayerEntity player) {
                if(MurderMysteryTask.hasKillerWeapon(player)){
                    threatTable.recordAttackAnimation(entity.getId());
                }
            }
            return;
        }
        threatTable.recordAttackAnimation(entity.getId());
    }
    public void onClientDamage() {
        _recentDamageTimer.reset();
        onAnyDamage(_mod.getPlayer());
    }
    public void onAnyDamage(Entity entity){
        threatTable.recordDamage(entity.getId());
    }
    public void onSwing(Entity entity, AnimType type){
        //Debug.logMessage("[DEBUG] Registered swing: " + entity.getName().getString() + " anim " + type.toString() );
        if(type.equals(AnimType.SWING_MAIN_HAND)) {
            recordOnSwing(entity);
        }
        //if(type.equals(AnimType.TAKE_DAMAGE)) {
            //    threatTable.recordDamage(entity.getId());
            //}
    }

    private void updatePlayerDamageTimer(String playerName) {
        _playerDamageTimers.computeIfAbsent(playerName, k -> new TimerReal(2.0)); // 2 second window
        _playerDamageTimers.get(playerName).reset();
    }

    public boolean wasRecentlyDamaged(String name) {
        return threatTable.isInCombat(name);
        //TimerReal timer = _playerDamageTimers.get(name);
        //if (timer == null) return false;
        //return !timer.elapsed();
    }


    public void onClientDeath(String killername){
        Debug.logMessage("confirmed death from "+killername);
        if(!killername.equals("undefined")){
            _mod.getInfoSender().onDeath(killername);
        }
        else if(Math.random()>0.5d){
            _mod.getInfoSender().onDeath("неизвестный");
        }
    }
    public void onClientKill(String name) {
        Debug.logMessage("confirmed kill -"+name);
        if(!name.equals("undefined")){
            _mod.getInfoSender().onKill(name);}
    }
    public ThreatTable getThreatTable() {
        return threatTable;
    }
    public String getThreatStatus() {
        return threatTable.toString();
    }
    public void onDamage(String name, float amount){
        if(name.equals(_mod.getPlayer().getName().getString())){
            _mod.getInfoSender().onDamage(amount);
        }
        if(amount>1&&name.equals(_lastAttackingPlayerName) && !_attackCheckTimer.elapsed()){
            Debug.logMessage("Урон по "+_lastAttackingPlayerName+" прошел!");
            _attackerCheckHit = false;
        }
        int id = threatTable.get(name);
        if (id != -1) {
            threatTable.recordDamageConfirmed(id, amount);
        }
        String att_name = threatTable.getLastAttacker(name);

        if (att_name != null) {
            Debug.logMessage("Получен урон игроком "+name+ " от "+att_name + ": " + amount);
        }
    }


    public void onChangeHealth(String name, float oldHealth, float newHealth) {
        //Debug.logMessage("Health change for " + name + ": " + oldHealth + " -> " + newHealth);
        float healthDelta = newHealth - oldHealth;

        // Case 1: Direct death detection (rare case where health hits 0)
        if (wasRecentlyDamaged(name)) {
            if (newHealth <= 0.0f) {
                onDeath(name);
                return;
            }

            // Case 2: Respawn detection (low health to full health while recently damaged)
            if (newHealth >= FULL_HEALTH && oldHealth <= DEATH_HEALTH_THRESHOLD) {
                onDeath(name);
                return;
            }

            // Case 3: too many heal at a time?
            if (healthDelta > DEATH_HEALTH_THRESHOLD) {
                onDeath(name);
                return;
            }
        }

        // Track damage
        if (healthDelta < 0) {
            updatePlayerDamageTimer(name);
            onDamage(name, -healthDelta);
        }
    }

    public void onClientMeleeAttack(Entity target){
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

    private void onDeath(String name, String killerName) {
        Debug.logMessage("Death: " + killerName + " killed " + name + ".");

        if (_mod.getPlayer().getName().getString().equals(name)) {
            // Player death

            onClientDeath(killerName);
        } else if (isPlayerKill(name)) {
            // Kill by player
            onClientKill(name);
        }

        // Clear damage timer after death
        _playerDamageTimers.remove(name);
    }


    private void onDeath(String name) {
        String killerName = determineKiller(name);
        onDeath(name, killerName);
    }
    public void onPlayerRemove(AbstractClientPlayerEntity player){
        if (player != null) {
            String playerName = player.getName().getString();
            float lastHealth = _prevPlayerHealth.getOrDefault(playerName, FULL_HEALTH);


            switch (_mod.getBehaviour().getDamageTrackerStrategy()) {
                case Smart: {
                    if (wasRecentlyDamaged(playerName)) {
                        onDeath(playerName);
                    }
                    break;
                }
                case Vanilla: {
                    // If player disconnected while at low health and recently damaged
                    if (lastHealth <= DEATH_HEALTH_THRESHOLD && wasRecentlyDamaged(playerName)) {
                        onDeath(playerName);
                    }
                    break;
                }
                case MurderMystery:
                    // TODO find killer with murder weapons
                    PlayerThreat threat = threatTable.getLastAttacker(playerName, true);
                    if (threat != null && threat.name != null) {
                        onDeath(playerName, threat.name);
                    } else {
                        onDeath(playerName);
                    }
                default:
                    break;
            }

            if(_mod.getBehaviour().getDamageTrackerStrategy().equals(DamageTrackerStrategy.MurderMystery)){

            }else if(_mod.getBehaviour().getDamageTrackerStrategy().equals(DamageTrackerStrategy.Smart)){

            }
        }
    }

    private String determineKiller(String name) {
        String killer = threatTable.getLastAttacker(name);
        if(killer!= null){
            return killer;
        }
        //if (_lastAttackingPlayerName != null && _lastAttackingPlayerIsLookingProbability > 0.70D) {
        //    return _lastAttackingPlayerName;
        //}
        return "undefined";
    }

    private boolean isPlayerKill(String name) {
        return _lastAttackingPlayerName != null &&
                _lastAttackingPlayerName.equals(name) &&
                _lastAttackingPlayerMyLookingProbability > 0.70D;
    }

    public void tick() {
        if (!AltoClef.inGame() || MinecraftClient.getInstance().world == null) return;

        List<AbstractClientPlayerEntity> currentPlayers = MinecraftClient.getInstance().world.getPlayers();

        // Handle player removals (possible death by disconnect)
        if (!_prevPlayerList.equals(currentPlayers)) {
            Set<AbstractClientPlayerEntity> removedPlayers = new HashSet<>(_prevPlayerList);
            removedPlayers.removeAll(currentPlayers);

            for (AbstractClientPlayerEntity player : removedPlayers) {
                onPlayerRemove(player);
            }

            // Update player tracking
            _prevPlayerList = new ArrayList<>(currentPlayers);
            updatePlayerStates(currentPlayers);
        }

        // Regular data updates for connected players
        for (AbstractClientPlayerEntity player : currentPlayers) {
            if (player != null && player.getName() != null) {
                String name = player.getName().getString();
                threatTable.updatePlayerData(name, player);
                float prevHealth = _prevPlayerHealth.getOrDefault(name, player.getHealth());
                float currentHealth = player.getHealth();

                if (prevHealth != currentHealth) {
                    onChangeHealth(name, prevHealth, currentHealth);
                    _prevPlayerHealth.put(name, currentHealth);
                }
            }

        }

        updateAttackingPlayerInfo();
    }

    private void updatePlayerStates(List<AbstractClientPlayerEntity> currentPlayers) {
        for (AbstractClientPlayerEntity player : currentPlayers) {
            if (player != null && player.getName() != null) {
                String name = player.getName().getString();
                _playerMap.put(name, player);
                _prevPlayerHealth.putIfAbsent(name, player.getHealth());
            }
        }
    }

    private void updateAttackingPlayerInfo() {
        LivingEntity attacking = _mod.getPlayer().getAttacking();
        if (attacking instanceof PlayerEntity) {
            _lastAttackingPlayerName = attacking.getName().getString();
            _lastAttackingPlayerIsLookingProbability = LookHelper.getLookingProbability(
                    (PlayerEntity)attacking, _mod.getPlayer());
            _lastAttackingPlayerMyLookingProbability = LookHelper.getLookingProbability(
                    _mod.getPlayer(), (PlayerEntity)attacking);
        }
    }

    @Override
    protected synchronized void updateState() {
        //Debug.logMessage("Обновлен стейт");
    }
    @Override
    protected void reset() {
        // Runs on world change
        threatTable.clearWorldData();
        _prevPlayerHealth.clear();
        _playerMap.clear();
    }

    public List<AbstractClientPlayerEntity> getPlayerList() {
        return _prevPlayerList;
    }
}
