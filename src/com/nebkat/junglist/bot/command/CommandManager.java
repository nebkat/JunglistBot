/*
 * Copyright 2013 Nebojsa Cvetkovic. All rights reserved.
 *
 * This file is part of JunglistIRC.
 *
 * JunglistIRC is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JunglistIRC is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JunglistIRC.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nebkat.junglist.bot.command;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nebkat.junglist.bot.Bot;
import com.nebkat.junglist.bot.plugin.Plugin;
import com.nebkat.junglist.irc.Irc;
import com.nebkat.junglist.irc.Log;
import com.nebkat.junglist.irc.Source;
import com.nebkat.junglist.irc.events.EventHandler;
import com.nebkat.junglist.irc.events.EventListener;
import com.nebkat.junglist.irc.events.irc.PrivMessageEvent;
import com.nebkat.junglist.irc.utils.Utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparators;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class CommandManager implements EventListener {
    private static final String TAG = "CommandManager";

    private final Bot mBot;

    private Map<String, UserLevel> mUserLevels = new HashMap<>();
    private Map<String, Command> mCommands = new HashMap<>();
    private Map<String, String> mCommandAliases = new HashMap<>();

    private final File mUserLevelsFile;

    private final Gson mGson = new Gson();

    public CommandManager(Bot bot, File userLevelsFile) {
        mBot = bot;

        if (userLevelsFile == null) {
            throw new IllegalArgumentException("User levels file must not be null");
        } else if (!userLevelsFile.isFile() || !userLevelsFile.exists()) {
            throw new IllegalArgumentException("User levels file must be a valid file");
        }

        mUserLevelsFile = userLevelsFile;

        mBot.getIrc().getEventHandlerManager().registerEvents(this);

        registerCommand(new Command("help", null, "Shows command list/help", "[<command>]", UserLevel.NORMAL, false));
    }

    public void loadUserLevels() {
        try (Reader reader = new FileReader(mUserLevelsFile)) {
            Map<String, Integer> result  = mGson.fromJson(reader, new TypeToken<Map<String, Integer>>(){}.getType());
            mUserLevels.clear();
            result.forEach((host, userLevel) -> mUserLevels.put(host, UserLevel.forLevel(userLevel)));
        } catch (IOException e) {
            Log.e(TAG, "Could not read user levels", e);
        }
    }

    public void saveUserLevels() {
        try (Writer writer = new FileWriter(mUserLevelsFile)) {
            ArrayList<LinkedHashMap<String, Object>> userLevels = new ArrayList<>();
            for (Map.Entry<String, UserLevel> userLevel : mUserLevels.entrySet()) {
                LinkedHashMap<String, Object> levelMap = new LinkedHashMap<>();
                levelMap.put("mask", userLevel.getKey());
                levelMap.put("level", userLevel.getValue().getLevel());
                userLevels.add(levelMap);
            }
            mGson.toJson(userLevels, writer);
        } catch (IOException e) {
            Log.e(TAG, "Could not save user levels", e);
        }
    }

    public void setUserLevel(String mask, UserLevel level) {
        mUserLevels.put(mask, level);
        saveUserLevels();
    }

    public void unsetUserLevel(String mask) {
        mUserLevels.remove(mask);
        saveUserLevels();
    }

    public void clearUserLevel(String clear) {
        mUserLevels.keySet().removeIf((mask) -> (Source.match(mask, clear)));
        saveUserLevels();
    }

    public Map<String, UserLevel> getUserLevels() {
        return Collections.unmodifiableMap(mUserLevels);
    }

    public UserLevel getLevelForHost(String host) {
        // Filter matching hosts and return highest UserLevel
        return mUserLevels.entrySet().stream()
                .filter((entry) -> Source.match(host, entry.getKey()))
                .map((entry) -> entry.getValue())
                .reduce(UserLevel.NORMAL, Comparators.greaterOf(UserLevel.COMPARATOR));
    }

    public Command getCommand(String command) {
        return mCommands.get(mCommandAliases.getOrDefault(command, command));
    }

    public void clearPluginCommands(Plugin plugin) {
        Set<String> remove = mCommands.values().stream()
                .filter((command) -> command.getPlugin() == plugin)
                .map(Command::getName)
                .collect(Collectors.toSet());

        mCommands.keySet().removeAll(remove);
        mCommandAliases.values().removeAll(remove);
    }

    public void registerCommand(Command command) {
        if (!mCommands.containsKey(command.getName())) {
            mCommands.put(command.getName(), command);
            command.getAliases().forEach((alias) -> mCommandAliases.putIfAbsent(alias, command.getName()));
        } else {
            Log.e(TAG, "Plugin " + (command.getPlugin() != null ? command.getPlugin().getDescription().getName() : "unknown") +
                    " tried to register command " + command.getName() + " which is already taken by plugin " +
                    (mCommands.get(command.getName()).getPlugin() != null ? mCommands.get(command.getName()).getPlugin().getDescription().getName() : "unknown"));
        }
    }

    public void unregisterCommand(Command command) {
        mCommands.remove(command.getName());
        mCommandAliases.values().removeIf((value) -> value.equals(command.getName()));
    }

    @EventHandler
    public void onMessage(PrivMessageEvent e) {
        if (!e.getMessage().startsWith(mBot.getCommandPrefix())) {
            return;
        }
        String[] split = e.getMessage().split(" ", 2);
        String command = split[0].substring(mBot.getCommandPrefix().length()).toLowerCase();
        if (Utils.empty(command)) {
            return;
        }
        String rawParams = Utils.indexOrDefault(split, 1, "");
        String[] params = !Utils.empty(rawParams) ? rawParams.split(" ") : new String[0];

        Command c = getCommand(command);
        if (c == null) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Unknown command \"" + command + "\"");
            return;
        }

        if (c.getPlugin() != null && !c.getPlugin().getEnabled()) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Unknown command \"" + command + "\". Plugin \"" + c.getPlugin().getDescription().getName() + "\" disabled");
            return;
        }

        UserLevel level = getLevelForHost(e.getSource().getRaw());
        if (c.getLevel().getLevel() > level.getLevel()) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": You do not have permission to run \"" + command + "\"");
            return;
        }

        CommandEvent commandEvent = new CommandEvent(e.getTime(), e.getSession(), e.getData(), e.getSource(), e.getTarget(), e.getMessage(), c, command, params, rawParams);
        mBot.getIrc().getEventHandlerManager().callEvent(commandEvent);
    }

    @EventHandler
    @CommandFilter("help")
    public void onHelpCommand(CommandEvent e) {
        if (e.getParams().length == 0) {
            // Filter and group
            TreeMap<String, StringJoiner> commands = mCommands.values().stream()
                    .filter((command) -> !command.getHidden())
                    .filter((command) -> command.getLevel().getLevel() <= getLevelForHost(e.getSource().getRaw()).getLevel())
                    .sorted()
                    .collect(Collectors.groupingBy((command) -> command.getPlugin() != null ? command.getPlugin().getDescription().getName() : "", () -> new TreeMap<String, StringJoiner>(Collator.getInstance()),
                            Collectors.mapping((command) -> mBot.getCommandPrefix() + command.getName(), Collectors.toStringJoiner(", "))));

            List<String> result = new ArrayList<>(commands.size());
            for (Map.Entry<String, StringJoiner> entry : commands.entrySet()) {
                if (!entry.getKey().equals("")) {
                    result.add("{" + entry.getValue().toString() + "}");
                } else {
                    result.add(entry.getValue().toString());
                }
            }
            String commandList = Utils.implode(result, ", ");
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Available commands: " + commandList);
        } else if (e.getParams().length == 1) {
            String command = e.getParams()[0];
            if (command.startsWith(mBot.getCommandPrefix())) {
                command = command.substring(mBot.getCommandPrefix().length());
            }
            Command c = getCommand(command);
            if (c == null) {
                Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Unknown command \"" + mBot.getCommandPrefix() + command + "\"");
                return;
            }

            List<String> information = new ArrayList<>();
            if (!command.equals(c.getName())) {
                information.add("name: \"" + c.getName() + "\"");
            }
            if (c.getDescription() != null) {
                information.add("description: \"" + c.getDescription() + "\"");
            }
            if (c.getUsage() != null) {
                information.add("usage: \"" + c.getUsage() + "\"");
            }
            if (c.getAliases() != null && c.getAliases().size() > 0) {
                information.add("aliases: {" + Utils.implode(c.getAliases(), ", ") + "}");
            }
            if (c.getLevel() != null) {
                information.add("level: " + c.getLevel().name());
            }
            if (c.getPlugin() != null) {
                information.add("plugin: \"" + c.getPlugin().getDescription().getName() + "\"");
            }

            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + mBot.getCommandPrefix() + command + " {" + Utils.implode(information, ", ") + "}");
        } else {
            e.showUsage(mBot);
        }
    }
}
