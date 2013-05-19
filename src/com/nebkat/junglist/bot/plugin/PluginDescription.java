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

/**
 * Provides access to a Plugins description file, plugin.json
 */
public final class PluginDescription {
    private String name = null;
    private String title = null;
    private String version = null;
    private String main = null;

    /**
     * Returns the name of a plugin.
     *
     * @return String name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the title of a plugin.
     *
     * @return String title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the version of a plugin
     *
     * @return String name.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the name of a plugin including the version
     *
     * @return String name.
     */
    public String getFullName() {
        return title + " v" + version;
    }

    /**
     * Returns the main class for a plugin.
     *
     * @return Java classpath.
     */
    public String getMain() {
        return main;
    }
}
