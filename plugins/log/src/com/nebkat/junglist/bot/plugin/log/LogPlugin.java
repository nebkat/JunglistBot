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

package com.nebkat.junglist.bot.plugin.log;

import com.nebkat.junglist.bot.plugin.Plugin;
import com.nebkat.junglist.irc.events.EventHandler;
import com.nebkat.junglist.irc.events.irc.IRCEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class LogPlugin extends Plugin {
    private static final int FLUSH_INTERVAL = 20;

    private Map<String, Writer> mWriters = new HashMap<>();
    private int mFlushCount = FLUSH_INTERVAL;

    @EventHandler
    public void onEvent(IRCEvent e) {
        try {
            if (!mWriters.containsKey(e.getSession().getServer())) {
                File out = new File(getStorage(), e.getSession().getServer());
                if (!out.exists() && !out.createNewFile()) {
                    return;
                }
                mWriters.put(e.getSession().getServer(), new FileWriter(out, true));
            }
            mWriters.get(e.getSession().getServer()).write(e.getTime() + " " + e.getData() + "\n");
            mFlushCount--;
            if (mFlushCount <= 0) {
                mFlushCount = FLUSH_INTERVAL;
                for (Writer writer : mWriters.values()) {
                    writer.flush();
                }
            }
        } catch (Exception ex) {
            // Ignore
        }
    }

    @Override
    public void onDisable() {
        for (Writer writer : mWriters.values()) {
            try {
                writer.flush();
                writer.close();
            } catch (IOException e) {
                // Ignore
            }
        }
        mWriters.clear();
    }
}
