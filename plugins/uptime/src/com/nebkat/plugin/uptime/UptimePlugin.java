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

package com.nebkat.plugin.uptime;

import com.nebkat.junglist.bot.command.Command;
import com.nebkat.junglist.bot.command.CommandEvent;
import com.nebkat.junglist.bot.command.CommandFilter;
import com.nebkat.junglist.bot.command.UserLevel;
import com.nebkat.junglist.bot.plugin.Plugin;
import com.nebkat.junglist.irc.Irc;
import com.nebkat.junglist.irc.events.EventHandler;
import com.nebkat.junglist.irc.utils.Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class UptimePlugin extends Plugin<UptimePlugin.Config> {
    private static final int TIME_STRING_SEGMENTS = 2;
    private static final int BYTE_STRING_DECIMALS = 2;

    @EventHandler
    @CommandFilter("uptime")
    public void onUptimeCommand(CommandEvent e) {
        long uptime = (System.currentTimeMillis() - getBot().getStartTime()) / 1000;
        Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Current bot uptime: " + getTimeLengthString(uptime) + ", Total bot uptime: " + getTimeLengthString(getConfig().uptime + uptime) + ", System uptime: " + getTimeLengthString(getSystemUptime()));
    }

    @EventHandler
    @CommandFilter("memory")
    public void onMemoryCommand(CommandEvent e) {
        if (Utils.indexOrDefault(e.getParams(), 0, "").equalsIgnoreCase("gc")) {
            System.gc();
        }
        long max = Runtime.getRuntime().maxMemory();
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();
        long used = total - free;
        Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Max VM size: " + getByteString(max) + ", Current VM size: " + getByteString(total) + " (" + Math.round(100 * used / total) + "% used), Used VM: " + getByteString(total - free));
    }

    @Override
    public void onCreate() {
        getBot().getCommandManager().registerCommand(new Command("uptime", this, "Uptime information", null, UserLevel.NORMAL, true));
        getBot().getCommandManager().registerCommand(new Command("memory", this, "Memory information", "[gc]", UserLevel.NORMAL, true));
    }

    @Override
    public void onDestroy() {
        getConfig().uptime += (System.currentTimeMillis() - getBot().getStartTime()) / 1000;
        saveConfig();
    }

    private long getSystemUptime() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/uptime"))) {
            return (long) Double.parseDouble(reader.readLine().split(" ")[0]);
        } catch (Exception e) {
            return 0;
        }
    }

    public String getByteString(long bytes) {
        String suffix;
        if (bytes > Math.pow(1024, 4)) {
            bytes /= Math.pow(1024, 4);
            suffix = "TB";
        } else if (bytes > Math.pow(1024, 3)) {
            bytes /= Math.pow(1024, 3);
            suffix = "GB";
        } else if (bytes > Math.pow(1024, 2)) {
            bytes /= Math.pow(1024, 2);
            suffix = "MB";
        } else if (bytes > 1024) {
            bytes /= 1024;
            suffix = "KB";
        } else {
            suffix = "B";
        }
        return ((double) Math.round(bytes * Math.pow(10, BYTE_STRING_DECIMALS)) / Math.pow(10, BYTE_STRING_DECIMALS)) + suffix;
    }

    public String getTimeLengthString(long uptime) {
        int minutes = Math.round(uptime / 60) % 60;
        int hours = Math.round(uptime / 60 / 60) % 24;
        int days = Math.round(uptime / 60 / 60 / 24);
        int seconds = Math.round(uptime) % 60;

        List<String> list = new ArrayList<>();
        int total = 0;
        if (days >= 1 && total < TIME_STRING_SEGMENTS) {
            list.add(days + " day" + (days > 1 ? "s" : ""));
            total++;
        }
        if (hours >= 1 && total < TIME_STRING_SEGMENTS) {
            list.add(hours + " hour" + (hours > 1 ? "s" : ""));
            total++;
        }
        if (minutes >= 1 && total < TIME_STRING_SEGMENTS) {
            list.add(minutes + " minute" + (minutes > 1 ? "s" : ""));
            total++;
        }
        if (seconds >= 1 && total < TIME_STRING_SEGMENTS) {
            list.add(seconds + " second" + (seconds > 1 ? "s" : ""));
        }
        return Utils.implode(list, ", ");
    }

    public class Config {
        public long uptime;
    }
}