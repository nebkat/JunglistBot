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

package com.nebkat.junglist.bot.plugin.op;

import com.nebkat.junglist.bot.command.Command;
import com.nebkat.junglist.bot.command.CommandEvent;
import com.nebkat.junglist.bot.command.UserLevel;
import com.nebkat.junglist.bot.plugin.Plugin;
import com.nebkat.junglist.irc.Irc;
import com.nebkat.junglist.irc.events.EventHandler;
import com.nebkat.junglist.irc.events.EventListener;
import com.nebkat.junglist.irc.events.irc.InviteEvent;
import com.nebkat.junglist.irc.events.irc.JoinEvent;
import com.nebkat.junglist.irc.events.irc.response.ResponseTopicMessageEvent;
import com.nebkat.junglist.irc.events.irc.response.ResponseTopicNoneEvent;
import com.nebkat.junglist.irc.events.irc.response.error.NotOnChannelErrorEvent;
import com.nebkat.junglist.irc.utils.Utils;

import java.util.List;

public class OPPlugin extends Plugin<OPPlugin.Config> {

    @EventHandler
    public void onCommand(CommandEvent e) {
        if (e.getCommand().getName().equals("op")) {
            Irc.op(e.getSession(), Utils.indexOrDefault(e.getParams(), 1, e.getTarget().getName()), Utils.indexOrDefault(e.getParams(), 0, e.getSource().getNick()));
        } else if (e.getCommand().getName().equals("deop")) {
            Irc.deop(e.getSession(), Utils.indexOrDefault(e.getParams(), 1, e.getTarget().getName()), Utils.indexOrDefault(e.getParams(), 0, e.getSource().getNick()));
        } else if (e.getCommand().getName().equals("voice")) {
            Irc.voice(e.getSession(), Utils.indexOrDefault(e.getParams(), 1, e.getTarget().getName()), Utils.indexOrDefault(e.getParams(), 0, e.getSource().getNick()));
        } else if (e.getCommand().getName().equals("devoice")) {
            Irc.devoice(e.getSession(), Utils.indexOrDefault(e.getParams(), 1, e.getTarget().getName()), Utils.indexOrDefault(e.getParams(), 0, e.getSource().getNick()));
        } else if (e.getCommand().getName().equals("ban")) {
            Irc.ban(e.getSession(), Utils.indexOrDefault(e.getParams(), 1, e.getTarget().getName()), Utils.indexOrDefault(e.getParams(), 0, e.getSource().getNick()));
        } else if (e.getCommand().getName().equals("unban")) {
            Irc.unban(e.getSession(), Utils.indexOrDefault(e.getParams(), 1, e.getTarget().getName()), Utils.indexOrDefault(e.getParams(), 0, e.getSource().getNick()));
        } else if (e.getCommand().getName().equals("mute")) {
            Irc.mute(e.getSession(), Utils.indexOrDefault(e.getParams(), 1, e.getTarget().getName()), Utils.indexOrDefault(e.getParams(), 0, e.getSource().getNick()));
        } else if (e.getCommand().getName().equals("unmute")) {
            Irc.unmute(e.getSession(), Utils.indexOrDefault(e.getParams(), 1, e.getTarget().getName()), Utils.indexOrDefault(e.getParams(), 0, e.getSource().getNick()));
        } else if (e.getCommand().getName().equals("kick")) {
            Irc.kick(e.getSession(), Utils.indexOrDefault(e.getParams(), 1, e.getTarget().getName()), Utils.indexOrDefault(e.getParams(), 0, e.getSource().getNick()), (e.getParams().length > 2 ? e.getRawParamsAfter(2) : null));
        } else if (e.getCommand().getName().equals("kickban")) {
            Irc.ban(e.getSession(), Utils.indexOrDefault(e.getParams(), 1, e.getTarget().getName()), Utils.indexOrDefault(e.getParams(), 0, e.getSource().getNick()));
            Irc.kick(e.getSession(), Utils.indexOrDefault(e.getParams(), 1, e.getTarget().getName()), Utils.indexOrDefault(e.getParams(), 0, e.getSource().getNick()), (e.getParams().length > 2 ? e.getRawParamsAfter(2) : null));
        } else if (e.getCommand().getName().equals("topic")) {
            if (e.getParams().length <= 1) {
                final String channel = Utils.indexOrDefault(e.getParams(), 0, e.getTarget().getName());
                Irc.topic(e.getSession(), channel);
                getIrc().getEventHandlerManager().registerEvents(new EventListener() {
                    @EventHandler
                    public void onResponseTopicMessageEvent(ResponseTopicNoneEvent event) {
                        if (event.getChannel().getName().equalsIgnoreCase(channel)) {
                            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": No topic set for " + channel);
                            getIrc().getEventHandlerManager().unregisterEvents(this);
                        }
                    }

                    @EventHandler
                    public void onResponseTopicMessageEvent(ResponseTopicMessageEvent event) {
                        if (event.getChannel().getName().equalsIgnoreCase(channel)) {
                            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Topic for " + channel + ": " + event.getTopic());
                            getIrc().getEventHandlerManager().unregisterEvents(this);
                        }
                    }

                    @EventHandler
                    public void onNotOnChannelErrorEvent(NotOnChannelErrorEvent event) {
                        if (event.getChannel().equalsIgnoreCase(channel)) {
                            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Could not retrieve topic of " + channel);
                            getIrc().getEventHandlerManager().unregisterEvents(this);
                        }
                    }
                });
            } else if (e.getParams().length >= 2) {
                Irc.topic(e.getSession(), e.getParams()[0], e.getRawParamsAfter(1));
            }
        } else if (e.getCommand().getName().equals("join")) {
            Irc.join(e.getSession(), Utils.indexOrDefault(e.getParams(), 0, e.getTarget().getName()), Utils.indexOrDefault(e.getParams(), 1, null));
        } else if (e.getCommand().getName().equals("part")) {
            Irc.part(e.getSession(), Utils.indexOrDefault(e.getParams(), 0, e.getTarget().getName()), (e.getParams().length > 1 ? e.getRawParamsAfter(1) : null));
        } else if (e.getCommand().getName().equals("quit")) {
            Irc.quit(e.getSession(), !Utils.empty(e.getRawParams()) ? e.getRawParams() : null);
        } else if (e.getCommand().getName().equals("invite")) {
            Irc.invite(e.getSession(), Utils.indexOrDefault(e.getParams(), 1, e.getTarget().getName()), Utils.indexOrDefault(e.getParams(), 0, e.getSource().getNick()));
        } else if (e.getCommand().getName().equals("nick")) {
            if (e.getParams().length < 1) {
                e.showUsage(getBot());
                return;
            }
            Irc.nick(e.getSession(), e.getParams()[0]);
        } else if (e.getCommand().getName().equals("message")) {
            if (e.getParams().length < 2) {
                e.showUsage(getBot());
                return;
            }
            Irc.message(e.getSession(), e.getParams()[0], e.getRawParamsAfter(1));
        } else if (e.getCommand().getName().equals("raw")) {
            if (e.getParams().length < 1) {
                e.showUsage(getBot());
                return;
            }
            Irc.write(e.getSession(), e.getRawParams());
        }
    }

    @EventHandler
    public void onInvite(InviteEvent e) {
        if (getBot().getCommandManager().getLevelForHost(e.getSource().getRaw()).getLevel() > UserLevel.ADMIN.getLevel()) {
            Irc.join(e.getSession(), e.getChannel());
        }
    }

    @EventHandler
    public void onJoin(JoinEvent e) {
        if (getBot().getCommandManager().getLevelForHost(e.getSource().getRaw()).getLevel() > UserLevel.MODERATOR.getLevel() &&
                mConfig.autoOp.contains(e.getTarget().getName())) {
            Irc.op(e.getSession(), e.getChannel(), e.getUser().getNick());
        }
    }

    @Override
    public void onCreate() {
        getBot().getCommandManager().registerCommand(new Command("op", this, "Gives operator privileges to a user", "[<user>] [<channel>]", UserLevel.MODERATOR, false));
        getBot().getCommandManager().registerCommand(new Command("deop", this, "Removes operator privileges from a user", "[<user>] [<channel>]", UserLevel.MODERATOR, false));
        getBot().getCommandManager().registerCommand(new Command("voice", this, "Gives voice privileges to a user", "[<user>] [<channel>]", UserLevel.MODERATOR, false));
        getBot().getCommandManager().registerCommand(new Command("devoice", this, "Removes voice privileges from a user", "[<user>] [<channel>]", UserLevel.MODERATOR, false));
        getBot().getCommandManager().registerCommand(new Command("ban", this, "Bans a user", "[<user>] [<channel>]", UserLevel.MODERATOR, false));
        getBot().getCommandManager().registerCommand(new Command("unban", this, "Unbans a user", "[<user>] [<channel>]", UserLevel.MODERATOR, false));
        getBot().getCommandManager().registerCommand(new Command("mute", this, "Mutes a user", "[<user>] [<channel>]", UserLevel.MODERATOR, false));
        getBot().getCommandManager().registerCommand(new Command("unmute", this, "Unmutes a user", "[<user>] [<channel>]", UserLevel.MODERATOR, false));
        getBot().getCommandManager().registerCommand(new Command("kick", this, "Kicks a user", "[<user>] [<channel>] [<reason>]", UserLevel.MODERATOR, false));
        getBot().getCommandManager().registerCommand(new Command("kickban", this, "Kicks and bans a user", "[<user>] [<channel>] [<reason>]", UserLevel.MODERATOR, false));
        getBot().getCommandManager().registerCommand(new Command("topic", this, "Sets a channel topic", "[<channel>] [<topic>]", UserLevel.MODERATOR, false));
        getBot().getCommandManager().registerCommand(new Command("join", this, "Joins a channel", "<channel>", UserLevel.ADMIN, false));
        getBot().getCommandManager().registerCommand(new Command("part", this, "Parts a channel", "[<channel>] [<reason>]", UserLevel.ADMIN, false));
        getBot().getCommandManager().registerCommand(new Command("quit", this, "Quits a session", "[<channel>] [<reason>]", UserLevel.OWNER, false));
        getBot().getCommandManager().registerCommand(new Command("invite", this, "Invites a user", "[<user>] [<channel>]", UserLevel.MODERATOR, false));
        getBot().getCommandManager().registerCommand(new Command("nick", this, "Requests bot nickname", "<nick>", UserLevel.OWNER, false));
        getBot().getCommandManager().registerCommand(new Command("message", this, "Sends a message to a target", "<target> <message>", UserLevel.OWNER, false));
        getBot().getCommandManager().registerCommand(new Command("raw", this, "Sends a raw message to the server", "<message>", UserLevel.OWNER, false));
    }

    public class Config {
        public List<String> autoOp;
    }
}