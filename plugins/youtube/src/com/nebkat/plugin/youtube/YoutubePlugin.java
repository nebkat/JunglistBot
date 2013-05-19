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

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.IError;
import com.nebkat.junglist.bot.command.Command;
import com.nebkat.junglist.bot.command.CommandEvent;
import com.nebkat.junglist.bot.command.CommandFilter;
import com.nebkat.junglist.bot.command.UserLevel;
import com.nebkat.junglist.bot.plugin.Plugin;
import com.nebkat.junglist.irc.Log;
import com.nebkat.junglist.irc.events.EventHandler;

public class YoutubePlugin extends Plugin {
    @EventHandler
    @CommandFilter("youtube")
    public void onYoutubeCommand(CommandEvent e) {
        System.out.println(YoutubeRetriever.getLocation("gWJ7a-tlJEk"));
        IMediaReader reader = ToolFactory.makeReader(YoutubeRetriever.getLocation("gWJ7a-tlJEk"));
        IMediaWriter writer = ToolFactory.makeWriter(getStorage().getAbsolutePath() + "/test.ogg", reader);
        reader.addListener(writer);

        IError error;
        while ((error = reader.readPacket()) == null);
        Log.e("WTF", error.getDescription() + " " + error.getErrorNumber());
    }

    @Override
    public void onCreate() {
        getBot().getCommandManager().registerCommand(new Command("youtube", this, "Makes an animated gif image from a youtube video", "<video> <start> <end>", UserLevel.NORMAL, true));
    }


}
