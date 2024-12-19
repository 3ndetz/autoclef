package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasksystem.Task;

import java.util.List;
import java.util.stream.Collectors;

public class StatusCommand extends Command {
    public StatusCommand() throws CommandException {
        super("status", "Get status of currently executing command", new Arg(String.class, "all", null, 0));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        List<Task> tasks = mod.getUserTaskChain().getTasks();
        if (tasks.isEmpty()) {
            mod.log("No tasks currently running.");
        } else {
            String statusType = parser.get(String.class);
            StringBuilder statusBuilder = new StringBuilder();

            // Add header
            statusBuilder.append("=== Current Tasks Status ===\n");

            // Always show the current task with special formatting
            statusBuilder.append("→ Current Task: ").append(tasks.get(0).toString()).append("\n");

            // If "all" parameter is provided, show all tasks in the chain
            if (statusType != null && statusType.equalsIgnoreCase("all")) {
                if (tasks.size() > 1) {
                    statusBuilder.append("\nPending Tasks:\n");
                    String pendingTasks = tasks.stream()
                            .skip(1) // Skip the first task as it's already displayed
                            .map(task -> "  • " + task.toString())
                            .collect(Collectors.joining("\n"));
                    statusBuilder.append(pendingTasks);
                }
            }

            // Add footer
            statusBuilder.append("\n=========================");

            mod.log(statusBuilder.toString());
        }
        finish();
    }
}
