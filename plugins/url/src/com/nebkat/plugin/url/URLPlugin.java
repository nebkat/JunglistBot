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

package com.nebkat.plugin.url;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLPlugin extends Plugin<URLPlugin.Config> {
    private static final Pattern URL_MATCHER = Pattern.compile("\\b(https?|ftp)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
    private static final Pattern TITLE_MATCHER = Pattern.compile("<title.*?>(.*?)</title>");

    @EventHandler
    @CommandFilter("url")
    public void onUrlCommand(CommandEvent e) {
        if (e.getParams().length < 1) {
            e.showUsage(getBot());
            return;
        }

        String action = e.getParams()[0];
        final String channel = e.getParams().length < 2 ? e.getTarget().getName() : e.getParams()[1];
        if (action.equalsIgnoreCase("add")) {
            if (mConfig.channels.stream().anyMatch((c) -> c.equalsIgnoreCase(channel))) {
                Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + channel + " already in URL title list");
                return;
            }
            mConfig.channels.add(channel);
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + channel + " added to URL title list");
        } else if (action.equalsIgnoreCase("remove")) {
            mConfig.channels.removeIf((c) -> c.equalsIgnoreCase(channel));
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + channel + " removed from URL title list");
        } else {
            e.showUsage(getBot());
        }
        saveConfig();
    }

    @EventHandler
    public void onMessage(PrivMessageEvent e) {
        // Filter targets and ignores
        if ((mConfig.channels != null && mConfig.channels.stream().noneMatch((channel) -> channel.equalsIgnoreCase(e.getTarget().getName()))) ||
                (mConfig.ignore != null && mConfig.ignore.stream().anyMatch((ignore) -> e.getSource().match(ignore)))) {
            return;
        }

        Matcher matcher = URL_MATCHER.matcher(e.getMessage());
        if (!matcher.find()) {
            return;
        }
        String url = matcher.group();

        HttpGet get = new HttpGet(url);

        // Execute the request
        HttpContext context = new BasicHttpContext();
        HttpResponse response;
        try {
            response = ConnectionManager.getHttpClient().execute(get, context);
        } catch (IOException ex) {
            get.abort();
            return;
        }

        Header contentType = response.getEntity().getContentType();
        if (contentType == null) {
            get.abort();
            return;
        }
        String mimeType = contentType.getValue().split(";")[0].trim();
        if (!mimeType.equals("text/html") || response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            get.abort();
            return;
        }

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            get.abort();
            return;
        }

        HttpHost currentHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
        boolean redirected = context.getAttribute(ConnectionManager.REDIRECTED) != null;

        StringBuilder page = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (page.length() > 2 * 1024 * 1024) {
                    reader.close();
                    get.abort();
                    return;
                }
                page.append(line);
                matcher = TITLE_MATCHER.matcher(page);
                if (matcher.find()) {
                    String title = StringEscapeUtils.unescapeHtml4(matcher.group(1).trim());
                    if (title.length() <= 0) {
                        return;
                    } else if (title.length() > 100) {
                        title = title.substring(0, 100) + "...";
                    }
                    Irc.message(e.getSession(), e.getTarget(), "[Link] " + Irc.TEXT_BOLD + currentHost.toHostString() + Irc.TEXT_RESET + (redirected ? " [redirected]" : "") + ": " + title);
                    return;
                }
            }
        } catch (IOException ex) {
            // Ignore
        }
    }

    public void onCreate() {
        getBot().getCommandManager().registerCommand(new Command("url", this, "Set channels for url plugin", "add/remove [<channel>]", UserLevel.ADMIN, false));
    }

    public class Config {
        public List<String> channels;
        public List<String> ignore;
    }
}
