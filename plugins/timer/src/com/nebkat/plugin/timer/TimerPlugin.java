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

package com.nebkat.plugin.timer;

import com.nebkat.junglist.bot.command.Command;
import com.nebkat.junglist.bot.command.CommandEvent;
import com.nebkat.junglist.bot.command.CommandFilter;
import com.nebkat.junglist.bot.command.UserLevel;
import com.nebkat.junglist.bot.plugin.Plugin;
import com.nebkat.junglist.irc.Irc;
import com.nebkat.junglist.irc.Session;
import com.nebkat.junglist.irc.Source;
import com.nebkat.junglist.irc.Target;
import com.nebkat.junglist.irc.events.EventHandler;
import com.nebkat.junglist.irc.events.irc.PrivMessageEvent;
import com.nebkat.junglist.irc.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class TimerPlugin extends Plugin {
    private Timer mTimer;
    private Map<String, CommandExecutorTimerTask> mTasks = new HashMap<>();

    @EventHandler
    @CommandFilter("timer")
    public void onTimerCommand(CommandEvent e) {
        if (e.getParams().length < 1) {
            e.showUsage(getBot());
            return;
        }
        String action = e.getParams()[0];
        if (action.equalsIgnoreCase("list")) {
            String tasks = Utils.implode(new ArrayList<>(mTasks.keySet()), ", ");
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + tasks);
        } else if (action.equalsIgnoreCase("set")) {
            if (e.getParams().length < 4) {
                e.showUsage(getBot());
                return;
            }
            String name = e.getParams()[1];
            if (mTasks.containsKey(name)) {
                Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Timer \"" + name + "\" already exists.");
                return;
            }
            int timeout;
            try {
                timeout = Integer.parseInt(e.getParams()[2]);
            } catch (NumberFormatException ex) {
                 Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Seconds parameter must be a number between 1 and " + Integer.MAX_VALUE);
                return;
            }
            if (timeout < 1) {
                Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Seconds parameter must be a number between 1 and " + Integer.MAX_VALUE);
                return;
            }
            String message = e.getParams()[3];
            if (!message.startsWith(getBot().getCommandPrefix())) {
                message = getBot().getCommandPrefix() + message;
            }
            CommandExecutorTimerTask task = new CommandExecutorTimerTask(name, e.getSession(), e.getTarget(), e.getSource(), message);
            mTasks.put(name, task);
            mTimer.schedule(task, timeout * 1000);
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Timer \"" + name + "\" set for " + timeout + "s.");
        } else if (action.equalsIgnoreCase("unset")) {
            if (e.getParams().length < 2) {
                e.showUsage(getBot());
                return;
            }
            String name = e.getParams()[1];
            if (mTasks.containsKey(name)) {
                mTasks.get(name).cancel();
                mTasks.remove(name);
                Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Timer \"" + name + "\" cancelled.");
            } else {
                Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Unknown timer \"" + name + "\".");
            }
        } else {
            e.showUsage(getBot());
        }
    }

    @Override
    public void onCreate() {
        getBot().getCommandManager().registerCommand(new Command("timer", this, "Sets timers for command execution", "list/set/unset [id] [seconds] [:<command>] ", UserLevel.ADMIN, false));
    }

    @Override
    public void onEnable() {
        mTimer = new Timer();
    }

    @Override
    public void onDisable() {
        mTimer.cancel();
        mTasks.clear();
    }

    private class CommandExecutorTimerTask extends TimerTask {
        private String mName;
        private Session mSession;
        private Target mTarget;
        private Source mSource;
        private String mMessage;

        public CommandExecutorTimerTask(String name, Session session, Target target, Source source, String message) {
            super();
            mName = name;
            mSession = session;
            mTarget = target;
            mSource = source;
            mMessage = message;
        }

        @Override
        public void run() {
            getBot().getCommandManager().onMessage(new PrivMessageEvent(System.currentTimeMillis(), mSession, null, mSource, mTarget, mMessage));
            mTasks.remove(mName);
        }
    }
}