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

package com.nebkat.plugin.gerrit;

import com.google.gson.Gson;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.nebkat.junglist.bot.plugin.Plugin;
import com.nebkat.junglist.irc.Irc;
import com.nebkat.junglist.irc.utils.InputThread;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GerritPlugin extends Plugin<GerritPlugin.Config> {
    private static final int GERRIT_STREAM_PORT = 29418;
    private static final String GERRIT_STREAM_COMMAND = "gerrit stream-events";

    private Gson mGson = new Gson();
    private JSch mSSH;
    private Map<Session, List<String>> mSessions = new HashMap<>();

    @Override
    public void onCreate() {
        mSSH = new JSch();
        saveConfig();
    }

    public void onEnable() {
        try {
            mSSH.addIdentity(mConfig.key);
            mSSH.setKnownHosts(mConfig.hosts);

            for (Map.Entry<String, List<String>> gerrit : mConfig.gerrit.entrySet()) {
                final Session session = mSSH.getSession(mConfig.user, gerrit.getKey(), GERRIT_STREAM_PORT);
                mSessions.put(session, gerrit.getValue());
                session.connect();

                ChannelExec channel = (ChannelExec) session.openChannel("exec");
                channel.setCommand(GERRIT_STREAM_COMMAND);
                channel.setInputStream(null);
                channel.connect();
                InputStream stream = channel.getInputStream();

                new InputThread(stream, new InputThread.Callback() {
                    @Override
                    public void onLineRead(String line) {
                        GerritPlugin.this.onLineRead(session, line);
                    }

                    @Override
                    public void onStreamClosed() {
                    }
                }).start();
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    public void onDisable() {
        mSessions.keySet().forEach(Session::disconnect);
        mSessions.clear();
    }

    public void onLineRead(Session session, String line) {
        GerritEvent event = mGson.fromJson(line, GerritEvent.class);
        /*if (GerritEvent.TYPE_PATCHSET_ADD.equals(event.type)) {
            for (String channel : mSessions.get(session)) {
                Irc.message(getBot().getSession(), channel, "[Gerrit] " + accountName(event.patchSet.uploader) + " uploaded " + (event.patchSet.number > 1 ? "patchset " + event.patchSet.number : "") + "\"" + event.change.subject + "\" by " + accountName(event.change.owner) + " for " + event.change.project + "[" + event.change.branch + "] - " + event.change.url);
            }
        } else if (GerritEvent.TYPE_CHANGE_ABANDONED.equals(event.type)) {
            for (String channel : mSessions.get(session)) {
                Irc.message(getBot().getSession(), channel, "[Gerrit] " + accountName(event.abandoner) + " abandoned \"" + event.change.subject + "\" by " + accountName(event.change.owner) + " for " + event.change.project + "[" + event.change.branch + "] - " + event.change.url);
            }
        } else */if (GerritEvent.TYPE_CHANGE_MERGED.equals(event.type)) {
            for (String channel : mSessions.get(session)) {
                Irc.message(getBot().getSession(), channel, "[Gerrit] " + accountName(event.submitter) + " merged \"" + event.change.subject + "\" by " + accountName(event.change.owner) + " for " + event.change.project + "[" + event.change.branch + "] - " + event.change.url);
            }

        }/* else if (GerritEvent.TYPE_COMMENT_ADDED.equals(event.type)) {
            for (String channel : mSessions.get(session)) {
                Irc.message(getBot().getSession(), channel, "[Gerrit] " + accountName(event.author) + " commented \"" + event.comment + "\" on \"" + event.change.subject + "\" by " + accountName(event.change.owner) + " for " + event.change.project + "[" + event.change.branch + "] - " + event.change.url);
            }
        }*/
    }

    public String accountName(GerritEvent.Account account) {
        return account.username != null ? account.username : account.name + " <" + account.email + ">";
    }

    public class Config {
        public Map<String, List<String>> gerrit;
        public String user;
        public String hosts;
        public String key;
    }

    public class GerritEvent {
        public static final String TYPE_PATCHSET_ADD = "patchset-added";
        public static final String TYPE_CHANGE_ABANDONED = "change-abandoned";
        public static final String TYPE_CHANGE_MERGED = "change-merged";
        public static final String TYPE_COMMENT_ADDED = "comment-added";

        public String type;
        public Change change;
        public Patchset patchSet;
        public Account abandoner;
        public Account submitter;
        public Account author;
        public String comment;

        public class Change {
            public String project;
            public String branch;
            public String id;
            public String subject;
            public Account owner;
            public String url;
        }

        public class Account {
            public String name;
            public String email;
            public String username;
        }

        public class Patchset {
            public int number;
            public String revision;
            public Account uploader;
        }
    }
}
