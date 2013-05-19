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

import com.nebkat.junglist.bot.Bot;
import com.nebkat.junglist.irc.Irc;
import com.nebkat.junglist.irc.Session;
import com.nebkat.junglist.irc.Source;
import com.nebkat.junglist.irc.Target;
import com.nebkat.junglist.irc.events.irc.MessageEvent;
import com.nebkat.junglist.irc.utils.Utils;

import java.lang.annotation.Annotation;
import java.util.Arrays;

public class CommandEvent extends MessageEvent {
    protected Command mCommand;
    protected String mCommandString;
    protected String[] mParams;
    protected String mRawParams;
    protected UserLevel mUserLevel;

    public CommandEvent(long time, Session session, String data, Source source, Target target, String message, Command command, String commandString, String[] params, String rawParams) {
        super(time, session, data, source, target, message);
        mCommand = command;
        mCommandString = commandString;
        mParams = params;
        mRawParams = rawParams;
    }

    public Command getCommand() {
        return mCommand;
    }

    public String getCommandString() {
        return mCommandString;
    }

    public String[] getParams() {
        return mParams;
    }

    public String getParamOrDefault(int index, String def) {
        if (index >= 0 && index < mParams.length) {
            return mParams[index];
        }
        return def;
    }

    public String[] getParamsAfter(int params) {
        if (params >= mParams.length) {
            throw new ArrayIndexOutOfBoundsException("params is bigger than mParams.length");
        }
        return Arrays.copyOfRange(mParams, params, mParams.length);
    }

    public String getRawParams() {
        return mRawParams;
    }

    public String getRawParamsAfter(int params) {
        if (params >= mParams.length) {
            throw new ArrayIndexOutOfBoundsException("params is bigger than mParams.length");
        }
        String[] split = Utils.splitUntilOccurrence(mRawParams, " ", " ", params);
        return split[split.length - 1];
    }


    public void showUsage(Bot bot) {
        Irc.message(getSession(), getTarget(), getSource().getNick() + ": Usage " + bot.getCommandPrefix() + getCommand().getName() + " " + getCommand().getUsage());
    }

    @Override
    protected boolean filter(Annotation[] filters) {
        for (Annotation filter : filters) {
            if (filter instanceof CommandFilter) {
                String command = ((CommandFilter) filter).value();
                if (!mCommand.getName().equalsIgnoreCase(command)) {
                    return false;
                }
            }
        }
        return super.filter(filters);
    }

    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() + "] <" + mSource.getNick() + "> ran \"" + mMessage + "\" on " + mTarget.getName();
    }
}
