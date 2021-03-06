package playerstoragev2;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;

import org.bukkit.entity.Player;

// This class was automatically generated by the CommandAPI
public class cmd$Command {

@SuppressWarnings("unchecked")
public static void register() {

    new CommandAPICommand("playerstorage")
        .withArguments(
            new MultiLiteralArgument("save-all")
                .setListed(false)
        )
        .withAliases("ps")
        .executes((sender, args) -> {
            cmd.save(sender);
        })
        .register();

    new CommandAPICommand("playerstorage")
        .withArguments(
            new MultiLiteralArgument("save")
                .setListed(false)
        )
        .withAliases("ps")
        .withArguments(new PlayerArgument("player"))
        .executes((sender, args) -> {
            cmd.save(sender, (Player) args[0]);
        })
        .register();

    new CommandAPICommand("playerstorage")
        .withArguments(
            new MultiLiteralArgument("load")
                .setListed(false)
        )
        .withAliases("ps")
        .withArguments(new StringArgument("player"))
        .executes((sender, args) -> {
            cmd.load(sender, (String) args[0]);
        })
        .register();

    new CommandAPICommand("playerstorage")
        .withArguments(
            new MultiLiteralArgument("info")
                .setListed(false)
        )
        .withAliases("ps")
        .withArguments(new StringArgument("player"))
        .executes((sender, args) -> {
            cmd.print(sender, (String) args[0]);
        })
        .register();

    new CommandAPICommand("playerstorage")
        .withArguments(
            new MultiLiteralArgument("cfg-reload")
                .setListed(false)
        )
        .withAliases("ps")
        .executes((sender, args) -> {
            cmd.cfg_reload(sender);
        })
        .register();

    }

}
