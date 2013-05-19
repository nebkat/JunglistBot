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

package com.nebkat.junglist.bot.plugin;

import com.google.gson.Gson;
import com.nebkat.junglist.bot.Bot;
import com.nebkat.junglist.bot.command.Command;
import com.nebkat.junglist.bot.command.CommandEvent;
import com.nebkat.junglist.bot.command.CommandFilter;
import com.nebkat.junglist.bot.command.UserLevel;
import com.nebkat.junglist.irc.Irc;
import com.nebkat.junglist.irc.Log;
import com.nebkat.junglist.irc.events.EventHandler;
import com.nebkat.junglist.irc.utils.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PluginManager implements com.nebkat.junglist.irc.events.EventListener {
    private static final String TAG = "PluginManager";

    private final Bot mBot;
    private final PluginLoader mPluginLoader;
    private final File mPluginDirectory;

    private final Map<String, Plugin> mPlugins = new HashMap<>();

    public PluginManager(Bot bot, File directory) {
        mBot = bot;
        mPluginLoader = new PluginLoader(bot, this);

        if (directory == null) {
            throw new IllegalArgumentException("Plugin directory must not be null");
        } else if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Plugin directory must be a valid directory");
        }
        mPluginDirectory = directory;

        mBot.getIrc().getEventHandlerManager().registerEvents(this);
        mBot.getCommandManager().registerCommand(new Command("plugin", null, "Manage plugins", "list/reload/load/unload/enable/disable [<plugin>]", UserLevel.OWNER, false));
    }

    public void loadPlugins() {
        Utils.forEach(mPluginDirectory.listFiles(file -> file.isFile() && file.getName().toLowerCase().endsWith(".jar")), this::loadPlugin);
    }

    public void unloadPlugins() {
        Utils.forEach(getPlugins(), this::unloadPlugin);
    }

    public synchronized Plugin loadPlugin(File file) {
        if (file == null) {
            Log.e(TAG, "Plugin file is null");
            return null;
        } else if (!file.isFile()) {
            Log.e(TAG, "Plugin file does not exist or is a directory");
            return null;
        }
        long start = System.currentTimeMillis();

        PluginDescription description;
        try {
            description = mPluginLoader.getPluginDescription(file);
        } catch (InvalidDescriptionException ex) {
            Log.e(TAG, "Could not load plugin: " + file.toURI().toASCIIString(), ex);
            return null;
        }
        try {
            Plugin p = mPluginLoader.loadPlugin(file);
            mPlugins.put(description.getName(), p);
            p.create();
            enablePlugin(p);
            Log.i(TAG, "Loaded plugin (" + (System.currentTimeMillis() - start) + "ms): " + description.getFullName());
            return p;
        } catch (Exception ex) {
            Log.e(TAG, "Could not load plugin: " + description.getFullName() + " " + file.toURI().toASCIIString(), ex);
        }
        return null;
    }

    public synchronized void unloadPlugin(Plugin plugin) {
        disablePlugin(plugin);
        plugin.destroy();
        mPlugins.remove(plugin.getDescription().getName());
        mPluginLoader.unloadPlugin(plugin);
        mBot.getCommandManager().clearPluginCommands(plugin);
    }

    public void enablePlugin(Plugin plugin) {
        if (!plugin.getEnabled()) {
            mBot.getIrc().getEventHandlerManager().registerEvents(plugin);
            plugin.enable();
        }
    }

    public void disablePlugin(Plugin plugin) {
        if (plugin.getEnabled()) {
            mBot.getIrc().getEventHandlerManager().unregisterEvents(plugin);
            plugin.disable();
        }
    }

    public synchronized Plugin getPlugin(String name) {
        return mPlugins.get(name);
    }

    public synchronized Plugin[] getPlugins() {
        return mPlugins.values().toArray(new Plugin[mPlugins.size()]);
    }

    public File getPluginStorage(Plugin plugin) {
        File file = new File(mPluginDirectory.getAbsolutePath() + "/" + plugin.getDescription().getName());
        if (file.isDirectory() || file.mkdirs()) {
            return file;
        }
        return null;
    }

    public void savePluginConfig(Plugin plugin) {
        if (plugin.getConfig() == null || plugin.getStorage() == null) {
            return;
        }
        try (Writer writer = new FileWriter(plugin.getStorage().getPath() + "/config.json")) {
            new Gson().toJson(plugin.getConfig(), writer);
        } catch (IOException e) {
            Log.e(TAG, "Could not save plugin config: " + plugin.getDescription().getTitle(), e);
        }
    }

    @EventHandler
    @CommandFilter("plugin")
    public void onCommand(CommandEvent e) {
        if (e.getParams().length < 1) {
            e.showUsage(mBot);
            return;
        }
        String action = e.getParams()[0];
        if (action.equalsIgnoreCase("reload")) {
            long start = System.currentTimeMillis();

            // New/removed/updated
            Set<String> pluginSet = new HashSet<>(mPlugins.keySet());
            Map<String, byte[]> oldPluginChecksums = new HashMap<>(mPlugins.size());
            mPlugins.entrySet().forEach((plugin) -> oldPluginChecksums.put(plugin.getKey(), plugin.getValue().getChecksum()));

            unloadPlugins();
            loadPlugins();
            int loaded = mPlugins.size();

            // New/removed/updated
            pluginSet.addAll(mPlugins.keySet());
            Map<String, byte[]> newPluginChecksums = new HashMap<>(mPlugins.size());
            mPlugins.entrySet().forEach((plugin) -> newPluginChecksums.put(plugin.getKey(), plugin.getValue().getChecksum()));

            int newPlugins = 0;
            int removedPlugins = 0;
            int changedPlugins = 0;
            for (String plugin : pluginSet) {
                if (!oldPluginChecksums.containsKey(plugin)) {
                    newPlugins++;
                } else if (!newPluginChecksums.containsKey(plugin)) {
                    removedPlugins++;
                } else if (!Arrays.equals(oldPluginChecksums.get(plugin), newPluginChecksums.get(plugin))) {
                    changedPlugins++;
                }
            }

            String message = loaded + " plugins loaded in " + (System.currentTimeMillis() - start) + "ms.";
            List<String> extras = new ArrayList<>(3);
            if (newPlugins > 0) {
                extras.add(newPlugins + " new");
            }
            if (removedPlugins > 0) {
                extras.add(removedPlugins + " removed");
            }
            if (changedPlugins > 0) {
                extras.add(changedPlugins + " updated");
            }
            if (extras.size() > 0) {
                message += " (" + Utils.implode(extras, ", ") + ")";
            }
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + message);
        } else if (action.equalsIgnoreCase("list")) {
            List<String> plugins = new ArrayList<>();
            for (Plugin plugin : mPlugins.values()) {
                String name = plugin.getDescription().getName();
                if (!plugin.getEnabled()) {
                    name += "(disabled)";
                }
                plugins.add(name);
            }
            Collections.sort(plugins);
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Plugins: " + Utils.implode(plugins, ", "));
        } else {
            if (e.getParams().length < 2) {
                e.showUsage(mBot);
                return;
            }
            if (action.equalsIgnoreCase("enable")) {
                if (getPlugin(e.getParams()[1]) != null) {
                    enablePlugin(getPlugin(e.getParams()[1]));
                    Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + getPlugin(e.getParams()[1]).getDescription().getTitle() + " enabled");
                }
            } else if (action.equalsIgnoreCase("disable")) {
                if (getPlugin(e.getParams()[1]) != null) {
                    disablePlugin(getPlugin(e.getParams()[1]));
                    Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + getPlugin(e.getParams()[1]).getDescription().getTitle() + " disabled");
                }
            } else if (action.equalsIgnoreCase("load")) {
                File file = new File(e.getParams()[1]);
                Plugin plugin = loadPlugin(file);
                if (plugin == null) {
                    Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error loading plugin: " + file.toURI().toASCIIString());
                } else {
                    Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + plugin.getDescription().getFullName() + " loaded");
                }
            } else if (action.equalsIgnoreCase("unload")) {
                Plugin plugin = getPlugin(e.getParams()[1]);
                if (plugin != null) {
                    unloadPlugin(plugin);
                    Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + plugin.getDescription().getTitle() + " unloaded");
                } else {
                    Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Unknown plugin " + e.getParams()[1]);
                }
            }
        }
    }
}
