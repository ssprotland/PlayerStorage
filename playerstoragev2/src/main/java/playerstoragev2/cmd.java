package playerstoragev2;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.APlayerArgument;
import dev.jorel.commandapi.annotations.arguments.AStringArgument;

@Command("playerstorage")
@Alias("ps")
public class cmd {

    @Subcommand("save-all")
    public static void save(CommandSender sender) {
        sender.sendMessage("saving...");
        try {
            PlayerStorage.getInstance().cache.forEach((name, player) -> {
                PlayerStorage.save(player);
            });
        } catch (Exception e) {
            // e.printStackTrace();
        }
        sender.sendMessage("done!");
    }

    @Subcommand("save")
    public static void save(CommandSender sender, @APlayerArgument Player player) {
        sender.sendMessage("saving...");
        PlayerStorage.save(player.getName());
        sender.sendMessage("done!");
    }

    @Subcommand("load")
    public static void load(CommandSender sender, @AStringArgument String player) {
        sender.sendMessage("loading...");
        PlayerStorage.getPlayer(player);
        sender.sendMessage("done!");
    }

    // @Subcommand("info")
    // public static void print(CommandSender sender, @APlayerArgument Player
    // player) {
    // sender.sendMessage("======info about player " + player.getName() + "======");
    // sender.sendMessage(PlayerStorage.getInstance().storage.info(player.getName()));
    // sender.sendMessage("=========================================");
    // }

    @Subcommand("info")
    public static void print(CommandSender sender, @AStringArgument String player) {
        sender.sendMessage("======info about player " + player + "======");
        sender.sendMessage(PlayerStorage.info(player));
        sender.sendMessage("=========================================");
    }

    @Subcommand("cfg-reload")
    public static void cfg_reload(CommandSender sender) {
        sender.sendMessage("reloading...");
        PlayerStorage.getInstance().reloadConfig();
        PlayerStorage.reloadCfg();
        sender.sendMessage(Boolean.toString(PlayerStorage.getInstance().debug));
        sender.sendMessage("done!");
    }
}
