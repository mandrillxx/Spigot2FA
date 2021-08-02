package me.mandrillxx.spigot2fa;

import me.mandrillxx.spigot2fa.commands.Command2FA;
import me.mandrillxx.spigot2fa.player.AuthListener;
import me.mandrillxx.spigot2fa.util.Config;
import me.mandrillxx.spigot2fa.util.UtilDatabase;
import me.mandrillxx.spigot2fa.util.UtilTwoFactor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Spigot2FA extends JavaPlugin {

    /**
     * A list of players who are locked until they
     * enter a valid 2FA code.
     */
    public static final List<UUID> authLockedPlayers = new ArrayList<>();

    /**
     * The main configuration file for the plugin.
     */
    public static File CONFIG_FILE = null;

    /**
     * The database for player 2fa codes
     */
    public static UtilDatabase database;

    @Override
    public void onEnable() {
        CONFIG_FILE = new File(getDataFolder() + "/config.yml");
        Config.init(CONFIG_FILE);

        database = new UtilDatabase(new File(getDataFolder(), "data.db"));

        getServer().getPluginManager().registerEvents(new AuthListener(new UtilTwoFactor()), this);
        getServer().getCommandMap().register("2fa", new Command2FA());
        getLogger().info("Successfully enabled Spigot2FA.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Successfully disabled Spigot2FA");
    }

}
