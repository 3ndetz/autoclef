package adris.altoclef.trackers.threats;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.SneakEvent;
import adris.altoclef.eventbus.events.multiplayer.TeleportEvent;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.time.TimerReal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.stream.Collectors;

public class ThreatTable {
    public AltoClef _mod;
    public class PlayerThreat {
        public PlayerThreat(int new_id){
            this.id = new_id;
        }
        public int id;
        public String name;
        public double combatTime = 10;
        public double damagedTime = 0.4;
        private final TimerReal lastAttackTimer = new TimerReal(damagedTime);
        private final TimerReal lastDamagedTimer = new TimerReal(damagedTime);
        private final TimerReal damagedTimer = new TimerReal(damagedTime);
        private final TimerReal combatEngagementTimer = new TimerReal(combatTime);
        private TimerReal shouldAvoidTimer = new TimerReal(20);
        private TimerReal shouldKillTimer = new TimerReal(50);
        private int lastAttackerEntityId = -1;
        private float lastDamageAmount = 0;
        public float cumulativeDamage = 0; // damage sum in last combat
        public float lastHealth = 20.0f;
        public Vec3d lastPos;
        public Vec3d lastRotationVec;
        public boolean sneak = false;
        public int sneakRate = 0;
        private final TimerReal shiftTimer = new TimerReal(5);

        // Add map to track potential attackers and their attack timers
        private final Map<Integer, TimerReal> potentialAttackers = new HashMap<>();

        public void addPotentialAttacker(int entityId) {
            potentialAttackers.putIfAbsent(entityId, new TimerReal(2.0));
            potentialAttackers.get(entityId).reset();
        }

        public List<Integer> getRecentAttackers() {
            List<Integer> recent = new ArrayList<>();
            for (Map.Entry<Integer, TimerReal> entry : potentialAttackers.entrySet()) {
                if (!entry.getValue().elapsed()) {
                    recent.add(entry.getKey());
                }
            }
            return recent;
        }
    }

    private final Map<Integer, String> entityIdToName = new HashMap<>();
    private final Map<String, PlayerThreat> playerThreats = new HashMap<>();

    public ThreatTable(AltoClef mod) {
        this._mod = mod;
        EventBus.subscribe(SneakEvent.class, evt -> onSneak(evt.entity, evt.sneak));
    }
    public void onSneak (Entity entity, boolean sneak) {
        if (entity instanceof PlayerEntity player) {

            PlayerThreat threat = playerThreats.get(player.getName().getString());
            if (threat != null) {
                if (sneak) {
                    threat.shiftTimer.reset();
                    threat.sneakRate += 1;
                }
                if (threat.sneakRate > 5) {
                    threat.shouldAvoidTimer.reset();
                }

                //Debug.logMessage("[SNEAK debug]Sneak detected: " + player.getName().getString()
                //        + ", sneak="+sneak +
                //        ", sneak rate="+ threat.sneakRate
                //        + ", shiftTimer " + threat.shiftTimer.getDuration()
                //        + ", avoidTimer" + threat.shouldAvoidTimer.elapsed() );

            }
        }

    }
    public void clearWorldData(){
        playerThreats.clear();
        entityIdToName.clear();
    }
    public int get(String name){
        return playerThreats.getOrDefault(name, new PlayerThreat(-1)).id;
    }
    public void registerPlayer(int entityId, boolean updateData) {
        if (playerThreats.entrySet().stream().anyMatch(e -> e.getValue().id == entityId)){
            // already exist
            return;
        }
        Entity entity = _mod.getWorld().getEntityById(entityId);
        if (entity instanceof PlayerEntity player) {
            String playerName = entity.getName().getString();
            if (playerName != null) {
                entityIdToName.put(entityId, playerName);
                playerThreats.putIfAbsent(playerName, new PlayerThreat(entityId));
                if (updateData)
                    updatePlayerData(playerName, player, false);
            }
        }
    }

    public void registerPlayer(int entityId) {
        registerPlayer(entityId, false);
    }

    public void recordAttackAnimation(int attackerEntityId) {
        registerPlayer(attackerEntityId);
        String attackerName = entityIdToName.get(attackerEntityId);
        if (attackerName != null) {
            PlayerThreat threat = playerThreats.get(attackerName);
            threat.lastAttackTimer.reset();

            // Check if this attacker is looking at any other players
            for (Map.Entry<String, PlayerThreat> entry : playerThreats.entrySet()) {
                if (!entry.getKey().equals(attackerName)) {
                        double lookingProbability = LookHelper.getLookingProbability(threat.lastPos, entry.getValue().lastPos, threat.lastRotationVec
                                //attacker.getEyePos(), target.getEyePos(), attacker.getRotationVec(0);
                        );//LookHelper.getLookingProbability((PlayerEntity)entityA, (PlayerEntity)damaged);

                        // If attacker is likely looking at this player, record them as potential attacker
                        if (lookingProbability > 0.7) {
                            entry.getValue().addPotentialAttacker(attackerEntityId);
                        }


                }
            }
        }
    }

    public double compareThreatProbablity(PlayerThreat a, PlayerThreat c){
        if (a != null && a.lastPos != null && a.lastRotationVec != null && c != null && c.lastPos != null) {
            double score = LookHelper.getLookingProbability(a.lastPos, c.lastPos, a.lastRotationVec);
            double distance = a.lastPos.distanceTo(c.lastPos);
            if (distance < 10) {
                score =  (10-distance) / 10;
            } else if (distance < 100) {
                score += (100-distance) / 100;
            } else {
                score -= 0.5;
            }
            return score;
        }
        return 0;
    }

    public int compareThreatsProbablity(PlayerThreat a, PlayerThreat b, PlayerThreat c){
        if (a != null && a.lastPos != null && a.lastRotationVec != null && b != null && b.lastPos != null && b.lastRotationVec != null && c != null && c.lastPos != null) {
            double probA = compareThreatProbablity(a, c);
            double probB = compareThreatProbablity(b, c);
            return Double.compare(probB, probA);
        }
        return 0;
    }

    public ArrayList<PlayerThreat> getAllRecentAttackers(String damagedName, boolean sorted){

        ArrayList<PlayerThreat> recentAttackers = new ArrayList<>(playerThreats.entrySet()
                .stream()
                .filter(a->!a.getValue().lastAttackTimer.elapsed() && !a.getKey().equals(damagedName))
                .collect(Collectors.toMap(e->e.getKey(), e->e.getValue())).values().stream().toList());

        if (!recentAttackers.isEmpty() && sorted) {
            PlayerThreat threat = playerThreats.get(damagedName);
            if (threat != null) {
                // Sort attackers by looking probability
                recentAttackers.sort((a, b) -> {
                    //Entity entityA = _mod.getWorld().getEntityById(a);
                    //Entity entityB = _mod.getWorld().getEntityById(b);
                    //Entity damaged = _mod.getWorld().getEntityById(damagedEntityId);
                    return compareThreatsProbablity(a, b, threat);
                });

            }

            //ebug.logMessage("Most likely attacker for " + damagedName + " is " + entityIdToName.get(threat.lastAttackerEntityId));
        }
        return recentAttackers;
    }
    public PlayerThreat getLastAttacker(String damagedName, boolean writeNew){
        ArrayList<PlayerThreat> recentAttackers = getAllRecentAttackers(damagedName);
        PlayerThreat threat = playerThreats.get(damagedName);
        if(!recentAttackers.isEmpty()){
            PlayerThreat lastAttackerThreat = recentAttackers.get(0);
            int attackerEntityId = lastAttackerThreat.id;
            if(attackerEntityId != -1) {
                if (writeNew) {
                    threat.lastAttackerEntityId = attackerEntityId;
                }
                return lastAttackerThreat;
            }
        }
        return null;
    }
    public ArrayList<PlayerThreat> getAllRecentAttackers(String damagedName){
        return getAllRecentAttackers(damagedName, true);
    }
    public void recordDamage(int damagedEntityId) {
        registerPlayer(damagedEntityId);
        String damagedName = entityIdToName.get(damagedEntityId);
        if (damagedName != null) {
            PlayerThreat threat = playerThreats.get(damagedName);
            threat.damagedTimer.reset();
            threat.lastDamagedTimer.reset();

            // Find the most likely attacker from recent attack animations

        }
    }

    public int recordDamageConfirmed(int damagedEntityId, float amount) {
        String damagedName = entityIdToName.get(damagedEntityId);
        if (damagedName != null) {
            PlayerThreat threat = playerThreats.get(damagedName);

            threat.lastDamageAmount = amount;
            threat.combatEngagementTimer.reset();
            // Update cumulative damage only if in combat
            if (!threat.combatEngagementTimer.elapsed()) {
                threat.cumulativeDamage += amount;
            } else {
                // Reset cumulative damage if starting new combat
                threat.cumulativeDamage = amount;
            }

            PlayerThreat attackerThreat = getLastAttacker(damagedName, true);
            if(attackerThreat != null){
                int attackerEntityId = attackerThreat.id;
                if(attackerEntityId != -1) {
                    if (attackerThreat.name != null && !attackerThreat.name.isBlank()) {
                        pursue(attackerThreat.name);
                    }
                    return attackerEntityId;
                }
            }
        }
        return -1;
    }

    public void updatePlayerData(String playerName, PlayerEntity entity, boolean register) {
        if (register)
            registerPlayer(entity.getId());
        PlayerThreat threat = playerThreats.get(playerName);
        if (threat != null){
            int entityId = entity.getId();
            if (threat.id != entityId) {
                entityIdToName.put(entityId, playerName);
                threat.id = entityId;
                entityIdToName.remove(threat.id);
            }
            if (threat.sneak != entity.isSneaking()){
                EventBus.publish(new SneakEvent(entity, entity.isSneaking()));
                threat.sneak = entity.isSneaking();
            }
            if (threat.sneakRate > 0 && threat.shiftTimer.elapsed()) {
                threat.sneakRate = 0;
            }
            if (entity.getPos() != null) {
                if (threat.lastPos == null) {

                    // Publish entity spawn event
                } else if (threat.lastPos != entity.getPos()) {
                    if (threat.lastPos.distanceTo(entity.getPos()) > 10) {
                        // Publish entity teleport event
                        EventBus.publish(new TeleportEvent(entity, threat.lastPos, entity.getPos()));
                    }
                }
                threat.lastPos = entity.getPos();
            }
            threat.lastHealth = entity.getHealth();
            // health change event, handled in tracker directly
            threat.lastRotationVec = entity.getRotationVec(0);
            threat.name = entity.getName().getString();
        }
    }
    public void updatePlayerData(String playerName, PlayerEntity entity) {
        updatePlayerData(playerName, entity, true);
    }

    public boolean isInCombat(String playerName) {
        PlayerThreat threat = playerThreats.get(playerName);
        return threat != null && (!threat.combatEngagementTimer.elapsed() || !threat.lastAttackTimer.elapsed() || !threat.lastDamagedTimer.elapsed() );
    }
    public boolean shouldAvoid(String playerName) {
        PlayerThreat threat = playerThreats.get(playerName);
        return threat != null && (!threat.shouldAvoidTimer.elapsed());
    }
    public boolean shouldAttack(String playerName) {
        PlayerThreat threat = playerThreats.get(playerName);
        return threat != null && (!threat.shouldKillTimer.elapsed());
    }
    public boolean avoid(String playerName) {
        PlayerThreat threat = playerThreats.get(playerName);
        if (threat != null) {
            threat.shouldAvoidTimer.reset();
            return true;
        }
        return false;
    }
    public boolean pursue(String playerName) {
        PlayerThreat threat = playerThreats.get(playerName);
        if (threat != null) {
            threat.shouldKillTimer.reset();
            return true;
        }
        return false;
    }
    public String getLastAttacker(String playerName) {
        PlayerThreat threat = playerThreats.get(playerName);
        if (threat != null && !threat.lastDamagedTimer.elapsed()) {
            return entityIdToName.get(threat.lastAttackerEntityId);
        }
        return null;
    }

    private Entity getEntityByPlayerName(String playerName) {
        for (Map.Entry<Integer, String> entry : entityIdToName.entrySet()) {
            if (entry.getValue().equals(playerName)) {
                return _mod.getWorld().getEntityById(entry.getKey());
            }
        }
        return null;
    }

    // Additional helper methods for getting threat information
    public float getCumulativeDamage(String playerName) {
        PlayerThreat threat = playerThreats.get(playerName);
        return threat != null ? threat.cumulativeDamage : 0f;
    }

    public float getCurrentHealth(String playerName) {
        PlayerThreat threat = playerThreats.get(playerName);
        return threat != null ? threat.lastHealth : 20.0f;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Table header
        sb.append("=== Threat Table Status ===\n");

        // No players registered
        if (playerThreats.isEmpty()) {
            sb.append("No players registered.\n");
            return sb.toString();
        }

        // Format each player's threat status
        for (Map.Entry<String, PlayerThreat> entry : playerThreats.entrySet()) {
            String playerName = entry.getKey();
            PlayerThreat threat = entry.getValue();

            // Player header
            sb.append("\n┌─ Player: ").append(playerName).append(" ─");
            // Fill with dashes to make header uniform length
            for (int i = 0; i < Math.max(0, 50 - playerName.length()); i++) sb.append("─");
            sb.append("┐\n");

            // Health and combat status
            sb.append("│ Health: ").append(String.format("%.1f/20.0", threat.lastHealth))
                    .append(isInCombat(playerName) ? " 🗡️ IN COMBAT" : " ⚔ PEACEFUL")
                    .append("\n");

            // Damage information
            sb.append("│ Last Damage: ").append(String.format("%.1f", threat.lastDamageAmount))
                    .append(" (Total in combat: ").append(String.format("%.1f", threat.cumulativeDamage))
                    .append(")\n");

            // Timers status
            sb.append("│ Timers:\n");
            sb.append("│   • Last Attack: ").append(formatTimer(threat.lastAttackTimer)).append("\n");
            sb.append("│   • Last Damaged: ").append(formatTimer(threat.lastDamagedTimer)).append("\n");
            sb.append("│   • Damaged: ").append(formatTimer(threat.damagedTimer)).append("\n");
            sb.append("│   • Combat: ").append(formatTimer(threat.combatEngagementTimer)).append("\n");

            // Recent attackers
            List<Integer> recentAttackers = threat.getRecentAttackers();
            if (!recentAttackers.isEmpty()) {
                sb.append("│ Recent Attackers:\n");
                for (Integer attackerId : recentAttackers) {
                    String attackerName = entityIdToName.get(attackerId);
                    if (attackerName != null) {
                        sb.append("│   • ").append(attackerName)
                                .append(" (ID: ").append(attackerId).append(")\n");
                    }
                }
            }

            // Last attacker
            if (threat.lastAttackerEntityId != -1) {
                String lastAttackerName = entityIdToName.get(threat.lastAttackerEntityId);
                if (lastAttackerName != null) {
                    sb.append("│ Last Attacker: ").append(lastAttackerName)
                            .append(" (ID: ").append(threat.lastAttackerEntityId).append(")\n");
                }
            }

            // Bottom border
            sb.append("└");
            for (int i = 0; i < 60; i++) sb.append("─");
            sb.append("┘\n");
        }

        return sb.toString();
    }

    /**
     * Helper method to format timer status
     */
    private String formatTimer(TimerReal timer) {
        if (timer.elapsed()) {
            return "Elapsed";
        }
        return String.format("Active (%.1fs left)", timer.getDuration());
    }
}