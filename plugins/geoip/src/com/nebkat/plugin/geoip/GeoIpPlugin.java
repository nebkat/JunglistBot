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

package com.nebkat.plugin.geoip;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
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
import com.nebkat.junglist.irc.events.EventListener;
import com.nebkat.junglist.irc.events.irc.response.UserHostEvent;
import com.nebkat.junglist.irc.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeoIpPlugin extends Plugin {
    private static final String GEOCODE_API_URL = "http://maps.googleapis.com/maps/api/geocode/json?address=%1$s&sensor=false";
    private static final String TIMEZONE_API_URL = "https://maps.googleapis.com/maps/api/timezone/json?location=%1$f,%2$f&timestamp=%3$d&sensor=false";
    private static final String GEOIP_URL = "http://freegeoip.net/json/%1$s";

    private static final Pattern FREENODE_WEB_IP_MATCHER = Pattern.compile("gateway/web/freenode/ip\\.((?:\\d{1,3}\\.){3}\\d{1,3})");

    private EventListener mUserHostListener;

    @EventHandler
    @CommandFilter("geo")
    public void onGeoCommand(CommandEvent e) {
        if (e.getParams().length < 2) {
            e.showUsage(getBot());
            return;
        }
        String action = e.getParams()[0];
        final String host = e.getParams()[1];
        if (action.equalsIgnoreCase("host")) {
            geoIp(e, host);
        } else if (action.equalsIgnoreCase("user")) {
            final CommandEvent originalEvent = e;
            getIrc().getEventHandlerManager().unregisterEvents(mUserHostListener);
            mUserHostListener = new EventListener(){
                @EventHandler
                public void onUserHost(UserHostEvent e) {
                    if (!host.equalsIgnoreCase(e.getNick())) {
                        Irc.message(originalEvent.getSession(), originalEvent.getTarget(), originalEvent.getSource().getNick() + ": Error resolving host for user " + host + " (user not online?)");
                        return;
                    }
                    String host = e.getHost();
                    if (host.contains("/")) {
                        Matcher matcher = FREENODE_WEB_IP_MATCHER.matcher(host);
                        if (matcher.find()) {
                            host = matcher.group(1);
                        }
                    }
                    geoIp(originalEvent, host);
                    getIrc().getEventHandlerManager().unregisterEvents(this);
                }
            };
            getIrc().getEventHandlerManager().registerEvents(mUserHostListener);
            Irc.userhost(e.getSession(), host);
        } else {
            e.showUsage(getBot());
        }
    }

    @EventHandler
    @CommandFilter("time")
    public void onTimeCommand(CommandEvent e) {
        if (e.getParams().length < 1) {
            return;
        }
        String target = e.getRawParams();

        HttpGet get = new HttpGet(String.format(GEOCODE_API_URL, target.replaceAll(" ", "+")));
        HttpResponse response;
        try {
            response = ConnectionManager.getHttpClient().execute(get);
        } catch (IOException ex) {
            get.abort();
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error fetching geocode data");
            return;
        }

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            get.abort();
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error fetching geocode data");
            return;
        }

        GoogleGeocode geocode;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            geocode = new Gson().fromJson(reader, GoogleGeocode.class);
        } catch (IOException ex) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error fetching geocode data");
            return;
        } catch (JsonParseException ex) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error parsing geocode data");
            return;
        }

        if (!geocode.status.equals(GoogleGeocode.STATUS_OK)) {
            switch (geocode.status) {
                case GoogleGeocode.STATUS_NO_RESULTS:
                    Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": No geocode data found for request");
                    break;
                case GoogleGeocode.STATUS_OVER_LIMIT:
                    Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Geocode api over limit");
                    break;
                default:
                    Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error fetching geocode data");
                    break;
            }
            return;
        }

        get = new HttpGet(String.format(TIMEZONE_API_URL, geocode.results.get(0).geometry.location.lat, geocode.results.get(0).geometry.location.lng, System.currentTimeMillis() / 1000));
        try {
            response = ConnectionManager.getHttpClient().execute(get);
        } catch (IOException ex) {
            get.abort();
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error fetching timezone data");
            return;
        }

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error fetching timezone data");
            return;
        }

        GoogleTimezone timezone;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            timezone = new Gson().fromJson(reader, GoogleTimezone.class);
        } catch (IOException ex) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error fetching timezone data");
            return;
        } catch (JsonParseException ex) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error parsing timezone data");
            return;
        }

        if (!geocode.status.equals(GoogleGeocode.STATUS_OK)) {
            switch (geocode.status) {
                case GoogleGeocode.STATUS_NO_RESULTS:
                    Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": No timezone data found for location");
                    break;
                case GoogleGeocode.STATUS_OVER_LIMIT:
                    Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Timezone api over limit");
                    break;
                default:
                    Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error fetching timezone data");
                    break;
            }
            return;
        }

        DateFormat date = new SimpleDateFormat("E d MMM HH:mm");
        date.setTimeZone(TimeZone.getTimeZone(timezone.timeZoneId));
        Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Time for " + geocode.results.get(0).formatted_address + " (" + timezone.timeZoneName + "): " + date.format(new Date()));
    }

    public void geoIp(CommandEvent e, String host) {
        if (e.getParams().length < 2) {
            return;
        }

        GeoIp location = getGeoIp(host);

        if (location == null) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error fetching geolocation data for (invalid?) host " + host);
            return;
        }

        List<String> information = new ArrayList<>();
        // Country
        if (!Utils.empty(location.country_name)) {
            information.add("country: \"" + location.country_name + "\"");
        } else if (!Utils.empty(location.country_code)) {
            information.add("country: \"" + location.country_code + "\"");
        }

        // Region
        if (!Utils.empty(location.region_name)) {
            information.add("region: \"" + location.region_name + "\"");
        }

        // City
        if (!Utils.empty(location.city)) {
            information.add("city: \"" + location.city + "\"");
        }

        // Lat/long
        if (location.latitude != 0f && location.longitude != 0f) {
            information.add("latlong: {" + location.latitude + ", " + location.longitude + "}");
        }

        // Time
        if (location.country_code != null && location.region_code != null) {
            String timeZone = GeoTimeZones.timeZoneByCountryAndRegion(location.country_code, location.region_code);
            if (timeZone != null) {
                DateFormat date = new SimpleDateFormat("E HH:mm z");
                date.setTimeZone(TimeZone.getTimeZone(timeZone));
                information.add("time: \"" + date.format(new Date()) + "\"");
            }
        }

        String result = location.ip + ": {" + Utils.implode(information, ", ") + "}";
        Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + result);
    }

    public GeoIp getGeoIp(String host) {
        HttpGet get = new HttpGet(String.format(GEOIP_URL, host));
        HttpResponse response;
        try {
            response = ConnectionManager.getHttpClient().execute(get);
        } catch (IOException ex) {
            get.abort();
            return null;
        }

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            get.abort();
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            return new Gson().fromJson(reader, GeoIp.class);
        } catch (IOException ex) {
            return null;
        } catch (JsonParseException ex) {
            return null;
        }
    }

    @Override
    public void onCreate() {
        getBot().getCommandManager().registerCommand(new Command("time", this, "Returns the time in a region", "[<region/timezone>]", UserLevel.NORMAL, false));
        getBot().getCommandManager().registerCommand(new Command("geo", this, "Finds the location of a user/ip/host", "user/host <user/host>", UserLevel.NORMAL, false, "geoip"));
    }

    @Override
    public void onDisable() {
        getIrc().getEventHandlerManager().unregisterEvents(mUserHostListener);
    }

    public class GeoIp {
        public String ip;
        public String country_code;
        public String country_name;
        public String region_code;
        public String region_name;
        public String city;
        public float longitude;
        public float latitude;
    }

    public class GoogleGeocode {
        public static final String STATUS_OK = "OK";
        public static final String STATUS_NO_RESULTS = "ZERO_RESULTS";
        public static final String STATUS_OVER_LIMIT = "OVER_QUERY_LIMIT";

        public String status;
        public List<Result> results;

        public class Result {
            public List<AddressComponent> address_components;
            public String formatted_address;
            public Geometry geometry;
            public List<String> types;

            public class AddressComponent {
                public String long_name;
                public String short_name;
                public List<String> types;
            }

            public class Geometry {
                public Location location;
                public String location_type;

                public class Location {
                    public float lat;
                    public float lng;
                }
            }
        }
    }

    public class GoogleTimezone {
        public static final String STATUS_OK = "OK";
        public static final String STATUS_NO_RESULTS = "ZERO_RESULTS";
        public static final String STATUS_OVER_LIMIT = "OVER_QUERY_LIMIT";

        public String status;
        public float dstOffset;
        public float rawOffset;
        public String timeZoneId;
        public String timeZoneName;
    }
}