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

package com.nebkat.plugin.cm;

import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CMPlugin extends Plugin<CMPlugin.Config> {
    private static final String TAG = "CMPlugin";

    private static final String CHANGELOG_URL = "http://changelog.bbqdroid.org/";
    private static final String CHANGELOG_DEVICE_URL = "http://changelog.bbqdroid.org/#%1$s/cm10.1/latest";
    private static final String DOWNLOAD_URL = "http://get.cm/";
    private static final String DOWNLOAD_API_URL="http://get.cm/api";
    private static final String WIKI_URL = "http://wiki.cyanogenmod.org/w/Devices";
    private static final String WIKI_DEVICE_URL = "http://wiki.cyanogenmod.org/w/%1$s_Info";

    private final Gson mGson = new Gson();

    @EventHandler
    @CommandFilter("changelog")
    public void onChangelogCommand(final CommandEvent e) {
        if (e.getParams().length < 1) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + CHANGELOG_URL);
        } else {
            Config.Device device = null;
            for (List<Config.Device> devices : mConfig.devices.values()) {
                for (Config.Device d : devices) {
                    if (d.code.equalsIgnoreCase(e.getParams()[0])) {
                        device = d;
                        break;
                    }
                }
            }
            if (device == null) {
                Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Unknown device " + e.getParams()[0]);
                return;
            }
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Changelog for " + device.name + ": " + String.format(CHANGELOG_DEVICE_URL, e.getParams()[0]));
        }
    }

    @EventHandler
    @CommandFilter("download")
    public void onDownloadCommand(CommandEvent e) {
        if (e.getParams().length < 1) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + DOWNLOAD_URL);
        } else {
            Config.Device device = null;
            for (List<Config.Device> devices : mConfig.devices.values()) {
                for (Config.Device d : devices) {
                    if (d.code.equalsIgnoreCase(e.getParams()[0])) {
                        device = d;
                        break;
                    }
                }
            }
            if (device == null) {
                Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Unknown device " + e.getParams()[0]);
                return;
            }

            HttpPost post = new HttpPost(DOWNLOAD_API_URL);
            GetCMApiRequest request = new GetCMApiRequest(e.getParams()[0]);

            StringEntity json;
            try {
                json = new StringEntity(new Gson().toJson(request));
            } catch (UnsupportedEncodingException uee) {
                Log.wtf(TAG, uee);
                return;
            }
            json.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            post.setEntity(json);

            // Execute the request
            HttpContext context = new BasicHttpContext();
            HttpResponse response;
            try {
                response = ConnectionManager.getHttpClient().execute(post, context);
            } catch (IOException ex) {
                Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error fetching data");
                return;
            }

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error fetching data");
                post.abort();
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
                GetCMApiResults results = mGson.fromJson(reader, GetCMApiResults.class);

                if (results.result.size() > 0) {
                    GetCMApiResults.Result latest = results.result.get(0);
                    Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Latest build (" + latest.channel + ") for " + device.name + ": " + latest.url + " [" + latest.md5sum.substring(0, 6) + "]");
                } else {
                    Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": No builds for " + device.name);
                }
            } catch (IOException ioe) {
                Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error fetching data");
            }
        }
    }

    @EventHandler
    @CommandFilter("supported")
    public void onSupportedCommand(CommandEvent e) {
        if (e.getParams().length < 1) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Supported OEMs: " + Utils.implode(mConfig.devices.keySet(), ","));
        } else {
            List<Config.Device> oem = null;
            for (Map.Entry<String, List<Config.Device>> o : mConfig.devices.entrySet()) {
                if (o.getKey().equalsIgnoreCase(e.getParams()[0])) {
                    oem = o.getValue();
                    break;
                }
            }
            if (oem == null) {
                Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Unknown OEM " + e.getParams()[0] + ", supported:" + Utils.implode(mConfig.devices.keySet(), ","));
                return;
            }
            List<String> codes = new ArrayList<>();
            for (Config.Device device : oem) {
                codes.add(device.code);
            }
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Supported " + e.getParams()[0] + " devices: " + Utils.implode(codes, ","));
        }
    }

    @EventHandler
    @CommandFilter("device")
    public void onDeviceCommand(CommandEvent e) {
        if (e.getParams().length < 1) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + WIKI_URL);
        } else {
            Config.Device device = null;
            for (List<Config.Device> devices : mConfig.devices.values()) {
                for (Config.Device d : devices) {
                    if (d.code.equalsIgnoreCase(e.getParams()[0])) {
                        device = d;
                        break;
                    }
                }
            }
            if (device == null) {
                Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Unknown device " + e.getParams()[0]);
            } else {
                Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Information for " + device.name + ": " + String.format(WIKI_DEVICE_URL, device.code));
            }

        }
    }

    @Override
    public void onCreate() {
        getBot().getCommandManager().registerCommand(new Command("changelog", this, "Shows CyanogenMod device changelog", "[<device>]", UserLevel.NORMAL, false));
        getBot().getCommandManager().registerCommand(new Command("download", this, "Shows latest nightly link for device", "[<device>]", UserLevel.NORMAL, false));
        getBot().getCommandManager().registerCommand(new Command("supported", this, "Shows supported OEMs and devices", "[<oem>]", UserLevel.NORMAL, false));
        getBot().getCommandManager().registerCommand(new Command("device", this, "Shows link to wiki page of device", "[<device>]", UserLevel.NORMAL, false, "install"));
        saveConfig();
    }

    public class Config {
        public Map<String, List<Device>> devices;

        public class Device {
            public String code;
            public String name;
        }
    }

    public class GetCMApiRequest {
        public String method = "get_all_builds";
        public Params params;

        public class Params {
            public String device;
            public String[] channels = new String[] {"nightly"};

            public Params(String device) {
                this.device = device;
            }
        }

        public GetCMApiRequest(String device) {
            this.params = new Params(device);
        }
    }

    public class GetCMApiResults {
        public Integer id;
        public List<Result> result;

        public class Result {
            public int size;
            public String url;
            public int timestamp;
            public String md5sum;
            public String changes;
            public String channel;
            public String filename;
        }
    }
}
