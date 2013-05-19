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

package com.nebkat.plugin.youtube;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import com.nebkat.junglist.bot.http.ConnectionManager;

public class YoutubeRetriever {
    private static final int PREFERRED_FORMAT = 18;
    private static final String YOUTUBE_API_URL = "http://www.youtube.com/get_video_info?video_id=%1$s&fmt=" + PREFERRED_FORMAT;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13";

    public static String getLocation(String videoId) {

        CookieStore cookieStore = new BasicCookieStore();
        HttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        HttpGet get = new HttpGet(String.format(YOUTUBE_API_URL, videoId));
        get.setHeader("User-Agent", USER_AGENT);
        HttpResponse response;
        try {
            response = ConnectionManager.getHttpClient().execute(get, localContext);
        } catch (IOException ex) {
            get.abort();
            return "";
        }

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            get.abort();
            return "";
        }

        String videoInfoData;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            videoInfoData = builder.toString();
        } catch (IOException ex) {
            return "";
        }

        Map<String, String> videoInfo = getNameValuePairMap(videoInfoData);
        String[] formats = videoInfo.get("url_encoded_fmt_stream_map").split(",");
        for (String format : formats) {
            Map<String, String> formatInfo = getNameValuePairMap(format);
            String itag = formatInfo.get("itag");
            if (itag.equals(Integer.toString(PREFERRED_FORMAT))) {
                String url = formatInfo.get("url");
                String sig = formatInfo.get("sig");
                return url + "&signature=" + sig;
            }
        }

        return "";
    }

    private static Map<String, String> getNameValuePairMap(String queryString) {
        List<NameValuePair> infoMap = new ArrayList<>();
        URLEncodedUtils.parse(infoMap, new Scanner(queryString), "UTF-8");
        Map<String, String> map = new HashMap<>(infoMap.size());
        infoMap.forEach((pair) -> map.put(pair.getName(), pair.getValue()));
        return map;
    }
}