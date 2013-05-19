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

package com.nebkat.junglist.bot.plugin;

import com.nebkat.junglist.bot.Bot;
import com.nebkat.junglist.irc.Irc;
import com.nebkat.junglist.irc.events.EventListener;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class Plugin<Config> implements EventListener {
    private Bot mBot;
    private PluginDescription mDescription;
    protected Config mConfig;
    private PluginManager mManager;
    private byte[] mChecksum;
    private boolean mCreated;
    private boolean mEnabled;

    void initialize(Bot bot, PluginDescription description, Config config, PluginManager manager, byte[] checksum) {
        mBot = bot;
        mDescription = description;
        mConfig = config;
        mChecksum = checksum;
        mManager = manager;
    }

    public PluginDescription getDescription() {
        return mDescription;
    }

    public Bot getBot() {
        return mBot;
    }

    public Irc getIrc() {
        return mBot.getIrc();
    }

    public PluginManager getManager() {
        return mManager;
    }

    void create() {
        if (!mCreated) {
            mCreated = true;
            onCreate();
        }
    }

    void destroy() {
        if (mCreated) {
            mCreated = false;
            onDestroy();
        }
    }

    protected void onCreate() {}

    protected void onDestroy() {}

    void enable() {
        if (!mEnabled) {
            mEnabled = true;
            onEnable();
        }
    }

    void disable() {
        if (mEnabled) {
            mEnabled = false;
            onDisable();
        }
    }

    protected void onEnable() {}

    protected void onDisable() {}

    public final boolean getEnabled() {
        return mEnabled;
    }

    public Config getConfig() {
        return mConfig;
    }

    public void saveConfig() {
        mManager.savePluginConfig(this);
    }

    public byte[] getChecksum() {
        return mChecksum;
    }

    protected final File getStorage() {
        return mManager.getPluginStorage(this);
    }

    Type getConfigType() {
        Type superclass = getClass().getGenericSuperclass();
        if (!(superclass instanceof Class)) {
           return ((ParameterizedType) superclass).getActualTypeArguments()[0];
        }
        return null;
    }
}
