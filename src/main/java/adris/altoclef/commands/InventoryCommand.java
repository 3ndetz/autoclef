package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.ui.MessagePriority;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.stream.Collectors;

public class InventoryCommand extends Command {
    public InventoryCommand() throws CommandException {
        super("inventory", "Prints the bot's inventory OR returns how many of an item the bot has", new Arg(String.class, "item", null, 1));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String item = parser.get(String.class);
        if (item == null) {
            // Print inventory as single message
            HashMap<String, Integer> counts = new HashMap<>();
            for (int i = 0; i < mod.getPlayer().getInventory().size(); ++i) {
                ItemStack stack = mod.getPlayer().getInventory().getStack(i);
                if (!stack.isEmpty()) {
                    String name = ItemHelper.stripItemName(stack.getItem());
                    counts.merge(name, stack.getCount(), Integer::sum);
                }
            }
            
            String inventory = counts.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));
            
            mod.log("Inventory: {" + inventory + "}", MessagePriority.OPTIONAL);
        } else {
            // Print specific item quantity
            Item[] matches = TaskCatalogue.getItemMatches(item);
            if (matches == null || matches.length == 0) {
                mod.logWarning("Item \"" + item + "\" is not catalogued.");
                finish();
                return;
            }
            int count = mod.getItemStorage().getItemCount(matches);
            mod.log(item + ": " + (count > 0 ? count : "none"));
        }
        finish();
    }
}