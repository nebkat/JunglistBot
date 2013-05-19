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

import com.google.gson.Gson;
import com.nebkat.junglist.bot.Bot;
import com.nebkat.junglist.irc.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginLoader {
    private static final String TAG = "PluginLoader";

    private Bot mBot;
    private PluginManager mManager;

    private final Gson mGson = new Gson();

    private final Map<Plugin, URLClassLoader> mClassLoaders = new HashMap<>();

    public PluginLoader(Bot bot, PluginManager manager) {
        mBot = bot;
        mManager = manager;
    }

    protected static byte[] getChecksum(File file) throws Exception {
        try (InputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            MessageDigest complete = MessageDigest.getInstance("MD5");
            int numRead;

            do {
                numRead = fis.read(buffer);
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead != -1);

            return complete.digest();
        }
    }

    @SuppressWarnings("unchecked")
    protected Plugin loadPlugin(File file) throws InvalidPluginException {
        if (file == null) {
            Log.e(TAG, "Plugin file is null");
            return null;
        } else if (!file.isFile()) {
            Log.e(TAG, "Plugin file does not exist or is a directory");
            return null;
        }

        byte[] checksum = null;
        try {
            checksum = getChecksum(file);
        } catch (Exception e) {
            Log.w(TAG, "Failed generating plugin checksum", e);
        }

        PluginDescription description;
        try {
            description = getPluginDescription(file);
        } catch (InvalidDescriptionException e) {
            throw new InvalidPluginException(e);
        }
        Plugin result;

        try {
            URL[] urls = new URL[] {file.toURI().toURL()};

            URLClassLoader loader = URLClassLoader.newInstance(urls, getClass().getClassLoader());

            Class<?> jarClass = Class.forName(description.getMain(), true, loader);
            if (!Plugin.class.isAssignableFrom(jarClass)) {
                throw new InvalidPluginException(description.getMain() + " does not extend Plugin.class");
            }
            Class<? extends Plugin> plugin = jarClass.asSubclass(Plugin.class);
            result = plugin.getConstructor().newInstance();

            Object config = getPluginConfig(file, mBot.getPluginDirectory().getPath() + "/" + description.getName(), result.getConfigType());

            result.initialize(mBot, description, config, mManager, checksum);

            mClassLoaders.put(result, loader);
        } catch (InvocationTargetException ex) {
            throw new InvalidPluginException(ex.getCause());
        } catch (Throwable ex) {
            throw new InvalidPluginException(ex);
        }

        return result;
    }

    protected void unloadPlugin(Plugin plugin) {
        if (mClassLoaders.containsKey(plugin)) {
            try {
                mClassLoaders.get(plugin).close();
            } catch (IOException e) {
                // Ignore
            }
            mClassLoaders.remove(plugin);
        }
    }

    protected PluginDescription getPluginDescription(File file) throws InvalidDescriptionException {
        if (file == null) {
            Log.e(TAG, "Plugin file is null");
            return null;
        } else if (!file.isFile()) {
            Log.e(TAG, "Plugin file does not exist or is a directory");
            return null;
        }

        try (JarFile jar = new JarFile(file)) {
            JarEntry entry = jar.getJarEntry("plugin.json");

            if (entry == null) {
                throw new InvalidDescriptionException(new FileNotFoundException("Jar does not contain plugin.json"));
            }

            Reader stream = new InputStreamReader(jar.getInputStream(entry));

            PluginDescription description = mGson.fromJson(stream, PluginDescription.class);
            if (description.getName() == null) {
                throw new InvalidDescriptionException("Plugin name is null");
            } else if (description.getMain() == null) {
                throw new InvalidDescriptionException("Plugin main is null");
            }
            return description;

        } catch (IOException ex) {
            Log.e(TAG, "Failed getting plugin description", ex);
            throw new InvalidDescriptionException(ex);
        }
    }

    protected Object getPluginConfig(File file, String path, Type type) throws InvalidDescriptionException {
        if (file == null) {
            return null;
        }

        JarFile jar = null;
        Reader stream = null;

        try {
            jar = new JarFile(file);
            JarEntry entry = jar.getJarEntry("config.json");

            if (entry == null) {
                return null;
            }


            File localConfig = new File(path + "/config.json");
            if (localConfig.exists()) {
                stream = new InputStreamReader(new FileInputStream(localConfig));
            } else {
                stream = new InputStreamReader(jar.getInputStream(entry));
            }

            return mGson.fromJson(stream, type != null ? type : Object.class);
        } catch (IOException ex) {
            Log.e(TAG, "Failed getting plugin config", ex);
            throw new InvalidDescriptionException(ex);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
            if (jar != null) {
                try {
                    jar.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }
}
