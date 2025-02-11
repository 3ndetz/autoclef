package adris.altoclef;

import adris.altoclef.commands.*;
import adris.altoclef.commands.multiplayer.*;
import adris.altoclef.commandsystem.CommandException;

/**
 * Initializes altoclef's built in commands.
 */
public class AltoClefCommands {

    public AltoClefCommands() throws CommandException {
        // List commands here
        AltoClef.getCommandExecutor().registerNewCommand(
                new BuildCommand(),
                new GameCommand(),
                new ShiftCommand(),
                new GraveCommand(),
                new SignCommand(),
                new TimeoutCommand(),
                new GestureCommand(),
                new PursueCommand(),
                new AvoidCommand(),
                new HelpCommand(),
                new GetCommand(),
                new FollowCommand(),
                new GiveCommand(),
                new EquipCommand(),
                new DepositCommand(),
                new StashCommand(),
                new GotoCommand(),
                new IdleCommand(),
                new CoordsCommand(),
                new StatusCommand(),
                new InventoryCommand(),
                new LocateStructureCommand(),
                new StopCommand(),
                new TestCommand(),
                new FoodCommand(),
                new MeatCommand(),
                new ReloadSettingsCommand(),
                new ReloadSettingsCommand(true),
                new GamerCommand(),
                new MarvionCommand(),
                new PunkCommand(),
                new CombatCommand(),
                new HeroCommand(),
                new SetGammaCommand(),
                new ListCommand(),
                new CoverWithSandCommand(),
                new CoverWithBlocksCommand(),
                new SelfCareCommand(),
                new SetSettingsCommand()
                //new TestMoveInventoryCommand(),
                //    new TestSwapInventoryCommand()
        );
    }
}
