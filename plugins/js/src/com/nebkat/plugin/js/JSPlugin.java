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

package com.nebkat.plugin.js;

import jdk.nashorn.api.scripting.NashornScriptEngine;
import com.nebkat.junglist.bot.command.Command;
import com.nebkat.junglist.bot.command.CommandEvent;
import com.nebkat.junglist.bot.command.CommandFilter;
import com.nebkat.junglist.bot.command.UserLevel;
import com.nebkat.junglist.bot.plugin.Plugin;
import com.nebkat.junglist.irc.Irc;
import com.nebkat.junglist.irc.Log;
import com.nebkat.junglist.irc.events.EventHandler;
import com.nebkat.junglist.irc.utils.Utils;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileReader;

public class JSPlugin extends Plugin<JSPlugin.Config> {
    private static final String TAG = "JSPlugin";

    private NashornScriptEngine mEngine;

    @EventHandler
    @CommandFilter("js")
    public void onJSCommand(CommandEvent e) {
        mEngine.put("event", e);
        try {
            Object result = mEngine.eval(e.getRawParams());
            Irc.message(e.getSession(), e.getTarget(), result != null ? result.toString() : "null");
        } catch (Exception ex) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error evaluating expression: " + ex.getMessage());
        }
    }

    private void loadPlugins(File directory) {
        if (directory == null || !directory.isDirectory()) {
            return;
        }

        Utils.forEach(directory.listFiles(file -> file.isFile() && file.getName().toLowerCase().endsWith(".js")), (file) -> {
            try {
                mEngine.eval(new FileReader(file));
            } catch (Exception e) {
                Log.e(TAG, "Error loading plugin", e);
            }
        });
    }

    @Override
    public void onCreate() {
        mEngine = (NashornScriptEngine) new ScriptEngineManager().getEngineByName("nashorn");

        Bindings bindings = mEngine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("bot", getBot());
        bindings.put("irc", getIrc());
        bindings.put("plugin", this);

        loadPlugins(new File(getConfig().plugins));

        getBot().getCommandManager().registerCommand(new Command("js", this, "Evaluates a javascript expression", "<expression>", UserLevel.OWNER, false, "javascript"));
        saveConfig();
    }



    public class Config {
        public String plugins;
    }
}
