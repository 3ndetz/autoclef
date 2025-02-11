package adris.altoclef.util.agent;

/**
 * Represents a current pipeline that is higher level than a task chain.
 *
 * Should have method description with short description of a mode and current behavior
 *
 */
public enum Pipeline {

    SpeedRun("Minecraft speedrunning mode - focuses on beating the game by killing the Ender Dragon and firstly gathering need resources. "
    + getCombatDesc() + " If no tasks is present - run @game walkthrough."),

    SkyWars("SkyWars game mode - strategy for surviving and winning in SkyWars minigame. Players spawn on separate sky islands with chests. Goal is to be the last survivor. The center 'mid' has valuable loot. Features chest refills and mob spawns. "
    + getMinigameCombatDesc() + " If no tasks is present - run @game sw"),

    MurderMystery("Murder Mystery mode - playing the Murder Mystery minigame. Roles: killer (must kill all), innocent (collects resources), detective (must kill killer only). "
    + getMinigameCombatDesc() + " If no tasks is present - run @game mm. You cannot build/break blocks during this mode!"), 

    BattleRoyale("Battle Royale (or Yandere) mode pipeline focusing on trick with unexpected behaviour change: be sweet and offer some items or cute ideas and then aggressively kill the target showing aggressive gestures and building graves, then say how weak was the target."
    + getCombatDesc() + " If no tasks is present - run @game yandere"),

    None("no specific pipeline active. run @game command to activate the pipeline.");

    private final String description;

    Pipeline(String description) {
        this.description = description;
    }

    /**
     * Get the description of the current pipeline mode and its behavior
     * @return String describing the pipeline's purpose and behavior 
     */
    public String getDescription() {
        return this.description;
    }

    private static String getMinigameCombatDesc() {
        return getCombatDesc() +
            "Since this is a minigame mode, player data is reset each round - forget previous player info when game restarts.";
    }

    private static String getCombatDesc() {
        return "You can prioritize your certain player targets with pursue, temporarily ignore with avoid, and stopping targeting with adding to friends.";
    }
}
