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

package com.nebkat.plugin.google;

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
import com.nebkat.junglist.irc.Log;
import com.nebkat.junglist.irc.events.EventHandler;
import com.nebkat.junglist.irc.utils.Utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;

public class GooglePlugin extends Plugin {
    private static final String GOOGLE = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=%1$s";
    private static final String TRANSLATE = "https://translate.google.com/translate_a/t?client=t&hl=en&multires=1&sc=1&sl=%1$s&ssel=0&tl=%2$s&tsel=0&uptl=en&text=%3$s";
    private static final String TTS = "http://translate.google.com/translate_tts?ie=UTF-8&q=%1$s&tl=%2$s";

    private final Gson mGson = new Gson();

    @EventHandler
    @CommandFilter("google")
    public void onGoogleCommand(CommandEvent e) {
        if (e.getRawParams().trim().length() <= 0) {
            e.showUsage(getBot());
            return;
        }
        try {
            String url = String.format(GOOGLE, URLEncoder.encode(e.getRawParams(), "UTF-8"));

            HttpGet get = new HttpGet(url);

            // Execute the request
            HttpContext context = new BasicHttpContext();
            HttpResponse response = ConnectionManager.getHttpClient().execute(get, context);

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                get.abort();
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            GoogleSearch search = mGson.fromJson(reader, GoogleSearch.class);

            if (!search.responseStatus.equals("200")) {
                Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error searching");
                return;
            }

            if (search.responseData.results.size() <= 0) {
                Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": No results found for that query");
                return;
            }

            String title = search.responseData.results.get(0).titleNoFormatting;
            String link = search.responseData.results.get(0).unescapedUrl;

            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + Irc.TEXT_BOLD + title + Irc.TEXT_RESET + ": " + link);
        } catch (Exception ex) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error searching");
            ex.printStackTrace();
        }
    }

    @EventHandler
    @CommandFilter("tts")
    public void onTTSCommand(CommandEvent e) {
        if (e.getParams().length < 2) {
            e.showUsage(getBot());
            return;
        }
        String lang = e.getParams()[0];
        String message = e.getRawParams().substring(lang.length()).trim().replace(" ", "%20");
        Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + String.format(TTS, message, lang));
    }

    @EventHandler
    @CommandFilter("translate")
    public void onTranslateCommand(CommandEvent e) {
        if (e.getParams().length < 3) {
            e.showUsage(getBot());
            return;
        }
        try {
            String[] message = e.getParamsAfter(2);
            String url = String.format(TRANSLATE, e.getParams()[0], e.getParams()[1], URLEncoder.encode(Utils.implode(message, "+"), "UTF-8"));

            HttpGet get = new HttpGet(url);

            // Execute the request
            HttpContext context = new BasicHttpContext();
            HttpResponse response = ConnectionManager.getHttpClient().execute(get, context);

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                get.abort();
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            LinkedHashMap translate = mGson.fromJson(reader, LinkedHashMap.class);

            //Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + BBQIRC.TEXT_BOLD + title + BBQIRC.TEXT_RESET + ": " + link);
        } catch (Exception ex) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error translating");
            Log.e("GooglePlugin", ex);
        }
    }

    @Override
    public void onCreate() {
        getBot().getCommandManager().registerCommand(new Command("google", this, "Searches a query google", "<query>", UserLevel.NORMAL, false));
        getBot().getCommandManager().registerCommand(new Command("translate", this, "Translates text", "<from> <to> <text>", UserLevel.NORMAL, false));
        getBot().getCommandManager().registerCommand(new Command("tts", this, "Returns a link to TTS", "<lang> <query>", UserLevel.NORMAL, false));
    }

    private class GoogleSearch {
        public String responseStatus;
        public ResponseData responseData;

        public class ResponseData {
            public List<Result> results;

            public class Result {
                public String unescapedUrl;
                public String url;
                public String visibleUrl;
                public String title;
                public String titleNoFormatting;
                public String content;
            }
        }
    }
}