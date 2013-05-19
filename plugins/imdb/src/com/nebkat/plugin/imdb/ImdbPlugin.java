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

package com.nebkat.plugin.imdb;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import com.nebkat.junglist.bot.command.Command;
import com.nebkat.junglist.bot.command.CommandEvent;
import com.nebkat.junglist.bot.command.CommandFilter;
import com.nebkat.junglist.bot.command.UserLevel;
import com.nebkat.junglist.bot.http.ConnectionManager;
import com.nebkat.junglist.bot.plugin.Plugin;
import com.nebkat.junglist.irc.Irc;
import com.nebkat.junglist.irc.events.EventHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImdbPlugin extends Plugin {

    private static final String IMDB_API_URL = "http://imdbapi.org/?title=%1$s&type=json&yg=%2$d&year=%3$d";
    private static final Pattern YEAR_MATCHER = Pattern.compile("\\(((19|20)\\d{2})\\)");

    @EventHandler
    @CommandFilter("imdb")
    public void onImdbCommand(CommandEvent e) {
        if (e.getParams().length < 1) {
            e.showUsage(getBot());
            return;
        }

        int year = -1;
        Matcher matcher = YEAR_MATCHER.matcher(e.getRawParams());
        if (matcher.find()) {
            year = Integer.parseInt(matcher.group(1));
        }

        HttpGet get = new HttpGet(String.format(IMDB_API_URL, e.getRawParams().replaceAll(" ", "%20"), year > -1 ? 1 : 0, year));
        HttpResponse response;
        try {
            response = ConnectionManager.getHttpClient().execute(get);
        } catch (IOException ex) {
            get.abort();
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error fetching movie data");
            return;
        }

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            get.abort();
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error fetching movie data");
            return;
        }

        Movie movie;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            List<Movie> list = new Gson().fromJson(reader, new TypeToken<List<Movie>>(){}.getType());
            movie = list.get(0);
        } catch (IOException ex) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error fetching movie data");
            return;
        } catch (JsonParseException ex) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Movie not found");
            return;
        }

        if (movie == null) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Movie not found");
            return;
        }

        Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + movie.imdb_url + ": " + movie.title + " (" + movie.year + ")" + (movie.directors != null && movie.directors.size() > 0 ? (" by " + movie.directors.get(0)) : "") + " rated " + movie.rating + "/10");
    }

    @Override
    public void onCreate() {
        getBot().getCommandManager().registerCommand(new Command("imdb", this, "Finds information about a move on the Internet Movie Database", "<movie>", UserLevel.NORMAL, false));
    }

    public class Movie {
        public List<String> directors;
        public String imdb_url;
        public float rating;
        public String title;
        public int year;
    }
}