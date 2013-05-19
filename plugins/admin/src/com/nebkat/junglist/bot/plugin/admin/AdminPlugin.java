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

package com.nebkat.junglist.bot.plugin.admin;

import com.nebkat.junglist.bot.command.Command;
import com.nebkat.junglist.bot.command.CommandEvent;
import com.nebkat.junglist.bot.command.CommandFilter;
import com.nebkat.junglist.bot.command.UserLevel;
import com.nebkat.junglist.bot.plugin.Plugin;
import com.nebkat.junglist.irc.Irc;
import com.nebkat.junglist.irc.events.EventHandler;
import com.nebkat.junglist.irc.utils.Utils;

import java.util.Map;

public class AdminPlugin extends Plugin {

    @EventHandler
    @CommandFilter("userlevel")
    public void onUserLevelCommand(CommandEvent e) {
        String action = e.getParamOrDefault(0, "");
        if (e.getParams().length < 1 ||
                ((action.equalsIgnoreCase("unset") || action.equalsIgnoreCase("clear")) && e.getParams().length < 2) ||
                (action.equalsIgnoreCase("set") && e.getParams().length < 3)) {
            e.showUsage(getBot());
            return;
        }
        if (action.equalsIgnoreCase("reload")) {
            getBot().getCommandManager().loadUserLevels();
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": User levels reloaded");
        } else if (action.equalsIgnoreCase("save")) {
            getBot().getCommandManager().loadUserLevels();
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": User levels saved");
        } else if (action.equalsIgnoreCase("list")) {
            Map<String, UserLevel> userLevels = getBot().getCommandManager().getUserLevels();
            Irc.notice(e.getSession(), e.getSource().getNick(), "Moderators:");
            userLevels.entrySet().stream()
                    .filter((entry) -> entry.getValue() == UserLevel.MODERATOR)
                    .forEach((entry) -> Irc.notice(e.getSession(), e.getSource().getNick(), "       " + entry.getKey()));
            Irc.notice(e.getSession(), e.getSource().getNick(), "Admins:");
            userLevels.entrySet().stream()
                    .filter((entry) -> entry.getValue() == UserLevel.ADMIN)
                    .forEach((entry) -> Irc.notice(e.getSession(), e.getSource().getNick(), "       " + entry.getKey()));
            Irc.notice(e.getSession(), e.getSource().getNick(), "Owners:");
            userLevels.entrySet().stream()
                    .filter((entry) -> entry.getValue() == UserLevel.OWNER)
                    .forEach((entry) -> Irc.notice(e.getSession(), e.getSource().getNick(), "       " + entry.getKey()));
        } else if (action.equalsIgnoreCase("set")) {
            String userLevel = e.getParams()[2];
            UserLevel level;
            try {
                level = UserLevel.forLevel(Integer.parseInt(userLevel));
            } catch (NumberFormatException nfe) {
                try {
                    level = UserLevel.valueOf(userLevel.toUpperCase());
                } catch (IllegalArgumentException iae) {
                    Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Level must be a valid integer or user level string");
                    return;
                }
            }
            getBot().getCommandManager().setUserLevel(e.getParams()[1], level);
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Gave " + level.name() + " privileges to '" + e.getParams()[1] + "'");
        } else if (action.equalsIgnoreCase("unset")) {
            getBot().getCommandManager().unsetUserLevel(e.getParams()[1]);
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Removed all privileges from '" + e.getParams()[1] + "'");
        } else if (action.equalsIgnoreCase("clear")) {
            getBot().getCommandManager().clearUserLevel(e.getParams()[1]);
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Removed all privileges from hosts matching '" + e.getParams()[1] + "'");
        } else if (action.equalsIgnoreCase("check")) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + e.getRawParams() + ": " + getBot().getCommandManager().getLevelForHost(e.getRawParams()).name());
        }
    }

    @Override
    public void onCreate() {
        getBot().getCommandManager().registerCommand(new Command("userlevel", this, "User privilege commands", "save/reload/list/set/unset/clear [<hostmask>] [<level>]", UserLevel.OWNER, false));
    }
}