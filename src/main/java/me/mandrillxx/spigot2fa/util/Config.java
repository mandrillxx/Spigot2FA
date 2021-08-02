package me.mandrillxx.spigot2fa.util;

import com.google.common.base.Throwables;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.logging.Level;

public class Config {

    private static File CONFIG_FILE;
    private static final String HEADER = "This is the main configuration file"
            + "\nfor Spigot2FA. All options are configurable"
            + "\nunless specified otherwise.";

    public static YamlConfiguration config;

    public static void init(File configFile) {
        CONFIG_FILE = configFile;
        config = new YamlConfiguration();

        try {
            config.load(CONFIG_FILE);
        } catch (final IOException ignored) {

        } catch (final InvalidConfigurationException e) {
            Bukkit.getLogger().severe("Could not load config.yml, please correct any syntax errors.");
            throw Throwables.propagate(e);
        }

        config.options().header(HEADER);
        config.options().copyDefaults(true);

        readConfig(Config.class, null);
    }

    static void readConfig(Class<?> clazz, Object instance)
    {
        for ( Method method : clazz.getDeclaredMethods() )
        {
            if ( Modifier.isPrivate( method.getModifiers() ) )
            {
                if ( method.getParameterTypes().length == 0 && method.getReturnType() == Void.TYPE )
                {
                    try
                    {
                        method.setAccessible( true );
                        method.invoke( instance );
                    } catch ( InvocationTargetException ex )
                    {
                        throw Throwables.propagate( ex.getCause() );
                    } catch ( Exception ex )
                    {
                        Bukkit.getLogger().log( Level.SEVERE, "Error invoking " + method, ex );
                    }
                }
            }
        }

        try
        {
            config.save( CONFIG_FILE );
        } catch ( IOException ex )
        {
            Bukkit.getLogger().log( Level.SEVERE, "Could not save " + CONFIG_FILE, ex );
        }
    }

    private static void set(String path, Object val)
    {
        config.set( path, val );
    }

    private static boolean getBoolean(String path, boolean def)
    {
        config.addDefault( path, def );
        return config.getBoolean( path, config.getBoolean( path ) );
    }

    private static int getInt(String path, int def)
    {
        config.addDefault( path, def );
        return config.getInt( path, config.getInt( path ) );
    }

    private static <T> List getList(String path, T def)
    {
        config.addDefault( path, def );
        return (List<T>) config.getList( path, config.getList( path ) );
    }

    private static String getString(String path, String def)
    {
        config.addDefault( path, def );
        return config.getString( path, config.getString( path ) );
    }

    private static double getDouble(String path, double def)
    {
        config.addDefault( path, def );
        return config.getDouble( path, config.getDouble( path ) );
    }

    public static String REQUIRED_PERMISSION = "spigot2fa.required";
    public static boolean PREVENT_MOVEMENT = true;
    public static boolean PREVENT_COMMANDS = true;
    public static boolean USE_BYPASS_CODE = true;
    public static String BYPASS_CODE = "16xa";
    public static String SERVER_NAME = "ServerName";
    private static void getConfigOptions() {
        REQUIRED_PERMISSION = getString("config.required-permission", REQUIRED_PERMISSION);
        PREVENT_MOVEMENT = getBoolean("config.prevent-movement", PREVENT_MOVEMENT);
        PREVENT_COMMANDS = getBoolean("config.prevent-commands", PREVENT_COMMANDS);
        USE_BYPASS_CODE = getBoolean("config.allow-bypass-code", USE_BYPASS_CODE);
        BYPASS_CODE = getString("config.bypass-code", BYPASS_CODE);
        SERVER_NAME = getString("config.server-name", SERVER_NAME);
    }

}
