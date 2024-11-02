package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.butler.ButlerConfig;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;

public class SetSettingsCommand extends Command {
    public SetSettingsCommand() throws CommandException {
        super("set", "set <setting name> <new value>", new Arg(String.class, "setting"), new Arg(String.class, "new value", 1, 1));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String setting_name = parser.get(String.class).toLowerCase();
        String new_value = parser.get(String.class);
        //ConfigHelper.reloadAllConfigs();
        boolean old_value;
        switch (setting_name) {
            case "":
                // None specified
                Debug.logWarning("Please specify a SETTING");
                break;
            case "autojoin":
                old_value = ButlerConfig.getInstance().autoJoin;
                ButlerConfig.getInstance().autoJoin = Boolean.parseBoolean(new_value);
                mod.log("Set setting "+setting_name+" from "+old_value+" to "+new_value+"!");
                break;
            case "stuckfix":
                old_value = ButlerConfig.getInstance().autoStuckFix;
                ButlerConfig.getInstance().autoStuckFix = Boolean.parseBoolean(new_value);
                mod.log("Set setting "+setting_name+" from "+old_value+" to "+new_value+"!");
                break;
            case "hud":
                old_value = mod.getModSettings().shouldShowTaskChain();
                mod.getModSettings().setShowTaskChainSetting(Boolean.parseBoolean(new_value));
                mod.log("Set setting "+setting_name+" from "+old_value+" to "+new_value+"!");
                break;
            default:
                mod.log("setting "+setting_name+" not exists");
                break;
        }

        finish();
    }
}