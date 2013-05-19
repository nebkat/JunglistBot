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

package com.nebkat.plugin.exec;

import com.nebkat.junglist.bot.command.Command;
import com.nebkat.junglist.bot.command.CommandEvent;
import com.nebkat.junglist.bot.command.CommandFilter;
import com.nebkat.junglist.bot.command.UserLevel;
import com.nebkat.junglist.bot.plugin.Plugin;
import com.nebkat.junglist.irc.Irc;
import com.nebkat.junglist.irc.events.EventHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ExecPlugin extends Plugin {
    @EventHandler
    @CommandFilter("exec")
    public void onExecCommand(CommandEvent e) {
        final Process exec;
        try {
            exec = Runtime.getRuntime().exec(!e.getCommandString().equalsIgnoreCase("bash") ? e.getRawParams() : "bash");
            if (e.getCommandString().equalsIgnoreCase("bash")) {
                exec.getOutputStream().write((e.getRawParams() + "\nexit\n").getBytes());
                exec.getOutputStream().flush();
            }
        } catch (IOException ex) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error running command: " + ex.getMessage());
            return;
        }

        try (BufferedReader input = new BufferedReader(new InputStreamReader(exec.getInputStream())); BufferedReader error = new BufferedReader(new InputStreamReader(exec.getErrorStream()))) {
            String line;
            while ((line = input.readLine()) != null) {
                Irc.message(e.getSession(), e.getTarget(), line);
            }
            while ((line = error.readLine()) != null) {
                Irc.message(e.getSession(), e.getTarget(), line);
            }
        } catch (IOException ex) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error running command: " + ex.getMessage());
        }
    }

    @Override
    public void onCreate() {
        getBot().getCommandManager().registerCommand(new Command("exec", this, "Executes a command", "<command>", UserLevel.OWNER, false, "bash"));
    }
}
