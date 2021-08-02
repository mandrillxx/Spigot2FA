package me.mandrillxx.spigot2fa.commands;

import me.mandrillxx.spigot2fa.Spigot2FA;
import me.mandrillxx.spigot2fa.util.Config;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static org.bukkit.ChatColor.*;

public class Command2FA extends BukkitCommand {

    public Command2FA() {
        super("2fa");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (sender instanceof Player && !sender.hasPermission("spigot2fa.reload")) {
            sender.sendMessage(RED + "You do not have permission to run this command!");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            try {
                Config.config.save(Spigot2FA.CONFIG_FILE);
                sender.sendMessage(GREEN + "Configuration successfully reloaded!");
            } catch (final Exception e) {
                e.printStackTrace();
                sender.sendMessage(RED + "An error occurred while attempting to save the configuration. Please check the server console for more details.");
            }
        } else {
            sender.sendMessage(RED + "Usage: /2fa reload");
        }

        return true;
    }
}
