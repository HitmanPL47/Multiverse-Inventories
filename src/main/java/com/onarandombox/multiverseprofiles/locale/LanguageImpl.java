package com.onarandombox.multiverseprofiles.locale;

import com.onarandombox.multiverseprofiles.util.ProfilesLog;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dumptruckman, SwearWord
 */
public enum LanguageImpl implements Language {

    ERROR("messages.generic.error"),
    SUCCESS("messages.generic.success"),
    INFO("messages.generic.info"),;

    private String path;
    private static FileConfiguration language = null;

    LanguageImpl(String path) {
        this.path = path;
    }

    /**
     * Retrieves the path for a config option
     *
     * @return The path for a config option
     */
    public String getPath() {
        return path;
    }

    public String getString(Object... args) {
        return getString(this, args);
    }

    public void bad(CommandSender sender, Object... args) {
        send(ChatColor.RED.toString() + LanguageImpl.ERROR.getString(), sender, args);
    }

    public void normal(CommandSender sender, Object... args) {
        send("", sender, args);
    }

    private void send(String prefix, CommandSender sender, Object... args) {
        List<String> messages = getStrings(this, args);
        for (int i = 0; i < messages.size(); i++) {
            if (i == 0) {
                sender.sendMessage(prefix + " " + messages.get(i));
            } else {
                sender.sendMessage(messages.get(i));
            }
        }
    }

    public void good(CommandSender sender, Object... args) {
        send(ChatColor.GREEN.toString() + LanguageImpl.SUCCESS.getString(), sender, args);
    }

    public void info(CommandSender sender, Object... args) {
        send(ChatColor.YELLOW.toString() + LanguageImpl.INFO.getString(), sender, args);
    }

    /**
     * Loads the language data into memory and sets defaults
     *
     * @throws java.io.IOException
     */
    public static void load(JavaPlugin plugin, String languageFileName) throws IOException {
        if (LanguageImpl.language == null) {
            // Make the data folders
            plugin.getDataFolder().mkdirs();

            // Check if the language file exists.  If not, create it.
            File languageFile = new File(plugin.getDataFolder(), languageFileName);
            load(languageFile);
        }
    }

    public static void load(File languageFile) throws IOException {
        if (LanguageImpl.language == null) {
            if (!languageFile.exists()) {
                languageFile.createNewFile();
            }

            // Load the language file into memory
            LanguageImpl.language = YamlConfiguration.loadConfiguration(languageFile);
        }
    }

    public static String formatString(String string, Object... args) {
        // Replaces & with the Section character
        string = string.replaceAll("&", Character.toString((char) 167));
        // If there are arguments, %n notations in the message will be
        // replaced
        if (args != null) {
            for (int j = 0; j < args.length; j++) {
                string = string.replace("%" + (j + 1), args[j].toString());
            }
        }
        return string;
    }

    /**
     * Gets a list of the messages for a given path.  Color codes will be
     * converted and any lines too long will be split into an extra element in
     * the list.  %n notated variables n the message will be replaced with the
     * optional arguments passed in.
     *
     * @param path Path of the message in the language yaml file.
     * @param args Optional arguments to replace %n variable notations
     * @return A List of formatted Strings
     */
    public static List<String> getStrings(LanguageImpl path, Object... args) {
        // Gets the messages for the path submitted
        List<Object> list = language.getList(path.getPath());

        List<String> message = new ArrayList<String>();
        if (list == null) {
            ProfilesLog.warning("Missing language for: " + path.getPath());
            return message;
        }
        // Parse each item in list
        for (int i = 0; i < list.size(); i++) {
            String temp = formatString(list.get(i).toString(), args);

            // Pass the line into the line breaker
            List<String> lines = Font.splitString(temp);
            // Add the broken up lines into the final message list to return
            for (int j = 0; j < lines.size(); j++) {
                message.add(lines.get(j));
            }
        }
        return message;
    }

    public static String getString(LanguageImpl language, Object... args) {
        List<Object> list = LanguageImpl.language.getList(language.getPath());
        if (list == null) {
            ProfilesLog.warning("Missing language for: " + language.getPath());
            return "";
        }
        if (list.isEmpty()) return "";
        return (formatString(list.get(0).toString(), args));
    }

    /**
     * Sends a custom string to a player.
     *
     * @param player
     * @param message
     * @param args
     */
    public static void sendMessage(CommandSender player, String message, Object... args) {
        List<String> messages = Font.splitString(formatString(message, args));
        sendMessages(player, messages);
    }

    public static void sendMessages(CommandSender player, List<String> messages) {
        for (String s : messages) {
            player.sendMessage(s);
        }
    }
}