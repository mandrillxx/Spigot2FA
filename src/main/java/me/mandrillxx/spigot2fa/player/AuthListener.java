package me.mandrillxx.spigot2fa.player;

import me.mandrillxx.spigot2fa.Spigot2FA;
import me.mandrillxx.spigot2fa.util.Config;
import me.mandrillxx.spigot2fa.util.UtilTwoFactor;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static org.bukkit.ChatColor.*;

public class AuthListener implements Listener {

    private final UtilTwoFactor utilTwoFactor;

    public AuthListener(UtilTwoFactor utilTwoFactor) {
        this.utilTwoFactor = utilTwoFactor;
    }

    /**
     * Adds player to auth locked array if they have the
     * required permission to become 2FA locked.
     *
     * @param event The {@link PlayerJoinEvent} event to listen to
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoinAuth(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission(Config.REQUIRED_PERMISSION)) {
            Spigot2FA.authLockedPlayers.add(player.getUniqueId());

            if (!hasGottenCode(player.getUniqueId())) {
                String keyString = utilTwoFactor.generateBase32Secret();

                try {
                    insert2FAData(player.getUniqueId(), keyString);
                } catch (final Exception e) {
                    e.printStackTrace();
                    return;
                }

                String url = "https://chart.googleapis.com/chart?chs=200x200&cht=qr&chl=200x200&chld=M|0&cht=qr&chl=otpauth://totp/" + Config.SERVER_NAME + "%3Fsecret%3" + keyString;

                TextComponent code = new TextComponent(GREEN + "Your 2FA verification code is " + YELLOW + keyString + GRAY + " (Left-Click to view scannable QR code)");
                code.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
                code.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(GREEN + "Left-Click to open URL containing a scannable QR code.").create()));

                player.spigot().sendMessage(code);
            } else {
                player.sendMessage(new String[] {
                        RED + "Please enter your 2FA code before accessing the server.",
                        RED + "You can enter your 2FA code by typing the code in chat."
                });
            }
        }
    }

    /**
     * Removes player from auth locked array.
     *
     * @param event The {@link PlayerQuitEvent} event to listen to
     */
    @EventHandler
    public void onPlayerQuitClearAuth(PlayerQuitEvent event) {
        Spigot2FA.authLockedPlayers.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Prevents the player from moving if they
     * haven't entered their 2FA code.
     *
     * @param event The {@link PlayerMoveEvent} event to listen to
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (Config.PREVENT_MOVEMENT && Spigot2FA.authLockedPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setTo(event.getFrom());
            event.getPlayer().sendMessage(new String[] {
                    RED + "Please enter your 2FA code before accessing the server.",
                    RED + "You can enter your 2FA code by typing the code in chat."
            });
        }
    }

    /**
     * Prevents the player from building if they
     * haven't entered their 2FA code.
     *
     * @param event The {@link BlockPlaceEvent} event to listen to
     */
    @EventHandler
    public void onPlayerBuildBlock(BlockPlaceEvent event) {
        if (Spigot2FA.authLockedPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(new String[] {
                    RED + "Please enter your 2FA code before accessing the server.",
                    RED + "You can enter your 2FA code by typing the code in chat."
            });
        }
    }

    /**
     * Prevents the player from breaking a block
     * if they haven't entered their 2FA code.
     *
     * @param event The {@link BlockBreakEvent} event to listen to
     */
    @EventHandler
    public void onPlayerBreakBlock(BlockBreakEvent event) {
        if (Spigot2FA.authLockedPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(new String[] {
                    RED + "Please enter your 2FA code before accessing the server.",
                    RED + "You can enter your 2FA code by typing the code in chat."
            });
        }
    }

    /**
     * Prevents the player from running commands
     * if they haven't entered their 2FA code.
     *
     * @param event The {@link PlayerCommandPreprocessEvent} event to listen to
     */
    @EventHandler
    public void onPlayerAttemptCommand(PlayerCommandPreprocessEvent event) {
        if (Config.PREVENT_COMMANDS && Spigot2FA.authLockedPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);

            event.getPlayer().sendMessage(new String[] {
                    RED + "Please enter your 2FA code before accessing the server.",
                    RED + "You can enter your 2FA code by typing the code in chat."
            });
        }
    }

    /**
     * Handles 2FA code entering & prevents the player
     * from chatting while they haven't entered their
     * 2FA code.
     *
     * @param event The {@link AsyncPlayerChatEvent} event to listen to
     */
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (Spigot2FA.authLockedPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);

            if (Config.USE_BYPASS_CODE && event.getMessage().split(" ")[0].equals(Config.BYPASS_CODE)) {
                Spigot2FA.authLockedPlayers.remove(event.getPlayer().getUniqueId());
                event.getPlayer().sendMessage(GREEN + "Thank you! You now have access to the server.");
                return;
            }

            try {
                int code = Integer.parseInt(event.getMessage());

                if (isCorrectCode(event.getPlayer().getUniqueId(), code)) {
                    Spigot2FA.authLockedPlayers.remove(event.getPlayer().getUniqueId());
                    event.getPlayer().sendMessage(GREEN + "Thank you! You now have access to the server.");
                } else {
                    event.getPlayer().sendMessage(RED + "You have entered an invalid 2FA code. Please ensure you have typed your code correctly.");
                }
            } catch (final NumberFormatException e) {
                event.getPlayer().sendMessage(new String[] {
                        RED + "Please enter your 2FA code before accessing the server.",
                        RED + "You can enter your 2FA code by typing the code in chat."
                });
            }
        }
    }

    /**
     * Check if the player has received their 2FA code yet.
     *
     * @param uuid The players UUID
     * @return Whether the player has gotten their 2FA code & secret.
     */
    private boolean hasGottenCode(UUID uuid) {
        return getSecretKey(uuid) != null;
    }

    private void insert2FAData(UUID uuid, String key) {
        try {
            PreparedStatement preparedStatement = Spigot2FA.database.getConnection()
                    .prepareStatement("insert into codes (uuid, code) values (?, ?);");

            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setString(2, key);

            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    private String getSecretKey(UUID uuid) {
        String secretKey = null;

        try {
            PreparedStatement preparedStatement = Spigot2FA.database.getConnection()
                    .prepareStatement("select * from codes where uuid=?;");

            preparedStatement.setString(1, uuid.toString());
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                secretKey = rs.getString("code");
            }

            preparedStatement.close();
        } catch (final SQLException e) {
            e.printStackTrace();
        }

        return secretKey;
    }

    private boolean isCorrectCode(UUID uuid, Integer code) {
        String secretKey = getSecretKey(uuid);

        try {
            return utilTwoFactor.generateCurrentNumber(secretKey).equals(String.valueOf(code));
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
