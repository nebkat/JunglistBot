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

package com.example.plugin;

import com.nebkat.junglist.bot.command.Command;
import com.nebkat.junglist.bot.command.CommandEvent;
import com.nebkat.junglist.bot.command.CommandFilter;
import com.nebkat.junglist.bot.command.UserLevel;
import com.nebkat.junglist.bot.plugin.Plugin;
import com.nebkat.junglist.irc.Irc;
import com.nebkat.junglist.irc.events.EventHandler;
import com.nebkat.junglist.irc.events.irc.PrivMessageEvent;
import com.nebkat.junglist.irc.utils.Utils;

public class ExamplePlugin extends Plugin<ExamplePlugin.Config> {
    @EventHandler
    public void onMessage(PrivMessageEvent e) {
        System.out.println("Received message!");
    }

    @EventHandler
    @CommandFilter("example")
    public void onCommand(CommandEvent e) {
        Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Received command with params: " + Utils.implode(e.getParams(), ", "));
    }

    @Override
    public void onCreate() {
        getBot().getCommandManager().registerCommand(new Command("example", this, "Description", "[*optional items*] <*strings*> *flags* || requiredflag [optionalflags] [<optionalstring>]", UserLevel.MODERATOR, false));

        System.out.print(getConfig().text + " " + getConfig().number);
    }

    public class Config {
        public String text;
        public int number;
    }
}
