package adris.altoclef.trackers.threats;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.time.TimerReal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.*;

public class ThreatTable {
    public AltoClef _mod;
    private class PlayerThreat {
        public PlayerThreat(int new_id){
            this.id = new_id;
        }
        public int id;
        public double combatTime = 4;
        public double damagedTime = 0.4;
        private final TimerReal lastAttackTimer = new TimerReal(damagedTime);
        private final TimerReal lastDamagedTimer = new TimerReal(damagedTime);
        private final TimerReal damagedTimer = new TimerReal(damagedTime);
        private final TimerReal combatEngagementTimer = new TimerReal(combatTime);
        private int lastAttackerEntityId = -1;
        private float lastDamageAmount = 0;
        public float cumulativeDamage = 0; // damage sum in last combat
        public float health = 20.0f;

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
    }
    public int get(String name){
        return playerThreats.getOrDefault(name, new PlayerThreat(-1)).id;
    }
    public void registerPlayer(int entityId) {
        // Update player health if available
        Entity entity = _mod.getWorld().getEntityById(entityId);
        if (entity instanceof PlayerEntity) {
            String playerName = entity.getName().getString();
            if (playerName != null) {
                entityIdToName.put(entityId, playerName);
                playerThreats.putIfAbsent(playerName, new PlayerThreat(entityId));
                PlayerThreat threat = playerThreats.get(playerName);
                threat.health = ((PlayerEntity) entity).getHealth();
            }
        }
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
                    Entity attacker = _mod.getWorld().getEntityById(attackerEntityId);
                    Entity target = getEntityByPlayerName(entry.getKey());

                    if (attacker instanceof PlayerEntity && target instanceof PlayerEntity) {
                        double lookingProbability = LookHelper.getLookingProbability(
                                (PlayerEntity)attacker,
                                (PlayerEntity)target
                        );

                        // If attacker is likely looking at this player, record them as potential attacker
                        if (lookingProbability > 0.7) {
                            entry.getValue().addPotentialAttacker(attackerEntityId);
                        }
                    }
                }
            }
        }
    }

    public void recordDamage(int damagedEntityId) {
        registerPlayer(damagedEntityId);
        String damagedName = entityIdToName.get(damagedEntityId);
        if (damagedName != null) {
            PlayerThreat threat = playerThreats.get(damagedName);
            threat.damagedTimer.reset();
            threat.lastDamagedTimer.reset();
            threat.combatEngagementTimer.reset();
            // Find the most likely attacker from recent attack animations
            List<Integer> recentAttackers = threat.getRecentAttackers();
            if (!recentAttackers.isEmpty()) {
                // Sort attackers by looking probability
                recentAttackers.sort((a, b) -> {
                    Entity entityA = _mod.getWorld().getEntityById(a);
                    Entity entityB = _mod.getWorld().getEntityById(b);
                    Entity damaged = _mod.getWorld().getEntityById(damagedEntityId);

                    if (entityA instanceof PlayerEntity && entityB instanceof PlayerEntity && damaged instanceof PlayerEntity) {
                        double probA = LookHelper.getLookingProbability((PlayerEntity)entityA, (PlayerEntity)damaged);
                        double probB = LookHelper.getLookingProbability((PlayerEntity)entityB, (PlayerEntity)damaged);
                        return Double.compare(probB, probA);
                    }
                    return 0;
                });
                int attackerEntityId = recentAttackers.get(0);
                // Set the most likely attacker
                threat.lastAttackerEntityId = attackerEntityId;


                //ebug.logMessage("Most likely attacker for " + damagedName + " is " + entityIdToName.get(threat.lastAttackerEntityId));
            }
        }
    }

    public void recordDamage(int damagedEntityId, int attackerEntityId, float amount) {
        String damagedName = entityIdToName.get(damagedEntityId);
        if (damagedName != null) {
            PlayerThreat threat = playerThreats.get(damagedName);

            threat.lastDamageAmount = amount;

            // Update cumulative damage only if in combat
            if (!threat.combatEngagementTimer.elapsed()) {
                threat.cumulativeDamage += amount;
            } else {
                // Reset cumulative damage if starting new combat
                threat.cumulativeDamage = amount;
            }

            // Update health
            Entity damaged = _mod.getWorld().getEntityById(damagedEntityId);
            if (damaged instanceof PlayerEntity) {
                threat.health = ((PlayerEntity) damaged).getHealth();
            }
        }
    }

    public boolean isInCombat(String playerName) {
        PlayerThreat threat = playerThreats.get(playerName);
        return threat != null && (!threat.combatEngagementTimer.elapsed() || !threat.lastAttackTimer.elapsed() || !threat.lastDamagedTimer.elapsed() );
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
        return threat != null ? threat.health : 20.0f;
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
            sb.append("│ Health: ").append(String.format("%.1f/20.0", threat.health))
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