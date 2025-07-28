package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.butler.ButlerConfig;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.util.helpers.ConfigHelper;
import adris.altoclef.util.helpers.SettingsReflectionHelper;

import java.util.List;

public class SetSettingsCommand extends Command {
    public SetSettingsCommand() throws CommandException {
        super("set", "set <setting name> <new value> | set list", new Arg(String.class, "setting"), new Arg(String.class, "new value", 0, 1));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String setting_name = parser.get(String.class).toLowerCase();
        
        // Special case: list all available settings
        if (setting_name.equals("list")) {
            listAllSettings(mod);
            finish();
            return;
        }
        
        String new_value = parser.get(String.class);
        if (new_value == null) {
            mod.log("Please specify a new value for setting: " + setting_name);
            finish();
            return;
        }

        // Try to set the setting using our reflection helper
        boolean success = false;
        
        // Try main settings first
        if (SettingsReflectionHelper.setSetting(mod.getModSettings(), setting_name, new_value)) {
            ConfigHelper.saveConfig("altoclef_settings.json", mod.getModSettings());
            ConfigHelper.reloadAllConfigs();
            mod.log("Successfully updated main setting!");
            success = true;
        } 
        // Try butler settings
        else if (SettingsReflectionHelper.setSetting(ButlerConfig.getInstance(), setting_name, new_value)) {
            ConfigHelper.saveConfig("configs/butler.json", ButlerConfig.getInstance());
            ConfigHelper.reloadAllConfigs();
            mod.log("Successfully updated butler setting!");
            success = true;
        }
        
        if (!success) {
            mod.log("Setting '" + setting_name + "' not found. Use 'set list' to see all available settings.");
        }

        finish();
    }

    private void listAllSettings(AltoClef mod) {
        mod.log("=== Available Settings ===");
        
        // List main settings
        mod.log("Main Settings (altoclef_settings.json):");
        List<SettingsReflectionHelper.SettingInfo> mainSettings = 
            SettingsReflectionHelper.getSettableFields(mod.getModSettings());
        for (SettingsReflectionHelper.SettingInfo setting : mainSettings) {
            mod.log("  " + setting.toString());
        }
        
        // List butler settings
        mod.log("Butler Settings (configs/butler.json):");
        List<SettingsReflectionHelper.SettingInfo> butlerSettings = 
            SettingsReflectionHelper.getSettableFields(ButlerConfig.getInstance());
        for (SettingsReflectionHelper.SettingInfo setting : butlerSettings) {
            mod.log("  " + setting.toString());
        }
        
        mod.log("=== Usage Examples ===");
        mod.log("  @set hud true");
        mod.log("  @set chat false");
        mod.log("  @set timer true");
        mod.log("  @set mobdefense false");
        mod.log("  @set autojoin true");
        mod.log("  @set containerItemMoveDelay 0.5");
    }
}