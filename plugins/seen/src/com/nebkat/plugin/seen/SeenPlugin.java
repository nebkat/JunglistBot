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

package com.nebkat.plugin.seen;

import com.nebkat.junglist.bot.command.Command;
import com.nebkat.junglist.bot.command.CommandEvent;
import com.nebkat.junglist.bot.command.CommandFilter;
import com.nebkat.junglist.bot.command.UserLevel;
import com.nebkat.junglist.bot.plugin.Plugin;
import com.nebkat.junglist.irc.Irc;
import com.nebkat.junglist.irc.events.EventHandler;
import com.nebkat.junglist.irc.events.irc.IRCEvent;
import com.nebkat.junglist.irc.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeenPlugin extends Plugin {
    private long mStartTime;
    private Map<String, PastEventInfo> mSeenEventMap = new HashMap<>();

    @EventHandler
    public void onEvent(IRCEvent e) {
        if (e.getSource().getNick() != null) {
            mSeenEventMap.putIfAbsent(e.getSource().getNick(), new PastEventInfo());
            PastEventInfo event = mSeenEventMap.get(e.getSource().getNick());
            event.time = e.getTime();
            event.event = e.toString();
        }
    }

    @EventHandler
    @CommandFilter("seen")
    public void onSeenCommand(CommandEvent e) {
        if (e.getParams().length < 1) {
            e.showUsage(getBot());
            return;
        }
        String nick = e.getParams()[0];
        if (mSeenEventMap.containsKey(nick)) {
            PastEventInfo event = mSeenEventMap.get(nick);
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + nick +
                    " last seen " + (getTimeLengthString(System.currentTimeMillis() - event.time)) +
                    " ago (" + event.event + ")");
        } else {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + nick +
                    " not seen in last " + (getTimeLengthString(System.currentTimeMillis() - mStartTime)));
        }
    }

    @Override
    public void onCreate() {
        getBot().getCommandManager().registerCommand(new Command("seen", this, "Says when a nick was last seen", "<nick>", UserLevel.NORMAL, false));
    }

    @Override
    public void onEnable() {
        mSeenEventMap.clear();
        mStartTime = System.currentTimeMillis();
    }

    private static class PastEventInfo {
        public long time;
        public String event;
    }

    public String getTimeLengthString(long uptimeMillis) {
        long uptime = uptimeMillis / 1000;
        int minutes = Math.round(uptime / 60) % 60;
        int hours = Math.round(uptime / 60 / 60) % 24;
        int days = Math.round(uptime / 60 / 60 / 24);
        int seconds = Math.round(uptime) % 60;

        List<String> list = new ArrayList<>();
        if (days >= 1) {
            list.add(days + " day" + (days > 1 ? "s" : ""));
        }
        if (hours >= 1) {
            list.add(hours + " hour" + (hours > 1 ? "s" : ""));
        }
        if (minutes >= 1) {
            list.add(minutes + " minute" + (minutes > 1 ? "s" : ""));
        }
        if (seconds >= 1) {
            list.add(seconds + " second" + (seconds > 1 ? "s" : ""));
        }
        return Utils.implode(list, ", ");
    }
}
