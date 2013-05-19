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

package com.nebkat.junglist.bot.command;

import com.nebkat.junglist.bot.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

public class Command implements Comparable<Command> {
    private String mName;
    private String mDescription;
    private String mUsage;
    private UserLevel mLevel;
    private boolean mHidden;
    private List<String> mAliases;
    private Plugin mPlugin;

    public Command(String name, Plugin plugin, String description, String usage, UserLevel level, boolean hidden, String... aliases) {
        mName = name;
        mPlugin = plugin;
        mDescription = description;
        mUsage = usage;
        mLevel = level;
        mHidden = hidden;
        mAliases = Arrays.asList(aliases);
    }

    public String getName() {
        return mName;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getUsage() {
        return mUsage;
    }

    public UserLevel getLevel() {
        return mLevel;
    }

    public boolean getHidden() {
        return mHidden;
    }

    public List<String> getAliases() {
        return mAliases;
    }

    public Plugin getPlugin() {
        return mPlugin;
    }

    @Override
    public int compareTo(Command to) {
        return getName().compareTo(to.getName());
    }
}
