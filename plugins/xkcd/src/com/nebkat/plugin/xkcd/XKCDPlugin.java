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

package com.nebkat.plugin.xkcd;


import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import com.nebkat.junglist.bot.command.Command;
import com.nebkat.junglist.bot.command.CommandEvent;
import com.nebkat.junglist.bot.command.CommandFilter;
import com.nebkat.junglist.bot.command.UserLevel;
import com.nebkat.junglist.bot.http.ConnectionManager;
import com.nebkat.junglist.bot.plugin.Plugin;
import com.nebkat.junglist.irc.Irc;
import com.nebkat.junglist.irc.events.EventHandler;
import com.nebkat.junglist.irc.events.irc.PrivMessageEvent;
import com.nebkat.junglist.irc.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class XKCDPlugin extends Plugin<XKCDPlugin.Config> {
    private static final String XKCD_API_URL = "http://xkcd.com/%1$s/info.0.json";

    private final Gson mGson = new Gson();

    @EventHandler
    @CommandFilter("xkcd")
    public void onCommand(CommandEvent e) {
        String url = String.format(XKCD_API_URL, Utils.indexOrDefault(e.getParams(), 0, ""));

        HttpGet get = new HttpGet(url);

        // Execute the request
        HttpContext context = new BasicHttpContext();
        HttpResponse response;
        try {
            response = ConnectionManager.getHttpClient().execute(get, context);
        } catch (IOException ex) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error fetching comic info");
            return;
        }

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error fetching comic info");
            return;
        }

        XKCDResult result;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            result = mGson.fromJson(reader, XKCDResult.class);
        } catch (Exception ex) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error parsing comic info");
            return;
        }

        Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + result.title + " [" + result.day + "/" + result.month + "/" + result.year + "]: " + result.img + " (" + result.alt + ")");
    }

    @EventHandler
    public void onMessage(PrivMessageEvent e) {
        mConfig.responses.entrySet().stream()
                .filter((entry) -> e.getMessage().equalsIgnoreCase(entry.getKey()))
                .findAny()
                .ifPresent((entry) -> Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + entry.getValue()));
    }

    @Override
    public void onCreate() {
        getBot().getCommandManager().registerCommand(new Command("xkcd", this, "Shows xkcd comic info", null, UserLevel.NORMAL, true));
    }

    public class XKCDResult {
        public int day;
        public int month;
        public int num;
        public int year;
        public String link;
        public String news;
        public String safe_title;
        public String transcript;
        public String alt;
        public String img;
        public String title;
    }

    public class Config {
        public Map<String, String> responses;
    }
}