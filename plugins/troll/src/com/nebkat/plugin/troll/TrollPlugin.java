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

package com.nebkat.plugin.troll;

import com.nebkat.junglist.bot.command.Command;
import com.nebkat.junglist.bot.command.CommandEvent;
import com.nebkat.junglist.bot.command.CommandFilter;
import com.nebkat.junglist.bot.command.UserLevel;
import com.nebkat.junglist.bot.plugin.Plugin;
import com.nebkat.junglist.irc.Irc;
import com.nebkat.junglist.irc.events.EventHandler;
import com.nebkat.junglist.irc.events.irc.MessageEvent;
import com.nebkat.junglist.irc.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TrollPlugin extends Plugin<TrollPlugin.Config> {
    private static final String WHATS_UP_DOOD = "whats up dood";
    private static final String BOOBS = "http://imgur.com/r/nsfw";

    private Random mRandom = new Random();
    private Map<String, Integer> mDerpCount = new HashMap<>();

    @EventHandler
    public void onMessage(MessageEvent e) {
        if (e.getMessage().contains("?")) {
            String target = e.getTarget().getName();
            if (mConfig.derp.contains(target)) {
                int count = mDerpCount.getOrDefault(target, 0);
                count += Utils.countMatches(e.getMessage(), "?");
                if (count > 7) {
                    Irc.message(e.getSession(), e.getTarget(), "derp");
                    count %= 7;
                }
                mDerpCount.put(target, count);
            }
        }
    }

    @EventHandler
    @CommandFilter("dood")
    public void onDerpCommand(CommandEvent e) {
        if (e.getParams().length == 0) {
            Irc.message(e.getSession(), e.getTarget(), WHATS_UP_DOOD);
        } else if (e.getParams().length == 1) {
            Irc.message(e.getSession(), e.getTarget().getName(), e.getParams()[0] + ",");
            Irc.message(e.getSession(), e.getTarget(), WHATS_UP_DOOD);
        } else if (e.getParams().length > 1) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Y U GREET SO MANY AT ONCE?!");
        }
    }

    @EventHandler
    @CommandFilter("boobs")
    public void onBoobsCommand(CommandEvent e) {
        Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + BOOBS);
    }

    @EventHandler
    @CommandFilter("duper")
    public void onDuperCommand(CommandEvent e) {
        Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + mConfig.duper.get(mRandom.nextInt(mConfig.duper.size())));
    }

    @Override
    public void onCreate() {
        getBot().getCommandManager().registerCommand(new Command("dood", this, null, "[<user>] [<channel>]", UserLevel.NORMAL, true));
        getBot().getCommandManager().registerCommand(new Command("boobs", this, null, null, UserLevel.NORMAL, true));
        getBot().getCommandManager().registerCommand(new Command("duper", this, null, null, UserLevel.NORMAL, true));
    }

    public class Config {
        public List<String> derp;
        public List<String> duper;
    }
}