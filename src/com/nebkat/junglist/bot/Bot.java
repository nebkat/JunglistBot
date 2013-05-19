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

package com.nebkat.junglist.bot;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.nebkat.junglist.bot.command.CommandManager;
import com.nebkat.junglist.bot.plugin.PluginManager;
import com.nebkat.junglist.irc.Irc;
import com.nebkat.junglist.irc.Log;
import com.nebkat.junglist.irc.Session;
import com.nebkat.junglist.irc.events.EventHandler;
import com.nebkat.junglist.irc.events.EventListener;
import com.nebkat.junglist.irc.events.IgnoreFilter;
import com.nebkat.junglist.irc.events.bbq.SessionDisconnectEvent;
import com.nebkat.junglist.irc.events.irc.IRCEvent;
import com.nebkat.junglist.irc.events.irc.NoticeEvent;
import com.nebkat.junglist.irc.events.irc.SourceFilter;
import com.nebkat.junglist.irc.events.irc.response.MotdEvent;
import com.nebkat.junglist.irc.events.irc.response.ServerConnectedEvent;
import com.nebkat.junglist.irc.events.irc.response.error.NickErrorEvent;
import com.nebkat.junglist.irc.utils.ANSIColorSystemOutLogger;
import com.nebkat.junglist.irc.utils.InputThread;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Bot implements InputThread.Callback, EventListener {
    private static final String TAG = "Bot";

    private InputThread mCliInputThread;

    private Configuration mConfiguration;

    private final Irc mIrc;
    private final PluginManager mPluginManager;
    private final CommandManager mCommandManager;
    private Session mSession;

    private long mStartTime;
    private List<String> mNicks;
    private int mNickIndex = 0;
    private boolean mNickSuccess = false;

    public static void main(String[] args) {
        new Bot();
    }

    public Bot() {
        Log.setLogger(new ANSIColorSystemOutLogger());

        Log.i(TAG, "Starting");
        mStartTime = System.currentTimeMillis();

        Runtime.getRuntime().addShutdownHook(new Thread(this::close));

        Log.i(TAG, "Reading settings.json");
        try {
            mConfiguration = new Gson().fromJson(new FileReader(new File("settings.json")), Configuration.class);
        } catch (FileNotFoundException | JsonSyntaxException e) {
            Log.e(TAG, "Could not read settings.json", e);
            System.exit(0);
        }


        Log.i(TAG, "Initiating BBQIRC");
        mIrc = new Irc();
        mIrc.getEventHandlerManager().registerEvents(this);

        Log.v(TAG, "Opening CLI input thread");
        mCliInputThread = new InputThread(System.in, this);
        mCliInputThread.start();

        mNicks = mConfiguration.getIrcConfiguration().getNicks();
        String user = mConfiguration.getIrcConfiguration().getUser();
        Configuration.IrcConfiguration.ServerConfiguration server = mConfiguration.getIrcConfiguration().getServerConfiguration();
        String host = server.getName() + (server.getPort() != -1 ? ":" + server.getPort() : "");
        Configuration.IrcConfiguration.AuthConfiguration auth = mConfiguration.getIrcConfiguration().getAuthConfiguration();

        Log.i(TAG, "Connecting to server " + host);
        try {
            mSession = mIrc.connect(host);
        } catch (IOException e) {
            Log.e(TAG, "Could not connect to server: " + host, e);
            System.exit(0);
        }

        Irc.nick(mSession, mNicks.get(mNickIndex));
        Irc.user(mSession, user, 8, "BBQBOT");
        if (auth != null) {
            switch (auth.getService()) {
                case NickServ:
                    Irc.message(mSession, "NickServ", "IDENTIFY " + auth.getUser() + " " + auth.getPass());
                    break;
            }
        }

        Log.i(TAG, "Initiating command manager");
        mCommandManager = new CommandManager(this, getUserLevelsFile());
        mCommandManager.loadUserLevels();

        Log.i(TAG, "Initiating plugin manager");
        mPluginManager = new PluginManager(this, getPluginDirectory());
        mPluginManager.loadPlugins();
    }

    public Irc getIrc() {
        return mIrc;
    }

    public Session getSession() {
        return mSession;
    }

    public CommandManager getCommandManager() {
        return mCommandManager;
    }

    @Override
    public void onLineRead(String line) {
        if (line.startsWith("/close")) {
            close();
        }
        Irc.write(mSession, line);
    }

    @Override
    public void onStreamClosed() {
        close();
    }

    @EventHandler
    @IgnoreFilter(MotdEvent.class)
    public void onEvent(IRCEvent e) {
        Log.v(e.getSession().getServer(), e.getClass().getSimpleName() + ": " + e.getData());
    }

    @EventHandler
    public void onDisconnect(SessionDisconnectEvent e) {
        Log.w(TAG, "Disconnected from " + e.getSession().getServer());
        Configuration.IrcConfiguration.ServerConfiguration server = mConfiguration.getIrcConfiguration().getServerConfiguration();
        String host = server.getName() + (server.getPort() != -1 ? ":" + server.getPort() : "");
        while (true) {
            try {
                mSession = mIrc.connect(host);
                return;
            } catch (IOException ex) {
                close();
            }
        }
    }

    @EventHandler
    public void onNickError(NickErrorEvent e) {
        if (!mNickSuccess) {
            Irc.nick(mSession, mNicks.get(++mNickIndex));
        }
    }

    @EventHandler
    public void onConnected(ServerConnectedEvent e) {
        mNickSuccess = true;

        if (!mConfiguration.getIrcConfiguration().getAuthConfiguration().getDelayJoin()) {
            join();
        }
    }

    @EventHandler
    @SourceFilter("NickServ!NickServ@services")
    public void onNickServNotice(NoticeEvent e) {
        if (mConfiguration.getIrcConfiguration().getAuthConfiguration().getService() ==
                Configuration.IrcConfiguration.AuthConfiguration.AuthService.NickServ &&
                mConfiguration.getIrcConfiguration().getAuthConfiguration().getDelayJoin()) {
            if (e.getMessage().toLowerCase().contains("you are now identified")) {
                join();
            }
        }
    }

    private void join() {
        List<String> joins = mConfiguration.getIrcConfiguration().getJoins();
        if (joins != null) {
            joins.forEach((join) -> Irc.join(mSession, join));
        }
    }

    private void close() {
        Log.i(TAG, "Stopping Bot");

        if (mIrc != null) {
            Log.i(TAG, "Closing BBQIRC");
            mIrc.getSessions().forEach(Irc::quit);
            mIrc.close();
        }

        if (mPluginManager != null) {
            Log.i(TAG, "Closing Plugin Manager");
            mPluginManager.unloadPlugins();
        }

        if (mCliInputThread != null) {
            Log.i(TAG, "Closing CLI Input Thread");
            mCliInputThread.interrupt();
        }
    }

    public File getUserLevelsFile() {
        return new File(mConfiguration.getBotConfiguration().getLevels());
    }

    public File getPluginDirectory() {
        return new File(mConfiguration.getBotConfiguration().getPlugins());
    }

    public String getCommandPrefix() {
        return mConfiguration.getBotConfiguration().getPrefix();
    }

    public long getStartTime() {
        return mStartTime;
    }
}
